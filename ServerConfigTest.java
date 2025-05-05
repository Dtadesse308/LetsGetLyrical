package edu.usc.csci310.project.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ServerConfigTest {

    @Test
    void testServletContainerExists() {
        ServerConfig serverConfig = new ServerConfig();

        ServletWebServerFactory factory = serverConfig.servletContainer();
        assertNotNull(factory, "Factory should not be null");
    }
}