package edu.usc.csci310.project.controllers;

import edu.usc.csci310.project.requests.LoginRegisterRequest;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LoginControllerTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private LoginController loginController;

    private static final String SUPABASE_URL = "https://cqqwwzmgbeckmdotlwdx.supabase.co";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_HASHED_PASSWORD = DigestUtils.sha256Hex(TEST_PASSWORD);
    private static final Integer TEST_USER_ID = 1;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        loginController.setRestTemplate(restTemplate);
    }

    @Test
    public void testSuccessfulLogin() {
        LoginRegisterRequest loginRequest = new LoginRegisterRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword(TEST_PASSWORD);

        List<LoginRegisterRequest> users = new ArrayList<>();
        LoginRegisterRequest user = new LoginRegisterRequest();
        user.setId(TEST_USER_ID);
        user.setUsername(TEST_USERNAME);
        user.setPassword(TEST_HASHED_PASSWORD);
        users.add(user);

        ResponseEntity<List<LoginRegisterRequest>> getUserResponse =
                new ResponseEntity<>(users, HttpStatus.OK);
        when(restTemplate.exchange(
                contains(SUPABASE_URL + "/rest/v1/users?username=eq." + TEST_USERNAME),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(getUserResponse);

        // account locked check
        List<Map<String, Object>> accountLockCheckUsers = new ArrayList<>();
        Map<String, Object> lockCheckUser = new HashMap<>();
        lockCheckUser.put("attempts", 0);
        lockCheckUser.put("last_failed_attempt", null);
        lockCheckUser.put("third_last_failed_attempt", null);
        accountLockCheckUsers.add(lockCheckUser);
        ResponseEntity<List<Map<String, Object>>> lockCheckResponse =
                new ResponseEntity<>(accountLockCheckUsers, HttpStatus.OK);

        //mock reset attempts
        List<Map<String, Object>> userDetails = new ArrayList<>();
        Map<String, Object> userDetail = new HashMap<>();
        userDetail.put("username", TEST_USERNAME);
        userDetail.put("password", TEST_HASHED_PASSWORD);
        userDetail.put("is_private", false);
        userDetails.add(userDetail);
        ResponseEntity<List<Map<String, Object>>> userDetailsResponse =
                new ResponseEntity<>(userDetails, HttpStatus.OK);

        when(restTemplate.exchange(
                contains(SUPABASE_URL + "/rest/v1/users?id=eq." + TEST_USER_ID),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(lockCheckResponse)
                .thenReturn(userDetailsResponse);

        ResponseEntity<String> resetResponse = new ResponseEntity<>("", HttpStatus.OK);
        when(restTemplate.exchange(
                contains(SUPABASE_URL + "/rest/v1/users?id=eq." + TEST_USER_ID),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(resetResponse);

        ResponseEntity<?> response = loginController.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof LoginRegisterRequest);
        LoginRegisterRequest result = (LoginRegisterRequest) response.getBody();
        assertEquals(TEST_USER_ID, result.getId());
        assertEquals(TEST_USERNAME, result.getUsername());
        assertNull(result.getPassword());
    }

    @Test
    public void testUserNotFound() {
        LoginRegisterRequest loginRequest = new LoginRegisterRequest();
        loginRequest.setUsername("nonexistentuser");
        loginRequest.setPassword(TEST_PASSWORD);

        //mock user not found
        ResponseEntity<List<LoginRegisterRequest>> emptyResponse =
                new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(emptyResponse);

        ResponseEntity<?> response = loginController.login(loginRequest);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> errorMap = (Map<String, String>) response.getBody();
        assertEquals("User not found", errorMap.get("message"));
    }

    @Test
    public void testWrongPassword() {
        LoginRegisterRequest loginRequest = new LoginRegisterRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword("wrongPassword");

        List<LoginRegisterRequest> users = new ArrayList<>();
        LoginRegisterRequest user = new LoginRegisterRequest();
        user.setId(TEST_USER_ID);
        user.setUsername(TEST_USERNAME);
        user.setPassword(TEST_HASHED_PASSWORD);
        users.add(user);

        ResponseEntity<List<LoginRegisterRequest>> getUserResponse =
                new ResponseEntity<>(users, HttpStatus.OK);
        when(restTemplate.exchange(
                contains(SUPABASE_URL + "/rest/v1/users?username=eq." + TEST_USERNAME),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(getUserResponse);

        // mock acc locked
        List<Map<String, Object>> accountLockCheckUsers = new ArrayList<>();
        Map<String, Object> lockCheckUser = new HashMap<>();
        lockCheckUser.put("attempts", 0);
        lockCheckUser.put("last_failed_attempt", null);
        lockCheckUser.put("third_last_failed_attempt", null);
        accountLockCheckUsers.add(lockCheckUser);
        ResponseEntity<List<Map<String, Object>>> lockCheckResponse =
                new ResponseEntity<>(accountLockCheckUsers, HttpStatus.OK);

        List<Map<String, Object>> userDetails = new ArrayList<>();
        Map<String, Object> userDetail = new HashMap<>();
        userDetail.put("username", TEST_USERNAME);
        userDetail.put("password", TEST_HASHED_PASSWORD);
        userDetail.put("is_private", false);
        userDetail.put("attempts", 0);
        userDetail.put("last_failed_attempt", null);
        userDetails.add(userDetail);
        ResponseEntity<List<Map<String, Object>>> userDetailsResponse =
                new ResponseEntity<>(userDetails, HttpStatus.OK);

        when(restTemplate.exchange(
                contains(SUPABASE_URL + "/rest/v1/users?id=eq." + TEST_USER_ID),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(lockCheckResponse)
                .thenReturn(userDetailsResponse);

        ResponseEntity<String> updateResponse = new ResponseEntity<>("", HttpStatus.OK);
        when(restTemplate.exchange(
                contains(SUPABASE_URL + "/rest/v1/users?id=eq." + TEST_USER_ID),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(updateResponse);

        ResponseEntity<?> response = loginController.login(loginRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> errorMap = (Map<String, String>) response.getBody();
        assertEquals("Wrong password", errorMap.get("message"));
    }

    @Test
    public void testLockedAccount() {
        LoginRegisterRequest loginRequest = new LoginRegisterRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword(TEST_PASSWORD);

        List<LoginRegisterRequest> users = new ArrayList<>();
        LoginRegisterRequest user = new LoginRegisterRequest();
        user.setId(TEST_USER_ID);
        user.setUsername(TEST_USERNAME);
        user.setPassword(TEST_HASHED_PASSWORD);
        users.add(user);

        ResponseEntity<List<LoginRegisterRequest>> getUserResponse =
                new ResponseEntity<>(users, HttpStatus.OK);
        when(restTemplate.exchange(
                contains(SUPABASE_URL + "/rest/v1/users?username=eq." + TEST_USERNAME),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(getUserResponse);

        //mock account locked
        List<Map<String, Object>> accountLockCheckUsers = new ArrayList<>();
        Map<String, Object> lockCheckUser = new HashMap<>();
        lockCheckUser.put("attempts", 3);

        //failed attempts
        Instant now = Instant.now();
        Instant lastFailed = now.minusSeconds(10); // 10 seconds ago (still within lock period)
        Instant thirdLastFailed = now.minusSeconds(40); // 40 seconds ago (within 60 second window)

        lockCheckUser.put("last_failed_attempt", lastFailed.toString());
        lockCheckUser.put("third_last_failed_attempt", thirdLastFailed.toString());
        accountLockCheckUsers.add(lockCheckUser);

        ResponseEntity<List<Map<String, Object>>> lockCheckResponse =
                new ResponseEntity<>(accountLockCheckUsers, HttpStatus.OK);

        when(restTemplate.exchange(
                contains(SUPABASE_URL + "/rest/v1/users?id=eq." + TEST_USER_ID),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(lockCheckResponse);

        ResponseEntity<?> response = loginController.login(loginRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> errorMap = (Map<String, String>) response.getBody();
        assertEquals("Account is locked. Try again after 30 seconds.", errorMap.get("message"));
    }

    @Test
    public void testLockedAccountExpired() {
        LoginRegisterRequest loginRequest = new LoginRegisterRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword(TEST_PASSWORD);

        List<LoginRegisterRequest> users = new ArrayList<>();
        LoginRegisterRequest user = new LoginRegisterRequest();
        user.setId(TEST_USER_ID);
        user.setUsername(TEST_USERNAME);
        user.setPassword(TEST_HASHED_PASSWORD);
        users.add(user);

        ResponseEntity<List<LoginRegisterRequest>> getUserResponse =
                new ResponseEntity<>(users, HttpStatus.OK);
        when(restTemplate.exchange(
                contains(SUPABASE_URL + "/rest/v1/users?username=eq." + TEST_USERNAME),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(getUserResponse);

        //mock lock expired
        List<Map<String, Object>> accountLockCheckUsers = new ArrayList<>();
        Map<String, Object> lockCheckUser = new HashMap<>();
        lockCheckUser.put("attempts", 3);

        //failed attempts
        Instant now = Instant.now();
        Instant lastFailed = now.minusSeconds(40); // 40 sec (lock expired)
        Instant thirdLastFailed = now.minusSeconds(70); // 70 seconds

        lockCheckUser.put("last_failed_attempt", lastFailed.toString());
        lockCheckUser.put("third_last_failed_attempt", thirdLastFailed.toString());
        accountLockCheckUsers.add(lockCheckUser);

        ResponseEntity<List<Map<String, Object>>> lockCheckResponse =
                new ResponseEntity<>(accountLockCheckUsers, HttpStatus.OK);

        List<Map<String, Object>> userDetails = new ArrayList<>();
        Map<String, Object> userDetail = new HashMap<>();
        userDetail.put("id", TEST_USER_ID);
        userDetail.put("username", TEST_USERNAME);
        userDetail.put("password", TEST_HASHED_PASSWORD);
        userDetail.put("is_private", false);
        userDetails.add(userDetail);

        ResponseEntity<List<Map<String, Object>>> userDetailsResponse =
                new ResponseEntity<>(userDetails, HttpStatus.OK);

        when(restTemplate.exchange(
                contains(SUPABASE_URL + "/rest/v1/users?id=eq." + TEST_USER_ID),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(lockCheckResponse)
                .thenReturn(userDetailsResponse);

        ResponseEntity<String> resetResponse = new ResponseEntity<>("", HttpStatus.OK);
        when(restTemplate.exchange(
                contains(SUPABASE_URL + "/rest/v1/users?id=eq." + TEST_USER_ID),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(resetResponse);

        ResponseEntity<?> response = loginController.login(loginRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testResetAttemptsNullResponse() {
        // Test scenario where response is null when resetting attempts
        LoginRegisterRequest loginRequest = new LoginRegisterRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword(TEST_PASSWORD);

        List<LoginRegisterRequest> users = new ArrayList<>();
        LoginRegisterRequest user = new LoginRegisterRequest();
        user.setId(TEST_USER_ID);
        user.setUsername(TEST_USERNAME);
        user.setPassword(TEST_HASHED_PASSWORD);
        users.add(user);

        ResponseEntity<List<LoginRegisterRequest>> getUserResponse =
                new ResponseEntity<>(users, HttpStatus.OK);
        when(restTemplate.exchange(
                contains(SUPABASE_URL + "/rest/v1/users?username=eq." + TEST_USERNAME),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(getUserResponse);

        List<Map<String, Object>> accountLockCheckUsers = new ArrayList<>();
        Map<String, Object> lockCheckUser = new HashMap<>();
        lockCheckUser.put("attempts", 0);
        lockCheckUser.put("last_failed_attempt", null);
        lockCheckUser.put("third_last_failed_attempt", null);
        accountLockCheckUsers.add(lockCheckUser);
        ResponseEntity<List<Map<String, Object>>> lockCheckResponse =
                new ResponseEntity<>(accountLockCheckUsers, HttpStatus.OK);

        // Mock null response when getting user details for reset
        ResponseEntity<List<Map<String, Object>>> nullUserResponse =
                new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                contains(SUPABASE_URL + "/rest/v1/users?id=eq." + TEST_USER_ID),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(lockCheckResponse)
                .thenReturn(nullUserResponse);

        ResponseEntity<?> response = loginController.login(loginRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testResetAttemptsEmptyResponse() {
        // Test scenario where response is empty when resetting attempts
        LoginRegisterRequest loginRequest = new LoginRegisterRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword(TEST_PASSWORD);

        List<LoginRegisterRequest> users = new ArrayList<>();
        LoginRegisterRequest user = new LoginRegisterRequest();
        user.setId(TEST_USER_ID);
        user.setUsername(TEST_USERNAME);
        user.setPassword(TEST_HASHED_PASSWORD);
        users.add(user);

        ResponseEntity<List<LoginRegisterRequest>> getUserResponse =
                new ResponseEntity<>(users, HttpStatus.OK);
        when(restTemplate.exchange(
                contains(SUPABASE_URL + "/rest/v1/users?username=eq." + TEST_USERNAME),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(getUserResponse);

        List<Map<String, Object>> accountLockCheckUsers = new ArrayList<>();
        Map<String, Object> lockCheckUser = new HashMap<>();
        lockCheckUser.put("attempts", 0);
        lockCheckUser.put("last_failed_attempt", null);
        lockCheckUser.put("third_last_failed_attempt", null);
        accountLockCheckUsers.add(lockCheckUser);
        ResponseEntity<List<Map<String, Object>>> lockCheckResponse =
                new ResponseEntity<>(accountLockCheckUsers, HttpStatus.OK);

        // Mock empty response when getting user details for reset
        ResponseEntity<List<Map<String, Object>>> emptyUserResponse =
                new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);

        when(restTemplate.exchange(
                contains(SUPABASE_URL + "/rest/v1/users?id=eq." + TEST_USER_ID),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(lockCheckResponse)
                .thenReturn(emptyUserResponse);

        ResponseEntity<?> response = loginController.login(loginRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testResetAttemptsMissingFields() {
        // Test scenario where user details are missing fields
        LoginRegisterRequest loginRequest = new LoginRegisterRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword(TEST_PASSWORD);

        List<LoginRegisterRequest> users = new ArrayList<>();
        LoginRegisterRequest user = new LoginRegisterRequest();
        user.setId(TEST_USER_ID);
        user.setUsername(TEST_USERNAME);
        user.setPassword(TEST_HASHED_PASSWORD);
        users.add(user);

        ResponseEntity<List<LoginRegisterRequest>> getUserResponse =
                new ResponseEntity<>(users, HttpStatus.OK);
        when(restTemplate.exchange(
                contains(SUPABASE_URL + "/rest/v1/users?username=eq." + TEST_USERNAME),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(getUserResponse);

        List<Map<String, Object>> accountLockCheckUsers = new ArrayList<>();
        Map<String, Object> lockCheckUser = new HashMap<>();
        lockCheckUser.put("attempts", 0);
        lockCheckUser.put("last_failed_attempt", null);
        lockCheckUser.put("third_last_failed_attempt", null);
        accountLockCheckUsers.add(lockCheckUser);
        ResponseEntity<List<Map<String, Object>>> lockCheckResponse =
                new ResponseEntity<>(accountLockCheckUsers, HttpStatus.OK);

        // Mock response with missing fields
        List<Map<String, Object>> userDetails = new ArrayList<>();
        Map<String, Object> userDetail = new HashMap<>();
        userDetail.put("username", TEST_USERNAME);
        // Missing password and is_private fields
        userDetails.add(userDetail);
        ResponseEntity<List<Map<String, Object>>> userDetailsResponse =
                new ResponseEntity<>(userDetails, HttpStatus.OK);

        when(restTemplate.exchange(
                contains(SUPABASE_URL + "/rest/v1/users?id=eq." + TEST_USER_ID),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(lockCheckResponse)
                .thenReturn(userDetailsResponse);

        ResponseEntity<?> response = loginController.login(loginRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testHandleFailedLoginAttemptNullResponse() {
        // Test failed login with null response
        LoginRegisterRequest loginRequest = new LoginRegisterRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword("wrongPassword");

        List<LoginRegisterRequest> users = new ArrayList<>();
        LoginRegisterRequest user = new LoginRegisterRequest();
        user.setId(TEST_USER_ID);
        user.setUsername(TEST_USERNAME);
        user.setPassword(TEST_HASHED_PASSWORD);
        users.add(user);

        ResponseEntity<List<LoginRegisterRequest>> getUserResponse =
                new ResponseEntity<>(users, HttpStatus.OK);
        when(restTemplate.exchange(
                contains(SUPABASE_URL + "/rest/v1/users?username=eq." + TEST_USERNAME),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(getUserResponse);

        List<Map<String, Object>> accountLockCheckUsers = new ArrayList<>();
        Map<String, Object> lockCheckUser = new HashMap<>();
        lockCheckUser.put("attempts", 0);
        lockCheckUser.put("last_failed_attempt", null);
        lockCheckUser.put("third_last_failed_attempt", null);
        accountLockCheckUsers.add(lockCheckUser);
        ResponseEntity<List<Map<String, Object>>> lockCheckResponse =
                new ResponseEntity<>(accountLockCheckUsers, HttpStatus.OK);

        // Mock null response for failed login attempt
        ResponseEntity<List<Map<String, Object>>> nullUserResponse =
                new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.exchange(
                contains(SUPABASE_URL + "/rest/v1/users?id=eq." + TEST_USER_ID),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(lockCheckResponse)
                .thenReturn(nullUserResponse);

        ResponseEntity<?> response = loginController.login(loginRequest);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> errorMap = (Map<String, String>) response.getBody();
        assertEquals("Wrong password", errorMap.get("message"));
    }

    @Test
    public void testHandleFailedLoginAttemptEmptyResponse() {
        // Test failed login with empty response
        LoginRegisterRequest loginRequest = new LoginRegisterRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword("wrongPassword");

        List<LoginRegisterRequest> users = new ArrayList<>();
        LoginRegisterRequest user = new LoginRegisterRequest();
        user.setId(TEST_USER_ID);
        user.setUsername(TEST_USERNAME);
        user.setPassword(TEST_HASHED_PASSWORD);
        users.add(user);

        ResponseEntity<List<LoginRegisterRequest>> getUserResponse =
                new ResponseEntity<>(users, HttpStatus.OK);
        when(restTemplate.exchange(
                contains(SUPABASE_URL + "/rest/v1/users?username=eq." + TEST_USERNAME),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(getUserResponse);

        List<Map<String, Object>> accountLockCheckUsers = new ArrayList<>();
        Map<String, Object> lockCheckUser = new HashMap<>();
        lockCheckUser.put("attempts", 0);
        lockCheckUser.put("last_failed_attempt", null);
        lockCheckUser.put("third_last_failed_attempt", null);
        accountLockCheckUsers.add(lockCheckUser);
        ResponseEntity<List<Map<String, Object>>> lockCheckResponse =
                new ResponseEntity<>(accountLockCheckUsers, HttpStatus.OK);

        // Mock empty response for failed login attempt
        ResponseEntity<List<Map<String, Object>>> emptyUserResponse =
                new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);

        when(restTemplate.exchange(
                contains(SUPABASE_URL + "/rest/v1/users?id=eq." + TEST_USER_ID),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(lockCheckResponse)
                .thenReturn(emptyUserResponse);

        ResponseEntity<?> response = loginController.login(loginRequest);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> errorMap = (Map<String, String>) response.getBody();
        assertEquals("Wrong password", errorMap.get("message"));
    }

    @Test
    public void testHandleFailedLoginAttemptMissingFields() {
        // Test failed login with missing fields
        LoginRegisterRequest loginRequest = new LoginRegisterRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword("wrongPassword");

        List<LoginRegisterRequest> users = new ArrayList<>();
        LoginRegisterRequest user = new LoginRegisterRequest();
        user.setId(TEST_USER_ID);
        user.setUsername(TEST_USERNAME);
        user.setPassword(TEST_HASHED_PASSWORD);
        users.add(user);

        ResponseEntity<List<LoginRegisterRequest>> getUserResponse =
                new ResponseEntity<>(users, HttpStatus.OK);
        when(restTemplate.exchange(
                contains(SUPABASE_URL + "/rest/v1/users?username=eq." + TEST_USERNAME),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(getUserResponse);

        List<Map<String, Object>> accountLockCheckUsers = new ArrayList<>();
        Map<String, Object> lockCheckUser = new HashMap<>();
        lockCheckUser.put("attempts", 0);
        lockCheckUser.put("last_failed_attempt", null);
        lockCheckUser.put("third_last_failed_attempt", null);
        accountLockCheckUsers.add(lockCheckUser);
        ResponseEntity<List<Map<String, Object>>> lockCheckResponse =
                new ResponseEntity<>(accountLockCheckUsers, HttpStatus.OK);

        // Response with missing fields for failed login
        List<Map<String, Object>> userDetails = new ArrayList<>();
        Map<String, Object> userDetail = new HashMap<>();
        // Missing fields
        userDetails.add(userDetail);
        ResponseEntity<List<Map<String, Object>>> userDetailsResponse =
                new ResponseEntity<>(userDetails, HttpStatus.OK);

        when(restTemplate.exchange(
                contains(SUPABASE_URL + "/rest/v1/users?id=eq." + TEST_USER_ID),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(lockCheckResponse)
                .thenReturn(userDetailsResponse);

        ResponseEntity<String> updateResponse = new ResponseEntity<>("", HttpStatus.OK);
        when(restTemplate.exchange(
                contains(SUPABASE_URL + "/rest/v1/users?id=eq." + TEST_USER_ID),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(updateResponse);

        ResponseEntity<?> response = loginController.login(loginRequest);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> errorMap = (Map<String, String>) response.getBody();
        assertEquals("Wrong password", errorMap.get("message"));
    }

    @Test
    public void testHandleFailedLoginAttemptNullAttempts() {
        // Test failed login with null attempts
        LoginRegisterRequest loginRequest = new LoginRegisterRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword("wrongPassword");

        List<LoginRegisterRequest> users = new ArrayList<>();
        LoginRegisterRequest user = new LoginRegisterRequest();
        user.setId(TEST_USER_ID);
        user.setUsername(TEST_USERNAME);
        user.setPassword(TEST_HASHED_PASSWORD);
        users.add(user);

        ResponseEntity<List<LoginRegisterRequest>> getUserResponse =
                new ResponseEntity<>(users, HttpStatus.OK);
        when(restTemplate.exchange(
                contains(SUPABASE_URL + "/rest/v1/users?username=eq." + TEST_USERNAME),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(getUserResponse);

        List<Map<String, Object>> accountLockCheckUsers = new ArrayList<>();
        Map<String, Object> lockCheckUser = new HashMap<>();
        lockCheckUser.put("attempts", 0);
        lockCheckUser.put("last_failed_attempt", null);
        lockCheckUser.put("third_last_failed_attempt", null);
        accountLockCheckUsers.add(lockCheckUser);
        ResponseEntity<List<Map<String, Object>>> lockCheckResponse =
                new ResponseEntity<>(accountLockCheckUsers, HttpStatus.OK);

        // Response with null attempts for failed login
        List<Map<String, Object>> userDetails = new ArrayList<>();
        Map<String, Object> userDetail = new HashMap<>();
        userDetail.put("username", TEST_USERNAME);
        userDetail.put("password", TEST_HASHED_PASSWORD);
        userDetail.put("is_private", false);
        userDetail.put("attempts", null);
        userDetail.put("last_failed_attempt", null);
        userDetails.add(userDetail);
        ResponseEntity<List<Map<String, Object>>> userDetailsResponse =
                new ResponseEntity<>(userDetails, HttpStatus.OK);

        when(restTemplate.exchange(
                contains(SUPABASE_URL + "/rest/v1/users?id=eq." + TEST_USER_ID),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(lockCheckResponse)
                .thenReturn(userDetailsResponse);

        ResponseEntity<String> updateResponse = new ResponseEntity<>("", HttpStatus.OK);
        when(restTemplate.exchange(
                contains(SUPABASE_URL + "/rest/v1/users?id=eq." + TEST_USER_ID),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(updateResponse);

        ResponseEntity<?> response = loginController.login(loginRequest);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> errorMap = (Map<String, String>) response.getBody();
        assertEquals("Wrong password", errorMap.get("message"));
    }
}