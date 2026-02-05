package com.mobileAutomation.steps;

import com.mobileAutomation.flows.LoginResult;
import com.mobileAutomation.pages.HomePage;
import com.mobileAutomation.pages.InvalidLoginDialog;
import com.mobileAutomation.pages.LoginPage;
import com.mobileAutomation.pages.SecretPage;
import io.cucumber.java.en.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

public class LoginSteps {

    private LoginResult loginResult;
    private SecretPage secretPage;
    private final HomePage homePage = new HomePage();
    private final LoginPage loginPage = new LoginPage();
    private static final Logger logger = LoggerFactory.getLogger(LoginSteps.class);

    @Given("the application is launched")
    public void appLaunched() {
        // Driver & app are started by Hooks
        // Intentionally empty
    }

    @Given("the user navigates to the login screen")
    public void loginScreenNavigation() {
        homePage.goToLogin();
    }

    @Given("the user is on the login screen")
    public void verifyLoginScreen() {
        Assert.assertTrue(loginPage.isVisible(), "Login screen not visible");
    }

    @When("the user logs in with username {string} and password {string}")
    public void login(String user, String pw) {
        // loginResult = new LoginPage().submitLogin(user, pw);
        loginResult = loginPage.submitLogin(user, pw);
    }

    @Then("the secret area should be displayed")
    public void secretArea() {
        Assert.assertTrue(
                loginResult instanceof LoginResult.Success,
                "Expected successful login, but result was " + loginResult.getClass().getSimpleName()
        );
        secretPage = ((LoginResult.Success) loginResult).page();
        //Assert.assertTrue(secretPage.isDisplayed());
        Assert.assertTrue(secretPage.isVisible());
    }

    @And("the logged in user should be {string}")
    public void verifyUser(String username) {
        Assert.assertTrue(secretPage.loggedInUserText().contains(username), "Expected username to be displayed");
        secretPage.logOut();
    }

    @Then("an invalid login alert should be shown")
    public void invalidLogin() {
        Assert.assertTrue(
                loginResult instanceof LoginResult.Invalid,
                "Expected invalid login result, but got " + loginResult.getClass().getSimpleName()
        );
        InvalidLoginDialog dialog = ((LoginResult.Invalid) loginResult).dialog();

        Assert.assertTrue(dialog.isVisible());
        dialog.accept();
    }
}
