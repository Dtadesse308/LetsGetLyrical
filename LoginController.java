package edu.usc.csci310.project.controllers;

import edu.usc.csci310.project.requests.LoginRegisterRequest;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/login")
public class LoginController {
    private static final String SUPABASE_URL = "https://cqqwwzmgbeckmdotlwdx.supabase.co";
    private static final int LOCK_DURATION_SECONDS = 30;
    private RestTemplate restTemplate;

    @Value("${SUPABASE_API_KEY}")
    private String SUPABASE_API_KEY;

    public LoginController() {
        this.restTemplate = new RestTemplate();
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping
    public ResponseEntity<?> login(@RequestBody LoginRegisterRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        String hashedPassword = DigestUtils.sha256Hex(password);

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", SUPABASE_API_KEY);
        headers.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        String url = SUPABASE_URL + "/rest/v1/users?username=eq." + username;
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<List<LoginRegisterRequest>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        List<LoginRegisterRequest> users = response.getBody();

        if (users == null || users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "User not found"));
        }

        LoginRegisterRequest user = users.get(0);

        boolean isLocked = checkIfAccountLocked(user.getId(), headers);
        if (isLocked) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Account is locked. Try again after 30 seconds."));
        }

        if (user.getPassword().equals(hashedPassword)) {
            resetAttempts(user.getId(), headers);
            LoginRegisterRequest result = new LoginRegisterRequest();
            result.setId(user.getId());
            result.setUsername(user.getUsername());
            return ResponseEntity.ok(result);
        } else {
            handleFailedLoginAttempt(user.getId(), headers);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Wrong password"));
        }
    }

    private boolean checkIfAccountLocked(Integer userId, HttpHeaders headers) {
        String url = SUPABASE_URL + "/rest/v1/users?id=eq." + userId + "&select=attempts,last_failed_attempt,third_last_failed_attempt";

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        List<Map<String, Object>> users = response.getBody();
        if (users == null || users.isEmpty()) {
            return false;
        }

        Map<String, Object> user = users.get(0);
        Integer attempts = (Integer) user.get("attempts");
        Object lastFailedObj = user.get("last_failed_attempt");
        Object thirdLastFailedObj = user.get("third_last_failed_attempt");

        if (attempts == null || attempts < 3 || lastFailedObj == null || thirdLastFailedObj == null) {
            return false;
        }

        try {
            Instant lastFailed = Instant.parse(lastFailedObj.toString());
            Instant thirdLastFailed = Instant.parse(thirdLastFailedObj.toString());
            Instant now = Instant.now();

            if (ChronoUnit.SECONDS.between(thirdLastFailed, lastFailed) <= 60) {
                if (ChronoUnit.SECONDS.between(lastFailed, now) < LOCK_DURATION_SECONDS) {
                    return true;
                } else {
                    resetAttempts(userId, headers);
                    return false;
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error checking if account locked: " + e.getMessage());
            return false;
        }
    }

    private void resetAttempts(Integer userId, HttpHeaders headers) {
        try {
            String getUserUrl = SUPABASE_URL + "/rest/v1/users?id=eq." + userId;

            ResponseEntity<List<Map<String, Object>>> userResponse = restTemplate.exchange(
                    getUserUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<>() {}
            );

            if (userResponse.getBody() == null || userResponse.getBody().isEmpty()) {
                return;
            }

            Map<String, Object> user = userResponse.getBody().get(0);
            String username = (String) user.get("username");
            String password = (String) user.get("password");
            Boolean isPrivate = (Boolean) user.get("is_private");

            if (username == null || password == null || isPrivate == null) {
                return;
            }

            String patchUrl = SUPABASE_URL + "/rest/v1/users?id=eq." + userId;

            HttpHeaders patchHeaders = new HttpHeaders();
            patchHeaders.putAll(headers);
            patchHeaders.set("Prefer", "return=minimal, resolution=merge-duplicates");

            Map<String, Object> updates = new HashMap<>();
            updates.put("id", userId);
            updates.put("username", username);
            updates.put("password", password);
            updates.put("is_private", isPrivate);
            updates.put("attempts", 0);
            updates.put("last_failed_attempt", null);
            updates.put("third_last_failed_attempt", null);

            HttpEntity<Map<String, Object>> updateEntity = new HttpEntity<>(updates, patchHeaders);

            restTemplate.exchange(
                    patchUrl,
                    HttpMethod.POST,
                    updateEntity,
                    String.class
            );
        } catch (Exception e) {
            System.err.println("Error resetting attempts: " + e.getMessage());
        }
    }

    private void handleFailedLoginAttempt(Integer userId, HttpHeaders headers) {
        try {
            String getUserUrl = SUPABASE_URL + "/rest/v1/users?id=eq." + userId;

            ResponseEntity<List<Map<String, Object>>> userResponse = restTemplate.exchange(
                    getUserUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<>() {}
            );

            if (userResponse.getBody() == null || userResponse.getBody().isEmpty()) {
                return;
            }

            Map<String, Object> user = userResponse.getBody().get(0);
            String username = (String) user.get("username");
            String password = (String) user.get("password");
            Boolean isPrivate = (Boolean) user.get("is_private");
            Integer attempts = (Integer) user.get("attempts");
            Object lastFailedObj = user.get("last_failed_attempt");

            if (username == null || password == null || isPrivate == null) {
                return;
            }

            if (attempts == null) {
                attempts = 0;
            }

            String updateUrl = SUPABASE_URL + "/rest/v1/users?id=eq." + userId;

            HttpHeaders updateHeaders = new HttpHeaders();
            updateHeaders.putAll(headers);
            updateHeaders.set("Prefer", "return=minimal, resolution=merge-duplicates");

            Map<String, Object> updates = new HashMap<>();
            String now = Instant.now().toString();

            updates.put("id", userId);
            updates.put("username", username);
            updates.put("password", password);
            updates.put("is_private", isPrivate);

            if (attempts == 0) {
                updates.put("attempts", 1);
                updates.put("last_failed_attempt", now);
            } else if (attempts == 1) {
                updates.put("attempts", 2);
                if (lastFailedObj != null) {
                    updates.put("third_last_failed_attempt", lastFailedObj.toString());
                }
                updates.put("last_failed_attempt", now);
            } else {
                updates.put("attempts", attempts + 1);
                if (lastFailedObj != null) {
                    updates.put("third_last_failed_attempt", lastFailedObj.toString());
                }
                updates.put("last_failed_attempt", now);
            }

            HttpEntity<Map<String, Object>> updateEntity = new HttpEntity<>(updates, updateHeaders);

            restTemplate.exchange(
                    updateUrl,
                    HttpMethod.POST,
                    updateEntity,
                    String.class
            );
        } catch (Exception e) {
            System.err.println("Error handling failed login attempt: " + e.getMessage());
        }
    }
}

