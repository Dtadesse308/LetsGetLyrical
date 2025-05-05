package edu.usc.csci310.project.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


class UserControllerTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        ReflectionTestUtils.setField(userController, "restTemplate", restTemplate);
    }

    @Test
    void testGetPrivacySetting_Success() {
        Integer userID = 120;
        Map<String, Object> userData = new HashMap<>();
        userData.put("is_private", true);

        ResponseEntity<Map[]> responseEntity = new ResponseEntity<>(new Map[]{userData}, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map[].class)
        )).thenReturn(responseEntity);

        Map<String, Boolean> result = userController.getPrivacySetting(userID);

        assertNotNull(result);
        assertTrue(result.containsKey("is_private"));
        assertTrue(result.get("is_private"));

        verify(restTemplate).exchange(
                contains("/rest/v1/users?id=eq." + userID),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map[].class)
        );
    }

    @Test
    void testGetPrivacySetting_NullIsPrivate() {
        Integer userID = 120;
        Map<String, Object> userData = new HashMap<>();
        userData.put("is_private", null);  // is_private is null

        ResponseEntity<Map[]> responseEntity = new ResponseEntity<>(new Map[]{userData}, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map[].class)
        )).thenReturn(responseEntity);

        Map<String, Boolean> result = userController.getPrivacySetting(userID);

        assertNotNull(result);
        assertTrue(result.containsKey("is_private"));
        assertTrue(result.get("is_private"));

        verify(restTemplate).exchange(
                contains("/rest/v1/users?id=eq." + userID),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map[].class)
        );
    }

    @Test
    void testGetPrivacySetting_NullResponseBody() {
        // Arrange
        Integer userID = 123;

        // Mock the GET response to return null body
        ResponseEntity<Map[]> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map[].class)
        )).thenReturn(responseEntity);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            userController.getPrivacySetting(userID);
        });

        assertEquals("User not found", exception.getMessage());

        verify(restTemplate).exchange(
                contains("/rest/v1/users?id=eq." + userID),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map[].class)
        );
    }

    @Test
    void testGetPrivacySetting_UserNotFound() {
        Integer userID = 100;  // Non-existent user

        ResponseEntity<Map[]> responseEntity = new ResponseEntity<>(new Map[]{}, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map[].class)
        )).thenReturn(responseEntity);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userController.getPrivacySetting(userID);
        });

        assertEquals("User not found", exception.getMessage());

        verify(restTemplate).exchange(
                contains("/rest/v1/users?id=eq." + userID),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map[].class)
        );
    }

    @Test
    void testUpdatePrivacySetting_Success() {
        Integer userID = 120;
        Boolean isPrivate = true;

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", "testuser");
        userData.put("password", "encrypted_password");
        userData.put("is_private", false);

        ResponseEntity<Map[]> getResponseEntity = new ResponseEntity<>(new Map[]{userData}, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map[].class)
        )).thenReturn(getResponseEntity);

        ResponseEntity<Void> putResponseEntity = new ResponseEntity<>(HttpStatus.NO_CONTENT);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(putResponseEntity);

        ResponseEntity<String> response = userController.updatePrivacySetting(userID, isPrivate);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Privacy setting updated successfully", response.getBody());

        verify(restTemplate).exchange(
                contains("/rest/v1/users?id=eq." + userID),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map[].class)
        );

        verify(restTemplate).exchange(
                contains("/rest/v1/users?id=eq." + userID),
                eq(HttpMethod.PUT),
                argThat(entity -> {
                    Map<String, Object> body = (Map<String, Object>) entity.getBody();
                    return body.get("is_private").equals(isPrivate) &&
                            body.get("username").equals("testuser") &&
                            body.get("password").equals("encrypted_password");
                }),
                eq(Void.class)
        );
    }

    @Test
    void testUpdatePrivacySetting_UnexpectedResponse() {
        Integer userID = 120;
        Boolean isPrivate = false;

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", "testuser");
        userData.put("is_private", true);

        ResponseEntity<Map[]> getResponseEntity = new ResponseEntity<>(new Map[]{userData}, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map[].class)
        )).thenReturn(getResponseEntity);

        ResponseEntity<Void> putResponseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Unexpected status

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(putResponseEntity);

        ResponseEntity<String> response = userController.updatePrivacySetting(userID, isPrivate);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected response from Supabase: 400 BAD_REQUEST", response.getBody());
    }


    @Test
    void testUpdatePrivacySetting_Exception() {
        Integer userID = 123;
        Boolean isPrivate = true;

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", "testuser");
        userData.put("is_private", false);

        ResponseEntity<Map[]> getResponseEntity = new ResponseEntity<>(new Map[]{userData}, HttpStatus.OK);

        when(restTemplate.exchange(
                contains("/rest/v1/users?id=eq." + userID),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map[].class)
        )).thenReturn(getResponseEntity);

        HttpClientErrorException serverException = new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server error");

        when(restTemplate.exchange(
                contains("/rest/v1/users?id=eq." + userID),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenThrow(serverException);

        ResponseEntity<String> response = userController.updatePrivacySetting(userID, isPrivate);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).contains("Error updating privacy setting"));
        assertTrue(response.getBody().contains("Server error"));
    }

    @Test
    void testUpdatePrivacySetting_UserNotFound() {
        // Arrange
        Integer userID = 999; // Non-existent user
        Boolean isPrivate = true;

        // Mock the GET response to return empty array (user not found)
        ResponseEntity<Map[]> getResponseEntity = new ResponseEntity<>(new Map[]{}, HttpStatus.OK);

        when(restTemplate.exchange(
                contains("/rest/v1/users?id=eq." + userID),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map[].class)
        )).thenReturn(getResponseEntity);

        // Act
        ResponseEntity<String> response = userController.updatePrivacySetting(userID, isPrivate);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody());

        // Verify the GET request was made
        verify(restTemplate).exchange(
                contains("/rest/v1/users?id=eq." + userID),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map[].class)
        );

        // Verify that no PUT request was made
        verify(restTemplate, never()).exchange(
                anyString(),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }

    @Test
    void testUpdatePrivacySetting_NullResponseBody() {
        // Arrange
        Integer userID = 123;
        Boolean isPrivate = true;

        // Mock the GET response to return null body
        ResponseEntity<Map[]> getResponseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                contains("/rest/v1/users?id=eq." + userID),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map[].class)
        )).thenReturn(getResponseEntity);

        // Act
        ResponseEntity<String> response = userController.updatePrivacySetting(userID, isPrivate);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody());

        // Verify the GET request was made
        verify(restTemplate).exchange(
                contains("/rest/v1/users?id=eq." + userID),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map[].class)
        );

        // Verify that no PUT request was made
        verify(restTemplate, never()).exchange(
                anyString(),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }
}