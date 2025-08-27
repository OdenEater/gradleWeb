package org.example.controller.restapi;

import org.example.entity.User;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public User createUser(@RequestParam String username, @RequestParam String password) {
        // usernameが一致するものがあればエラー
        try {
            if (userService.getUserByUsername(username) != null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
            }
        } catch (IllegalArgumentException e) {
            // ユーザーが存在しない場合は新規追加処理へ進む（何もしない）
        }
        User user = userService.registerUser(username, password);
        return user;
    }

    @DeleteMapping("/{id}")
    public String deleteUserById(@PathVariable Long id) {
        if (userService.deleteUser(id)) {
            return "User with ID " + id + " deleted successfully.";
        } else {
            return "Failed to delete user with ID " + id + ".";
        }
    }

    @GetMapping("/{username}")
    public User getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username);

    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User updateRequest) {
        // idで既存ユーザ取得
        User user = userService.getUserById(id);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        // username, password, enabled など更新
        user.setUsername(updateRequest.getUsername());
        user.setPassword(updateRequest.getPassword());
        user.setEnabled(updateRequest.isEnabled());
        // 更新処理
        User updatedUser = userService.updateUser(user);
        return updatedUser;
    }
}
