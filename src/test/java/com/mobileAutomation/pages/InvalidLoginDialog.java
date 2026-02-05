package com.mobileAutomation.pages;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class InvalidLoginDialog extends BasePage {

    @SuppressWarnings("unused")
    @AndroidFindBy(id = "android:id/button1")
    @iOSXCUITFindBy(accessibility = "OK")
    private WebElement okButton;

    private static final By errorMessage = byText("Invalid login credentials");

    public boolean isVisible() {
        return isVisible(errorMessage);
    }

    public LoginPage accept() {
        click(okButton);
        return new LoginPage();
    }
}