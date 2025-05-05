package edu.usc.csci310.project.controllers;

import edu.usc.csci310.project.requests.LoginRegisterRequest;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/register")
public class RegisterController {
    private static final String SUPABASE_URL = "https://cqqwwzmgbeckmdotlwdx.supabase.co";
    private RestTemplate restTemplate;

    @Value("${SUPABASE_API_KEY}")
    private String SUPABASE_API_KEY;

    public RegisterController() {
        this.restTemplate = new RestTemplate();
    }
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping
    public LoginRegisterRequest register(@RequestBody LoginRegisterRequest registerRequest) {
        String url = SUPABASE_URL + "/rest/v1/users";

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", SUPABASE_API_KEY);
        headers.set("Authorization", "Bearer " + SUPABASE_API_KEY);
        headers.set("Content-Type", "application/json");
        headers.set("Prefer", "return=representation");

        // HASH
        LoginRegisterRequest hashedRequest = new LoginRegisterRequest();
        hashedRequest.setPassword(DigestUtils.sha256Hex(registerRequest.getPassword()));
        hashedRequest.setUsername(registerRequest.getUsername());
        //hashedRequest.setUsername(DigestUtils.sha256Hex(registerRequest.getUsername()));

        HttpEntity<LoginRegisterRequest> entity = new HttpEntity<>(hashedRequest, headers);
        try {
            ResponseEntity<List<LoginRegisterRequest>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );
            List<LoginRegisterRequest> user = response.getBody();

            if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                registerRequest.setId(user.get(0).getId());
                return registerRequest;
            }
            else {
                throw new RuntimeException("Invalid request");
            }
        }
        catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                throw new RuntimeException("Username already exists");
            }
        }
        return null;
    }
}
