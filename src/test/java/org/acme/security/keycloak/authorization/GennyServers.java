package org.acme.security.keycloak.authorization;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import life.genny.notes.models.Note;
import life.genny.notes.utils.PropertiesReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.PullPolicy;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;

//@QuarkusTestResource(MySQLServer.class)
public class GennyServers implements QuarkusTestResourceLifecycleManager {
	private static final Logger log = LoggerFactory.getLogger(GennyServers.class);
	static public String keycloakUrl;

	private GenericContainer mysql;
    private GenericContainer keycloak;
    
    public static  String KEYCLOAK_SERVER_PORT = new PropertiesReader("genny.properties").getProperty("keycloak.test.port","8580");
    public static String KEYCLOAK_VERSION = new PropertiesReader("genny.properties").getProperty("keycloak.version","12.0.1");
    public static String MYSQL_PORT = new PropertiesReader("genny.properties").getProperty("mysql.test.port","3336");
    
    @Override
    public Map<String, String> start() {
    	
    	Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(log);
    	
    	System.out.println("XXXXXKeycloak Server port = "+Integer.parseInt(KEYCLOAK_SERVER_PORT));
    	System.out.println("XXXXXKeycloak Version     = "+KEYCLOAK_VERSION);
    	System.out.println("XXXXXMySQl Port     = "+MYSQL_PORT);
    	
        Network network = Network.newNetwork();

    	  mysql = new FixedHostPortGenericContainer("gennyproject/mysql:8x")
                  .withFixedExposedPort(Integer.parseInt(MYSQL_PORT), 3333)
                  .withExposedPorts(3306)
                  .withNetwork(network)
                  .withNetworkAliases("mysql")
                  .withEnv("MYSQL_USERNAME","genny")
                  .withEnv("MYSQL_URL","mysql")
                  .withEnv("MYSQL_DB","gennydb")
                  .withEnv("MYSQL_PORT","3306")
                  .withEnv("MYSQL_ALLOW_EMPTY","")
                  .withEnv("MYSQL_RANDOM_ROOT_PASSWORD","no")
                  .withEnv("MYSQL_DATABASE","gennydb")
                  .withEnv("MYSQL_USER","genny")
                  .withEnv("MYSQL_PASSWORD","password")
                  .withEnv("MYSQL_ROOT_PASSWORD","password")
                  .withEnv("ADMIN_USERNAME","admin")
                  .withEnv("ADMIN_PASSWORD","password")
                  .withEnv("MYSQL_ROOT_PASSWORD","password")
                 .waitingFor(Wait.forLogMessage(".*ready for connection.*\\n", 1))
                 .withLogConsumer(logConsumer)
                  .withStartupTimeout(Duration.ofMinutes(3));
    	
    	  System.out.println("MySQL Starting");
    	  mysql.start();
    	  
    	  String mysqlAddress = mysql.getIpAddress();
    	  Integer mysqlPort = mysql.getMappedPort(3306);
    	  System.out.println("MySQL Started and is at  "+mysqlAddress+":"+mysqlPort);
    	  

    	  
        keycloak = new FixedHostPortGenericContainer("quay.io/keycloak/keycloak:" + KEYCLOAK_VERSION)
                .withFixedExposedPort(Integer.parseInt(KEYCLOAK_SERVER_PORT), 8080)
     //           .withFixedExposedPort(Integer.parseInt(KEYCLOAK_SERVER_PORT)-37, 8443)
                .dependsOn(mysql)
                .withExposedPorts(8080)
                .withNetwork(network)
                .withImagePullPolicy(PullPolicy.alwaysPull())
               .withEnv("KEYCLOAK_USER", "admin")
                .withEnv("KEYCLOAK_PASSWORD", "admin")
                .withEnv("KEYCLOAK_LOGLEVEL", "debug")
                .withEnv("KEYCLOAK_IMPORT", "/config/realm.json")
                 
//                .withEnv("DB_VENDOR", "H2")
                .withEnv("DB_VENDOR", "mysql")
                .withEnv("DB_ADDR", "mysql")
                .withEnv("DB_PORT", "3306")
                .withEnv("DB_DATABASE", "gennydb")
                .withEnv("DB_USER", "genny")
                .withEnv("DB_PASSWORD", "password")
                .withEnv("JAVA_OPTS_APPEND", "-Djava.awt.headless=true")
                .withEnv("PREPEND_JAVA_OPTS", "-Dkeycloak.profile=preview -Dkeycloak.profile.feature.token_exchange=enabled -Dkeycloak.profile.feature.account_api=enabled")
                .withClasspathResourceMapping("quarkus-realm.json", "/config/realm.json", BindMode.READ_ONLY)
                .waitingFor(Wait.forHttp("/auth"))
                .withLogConsumer(logConsumer)
                .withStartupTimeout(Duration.ofMinutes(3));
        
        System.out.println("Keycloak Starting");
        keycloak.start();
        System.out.println("Keycloak Started");
        final String logs = keycloak.getLogs();
        
      //  System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&\n"+logs);
        
        keycloakUrl = "http://"+keycloak.getContainerIpAddress()+":8580";//+keycloak.getMappedPort(8080);
        System.out.println("keycloakURL = "+keycloakUrl);
        return Collections.emptyMap();
    }

    @Override
    public void stop() {
        keycloak.stop();
        mysql.stop();
        System.out.println("All stopped");
    }


}
