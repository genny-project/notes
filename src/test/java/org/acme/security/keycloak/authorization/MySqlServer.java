package org.acme.security.keycloak.authorization;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

//@QuarkusTestResource(GennyServers.class)
public class MySqlServer implements QuarkusTestResourceLifecycleManager {
//public class GennyServers implements BeforeAllCallback, AfterAllCallback {

	private static final Logger log = LoggerFactory.getLogger(MySqlServer.class);
    public static String MYSQL_PORT = "3336";//new PropertiesReader("genny.properties").getProperty("mysql.test.port","3336");
    
    
    public static GenericContainer   mysql = new FixedHostPortGenericContainer("gennyproject/mysql:8x")
            .withFixedExposedPort(Integer.parseInt(MYSQL_PORT), 3306)
            //.withExposedPorts(3306)
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
            .withStartupTimeout(Duration.ofMinutes(3));
    
 

    
    @Override
    public Map<String, String> start() {

    	Map<String, String> returnCollections = new HashMap<String,String>();
    	
    	Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(log);
    	
    	System.out.println("XXXXXMySQl Port     = "+MYSQL_PORT);
    	
 
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
    	  

 
        return returnCollections;
    }

    @Override
    public void stop() {
//      try {
//      Thread.sleep(10000);
//      } catch (Exception e) {}

//        keycloak.stop();
        mysql.stop();
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
