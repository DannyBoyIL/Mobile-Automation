package com.mobileAutomation.pages;

import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;

public class HomePage extends BasePage {

    @SuppressWarnings("unused")
    @AndroidFindBy(accessibility = "Login Screen")
    @iOSXCUITFindBy(accessibility = "Login Screen")
    private WebElement loginEntry;


    @SuppressWarnings("unused")
//    @AndroidFindBy(uiAutomator =
//            "new UiScrollable(new UiSelector().scrollable(true).instance(0))"
//            + ".scrollIntoView(new UiSelector().description(\"Webview Demo\"))")
    @AndroidFindBy(accessibility = "Webview Demo")
    @iOSXCUITFindBy(accessibility = "Webview Demo")
    private WebElement webViewEntry;

    public void goToLogin() {
        click(loginEntry);
    }

    public void goToWebViewDemo() {
        click(webViewEntry);
    }
}
