package org.acme.security.keycloak.authorization;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import life.genny.notes.models.Note;
import life.genny.notes.utils.PropertiesReader;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;




import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

//@QuarkusTestResource(GennyServers.class)
public class KeycloakServer implements QuarkusTestResourceLifecycleManager {
//public class GennyServers implements BeforeAllCallback, AfterAllCallback {

	private static final Logger log = LoggerFactory.getLogger(KeycloakServer.class);
	static public String keycloakUrl;

	  private static final String KEYCLOAK_SERVER_URL = System.getProperty("keycloak.ssl.url", "https://localhost:8543/auth");

    public static  String KEYCLOAK_SERVER_PORT = new PropertiesReader("genny.properties").getProperty("keycloak.test.port","8580");
    public static String KEYCLOAK_VERSION = new PropertiesReader("genny.properties").getProperty("keycloak.version","11.0.3");
    public static String MYSQL_PORT = new PropertiesReader("genny.properties").getProperty("mysql.test.port","3336");
    
    
 
    
    private static GenericContainer     keycloak = new FixedHostPortGenericContainer("quay.io/keycloak/keycloak:" + KEYCLOAK_VERSION)
            .withFixedExposedPort(Integer.parseInt(KEYCLOAK_SERVER_PORT), 8080)
            .withFixedExposedPort(Integer.parseInt(KEYCLOAK_SERVER_PORT)-37, 8443)
            .dependsOn(MySqlServer.mysql)
            .withExposedPorts(8080)
           // .withImagePullPolicy(PullPolicy.alwaysPull())
           .withEnv("KEYCLOAK_USER", "admin")
            .withEnv("KEYCLOAK_PASSWORD", "admin")
            .withEnv("KEYCLOAK_LOGLEVEL", "debug")
            .withEnv("KEYCLOAK_IMPORT", "/config/realm.json")
             
//            .withEnv("DB_VENDOR", "H2")
            .withEnv("DB_VENDOR", "mysql")
            .withEnv("DB_ADDR", "127.0.0.1")
            .withEnv("DB_PORT", MYSQL_PORT)
            .withEnv("DB_DATABASE", "gennydb")
            .withEnv("DB_USER", "genny")
            .withEnv("DB_PASSWORD", "password")
            .withEnv("JAVA_OPTS_APPEND", "-Djava.awt.headless=true")
            .withEnv("PREPEND_JAVA_OPTS", "-Dkeycloak.profile=preview -Dkeycloak.profile.feature.token_exchange=enabled -Dkeycloak.profile.feature.account_api=enabled")
            .withClasspathResourceMapping("quarkus-realm.json", "/config/realm.json", BindMode.READ_ONLY)
            .waitingFor(Wait.forHttp("/auth"))
          //  .waitingFor(Wait.forLogMessage(".*Admin console .*", 1))
          //  .withLogConsumer(logConsumer)
            .withStartupTimeout(Duration.ofMinutes(3));
    
//    private static final MySQLContainer mysql = new MySQLContainer<>("mysql:8.0.22")
//            .withDatabaseName("gennydb")
//            .withUsername("genny")
//            .withPassword("password")
//            .withExposedPorts(3306);

    
    @Override
    public Map<String, String> start() {
//    @Override
//    public void beforeAll(ExtensionContext extensionContext) {

    	Map<String, String> returnCollections = new HashMap<String,String>();
    	
    	Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(log);
    	
    	System.out.println("XXXXXKeycloak Server port = "+Integer.parseInt(KEYCLOAK_SERVER_PORT));
    	System.out.println("XXXXXKeycloak Version     = "+KEYCLOAK_VERSION);
    	System.out.println("XXXXXMySQl Port     = "+MYSQL_PORT);
    	
      

        
        System.out.println("Keycloak Starting");
        keycloak.start();
        System.out.println("Keycloak Started");
      // logs = keycloak.getLogs();
        
        
      //  System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&\n"+logs);
        
        
        //keycloakUrl = "http://"+keycloak.getContainerIpAddress()+":8580"/*+keycloak.getMappedPort(8080)*/+"/auth";
        keycloakUrl = "http://"+keycloak.getContainerIpAddress()+":8580"/*+keycloak.getMappedPort(8080)*/+"/auth/realms/quarkus";
        System.out.println("keycloakURL = "+keycloakUrl);
        
     //   returnCollections.put("quarkus.oidc.auth-server-url", keycloakUrl);
        returnCollections.putAll(Collections.singletonMap("quarkus.oidc.auth-server-url", keycloakUrl));
        returnCollections.putAll(Collections.singletonMap("%test.quarkus.oidc.auth-server-url", keycloakUrl));
        returnCollections.putAll(Collections.singletonMap("%test.quarkus.oidc.client-id", "backend-service"));
        returnCollections.putAll(Collections.singletonMap("quarkus.oidc.client-id", "backend-service"));
        returnCollections.putAll(Collections.singletonMap("quarkus.oidc.credentials.secret", "secret"));
        returnCollections.putAll(Collections.singletonMap("%test.quarkus.oidc.credentials.secret", "secret"));
        
         
//        try {
//        Thread.sleep(10000);
//        } catch (Exception e) {}
       // return Collections.emptyMap();
       // return Collections.singletonMap("quarkus.datasource.jdbc.url", mysql.getJdbcUrl());
        return returnCollections;
    }

    @Override
    public void stop() {
      try {
      Thread.sleep(10000);
      } catch (Exception e) {}

        keycloak.stop();
//        mysql.stop();
//        System.out.println("All stopped (keycloak and mysql) ");
    }
    
//    @Override
//    public void afterAll(ExtensionContext extensionContext) {
// //       keycloak.stop();
// //       mysql.stop();
// //       System.out.println("All stopped (keycloak and mysql) ");
//
//    }


}
