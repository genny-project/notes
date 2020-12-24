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
public class GennyServers implements QuarkusTestResourceLifecycleManager {
//public class GennyServers implements BeforeAllCallback, AfterAllCallback {

	private static final Logger log = LoggerFactory.getLogger(GennyServers.class);
	static public String keycloakUrl;

//	private GenericContainer mysql;
//    private GenericContainer keycloak;
	  private static final String KEYCLOAK_SERVER_URL = System.getProperty("keycloak.ssl.url", "https://localhost:8543/auth");

    public static  String KEYCLOAK_SERVER_PORT = new PropertiesReader("genny.properties").getProperty("keycloak.test.port","8580");
    public static String KEYCLOAK_VERSION = new PropertiesReader("genny.properties").getProperty("keycloak.version","11.0.3");
    public static String MYSQL_PORT = new PropertiesReader("genny.properties").getProperty("mysql.test.port","3336");
    
    
    private static   Network network = Network.newNetwork();
    
    private static GenericContainer   mysql = new FixedHostPortGenericContainer("gennyproject/mysql:8x")
            .withFixedExposedPort(Integer.parseInt(MYSQL_PORT), 3306)
          //  .withExposedPorts(3306)
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
     //      .withLogConsumer(logConsumer)
            .withStartupTimeout(Duration.ofMinutes(30));
    
    
    private static GenericContainer     keycloak = new FixedHostPortGenericContainer("quay.io/keycloak/keycloak:" + KEYCLOAK_VERSION)
            .withFixedExposedPort(Integer.parseInt(KEYCLOAK_SERVER_PORT), 8080)
 //           .withFixedExposedPort(Integer.parseInt(KEYCLOAK_SERVER_PORT)-37, 8443)
            .dependsOn(mysql)
            .withExposedPorts(8080)
            .withNetwork(network)
           // .withImagePullPolicy(PullPolicy.alwaysPull())
           .withEnv("KEYCLOAK_USER", "admin")
            .withEnv("KEYCLOAK_PASSWORD", "admin")
            .withEnv("KEYCLOAK_LOGLEVEL", "debug")
            .withEnv("KEYCLOAK_IMPORT", "/config/realm.json")
             
//            .withEnv("DB_VENDOR", "H2")
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
          //  .waitingFor(Wait.forLogMessage(".*Admin console .*", 1))
          //  .withLogConsumer(logConsumer)
            .withStartupTimeout(Duration.ofMinutes(80));
    
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
    	
      

        
//        MySQLContainer mysql = new MySQLContainer<>("mysql:8")
//                .withDatabaseName("gennydb")
//                .withUsername("genny")
//                .withPassword("password")
//                .withExposedPorts(3306)
//                .withNetwork(network)
//                .withNetworkAliases("mysql")
//                .waitingFor(Wait.forLogMessage(".*ready for connection.*\\n", 1))
//                .withLogConsumer(logConsumer);
//
        
        
//    	  mysql = new FixedHostPortGenericContainer("gennyproject/mysql:8x")
//                  .withFixedExposedPort(Integer.parseInt(MYSQL_PORT), 3306)
//                //  .withExposedPorts(3306)
//                  .withNetwork(network)
//                  .withNetworkAliases("mysql")
//                  .withEnv("MYSQL_USERNAME","genny")
//                  .withEnv("MYSQL_URL","mysql")
//                  .withEnv("MYSQL_DB","gennydb")
//                  .withEnv("MYSQL_PORT","3306")
//                  .withEnv("MYSQL_ALLOW_EMPTY","")
//                  .withEnv("MYSQL_RANDOM_ROOT_PASSWORD","no")
//                  .withEnv("MYSQL_DATABASE","gennydb")
//                  .withEnv("MYSQL_USER","genny")
//                  .withEnv("MYSQL_PASSWORD","password")
//                  .withEnv("MYSQL_ROOT_PASSWORD","password")
//                  .withEnv("ADMIN_USERNAME","admin")
//                  .withEnv("ADMIN_PASSWORD","password")
//                  .withEnv("MYSQL_ROOT_PASSWORD","password")
//                 .waitingFor(Wait.forLogMessage(".*ready for connection.*\\n", 1))
//                 .withLogConsumer(logConsumer)
//                  .withStartupTimeout(Duration.ofMinutes(30));
    	
    	  System.out.println("MySQL Starting");
    	  mysql.start();
    	//  String logs = mysql.getLogs();
        //  System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&\n"+logs);
          String mysqljdbc = "jdbc:mysql://localhost:"+mysql.getMappedPort(3306)+"/gennydb?zeroDateTimeBehavior=convertToNull";
          System.out.println("mysql jdbc = "+mysqljdbc);
          returnCollections.putAll(Collections.singletonMap("%test.quarkus.datasource.jdbc.url", mysqljdbc));
          returnCollections.putAll(Collections.singletonMap("quarkus.datasource.jdbc.url", mysqljdbc));
  //  	  returnCollections.put("quarkus.datasource.jdbc.url", mysql.getJdbcUrl());
    	  
    	  String mysqlAddress = mysql.getIpAddress();
    	  Integer mysqlPort = mysql.getMappedPort(3306);
    	  System.out.println("MySQL Started and is at  "+mysqlAddress+":"+mysqlPort);
    	  

    	  
//        keycloak = new FixedHostPortGenericContainer("quay.io/keycloak/keycloak:" + KEYCLOAK_VERSION)
//                .withFixedExposedPort(Integer.parseInt(KEYCLOAK_SERVER_PORT), 8080)
//     //           .withFixedExposedPort(Integer.parseInt(KEYCLOAK_SERVER_PORT)-37, 8443)
//                .dependsOn(mysql)
//                .withExposedPorts(8080)
//                .withNetwork(network)
//               // .withImagePullPolicy(PullPolicy.alwaysPull())
//               .withEnv("KEYCLOAK_USER", "admin")
//                .withEnv("KEYCLOAK_PASSWORD", "admin")
//                .withEnv("KEYCLOAK_LOGLEVEL", "debug")
//                .withEnv("KEYCLOAK_IMPORT", "/config/realm.json")
//                 
////                .withEnv("DB_VENDOR", "H2")
//                .withEnv("DB_VENDOR", "mysql")
//                .withEnv("DB_ADDR", "mysql")
//                .withEnv("DB_PORT", "3306")
//                .withEnv("DB_DATABASE", "gennydb")
//                .withEnv("DB_USER", "genny")
//                .withEnv("DB_PASSWORD", "password")
//                .withEnv("JAVA_OPTS_APPEND", "-Djava.awt.headless=true")
//                .withEnv("PREPEND_JAVA_OPTS", "-Dkeycloak.profile=preview -Dkeycloak.profile.feature.token_exchange=enabled -Dkeycloak.profile.feature.account_api=enabled")
//                .withClasspathResourceMapping("quarkus-realm.json", "/config/realm.json", BindMode.READ_ONLY)
//                .waitingFor(Wait.forHttp("/auth"))
//              //  .waitingFor(Wait.forLogMessage(".*Admin console .*", 1))
//              //  .withLogConsumer(logConsumer)
//                .withStartupTimeout(Duration.ofMinutes(80));
        
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

//        keycloak.stop();
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
