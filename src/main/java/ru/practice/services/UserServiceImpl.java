package ru.practice.services;

import org.slf4j.LoggerFactory;
import ru.practice.dao.UserDAO;
import ru.practice.dao.UserDAOImpl;
import ru.practice.models.User;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.slf4j.Logger;

public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserDAO userDAO;

    public UserServiceImpl(UserDAOImpl userDAOImpl) {
        this.userDAO = userDAOImpl;
    }

    public User save(User user) {
        if (!isUserValid(user)) {
            logger.info("User was not created");
            throw new IllegalArgumentException("User is invalid");
        }

        User savedUser = userDAO.save(user);
        logger.info("User was created");
        return savedUser;
    }

    public Optional<User> read(int id) {
        return userDAO.readById(id);
    }

    public List<User> readAll() {
        return userDAO.readAll();
    }

    public User update(User user) {
        if (userDAO.readById(user.getId()).isEmpty()) {
            logger.info("User not exist");
            throw new NoSuchElementException("User not exist");
        }

        isUserValid(user);

        return userDAO.update(user);
    }

    public void delete(int id) {
        Optional<User> user = userDAO.readById(id);

        if (user.isEmpty()) {
            logger.info("User not found");
            return;
        }

        userDAO.delete(id);
    }

    private boolean isUserValid(User user) {
        boolean isValid = true;
        StringBuilder builder = new StringBuilder();

        int nameLength = user.getName().length();
        if (nameLength > 100 || nameLength == 0) {
            logger.warn("Name length should be between 1 and 100 characters");
            builder.append("Name length should be between 1 and 100 characters. ");
            isValid = false;
        }

        int emailLength = user.getEmail().length();
        if (emailLength > 100 || emailLength == 0) {
            logger.warn("Email length should be between 1 and 100 characters");
            builder.append("Email length should be between 1 and 100 characters. ");
            isValid = false;
        }

        Optional<User> userCheck = userDAO.readByEmail(user.getEmail());
        if ((userCheck.isPresent()) && (userCheck.get().getId() != user.getId())) {
            logger.warn("This email is already taken");
            builder.append("This email is already taken. ");
            isValid = false;
        }

        int userAge = user.getAge();
        if (userAge < 0 || userAge > 120) {
            logger.warn("Age should be in range of 0 and 120 years");
            builder.append("Age should be in range of 0 and 120 years. ");
            isValid = false;
        }
        if (!isValid) {
            throw new IllegalArgumentException(builder.toString());
        } else {
            return true;
        }
    }
}
