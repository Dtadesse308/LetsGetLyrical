package edu.usc.csci310.project.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

public class RestTemplateConfigTest {

    @Test
    void testRestTemplateCreation() {
        RestTemplateConfig config = new RestTemplateConfig();
        RestTemplate restTemplate = config.restTemplate();
        assertNotNull(restTemplate, "RestTemplate should not be null");
    }

    @Test
    void testRestTemplateIsNewInstance() {
        RestTemplateConfig config = new RestTemplateConfig();
        RestTemplate firstInstance = config.restTemplate();
        RestTemplate secondInstance = config.restTemplate();
        assertNotNull(firstInstance, "First RestTemplate instance should not be null");
        assertNotNull(secondInstance, "Second RestTemplate instance should not be null");
        assertNotSame(firstInstance, secondInstance,
                "Each call to restTemplate() should return a new instance");
    }
}