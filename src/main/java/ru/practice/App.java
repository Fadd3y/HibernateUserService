package ru.practice;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.practice.dao.UserDAO;
import ru.practice.models.User;

import java.util.*;

/**
 * Hello world!
 */
public class App {

    private static UserDAO userDAO;
    private static Scanner scanner;

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        scanner = new Scanner(System.in);

        Configuration configuration = new Configuration()
                .addAnnotatedClass(User.class);

        logger.info("User service started");

        try (SessionFactory sessionFactory = configuration.buildSessionFactory()) {
            userDAO = new UserDAO(sessionFactory);

            boolean isExit = false;
            String line;

            while(!isExit){
                System.out.println("\n" +
                        "User service. Choose option: \n" +
                        "1. create user \n" +
                        "2. show user \n" +
                        "3. show all users \n" +
                        "4. update user \n" +
                        "5. delete user \n" +
                        "6. exit");

                line = scanner.nextLine();

                logger.debug("Main menu option: {} (1-save, 2-show user, 3-show all, 4-update user, 5-delete user, 6-exit)", line);

                switch (line) {
                    case "1" -> createUser();
                    case "2" -> readUser();
                    case "3" -> readAllUsers();
                    case "4" -> updateUser();
                    case "5" -> deleteUser();
                    case "6" -> isExit = true;
                    default -> logger.warn("Unsupported command");
                }
            }
        }
    }

    private static void createUser() {
        logger.info("Creating user");

        System.out.println("Enter name:");
        String name = scanner.nextLine();

        System.out.println("Enter email:");
        String email = scanner.nextLine();

        System.out.println("Enter age:");

        logger.debug("Parsing age");

        Integer age = readInt();

        if (age == null) {
            logger.info("User was not created");
            return;
        }

        logger.debug("name={}, email={}, age={}", name, email, age);

        User user = new User(name, email, age);

        if (!isUserValid(user)){
            logger.info("User was not created");
            return;
        }

        logger.info("User was created");

        userDAO.save(user);
    }

    private static void readUser() {
        logger.info("Reading user");

        System.out.println("Enter id: ");

        logger.debug("Parsing id");

        Integer id = readInt();

        if (id == null || id <= 0) {
            logger.info("Invalid user id");
            return;
        }

        logger.debug("user id = {}", id);

        Optional<User> user = userDAO.read(id);
        if (user.isPresent()) {
            logger.info("User was found");
            System.out.println(user.get());
        } else {
            logger.info("User was not found");
        }
    }

    private static void readAllUsers() {
        logger.info("Reading all users");

        userDAO.readAll().forEach(System.out::println);
    }

    private static void updateUser() {
        logger.info("Updating user");

        System.out.println("Enter id: ");

        logger.debug("Parsing id");

        Integer id = readInt();

        if (id == null || id <= 0) {
            logger.info("Invalid user id");
            return;
        }

        User userToUpdate = userDAO.read(id).orElse(null);
        if (userToUpdate == null) {
            logger.info("User to update was not found");
            return;
        }

        boolean isSave = false;
        while(!isSave) {
            System.out.println("\nCurrent fields state: " + userToUpdate);
            System.out.println("Choose option: \n1. change name \n2. change email \n3. change age \n4. save changes");
            String line = scanner.nextLine();

            switch (line) {
                case "1" -> {
                    System.out.println("Enter new name");
                    userToUpdate.setName(scanner.nextLine());
                }
                case "2" -> {
                    System.out.println("Enter new email");
                    userToUpdate.setEmail(scanner.nextLine());
                }
                case "3" -> {
                    System.out.println("Enter new age");
                    Integer age = readInt();
                    if (age == null) continue;
                    userToUpdate.setAge(age);
                }
                case "4" -> isSave = true;
                default -> logger.warn("Unsupported command");
            }
        }
        logger.debug("\nFields state to update: {}", userToUpdate);
        if (!isUserValid(userToUpdate)){
            logger.info("User can't be updated with invalid data");
            return;
        }


        userDAO.update(userToUpdate);
    }

    private static void deleteUser() {
        logger.info("Deleting user");

        System.out.println("Enter id: ");

        logger.debug("Parsing id");

        Integer id = readInt();

        if (id == null || id <= 0) {
            logger.info("Invalid user id");
            return;
        }

        Optional<User> user = userDAO.read(id);

        if (user.isEmpty()) {
            logger.info("User not found");
        } else {
            userDAO.delete(id);
        }
    }

    private static boolean isUserValid(User user) {
        boolean isValid = true;

        int nameLength = user.getName().length();
        if (nameLength > 100 || nameLength == 0) {
            logger.warn("Name length should be between 1 and 100 characters");
            isValid = false;
        }

        int emailLength = user.getEmail().length();
        if (emailLength > 100 || emailLength == 0) {
            logger.warn("Email length should be between 1 and 100 characters");
            isValid = false;
        }

        Optional<User> userCheck = userDAO.read(user.getEmail());
        if ((userCheck.isPresent()) && (userCheck.get().getId() != user.getId())) {
            logger.warn("This email is already taken");
            isValid = false;
        }

        int userAge = user.getAge();
        if (userAge < 0 || userAge > 120) {
            logger.warn("Age should be in range of 0 and 120 years");
            isValid = false;
        }
        return isValid;
    }

    private static Integer readInt() {
        try {
            String line = scanner.nextLine();

            logger.debug("Line to parse as int: {}", line);

            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            logger.error("Line does not contain int. {}", Arrays.toString(e.getStackTrace()));
            return null;
        }
    }

}
