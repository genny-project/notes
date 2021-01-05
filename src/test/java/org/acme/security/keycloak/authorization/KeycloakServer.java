package org.acme.security.keycloak.authorization;


import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import life.genny.notes.utils.PropertiesReader;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class KeycloakServer implements QuarkusTestResourceLifecycleManager {
    private GenericContainer keycloak;
    
	static public String keycloakUrl;

    @Override
    public int order(){
      return 2;
    }
	 public static String KEYCLOAK_VERSION = new PropertiesReader("genny.properties").getProperty("keycloak.version","12.0.1");

    @Override
    public Map<String, String> start() {
    	
    	Map<String, String> returnCollections = new HashMap<String,String>();
    	
    	
        keycloak = new FixedHostPortGenericContainer("quay.io/keycloak/keycloak:" + KEYCLOAK_VERSION)//System.getProperty("keycloak.version"))
                .withEnv("KEYCLOAK_USER", "admin")
                .withEnv("KEYCLOAK_PASSWORD", "admin")
                .withEnv("KEYCLOAK_LOGLEVEL", "debug")
                .withEnv("KEYCLOAK_IMPORT", "/config/realm.json")
                 .dependsOn(MySqlServer.mysql)
                .withEnv("DB_VENDOR", "mysql")
                .withEnv("DB_ADDR", "mysql")
                .withEnv("DB_PORT", "3306")
                .withEnv("DB_DATABASE", "gennydb")
                .withEnv("DB_USER", "genny")
                .withNetwork(SharedNetwork.network)
                .withEnv("DB_PASSWORD", "password")
//                .withEnv("JAVA_OPTS_APPEND", "-Djava.awt.headless=true")
//                .withEnv("PREPEND_JAVA_OPTS", "-Dkeycloak.profile=preview -Dkeycloak.profile.feature.token_exchange=enabled -Dkeycloak.profile.feature.account_api=enabled")
                .withClasspathResourceMapping("quarkus-realm.json", "/config/realm.json", BindMode.READ_ONLY)
                .waitingFor(Wait.forLogMessage(".*Admin console listening.*\\n", 1))
                .withStartupTimeout(Duration.ofMinutes(2));
        keycloak.start();
        
        keycloakUrl = "http://"+keycloak.getContainerIpAddress()+":"+keycloak.getMappedPort(8080)+"/auth/realms/quarkus";
        //try {
          //Thread.sleep(30000);
        //} catch (InterruptedException e) {
          //// TODO Auto-generated catch block
          //e.printStackTrace();
        //}

        
        returnCollections.putAll(Collections.singletonMap("%test.quarkus.oidc.auth-server-url", keycloakUrl));
        returnCollections.putAll(Collections.singletonMap("%test.quarkus.oidc.client-id", "backend-service"));
        returnCollections.putAll(Collections.singletonMap("%test.quarkus.oidc.credentials.secret", "secret"));
        returnCollections.putAll(Collections.singletonMap("%test.keycloak.admin.password","admin"));
        returnCollections.putAll(Collections.singletonMap("%test.keycloak.admin.realm","quarkus"));
        returnCollections.putAll(Collections.singletonMap("quarkus.oidc.auth-server-url", keycloakUrl));
        returnCollections.putAll(Collections.singletonMap("quarkus.oidc.client-id", "backend-service"));
        returnCollections.putAll(Collections.singletonMap("quarkus.oidc.credentials.secret", "secret"));
        returnCollections.putAll(Collections.singletonMap("keycloak.admin.password","admin"));
        returnCollections.putAll(Collections.singletonMap("keycloak.admin.realm","quarkus"));

        //return Collections.emptyMap();
        return returnCollections;
    }

    @Override
    public void stop() {
        keycloak.stop();
    }
}
