package edu.usc.csci310.project.controllers;

import edu.usc.csci310.project.requests.LoginRegisterRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.web.client.HttpClientErrorException;
import org.mockito.Mockito;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegisterControllerTest {
    private static final String SUPABASE_URL = "https://cqqwwzmgbeckmdotlwdx.supabase.co";

    @Test
    public void testRegisterSuccess() {
        RegisterController controller = new RegisterController();
        RestTemplate mockRestTemplate = Mockito.mock(RestTemplate.class);
        controller.setRestTemplate(mockRestTemplate);

        LoginRegisterRequest registerRequest = new LoginRegisterRequest();
        registerRequest.setUsername("test");
        registerRequest.setPassword("1234");

        String url = SUPABASE_URL + "/rest/v1/users";

        List<LoginRegisterRequest> returnedList = Collections.singletonList(registerRequest);
        ResponseEntity<List<LoginRegisterRequest>> fakeResponse =
                new ResponseEntity<>(returnedList, HttpStatus.CREATED);

        Mockito.when(mockRestTemplate.exchange(
                Mockito.eq(url),
                Mockito.eq(HttpMethod.POST),
                Mockito.any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<List<LoginRegisterRequest>>>any())
        ).thenReturn(fakeResponse);

        LoginRegisterRequest result = controller.register(registerRequest);
        assertEquals("test", result.getUsername());
        assertEquals("1234", result.getPassword());
    }

    @Test
    public void testRegisterSuccessOk() {
        RegisterController controller = new RegisterController();
        RestTemplate mockRestTemplate = Mockito.mock(RestTemplate.class);
        controller.setRestTemplate(mockRestTemplate);

        LoginRegisterRequest registerRequest = new LoginRegisterRequest();
        registerRequest.setUsername("bob");
        registerRequest.setPassword("123");

        String url = SUPABASE_URL + "/rest/v1/users";
        List<LoginRegisterRequest> returnedList = Collections.singletonList(registerRequest);
        ResponseEntity<List<LoginRegisterRequest>> fakeResponse =
                new ResponseEntity<>(returnedList, HttpStatus.OK);

        Mockito.when(mockRestTemplate.exchange(
                Mockito.eq(url),
                Mockito.eq(HttpMethod.POST),
                Mockito.any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<List<LoginRegisterRequest>>>any())
        ).thenReturn(fakeResponse);

        LoginRegisterRequest result = controller.register(registerRequest);
        assertNotNull(result);
        assertEquals("bob", result.getUsername());
        assertEquals("123", result.getPassword());
    }

    @Test
    public void testInvalidRequest() {
        RegisterController controller = new RegisterController();
        RestTemplate mockRestTemplate = Mockito.mock(RestTemplate.class);
        controller.setRestTemplate(mockRestTemplate);

        LoginRegisterRequest registerRequest = new LoginRegisterRequest();
        registerRequest.setUsername("");
        registerRequest.setPassword("");

        String url = SUPABASE_URL + "/rest/v1/users";
        ResponseEntity<List<LoginRegisterRequest>> fakeResponse =
                new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

        Mockito.when(mockRestTemplate.exchange(
                Mockito.eq(url),
                Mockito.eq(HttpMethod.POST),
                Mockito.any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<List<LoginRegisterRequest>>>any())
        ).thenReturn(fakeResponse);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> controller.register(registerRequest));
        assertEquals("Invalid request", thrown.getMessage());
    }

    @Test
    public void testRegisterClientConflict() {
        RegisterController controller = new RegisterController();
        RestTemplate mockRestTemplate = Mockito.mock(RestTemplate.class);
        controller.setRestTemplate(mockRestTemplate);

        LoginRegisterRequest registerRequest = new LoginRegisterRequest();
        registerRequest.setUsername("conflict");
        registerRequest.setPassword("conflict");

        String url = SUPABASE_URL + "/rest/v1/users";
        HttpClientErrorException conflictException = HttpClientErrorException.create(
                HttpStatus.CONFLICT,
                "Conflict",
                HttpHeaders.EMPTY,
                null,
                null
        );

        Mockito.when(mockRestTemplate.exchange(
                Mockito.eq(url),
                Mockito.eq(HttpMethod.POST),
                Mockito.any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<List<LoginRegisterRequest>>>any())
        ).thenThrow(conflictException);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> controller.register(registerRequest));
        assertEquals("Username already exists", thrown.getMessage());
    }

    @Test
    public void testRegisterClientNonConflict() {
        RegisterController controller = new RegisterController();
        RestTemplate mockRestTemplate = Mockito.mock(RestTemplate.class);
        controller.setRestTemplate(mockRestTemplate);

        LoginRegisterRequest registerRequest = new LoginRegisterRequest();
        registerRequest.setUsername("bad");
        registerRequest.setPassword("bad");

        String url = SUPABASE_URL + "/rest/v1/users";
        HttpClientErrorException nonConflictException = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                HttpHeaders.EMPTY,
                null,
                null
        );

        Mockito.when(mockRestTemplate.exchange(
                Mockito.eq(url),
                Mockito.eq(HttpMethod.POST),
                Mockito.any(HttpEntity.class),
                ArgumentMatchers.<ParameterizedTypeReference<List<LoginRegisterRequest>>>any())
        ).thenThrow(nonConflictException);

        LoginRegisterRequest result = controller.register(registerRequest);
        assertNull(result);
    }
}
