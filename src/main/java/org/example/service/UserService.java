package org.example.service;

import lombok.extern.flogger.Flogger;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    //logger
    private final Logger logger= LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 一般ユーザー登録（自己登録）
     *
     * @param username    ユーザー名
     * @param rawPassword 平文パスワード
     * @return 登録されたユーザー
     */

    public User registerUser(String username, String rawPassword) {

        //ユーザ名の重複チェック
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        //空のユーザ名もNG
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        //パスワードのバリデーション
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        //ユーザーの登録
        User savedUser = new User();
        savedUser.setUsername(username);
        savedUser.setPassword(passwordEncoder.encode(rawPassword));
        savedUser.setEnabled(true);

        // ログ出力
        logger.info(username, " has been registered successfully.");

        return userRepository.save(savedUser);
    }

    /**
     * ユーザー名からユーザーを取得
     *
     * @param username ユーザー名
     * @return ユーザー情報
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }

    /**
     * ユーザ削除
     *
     * @param username ユーザー名
     * @return 削除成功したか否か
     */
    public boolean deleteUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        userRepository.delete(user);
        logger.info("User {} has been deleted successfully.", username);
        return true;
    }

}

