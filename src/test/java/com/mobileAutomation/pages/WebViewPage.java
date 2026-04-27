package com.mobileAutomation.pages;

import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import io.appium.java_client.pagefactory.iOSXCUITFindBy;
import org.openqa.selenium.WebElement;

/**
 * Native-context page object for the "Webview Demo" screen.
 *
 * <p>The three elements below are wired via testProps in the RN source
 * ({@code urlInput}, {@code navigateBtn}, {@code clearBtn}). On Android the RN
 * convention maps that to a content-description; on iOS to an accessibility id.
 * Both are reachable via the same {@link io.appium.java_client.pagefactory.AndroidFindBy}/
 * {@link io.appium.java_client.pagefactory.iOSXCUITFindBy} accessibility-id
 * annotations, which is the same pattern used throughout this framework.
 *
 * <p>iOS locators are kept for framework-style consistency even though iOS is
 * currently out of scope — when iOS is revisited they should work as-is, since
 * the testProps accessibility ids are identical across platforms.
 */
public class WebViewPage extends BasePage {

    @SuppressWarnings("unused")
    @AndroidFindBy(accessibility = "urlInput")
    @iOSXCUITFindBy(accessibility = "urlInput")
    private WebElement urlInput;

    @SuppressWarnings("unused")
    @AndroidFindBy(accessibility = "navigateBtn")
    @iOSXCUITFindBy(accessibility = "navigateBtn")
    private WebElement goButton;

    @SuppressWarnings("unused")
    @AndroidFindBy(accessibility = "clearBtn")
    @iOSXCUITFindBy(accessibility = "clearBtn")
    private WebElement clearButton;

    public boolean isVisible() {
        return isVisible(urlInput);
    }

    // TODO why do I return this?
    public WebViewPage enterUrl(String url) {
        if (driver() instanceof IOSDriver) {
            // RN TextInput on iOS doesn't reliably propagate XCUITest's clear() back
            // into component state, so a subsequent sendKeys appends to the old value.
            // Tapping the native Clear button goes through the component's own state
            // update, which is the reliable path (see @positive Clear scenario).
            // No-op when the field is already empty.
            click(clearButton);
        }
        type(urlInput, url);
        return this;
    }

    public void tapGo() {
        click(goButton);
    }

    public void tapClear() {
        click(clearButton);
    }

    /**
     * Reads the URL input's current value. Returns an empty string rather than
     * {@code null} so step assertions can compare with {@code ""}.
     *
     * <p>Android quirk: an empty {@code EditText} with an {@code android:hint}
     * reports the hint value as the {@code text} attribute rather than an empty
     * string. To distinguish "field is empty but showing placeholder" from
     * "user typed the placeholder text", we read the {@code hint} attribute
     * separately and normalise {@code text == hint} as empty. iOS's XCUITest
     * doesn't have this quirk — the {@code value} attribute is empty when the
     * field is empty.
     */
    public String urlFieldValue() {
        if (driver() instanceof IOSDriver) {
            String raw = getAttribute(urlInput, "value");
            return raw == null ? "" : raw;
        }
        String raw = getAttribute(urlInput, "text");
        String hint = getAttribute(urlInput, "hint");
        if (raw != null && hint != null && !hint.isEmpty() && raw.equals(hint)) {
            return "";
        }
        return raw == null ? "" : raw;
    }
}
