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

    public void accept() {
        if (!isVisible()) {
            logger.info("Invalid login alert not visible");
            return;
        }
        try {
            click(okButton);
        } catch (Exception e) {
            logger.warn("Failed to click invalid login OK button (likely auto-dismissed)", e);
        }
    }
}
