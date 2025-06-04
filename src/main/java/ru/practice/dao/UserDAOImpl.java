package ru.practice.dao;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.practice.models.User;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {

    private static final Logger logger = LoggerFactory.getLogger(UserDAOImpl.class);

    private final SessionFactory sessionFactory;

    public UserDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public User save(User user) {
        if (user == null) {
            throw new NullPointerException("User cant be null");
        }

        Session session = null;
        Transaction transaction = null;

        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();

            session.persist(user);

            transaction.commit();
            logger.info("User was saved to DB");

            return user;
        } catch (Exception e) {
            if (transaction != null && transaction.getStatus().isActive()) {
                transaction.rollback();
            }
            logger.error("Error while saving user in database");
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public Optional<User> readById(int id) {
        logger.info("Reading user by id operation");
        logger.debug("Searching user in DB, id = {}", id);

        try(Session session = sessionFactory.openSession()) {
            User user = session.find(User.class, id);
            logger.debug("Found user: {}", user);
            logger.info("Reading user by id operation is successful");
            return Optional.ofNullable(user);
        } catch (Exception e) {
            logger.error("Error while reading user by id from database");
            throw e;
        }
    }

    public Optional<User> readByEmail(String email) {
        logger.info("Reading user by email operation");
        logger.debug("Searching user in DB, email = {}", email);

        if (email == null) {
            throw new NullPointerException("Email cant be null");
        }

        try(Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<User> criteriaQuery = builder.createQuery(User.class);
            Root<User> root = criteriaQuery.from(User.class);
            criteriaQuery.select(root).where(builder.equal(root.get("email"), email));
            List<User> users = session.createQuery(criteriaQuery).getResultList();

            User user = users.isEmpty() ? null : users.get(0);
            logger.debug("Found user: {}", user);
            logger.info("Reading user by email operation is successful");
            return Optional.ofNullable(user);
        } catch (Exception e) {
            logger.error("Error while reading user by email from database");
            throw e;
        }
    }

    public List<User> readAll() {
        logger.info("Reading all users operation");

        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<User> all = builder.createQuery(User.class);
            Root<User> root = all.from(User.class);
            all.select(root);

            List<User> users = session.createQuery(all).getResultList();

            logger.info("Reading all users operation is successful");
            logger.debug("Found users: {}", users);
            return users;
        } catch (Exception e) {
            logger.error("Error while reading all users from database");
            throw e;
        }
    }

    public User update(User user) {
        logger.info("Updating user operation");

        if (user == null) {
            throw new NullPointerException("User cant be null");
        }

        Session session = null;
        Transaction transaction = null;

        try {
            session = sessionFactory.getCurrentSession();
            transaction = session.beginTransaction();

            User userToBeUpdated = session.find(User.class, user.getId());

            if (userToBeUpdated == null) throw new NoSuchElementException();

            userToBeUpdated.setName(user.getName());
            userToBeUpdated.setEmail(user.getEmail());
            userToBeUpdated.setAge(user.getAge());

            transaction.commit();
            logger.info("User was successfully updated");

            return userToBeUpdated;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error while updating user in database");
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public void delete(int id) {
        logger.info("Deleting user operation");

        Session session = null;
        Transaction transaction = null;

        try {
            session = sessionFactory.getCurrentSession();
            transaction = session.beginTransaction();

            session.remove(session.find(User.class, id));

            transaction.commit();
            logger.info("User was successfully deleted");
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error while deleting user in database");
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
