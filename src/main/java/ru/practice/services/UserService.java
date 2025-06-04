package ru.practice.services;

import ru.practice.models.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User save(User user);

    Optional<User> read(int id);

    List<User> readAll();

    User update(User user);

    void delete(int id);
}
