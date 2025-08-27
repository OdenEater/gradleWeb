package org.example.service;

import jakarta.transaction.Transactional;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Transactional
@Service
public class UserServiceImpl implements UserService {

    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User registerUser(String username, String rawPassword) {
        if (userRepository.findByUsername(username).isPresent()) {
            logger.warn("User with username {} already exists", username);
            throw new IllegalArgumentException(username + " already exists");
        }
        if (username == null || username.trim().isEmpty()) {
            logger.warn("Username cannot be null or empty");
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            logger.warn("Password cannot be null or empty");
            throw new IllegalArgumentException("Password cannot be empty");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEnabled(true); // ユーザーを有効に設定
        User savedUser = userRepository.save(user);
        logger.info("User {} registered successfully", username);
        return savedUser; // 登録されたユーザーを返す
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
    }

    @Override
    public boolean deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        userRepository.delete(user);
        logger.info("User with ID {} deleted successfully", userId);
        return true; // 削除成功を示す
    }

    @Override
    public boolean deleteUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
        userRepository.delete(user);
        logger.info("User {} deleted successfully", username);
        return true; // 削除成功を示す
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));
    }

    @Override
    public User updateUser(User user) {
        // 既存ユーザが存在するか確認
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + user.getId()));
        // username, password, enabled などを更新
        existingUser.setUsername(user.getUsername());
        existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        existingUser.setEnabled(user.isEnabled());
        User updatedUser = userRepository.save(existingUser);
        logger.info("User with ID {} updated successfully", user.getId());
        return updatedUser;
    }

}

