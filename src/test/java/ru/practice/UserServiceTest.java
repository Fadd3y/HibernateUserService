package ru.practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practice.dao.UserDAO;
import ru.practice.models.User;
import ru.practice.services.UserService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private UserService userService;

    @ParameterizedTest
    @MethodSource("provideValidFieldForObjectUser")
    public void testRead_byId_whenOk(int id, String name, String email, int age) {
        User user = new User(id, name, email, age);

        when(userDAO.readById(id)).thenReturn(Optional.of(user));

        Optional<User> result = userService.read(id);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    public void testRead_byId_whenNoUserInDB() {
        int id = 1;

        when(userDAO.readById(id)).thenReturn(Optional.empty());

        Optional<User> result = userService.read(id);

        assertTrue(result.isEmpty());
    }

    @Test
    public void testReadAll_whenOk() {
        List<User> users = List.of(
                new User(1, "test1", "test1@ya.ru", 12),
                new User(2, "test2", "test2@ya.ru", 28));

        when(userDAO.readAll()).thenReturn(users);

        List<User> result = userService.readAll();

        assertNotNull(result);
        assertEquals(result, users);
    }

    @Test
    public void testReadAll_whenNoUserInDB() {
        List<User> users = List.of();

        when(userDAO.readAll()).thenReturn(users);

        List<User> result = userService.readAll();

        assertNotNull(result);
        assertEquals(result, users);
    }

    @ParameterizedTest
    @MethodSource("provideValidFieldForObjectUser")
    public void testSave_whenUserIsValid(int id, String name, String email, int age) {
        User user = new User(id, name, email, age);

        doNothing().when(userDAO).save(user);

        userService.save(user);

        verify(userDAO, times(1)).save(user);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidFieldForObjectUser")
    public void testSave_whenUserIsNotValid(int id, String name, String email, int age) {
        User user = new User(id, name, email, age);
        User foundUser = new User(-1, name, "test2@ya.ru", 28);
        // Пользователь с совпадением email и без совпадения по id, но только для третьей строки аргументов.
        // Для остальных совпадения по email нет.
        Optional<User> emailDuplicationCondition =
                email.equals("test2@ya.ru") ? Optional.of(foundUser) : Optional.empty();
        emailDuplicationCondition.ifPresent(user228 -> System.out.println(user228.getEmail()));

        when(userDAO.readByEmail(email)).thenReturn(emailDuplicationCondition);

        userService.save(user);

        verify(userDAO, times(0)).save(user);
    }

    @ParameterizedTest
    @MethodSource("provideValidFieldForObjectUser")
    public void testUpdate_whenUserIsValid(int id, String name, String email, int age) {
        User user = new User(id, name, email, age);

        when(userDAO.readById(id)).thenReturn(Optional.of(user));
        doNothing().when(userDAO).update(user);

        userService.update(user);

        verify(userDAO, times(1)).update(user);
    }

    @ParameterizedTest
    @MethodSource("provideValidFieldForObjectUser")
    public void testUpdate_whenUserDoesNotExist(int id, String name, String email, int age) {
        User user = new User(id, name, email, age);

        when(userDAO.readById(id)).thenReturn(Optional.empty());

        userService.update(user);

        verify(userDAO, times(0)).update(user);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidFieldForObjectUser")
    public void testUpdate_whenUserIsNotValid(int id, String name, String email, int age) {
        User user = new User(id, name, email, age);
        User foundUser = new User(-1, name, "test2@ya.ru", 28);
        Optional<User> emailDuplicationCondition =
                email.equals("test2@ya.ru") ? Optional.of(foundUser) : Optional.empty();

        when(userDAO.readById(id)).thenReturn(Optional.of(user));
        when(userDAO.readByEmail(email)).thenReturn(emailDuplicationCondition);

        userService.update(user);

        verify(userDAO, times(0)).update(user);
    }

    @Test
    public void testDelete_whenOk() {
        int id = 1;
        User testUser = new User(1, "test", "test@ya.ru", 58);

        when(userDAO.readById(id)).thenReturn(Optional.of(testUser));
        doNothing().when(userDAO).delete(id);

        userService.delete(id);

        verify(userDAO, times(1)).delete(id);
    }

    @Test
    public void testDelete_whenUserDoeNotExist() {
        int id = 1;

        when(userDAO.readById(id)).thenReturn(Optional.empty());

        userService.delete(id);

        verify(userDAO, times(0)).delete(id);
    }


    private static Stream<Arguments> provideValidFieldForObjectUser() {
        return Stream.of(
                Arguments.of(1, "test1", "test1@ya.ru", 12),
                Arguments.of(2, "test2", "test2@ya.ru", 28)
        );
    }

    private static Stream<Arguments> provideInvalidFieldForObjectUser() {
        return Stream.of(
                Arguments.of(1, "", "test1@ya.ru", 12),
                Arguments.of(1, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test1@ya.ru", 12),
                Arguments.of(2, "test2", "test2@ya.ru", 28),
                Arguments.of(1, "", "test1@ya.ru", -3),
                Arguments.of(1, "", "test1@ya.ru", 130),
                Arguments.of(1, "", "test1@ya.ru", 0),
                Arguments.of(1, "test", "", 3),
                Arguments.of(1, "test", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 3)
        );
    }


}
