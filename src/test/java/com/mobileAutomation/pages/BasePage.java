package com.mobileAutomation.pages;

import com.mobileAutomation.driver.DriverManager;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.AppiumBy;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class BasePage {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    protected AppiumDriver getDriver() {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver == null) {
            throw new IllegalStateException(
                    "Driver is null. Did you forget to initialize it in Cucumber Hooks?"
            );
        }
        return driver;
    }

    protected WebDriverWait getWait() {
        return new WebDriverWait(getDriver(), DEFAULT_TIMEOUT);
    }

    protected WebElement findByAccessibilityId(String id) {
        return getWait().until(
                ExpectedConditions.visibilityOfElementLocated(
                        AppiumBy.accessibilityId(id)
                )
        );
    }

    protected void click(String accessibilityId) {
        findByAccessibilityId(accessibilityId).click();
    }

    protected void type(String accessibilityId, String text) {
        WebElement element = findByAccessibilityId(accessibilityId);
        element.clear();
        element.sendKeys(text);
    }

    protected boolean isDisplayed(String accessibilityId) {
        return findByAccessibilityId(accessibilityId).isDisplayed();
    }
}
