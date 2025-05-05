package edu.usc.csci310.project.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    private static final String SUPABASE_URL = "https://cqqwwzmgbeckmdotlwdx.supabase.co";
    private RestTemplate restTemplate;

    @Value("${SUPABASE_API_KEY}")
    private String SUPABASE_API_KEY;

    public UserController() {
        this.restTemplate = new RestTemplate();
    }

    @GetMapping("/privacy")
    public Map<String, Boolean> getPrivacySetting(@RequestParam Integer userID) {
        String url = SUPABASE_URL + "/rest/v1/users?id=eq." + userID;

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", SUPABASE_API_KEY);
        headers.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        headers.set("Accept", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map[].class
        );

        Map[] responseBody = response.getBody();
        if (responseBody == null || responseBody.length == 0) {
            throw new RuntimeException("User not found");
        }
        System.out.println(responseBody[0]);

        Boolean isPrivate = (Boolean) responseBody[0].get("is_private");
        System.out.println(isPrivate);
        if (isPrivate == null) {
            isPrivate = true;
        }

        Map<String, Boolean> result = new HashMap<>();
        result.put("is_private", isPrivate);
        return result;
    }

    @PutMapping("/privacy")
    public ResponseEntity<String> updatePrivacySetting(@RequestParam Integer userID, @RequestParam Boolean isPrivate) {
        try {
            String getUrl = SUPABASE_URL + "/rest/v1/users?id=eq." + userID;

            HttpHeaders getHeaders = new HttpHeaders();
            getHeaders.set("apikey", SUPABASE_API_KEY);
            getHeaders.set("Authorization", "Bearer " + SUPABASE_API_KEY);
            getHeaders.set("Accept", "application/json");

            HttpEntity<String> getEntity = new HttpEntity<>(getHeaders);
            ResponseEntity<Map[]> getResponse = restTemplate.exchange(
                    getUrl,
                    HttpMethod.GET,
                    getEntity,
                    Map[].class
            );

            Map[] responseBody = getResponse.getBody();
            if (responseBody == null || responseBody.length == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found");
            }

            Map<String, Object> userData = new HashMap<>(responseBody[0]);
            userData.put("is_private", isPrivate);

            String updateUrl = SUPABASE_URL + "/rest/v1/users?id=eq." + userID;

            HttpHeaders updateHeaders = new HttpHeaders();
            updateHeaders.set("apikey", SUPABASE_API_KEY);
            updateHeaders.set("Authorization", "Bearer " + SUPABASE_API_KEY);
            updateHeaders.set("Content-Type", "application/json");
            updateHeaders.set("Prefer", "return=minimal");

            HttpEntity<Map<String, Object>> updateEntity = new HttpEntity<>(userData, updateHeaders);

            ResponseEntity<Void> updateResponse = restTemplate.exchange(
                    updateUrl,
                    HttpMethod.PUT,
                    updateEntity,
                    Void.class
            );

            if (updateResponse.getStatusCode() == HttpStatus.NO_CONTENT) {
                return ResponseEntity.ok("Privacy setting updated successfully");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Unexpected response from Supabase: " + updateResponse.getStatusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating privacy setting: " + e.getMessage());
        }
    }

}
