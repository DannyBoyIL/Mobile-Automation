package com.mobileAutomation.steps;

import com.mobileAutomation.pages.LoginPage;
import io.cucumber.java.en.*;
import org.testng.Assert;

public class LoginSteps {

    private final LoginPage loginPage = new LoginPage();

    @Given("the application is launched")
    public void appIsLaunched() {
        // Driver is initialized in Hooks
    }

    @And("the user navigates to the Login screen")
    public void navigateToLoginScreen() {
        loginPage.openLoginScreen();
    }

    @When("the user enters username {string} and password {string}")
    public void enterCredentials(String username, String password) {
        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
    }

    @And("submits the login form")
    public void submitLogin() {
        loginPage.submitLogin();
    }

    @Then("the login result should be {string}")
    public void verifyLoginResult(String result) {
        if ("success".equalsIgnoreCase(result)) {
            Assert.assertTrue(
                    loginPage.isLoginSuccessful(),
                    "Expected login to succeed but it did not"
            );
        } else {
            Assert.assertTrue(
                    loginPage.isLoginErrorDisplayed(),
                    "Expected login error but none was shown"
            );
        }
    }
}
