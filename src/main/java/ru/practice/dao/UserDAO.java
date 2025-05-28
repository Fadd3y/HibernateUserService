package ru.practice.dao;

import jakarta.persistence.Query;
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
import java.util.Optional;

public class UserDAO {

    private SessionFactory sessionFactory;
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    public UserDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void save(User user) {
        Transaction transaction = null;

        try(Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            session.persist(user);

            transaction.commit();
            logger.info("User was saved to DB");
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error while saving user");
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
            logger.error("Error while reading all users");
            throw e;
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
            logger.error("Error while reading user by id");
            throw e;
        }
    }

    public Optional<User> readByEmail(String email) {
        logger.info("Reading user by email operation");
        logger.debug("Searching user in DB, email = {}", email);

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
            logger.error("Error while reading user by email");
            throw e;
        }
    }

    public void update(User user) {
        logger.info("Updating user operation");

        Transaction transaction = null;

        try(Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            User userToBeUpdated = session.find(User.class, user.getId());
            userToBeUpdated.setName(user.getName());
            userToBeUpdated.setEmail(user.getEmail());
            userToBeUpdated.setAge(user.getAge());

            transaction.commit();
            logger.info("User was successfully updated");
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error while updating user");
            throw e;
        }
    }

    public void delete(int id) {
        logger.info("Deleting user operation");

        Transaction transaction = null;

        try(Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            session.remove(session.find(User.class, id));

            transaction.commit();
            logger.info("User was successfully deleted");
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error while updating user");
            throw e;
        }
    }
}
