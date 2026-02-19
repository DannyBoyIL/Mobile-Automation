package com.mobileAutomation.pages;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(LoginPage.class);

    public void login(String username, String password) {

        logger.info("Entering credentials");

        clear(usernameField);
        clear(passwordField);

        if (username != null && !username.isEmpty()) {
            type(usernameField, username);
        }

        if (password != null && !password.isEmpty()) {
            type(passwordField, password);
        }

        logger.info("Tapping Login button");
        click(loginButton);
    }

    public boolean isVisible() {
        return isVisible(loginButton);
    }
}
