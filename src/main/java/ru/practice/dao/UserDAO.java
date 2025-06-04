package ru.practice.dao;

import ru.practice.models.User;

import java.util.List;
import java.util.Optional;

public interface UserDAO {

    User save(User user);

    Optional<User> readById(int id);

    Optional<User> readByEmail(String email);

    List<User> readAll();

    User update(User user);

    void delete(int id);
}
