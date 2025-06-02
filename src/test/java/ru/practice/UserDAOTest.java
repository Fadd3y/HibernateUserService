package ru.practice;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.practice.dao.UserDAO;
import ru.practice.models.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
public class UserDAOTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private static SessionFactory sessionFactory;
    private static UserDAO userDAO;

    @BeforeAll
    static void initHibernate() throws SQLException {
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
        userDAO = new UserDAO(sessionFactory);
    }

    @AfterAll
    static void afterAll() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @Test
    public void test() {

    }

}
