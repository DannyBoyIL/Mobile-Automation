package com.mobileAutomation.pages.webPages;

import com.mobileAutomation.pages.WebViewContent;
import org.openqa.selenium.By;

/**
 * Site-specific page object for the <a href="https://appiumpro.com">appiumpro.com</a>
 * homepage, as rendered inside the app's WebView.
 *
 * <p>Extends {@link WebViewContent} so it inherits the webview-aware primitives
 * ({@code type}, {@code click}, {@code valueOf}, {@code waitForUrlStartingWith},
 * the 15s {@code domWait}) and adds semantic methods on top — keeping CSS / id
 * selectors out of step definitions, the same way the native page objects keep
 * Appium {@code @FindBy} annotations out of their callers.
 *
 * <p>This mirrors the native POM convention one-to-one: a class per screen,
 * a field per meaningful locator, public methods that describe user intent.
 * The only difference is the driver engine underneath — chromedriver rather
 * than UiAutomator2 — which is a deliberately hidden implementation detail.
 *
 * <p>All methods assume the caller has already entered a {@code WEBVIEW_*}
 * context (typically via {@link com.mobileAutomation.driver.ContextManager#runInWebView}).
 */
public class AppiumProHomePage extends WebViewContent {

    /**
     * Mailchimp-generated id on the newsletter subscription email field. Stable
     * enough for a framework-sanity demonstration — if it ever churns the fix is
     * a one-line update here rather than in every step that touches the field.
     */
    private static final By emailInput = By.id("mce-EMAIL");

    /**
     * Types {@code email} into the Subscribe form's email field, clearing
     * existing content first.
     */
    public AppiumProHomePage enterEmail(String email) {
        type(emailInput, email);
        return this;
    }

    /** Current value of the Subscribe form's email field. */
    public String emailValue() {
        return valueOf(emailInput);
    }
}
