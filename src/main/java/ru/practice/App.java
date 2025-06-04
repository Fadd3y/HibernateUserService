package ru.practice;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.practice.dao.UserDAOImpl;
import ru.practice.models.User;
import ru.practice.services.UserService;
import ru.practice.services.UserServiceImpl;

import java.util.*;

public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private static Scanner scanner;
    private static UserService userService;


    public static void main(String[] args) {
        scanner = new Scanner(System.in);

        Configuration configuration = new Configuration()
                .addAnnotatedClass(User.class);

        logger.info("User service started");

        try (SessionFactory sessionFactory = configuration.buildSessionFactory()) {
            userService = new UserServiceImpl(new UserDAOImpl(sessionFactory));

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
            logger.error("User was not created: Invalid age");
            return;
        }

        logger.debug("name={}, email={}, age={}", name, email, age);

        User user = new User(name, email, age);
        try {
            userService.save(user);
        } catch (Exception e) {
            logger.error("User was not created: {}", e.getMessage());
        }
    }

    private static void readUser() {
        logger.info("Reading user");

        System.out.println("Enter id: ");

        logger.debug("Parsing id");

        Integer id = readInt();

        if (id == null || id <= 0) {
            logger.error("Invalid user id");
            return;
        }

        logger.debug("user id = {}", id);

        Optional<User> user = null;
        try {
            user = userService.read(id);
        } catch (Exception e) {
            logger.error("User was not read: {}", e.getStackTrace());
            return;
        }

        if (user.isPresent()) {
            logger.info("User was found");
            System.out.println(user.get());
        } else {
            logger.info("User was not found");
        }
    }

    private static void readAllUsers() {
        logger.info("Reading all users");

        List<User> users = null;
        try {
            users = userService.readAll();
        } catch (Exception e) {
            logger.error("Users was not read: {}", e.getStackTrace());
            return;
        }

        if (users.isEmpty()) {
            logger.info("There is no users in database");
            System.out.println("There is no users in database");
        } else {
            users.forEach(System.out::println);
        }
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

        User userToUpdate = null;
        try {
            userToUpdate = userService.read(id).orElse(null);
        } catch (Exception e) {
            logger.error("User was not checked for existence: {}", e.getStackTrace());
            return;
        }

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
        logger.debug("Fields state to update: {}", userToUpdate);

        try {
            userService.update(userToUpdate);
        } catch (Exception e) {
            logger.error("User was not updated: {}", e.getMessage());
        }
    }

    private static void deleteUser() {
        logger.info("Deleting user");

        System.out.println("Enter id: ");

        logger.debug("Parsing id");

        Integer id = readInt();
        if (id == null || id <= 0) {
            logger.error("Invalid user id");
            return;
        }

        try {
            userService.delete(id);
        } catch (Exception e) {
            logger.error("User was not deleted: {}", e.getStackTrace());
        }
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
