package org.example.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {


    //PasswordEncoderのBeanが正しく定義されていることを確認するテスト
    @Test
    void passwordEncoderBeanTest() {
        SecurityConfig securityConfig = new SecurityConfig();
        assertNotNull(securityConfig.passwordEncoder(), "PasswordEncoder bean should not be null");
    }

    //passwordEncoderの実施結果が正しいことを確認するテスト
    @Test
    void passwordEncoderTest() {
        SecurityConfig securityConfig = new SecurityConfig();
        String rawPassword = "password";
        String expectedEncodedPassword = "$2a$10$eO19yeYpJ/VgYT2iNKkIreAcf6FKelxykGvslJ4iZj7naHss3FSqu";

        String actualEncodedPassword = securityConfig.passwordEncoder().encode(rawPassword);
        assertNotNull(actualEncodedPassword, "Encoded password should not be null");
        assertEquals(expectedEncodedPassword,actualEncodedPassword);
    }


}