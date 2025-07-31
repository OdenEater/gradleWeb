package org.example.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void userConfigurationTest(){
        User user=new User(1L, "testuser", "password123", true);
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertTrue(user.isEnabled(), "ユーザーは有効であるべき");
    }

    @Test
    void returnCollectUser(){
        User user = new User();
        user.setEnabled(true);
        assertTrue(user.isEnabled(), "ユーザーは有効であるべき");

        user.setEnabled(false);
        assertFalse(user.isEnabled(), "ユーザーは無効であるべき");
    }

}