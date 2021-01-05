package org.acme.security.keycloak.authorization;

import io.quarkus.test.common.QuarkusTestResource;

import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.AccessTokenResponse;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
@QuarkusTestResource(KeycloakServer.class)
@QuarkusTestResource(MySqlServer.class)
public class PolicyEnforcerTest {

    @ConfigProperty(name = "quarkus.oidc.auth-server-url")
    Optional<String> keycloakUrl;

    static {
        RestAssured.useRelaxedHTTPSValidation();
    }

    
    @Test
    public void testAccessToken()
    {
    	System.out.println("Starting test");
    	String accessToken = getAccessToken("alice");
    	System.out.println("AccessToken Test="+accessToken);
    }
    
  //  @Test
    public void testAccessUserResource() {
        RestAssured.given().auth().oauth2(getAccessToken("alice"))
                .when().get("/api/users/me")
                .then()
                .statusCode(200);
        RestAssured.given().auth().oauth2(getAccessToken("jdoe"))
                .when().get("/api/users/me")
                .then()
                .statusCode(200);
    }

   // @Test
    public void testAccessAdminResource() {
        RestAssured.given().auth().oauth2(getAccessToken("alice"))
                .when().get("/api/admin")
                .then()
                .statusCode(403);
        RestAssured.given().auth().oauth2(getAccessToken("jdoe"))
                .when().get("/api/admin")
                .then()
                .statusCode(403);
        RestAssured.given().auth().oauth2(getAccessToken("admin"))
                .when().get("/api/admin")
                .then()
                .statusCode(200);
    }

   // @Test
    public void testPublicResource() {
        RestAssured.given()
                .when().get("/api/public")
                .then()
                .statusCode(204);
    }

    private String getAccessToken(String userName) {
        return RestAssured
                .given()
                .param("grant_type", "password")
                .param("username", userName)
                .param("password", userName)
                .param("client_id", "backend-service")
                .param("client_secret", "secret")
                .when()
                .post(keycloakUrl.get()+ "/protocol/openid-connect/token")
                .as(AccessTokenResponse.class).getToken();
    }
}
