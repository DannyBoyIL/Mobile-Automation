package com.mobileAutomation.steps;

import com.mobileAutomation.flows.LoginFlow;
import com.mobileAutomation.flows.LoginResult;
import com.mobileAutomation.pages.InvalidLoginDialog;
import com.mobileAutomation.pages.SecretPage;
import io.cucumber.java.en.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import static com.mobileAutomation.assertions.ResultAssertions.assertInstanceOf;

public class LoginSteps {

    private final LoginFlow loginFlow = new LoginFlow();
    private LoginResult loginResult;
    private SecretPage secretPage;
    private static final Logger logger = LoggerFactory.getLogger(LoginSteps.class);

    @When("the user logs in with username {string} and password {string}")
    public void login(String user, String pw) {
        logger.info("STEP: User logs in as '{}'", user);
        loginResult = loginFlow.login(user, pw);
    }

    @Then("the secret area should be displayed")
    public void secretArea() {
        logger.info("STEP: Verifying secret area is displayed");
        LoginResult.Success success = assertInstanceOf(LoginResult.Success.class, loginResult);
        secretPage = success.page();
        Assert.assertTrue(secretPage.isVisible());
    }

    @And("the logged in user should be {string}")
    public void verifyUser(String username) {
        Assert.assertTrue(secretPage.loggedInUserText().contains(username), "Expected username to be displayed");
        secretPage.logOut();
    }

    @Then("an invalid login alert should be shown")
    public void invalidLogin() {
        logger.info("STEP: Expect invalid login alert");
        LoginResult.Invalid invalid = assertInstanceOf(LoginResult.Invalid.class, loginResult);
        InvalidLoginDialog dialog = invalid.dialog();
        Assert.assertTrue(dialog.isVisible());
        dialog.accept();
    }
}
