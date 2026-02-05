package com.mobileAutomation.pages;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;

public class SecretPage extends BasePage {

    @SuppressWarnings("unused")
    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Secret Area']")
    @iOSXCUITFindBy(accessibility = "Secret Area")
    private WebElement pageTitle;

    @SuppressWarnings("unused")
    @AndroidFindBy(xpath = "//android.widget.TextView[contains(@text,'logged in as')]")
    @iOSXCUITFindBy(iOSClassChain = "**/XCUIElementTypeStaticText[`label CONTAINS 'logged in as'`]")
    private WebElement loggedUser;

    @SuppressWarnings("unused")
    @AndroidFindBy(xpath = "//android.view.ViewGroup[@resource-id='RNE_BUTTON_WRAPPER']")
    @iOSXCUITFindBy(accessibility = "Logout")
    private WebElement logoutButton;

    public boolean isVisible() {
        return isVisible(pageTitle);
    }

    public String loggedInUserText() {
        if (driver() instanceof io.appium.java_client.ios.IOSDriver) {
            String label = getAttribute(loggedUser, "label");
            return label != null ? label : text(loggedUser);
        }
        return getAttribute(loggedUser, "text");
    }

    public void logOut() {
        click(logoutButton);
    }
}
