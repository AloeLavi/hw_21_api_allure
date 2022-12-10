package tests;

import api.AuthorizationApi;
import com.codeborne.selenide.Configuration;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import models.lombok.CreateStepBody;
import models.lombok.CreateTestCaseBody;
import models.lombok.Step;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Cookie;

import java.util.List;

import static api.AuthorizationApi.ALLURE_TESTOPS_SESSION;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selectors.byName;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.is;


public class AllureTestopsTests {
    public final static String
            USERNAME = "allure8",
            PASSWORD = "allure8",
            USER_TOKEN = "efd32a69-217f-41fa-9701-55f54dd55cd4"; // create it in allure_url//user/30  6819c304-7f5d-4419-9574-8c16dcce0358


    @BeforeAll
    static void beforeAll() {
        Configuration.baseUrl = "https://allure.autotests.cloud";
        RestAssured.baseURI = "https://allure.autotests.cloud";
       // RestAssured.filters(withCustomTemplates());
    }

    @Test
    void createTestSteps() {

        AuthorizationApi authorizationApi = new AuthorizationApi();
        String xsrfToken = authorizationApi.getXsrfToken(USER_TOKEN);
        String authorizationCookie = authorizationApi
                .getAuthorizationCookie(USER_TOKEN, xsrfToken, USERNAME, PASSWORD);

        Faker faker = new Faker();
        String testCaseName = faker.name().nameWithMiddle();

        CreateTestCaseBody testCaseBody = new CreateTestCaseBody();
        testCaseBody.setName(testCaseName);
//        String testCaseBody = "{\"name\":\"Some random test\"}";

        Step firstStep = new Step();
        firstStep.setName("Шажочек1");
        Step secondStep = new Step();
        secondStep.setName("Шажочек2");

        CreateStepBody StepBody = new CreateStepBody();
        StepBody.setSteps(List.of(firstStep, secondStep));

        int testCaseId = given()
           //     .log().all()
                .header("X-XSRF-TOKEN", xsrfToken)
                .cookies("XSRF-TOKEN", xsrfToken,
                        ALLURE_TESTOPS_SESSION, authorizationCookie)
                .body(testCaseBody)
                .contentType(JSON)
                .queryParam("projectId", "1722")
                .post("/api/rs/testcasetree/leaf")
                .then()
            //    .log().body()
                .statusCode(200)
                .body("name", is(testCaseName))
                .body("automated", is(false))
                .body("external", is(false))
                .extract()
                .path("id");

        given()
                .log().all()
                .header("X-XSRF-TOKEN", xsrfToken)
                .cookies("XSRF-TOKEN", xsrfToken,
                        ALLURE_TESTOPS_SESSION, authorizationCookie)
                .body(StepBody)
                .contentType(JSON)
                .queryParam("projectId", "1722")
                .post("/api/rs/testcase/" + testCaseId + "/scenario")
                .then()
                .log().all()
                .statusCode(200)
               ;

        open("/favicon.ico");
        getWebDriver().manage().addCookie(new Cookie(ALLURE_TESTOPS_SESSION, authorizationCookie));
       open("/project/1722/test-cases/" + testCaseId);
       $(".TestCaseLayout__name").shouldHave(text(testCaseName));
        $$(".TreeElement__node").get(0).shouldHave(text("Шажочек1"));
        $$(".TreeElement__node").get(1).shouldHave(text("Шажочек2"));

    }

}