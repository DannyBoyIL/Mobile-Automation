package com.mobileAutomation.pages.alerts;

import com.mobileAutomation.pages.BasePage;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Shared base for native alert dialogs that consist of a text message and a
 * single OK button. Subclasses only need to declare the expected message
 * locator; visibility and dismissal mechanics live here.
 *
 * <p>Subclasses remain distinct types on purpose so that sealed result
 * hierarchies (e.g. {@code LoginResult.Invalid}, {@code WebViewResult.Blocked})
 * keep telling you <em>why</em> the dialog appeared at the type level, without
 * the call site having to string-match on the message.
 */
public abstract class MessageAlert extends BasePage {

    @SuppressWarnings("unused")
    @AndroidFindBy(id = "android:id/button1")
    @iOSXCUITFindBy(accessibility = "OK")
    private WebElement okButton;

    private final By messageLocator;

    protected MessageAlert(By messageLocator) {
        this.messageLocator = messageLocator;
    }

    public boolean isVisible() {
        return isVisible(messageLocator);
    }

    public void dismiss() {
        click(okButton);
    }
}
