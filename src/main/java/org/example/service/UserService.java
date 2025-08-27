package org.example.service;

import org.example.entity.User;

public interface UserService {
    User registerUser(String username, String password);

    User getUserByUsername(String username);

    User getUserById(Long id);

    User updateUser(User user);

    boolean deleteUser(Long userId);

    boolean deleteUser(String username);
}
