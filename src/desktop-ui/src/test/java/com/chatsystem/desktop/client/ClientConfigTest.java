package com.chatsystem.desktop.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class ClientConfigTest {
    @Test
    void derivesWsAndWssUrisAndResolvesPaths() {
        ClientConfig httpConfig = ClientConfig.defaults();
        assertEquals(URI.create("ws://127.0.0.1:8080/ws"), httpConfig.wsUri());
        assertEquals(URI.create("http://127.0.0.1:8080/api/v1/auth/me"), httpConfig.resolve("/api/v1/auth/me"));

        ClientConfig httpsConfig = new ClientConfig(
                URI.create("https://chat.example.test:8443/"),
                Duration.ofSeconds(3),
                Duration.ofSeconds(10),
                Path.of("client-truststore.p12"),
                "secret"
        );
        assertEquals(URI.create("wss://chat.example.test:8443/ws"), httpsConfig.wsUri());
        assertEquals(URI.create("https://chat.example.test:8443/api/v1/health"), httpsConfig.resolve("api/v1/health"));
    }

    @Test
    void loadsPropertiesWithDefaultsAndRejectsNonHttpSchemes() {
        Properties props = new Properties();
        props.setProperty(ClientConfig.BASE_URL_PROPERTY, "https://api.whatsup.test");
        props.setProperty(ClientConfig.CONNECT_TIMEOUT_SEC_PROPERTY, "7");
        props.setProperty(ClientConfig.REQUEST_TIMEOUT_SEC_PROPERTY, "30");

        ClientConfig parsed = ClientConfig.fromProperties(props);
        assertEquals(URI.create("https://api.whatsup.test"), parsed.baseUri());
        assertEquals(Duration.ofSeconds(7), parsed.connectTimeout());
        assertEquals(Duration.ofSeconds(30), parsed.requestTimeout());
        assertNull(parsed.tlsTrustStore());

        Properties bad = new Properties();
        bad.setProperty(ClientConfig.BASE_URL_PROPERTY, "ftp://invalid.test");
        assertThrows(IllegalArgumentException.class, () -> ClientConfig.fromProperties(bad));
    }
}
