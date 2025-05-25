package ru.practice;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import ru.practice.dao.UserDAO;
import ru.practice.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Hello world!
 */
public class App {

    private static UserDAO userDAO;
    private static Scanner scanner;

    public static void main(String[] args) {
        scanner = new Scanner(System.in);

        Configuration configuration = new Configuration()
                .addAnnotatedClass(User.class);

        try (SessionFactory sessionFactory = configuration.buildSessionFactory()) {
            userDAO = new UserDAO(sessionFactory);

            boolean isExit = false;
            String line;

            while(!isExit){
                System.out.println("\nChoose option: \n1. create user \n2. show user \n3. update user \n4. delete user \n5. exit");
                line = scanner.nextLine();

                switch (line) {
                    case "1" -> createUser();
                    case "2" -> readUser();
                    case "3" -> updateUser();
                    case "4" -> deleteUser();
                    case "5" -> isExit = true;
                    default -> System.out.println("Error: Unsupported command");
                }
            }
        }
    }

    private static void createUser() {
        System.out.println("Enter name:");
        String name = scanner.nextLine();

        System.out.println("Enter email:");
        String email = scanner.nextLine();

        Integer age = readInt("Enter age:", "Error: Not a number. Back to main menu\n");
        if (age == null) return;

        User user = new User(name, email, age);
        List<String> errors = validateUser(user);
        if (!errors.isEmpty()) {
            errors.forEach(System.out::println);
            System.out.println("Try again");
            return;
        }

        userDAO.create(user);
    }

    private static void readUser() {
        Integer id = readInt("Enter user id:", "Error: Not a number. Back to main menu\n");
        if (id == null) return;

        Optional<User> user = userDAO.read(id);

        System.out.println(user.isEmpty() ? "Error: User not found" : user.get());
    }

    private static void updateUser() {
        Integer id = readInt("Enter user id:", "Error: Not a number. Back to main menu\n");
        if (id == null) return;

        User userToUpdate = userDAO.read(id).orElse(null);
        if (userToUpdate == null) {
            System.out.println("Error: User not found");
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
                    Integer age = readInt("Enter age:", "Error: Not a number\n");
                    if (age == null) continue;
                    userToUpdate.setAge(age);
                }
                case "4" -> isSave = true;
                default -> System.out.println("Error: Unsupported command");
            }
        }

        List<String> errors = validateUser(userToUpdate);

        if (!errors.isEmpty()) {
            errors.forEach(System.out::println);
            System.out.println("Try again");
            return;
        }

        userDAO.update(userToUpdate);

    }

    private static void deleteUser() {
        Integer id = readInt("Enter user id:", "Error: Not a number. Back to main menu\n");
        if (id == null) return;

        Optional<User> user = userDAO.read(id);

        if (user.isEmpty()) {
            System.out.println("Error: User not found");
        } else {
            userDAO.delete(id);
        }
    }

    private static List<String> validateUser(User user) {
        ArrayList<String> errors = new ArrayList<>();

        int nameLength = user.getName().length();
        if (nameLength > 100 || nameLength == 0) {
            errors.add("Error: Name length should be between 1 and 100 characters");
        }

        int emailLength = user.getEmail().length();
        if (emailLength > 100 || emailLength == 0) {
            errors.add("Error: Email length should be between 1 and 100 characters");
        }

        if (userDAO.read(user.getEmail()).isPresent()) {
            errors.add("Error: This email is already taken");
        }

        int userAge = user.getAge();
        if (userAge < 0 || userAge > 120) {
            errors.add("Error: Age should be in range of 0 and 120 years");
        }

        return errors;
    }

    private static Integer readInt(String msg, String errMsg) {
        System.out.println(msg);

        String input = scanner.nextLine();

        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println(errMsg);
            return null;
        }
    }

}
