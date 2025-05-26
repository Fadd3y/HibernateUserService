package ru.practice.dao;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
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
        logger.info("Saving user to DB");

        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();

        session.persist(user);

        session.getTransaction().commit();

        logger.info("User was saved to DB");
    }

    public List<User> readAll() {
        logger.info("Getting all users from DB");
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<User> all = builder.createQuery(User.class);
        Root<User> root = all.from(User.class);
        all.select(root);

        List<User> users = session.createQuery(all).getResultList();

        logger.debug("Found users: {}", users);

        session.getTransaction().commit();

        logger.info("Users were found");
        return users;
    }

    public Optional<User> read(int id) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();

        User user = session.find(User.class, id);

        logger.info("Searching user in DB, id = {}", id);

        session.getTransaction().commit();

        return Optional.ofNullable(user);
    }

    public Optional<User> read(String email) {
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<User> criteriaQuery = builder.createQuery(User.class);
        Root<User> root = criteriaQuery.from(User.class);
        criteriaQuery.select(root).where(builder.equal(root.get("email"), email));
        List<User> users = session.createQuery(criteriaQuery).getResultList();

        session.getTransaction().commit();

        return Optional.ofNullable(users.isEmpty() ? null : users.get(0));
    }

    public void update(User user) {
        logger.info("Updating user");
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();

        User userToBeUpdated = session.find(User.class, user.getId());
        userToBeUpdated.setName(user.getName());
        userToBeUpdated.setEmail(user.getEmail());
        userToBeUpdated.setAge(user.getAge());

        session.getTransaction().commit();
        logger.info("User was successfully updated");
    }

    public void delete(int id) {
        logger.info("Deleting user");
        Session session = sessionFactory.getCurrentSession();
        session.beginTransaction();

        session.remove(session.find(User.class, id));

        session.getTransaction().commit();
        logger.info("User was successfully deleted");
    }
}
