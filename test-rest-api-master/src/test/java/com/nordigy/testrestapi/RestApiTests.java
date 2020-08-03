package com.nordigy.testrestapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;

import java.net.MalformedURLException;
import java.net.URL;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// It allows to refresh context(Database) before an each method. So your tests always will be executed on the same snapshot of DB.
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
class RestApiTests {

    @LocalServerPort
    private int port;

    @PostConstruct
    public void init() {
        RestAssured.port = port;
    }

    @Test
    public void shouldReturnCorrectUsersListSize() {
        given().log().all()
               .when().get("/api/users")
               .then().log().ifValidationFails()
               .statusCode(200)
               .body("page.totalElements", is(20));
    }

    @Test
    public void shouldCreateNewUser() {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("firstName", "Ivan");
        objectNode.put("lastName", "Ivanov");
        objectNode.put("dayOfBirth", "2000-01-01");
        objectNode.put("email", "asdas@asdas.tr");

        ObjectNode user = given().log().all()
                                 .body(objectNode)
                                 .contentType(ContentType.JSON)
                                 .when().post("/api/users")
                                 .then().log().ifValidationFails()
                                 .statusCode(201)
                                 .extract().body().as(ObjectNode.class);
        
        assertThat(user.get("id").asLong()).isGreaterThan(20);
    }

    // TODO: The test methods above are examples of test cases.
    //  Please add new cases below, but don't hesitate to refactor the whole class.
    @Test
    public void shouldReturnCorrectUserById() {
        given().log().all()
                .when().get("/api/users/1")
                .then().log().ifValidationFails()
                .statusCode(200)
                .body("id", is(1));
    }
    @Test
    public void shouldDeleteUser(){
        given().log().all().
                when().delete("/api/users/2")
                .then().log().ifValidationFails()
                .statusCode(204);
    }
        @Test
    public void shouldUpdateUsername(){
            ObjectNode user = given().log().all()
                    .when().get("/api/users/5")
                    .then().extract().body().as(ObjectNode.class);
            ObjectNode user2 = user.put("firstName","Sam");
            ObjectNode updatedUser = given().log().all()
                    .body(user2)
                    .contentType(ContentType.JSON)
                    .when().patch("/api/users/5")
                    .then().log().ifValidationFails()
                    .statusCode(200)
                    .extract().body().as(ObjectNode.class);
            assertThat(user.get("firstName")).isEqualTo(updatedUser.get("firstName"));
    }
    @Test
    public void firstnameAndLastnameCouldNotBeUpdatedToEmptyFields(){
        ObjectNode user = given().log().all()
                .when().get("/api/users/6")
                .then().extract().body().as(ObjectNode.class);
        ObjectNode user2 = user.put("firstName","");
        user2.put("lastName","");
        ObjectNode updatedUser = given().log().all()
                .body(user2)
                .contentType(ContentType.JSON)
                .when().put("/api/users/6")
                .then().log().ifValidationFails()
                .statusCode(400)
                .extract().body().as(ObjectNode.class);
    }
    @Test
    public void userWithInvalidEmailCouldNotBeCreated(){
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("firstName", "Ivan");
        objectNode.put("lastName", "Ivanov");
        objectNode.put("dayOfBirth", "2000-01-01");
        objectNode.put("email", "invalidEmail.without@");
        ObjectNode user = given().log().all()
                .body(objectNode)
                .contentType(ContentType.JSON)
                .when().post("/api/users")
                .then().log().ifValidationFails()
                .statusCode(400)
                .extract().body().as(ObjectNode.class);
    }
}