package com.mobileAutomation.pages;

import com.mobileAutomation.flows.LoginResult;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;
import org.testng.Assert;

public class LoginPage extends BasePage {

    @SuppressWarnings("unused")
    @AndroidFindBy(xpath = "//android.widget.EditText[@resource-id='username']")
    @iOSXCUITFindBy(accessibility = "username")
    private WebElement usernameField;

    @SuppressWarnings("unused")
    @AndroidFindBy(xpath = "//android.widget.EditText[@resource-id='password']")
    @iOSXCUITFindBy(accessibility = "password")
    private WebElement passwordField;

    @SuppressWarnings("unused")
    @AndroidFindBy(xpath = "//android.widget.Button[@resource-id='loginBtn']")
    @iOSXCUITFindBy(accessibility = "loginBtn")
    private WebElement loginButton;

    protected void enterUserName(String username) {
        if (username != null && !username.isEmpty()) {
            clear(usernameField);
            type(usernameField, username);
        }
    }

    protected void enterPassword(String password) {
        if (password != null && !password.isEmpty()) {
            clear(passwordField);
            type(passwordField, password);
        }
    }

    public boolean isVisible() {
        return isVisible(loginButton);
    }

    public LoginResult submitLogin(String user, String pw) {
        enterUserName(user);
        enterPassword(pw);
        click(loginButton);

        InvalidLoginDialog dialog = new InvalidLoginDialog();
        SecretPage secretPage = new SecretPage();

        if (dialog.isVisible()) {
            return new LoginResult.Invalid();
        }

        Assert.assertTrue(secretPage.isVisible(), "Neither error nor success screen appeared");
        return new LoginResult.Success(secretPage);
    }
}
