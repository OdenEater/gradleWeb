package org.example.controller.restapi;

import org.example.entity.User;
import org.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private Long testUserId;

    @BeforeEach
    void registerUserForTest() {
        // テスト用ユーザを新規追加
        String username = "testuser";
        String password = "testpass";
        User testUser = new User();
        testUser.setUsername(username);
        testUser.setPassword(password);
        testUser.setEnabled(true);
        // UserServiceのregisterUserで追加
        when(userService.registerUser(username, password)).thenReturn(testUser);
        when(userService.getUserByUsername(username)).thenReturn(testUser);
        // IDを仮で1Lとする（実際はDBから取得する場合も）
        testUser.setId(1L);
        testUserId = testUser.getId();
        when(userService.getUserById(testUserId)).thenReturn(testUser);
    }

    @Test
    @WithMockUser(username="testuser",roles={"USER"})
    void testUpdateUser_success() throws Exception {
        // 更新内容
        User updatedUser = new User();
        updatedUser.setId(testUserId);
        updatedUser.setUsername("updateduser");
        updatedUser.setPassword("updatedpass");
        updatedUser.setEnabled(false);
        when(userService.updateUser(any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/" + testUserId)
                .with(csrf()) // CSRF トークンを付与
                .contentType("application/json")
                .content("{\"username\":\"updateduser\",\"password\":\"updatedpass\",\"enabled\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updateduser"));
    }

    // テスト終了後にユーザ削除（@AfterEachなどで追加可能）
}