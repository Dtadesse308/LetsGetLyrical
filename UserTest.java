package edu.usc.csci310.project.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserTest {

    @Test
    void getId() {
        User user = new User(1, "username", "password");
        assertEquals(1, user.getId());
    }

    @Test
    void setId() {
        User user = new User();
        user.setId(2);
        assertEquals(2, user.getId());
    }

    @Test
    void getUsername() {
        User user = new User(1, "username", "password");
        assertEquals("username", user.getUsername());
    }

    @Test
    void setUsername() {
        User user = new User();
        user.setUsername("newUsername");
        assertEquals("newUsername", user.getUsername());
    }

    @Test
    void getPassword() {
        User user = new User(1, "username", "password");
        assertEquals("password", user.getPassword());
    }

    @Test
    void setPassword() {
        User user = new User();
        user.setPassword("newPassword");
        assertEquals("newPassword", user.getPassword());
    }

    @Test
    void contrUserWithPrivacySettings() {
        User user = new User(1, "username", "password", true);
        assertEquals("username", user.getUsername());
    }
}
