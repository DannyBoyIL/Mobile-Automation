package com.mobileAutomation.pages;

import io.appium.java_client.AppiumBy;

public class LoginPage extends BasePage {

    private static final String LOGIN_SCREEN_BUTTON = "Login Screen";
    private static final String USERNAME_FIELD = "username";
    private static final String PASSWORD_FIELD = "password";
    private static final String LOGIN_BUTTON = "loginBtn";
    private static final String SUCCESS_MESSAGE = "successMessage";
    private static final String ERROR_MESSAGE = "errorMessage";

    public void openLoginScreen() {
        click(LOGIN_SCREEN_BUTTON);
    }

    public void enterUsername(String username) {
        if (username != null && !username.isEmpty()) {
            type(USERNAME_FIELD, username);
        }
    }

    public void enterPassword(String password) {
        if (password != null && !password.isEmpty()) {
            type(PASSWORD_FIELD, password);
        }
    }

    public void submitLogin() {
        click(LOGIN_BUTTON);
    }

    public boolean isLoginSuccessful() {
        return isDisplayed(SUCCESS_MESSAGE);
    }

    public boolean isLoginErrorDisplayed() {
        return isDisplayed(ERROR_MESSAGE);
    }
}
