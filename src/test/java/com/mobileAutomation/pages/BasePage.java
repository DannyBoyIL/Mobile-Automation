package com.mobileAutomation.pages;

import com.mobileAutomation.driver.DriverManager;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public abstract class BasePage {

    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected BasePage() {
        PageFactory.initElements(
                new AppiumFieldDecorator(driver(), TIMEOUT),
                this
        );
    }

    protected AppiumDriver driver() {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver == null) {
            throw new IllegalStateException("Driver not initialized. Check Hooks.");
        }
        return driver;
    }

    protected String describe(WebElement element) {
        try {
            return element.toString();
        } catch (Exception e) {
            return "unknown element";
        }
    }

    protected WebDriverWait waitUntil() {
        return new WebDriverWait(driver(), TIMEOUT);
    }

    public void waitForVisibility(WebElement element) {
        waitUntil().until(ExpectedConditions.visibilityOf(element));
    }

    public void waitForVisibility(By locator) {
        waitUntil().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /* ---------- Locators ---------- */

    protected static By byAccessibilityId(String id) {
        return AppiumBy.accessibilityId(id);
    }

    protected static By byText(String text) {
        return AppiumBy.xpath("//*[contains(@text,'" + text + "') or contains(@label,'" + text + "')]");
    }

    /* ---------- Actions ---------- */

    protected void clear(WebElement element) {
        waitForVisibility(element);
        element.clear();
    }

    protected void click(WebElement element) {
        logger.debug("Clicking element: {}", describe(element));
        waitForVisibility(element);
        element.click();
        logger.debug("Click successful");
    }

    protected void type(WebElement element, String text) {
        // Intentionally NOT logging the value — `type()` is the entry point for
        // password fields too (LoginPage.enterCredentials -> type), and writing
        // raw passwords to logs at any level is bad practice. Length is enough
        // to confirm `sendKeys` actually moved characters; the field identity
        // comes from the element description.
        logger.debug("Typing {} chars into element: {}", text == null ? 0 : text.length(), describe(element));
        clear(element);
        element.sendKeys(text);
    }

    public String getAttribute(WebElement element, String attribute) {
        waitForVisibility(element);
        return element.getAttribute(attribute);
    }

    protected String text(WebElement element) {
        waitForVisibility(element);
        return element.getText();
    }

    protected boolean isVisible(WebElement element) {
        try {
            waitForVisibility(element);
            logger.debug("Element visible: {}", element);
            return true;
        } catch (Exception e) {
            logger.debug("Element NOT visible: {}", element);
            return false;
        }
    }

    protected boolean isVisible(By locator) {
        try {
            waitForVisibility(locator);
            logger.debug("Element visible: {}", locator);
            return true;
        } catch (Exception e) {
            logger.debug("Element NOT visible: {}", locator);
            return false;
        }
    }
}
