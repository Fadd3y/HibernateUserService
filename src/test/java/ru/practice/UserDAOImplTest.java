package ru.practice;

import jakarta.persistence.EntityExistsException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.exception.DataException;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.practice.dao.UserDAOImpl;
import ru.practice.models.User;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class UserDAOImplTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private static SessionFactory sessionFactory;
    private static UserDAOImpl userDAOImpl;

    @BeforeAll
    static void initHibernate() {
        Configuration configuration = new Configuration()
                .addAnnotatedClass(User.class);

        configuration.setProperties(new Properties() {{
            setProperty("hibernate.connection.url", postgres.getJdbcUrl());
            setProperty("hibernate.connection.username", postgres.getUsername());
            setProperty("hibernate.connection.password", postgres.getPassword());
        }});

        sessionFactory = configuration.buildSessionFactory();
    }

    @BeforeEach
    void initDAO() {
        userDAOImpl = new UserDAOImpl(sessionFactory);
    }

    @BeforeEach
    void resetDB() {
        Session session = null;
        Transaction transaction = null;

        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            session.createNativeQuery("truncate table users "
            ).executeUpdate();

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.getStatus().isActive()) {
                transaction.rollback();
            }
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @AfterAll
    static void afterAll() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @Test
    public void testSave_WhenOk() {
        String name = "Test";
        String email = "test@ya.ru";
        int age = 89;
        User user = new User(name, email, age);

        User savedUser = userDAOImpl.save(user);
        Optional<User> result = userDAOImpl.readByEmail(email);

        assertTrue(result.isPresent());
        assertNotEquals(0, result.get().getId());
        assertEquals(name, result.get().getName());
        assertEquals(email, result.get().getEmail());
        assertEquals(age, result.get().getAge());
        assertEquals(savedUser, user);
    }

    @Test
    public void testSave_whenNull() {
        User user = null;

        Exception exception = assertThrows(NullPointerException.class, () -> userDAOImpl.save(user));
        assertEquals("User cant be null", exception.getMessage());
    }

    @Test
    public void testSave_whenNotUnique() {
        String name = "Test";
        String email = "test@ya.ru";
        int age = 89;
        User user = new User(name, email, age);

        userDAOImpl.save(user);

        assertThrows(EntityExistsException.class, () -> userDAOImpl.save(user));
    }

    @Test
    public void testSave_whenDataTypeSizeViolations() {
        // 257 a
        String name = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        String email = "test@ya.ru";
        int age = 28;
        User user = new User(name, email, age);

        assertThrows(DataException.class, () -> userDAOImpl.save(user));
    }

    @Test
    public void testReadAll_whenOk() {
        User user1 = new User("name", "email", 56);
        User user2 = new User("name1", "email1", 54);
        List<User> users = List.of(user1, user2);

        userDAOImpl.save(user1);
        userDAOImpl.save(user2);
        List<User> result = userDAOImpl.readAll();

        assertEquals(2, users.size());
        assertEquals(users, result);
    }

    @Test
    public void testReadAll_whenNoUsersInDB() {
        List<User> empty = List.of();

        List<User> result = userDAOImpl.readAll();

        assertTrue(result.isEmpty());
        assertEquals(empty, result);
    }

    @Test
    public void testReadById_whenOk() {
        User user1 = new User("name", "email", 56);

        userDAOImpl.save(user1);
        Optional<User> expected = userDAOImpl.readByEmail("email");
        Optional<User> result = userDAOImpl.readById(expected.get().getId());

        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(user1, result.get());
    }

    @Test
    public void testReadById_UserWithThisIdIsNotExist() {

        Optional<User> result = userDAOImpl.readById(1);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testReadByEmail_whenOk() {
        String email = "email";
        User user1 = new User("name", email, 56);

        userDAOImpl.save(user1);
        Optional<User> result = userDAOImpl.readByEmail(email);

        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(user1, result.get());
    }

    @Test
    public void testReadByEmail_whenEmailIsNull() {
        String email = null;

        assertThrows(NullPointerException.class, () -> userDAOImpl.readByEmail(email));
    }

    @Test
    public void testUpdate_whenOk() {
        User user = new User("name1", "email1", 78);
        String newName = "name new";
        String newEmail = "email new";
        int newAge = 79;

        userDAOImpl.save(user);
        User userToBeUpdated = userDAOImpl.readByEmail("email1").get();
        userToBeUpdated.setName(newName);
        userToBeUpdated.setEmail(newEmail);
        userToBeUpdated.setAge(newAge);
        User updatedUser = userDAOImpl.update(userToBeUpdated);
        Optional<User> result = userDAOImpl.readById(userToBeUpdated.getId());

        assertNotNull(result);
        assertEquals(userToBeUpdated, result.get());
        assertEquals(userToBeUpdated, updatedUser);
    }

    @Test
    public void testUpdate_whenUserIsNull() {
        User user = null;

        assertThrows(NullPointerException.class, () -> userDAOImpl.update(user));
    }

    @Test
    public void testUpdate_whenUserDoesNotExist() {
        User user = new User("name1", "email1", 78);

        assertThrows(NoSuchElementException.class, () -> userDAOImpl.update(user));
    }

    @Test
    public void testDelete_whenOk() {
        User user = new User("sss", "dd@dfb.com", 61);
        userDAOImpl.save(user);
        User userToBeDeleted = userDAOImpl.readByEmail("dd@dfb.com").get();

        userDAOImpl.delete(userToBeDeleted.getId());
        Optional<User> deletedUser = userDAOImpl.readById(userToBeDeleted.getId());

        assertNotNull(deletedUser);
        assertTrue(deletedUser.isEmpty());
    }

    @Test
    public void testDelete_whenNoSuchUser() {
        assertThrows(IllegalArgumentException.class, () -> userDAOImpl.delete(0));
    }
}
