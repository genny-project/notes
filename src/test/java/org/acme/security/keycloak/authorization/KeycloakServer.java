package org.acme.security.keycloak.authorization;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

public class KeycloakServer implements QuarkusTestResourceLifecycleManager {
    private GenericContainer keycloak;

    @Override
    public Map<String, String> start() {
        keycloak = new FixedHostPortGenericContainer("quay.io/keycloak/keycloak:" + System.getProperty("keycloak.version"))
                .withFixedExposedPort(8180, 8080)
                .withFixedExposedPort(8543, 8443)
                //.withEnv("DB_VENDOR", "H2")
                .withEnv("DB_VENDOR", "mysql")
                .withEnv("DB_ADDR", "127.0.0.1")
                .withEnv("DB_PORT", "3336")
                .withEnv("DB_DATABASE", "gennydb")
                .withEnv("DB_USER", "genny")
                .withEnv("DB_PASSWORD", "password")
                .withEnv("KEYCLOAK_USER", "admin")
                .withEnv("KEYCLOAK_LOGLEVEL", "debug")
                .withEnv("KEYCLOAK_PASSWORD", "admin")
                .withEnv("JAVA_OPTS_APPEND", "-Djava.awt.headless=true")
                .withEnv("PREPEND_JAVA_OPTS", "-Dkeycloak.profile=preview -Dkeycloak.profile.feature.token_exchange=enabled -Dkeycloak.profile.feature.account_api=enabled")
                .withEnv("KEYCLOAK_IMPORT", "/config/realm.json")
                .withClasspathResourceMapping("quarkus-realm.json", "/config/realm.json", BindMode.READ_ONLY)
                .waitingFor(Wait.forHttp("/auth"))
                .withStartupTimeout(Duration.ofMinutes(2));
        keycloak.start();
        return Collections.emptyMap();
    }

    @Override
    public void stop() {
        keycloak.stop();
    }


}
