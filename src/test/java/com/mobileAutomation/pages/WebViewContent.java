package com.mobileAutomation.pages;

import com.mobileAutomation.driver.DriverManager;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * WebView-context page object. All methods must be invoked while Appium is in a
 * {@code WEBVIEW_*} context — callers should wrap calls in
 * {@link com.mobileAutomation.driver.ContextManager#runInWebView(java.util.function.Supplier)}.
 *
 * <p>Deliberately does <b>not</b> use PageFactory / Appium locator annotations: inside
 * a webview context the underlying driver is chromedriver, and we want plain W3C
 * selectors ({@link By#tagName}, {@link By#cssSelector}) rather than mobile locators.
 *
 * <p>Uses its own 15-second wait — real network loads (e.g. appiumpro.com) can
 * exceed {@link BasePage}'s 3-second native default.
 */
public class WebViewContent extends BasePage {

    private static final Duration DOM_TIMEOUT = Duration.ofSeconds(15);

    private static final Logger logger = LoggerFactory.getLogger(WebViewContent.class);

//    private AppiumDriver driver() {
//        AppiumDriver driver = DriverManager.getDriver();
//        if (driver == null) {
//            throw new IllegalStateException("Driver not initialized. Check Hooks.");
//        }
//        return driver;
//    }

    private WebDriverWait domWait() {
        return new WebDriverWait(driver(), DOM_TIMEOUT);
    }

    /** URL chromedriver reports for the currently loaded page. */
    public String currentUrl() {
        return driver().getCurrentUrl();
    }

    /**
     * Blocks until chromedriver reports a URL starting with {@code prefix} (case-insensitive),
     * up to {@link #DOM_TIMEOUT}. Needed because the RN app routes the Go tap through a
     * state change → JS bridge → native {@code WebView.loadUrl()}, so the URL the
     * webview reports right after the tap is still {@code about:blank} for a beat.
     *
     * <p>Returns the observed URL so callers can log / assert on it without a second read.
     *
     * @throws org.openqa.selenium.TimeoutException if the URL never starts with {@code prefix}
     */
    public String waitForUrlStartingWith(String prefix) {
        String lower = prefix.toLowerCase();
        return domWait().until(d -> {
            String current = d.getCurrentUrl();
            return (current != null && current.toLowerCase().startsWith(lower)) ? current : null;
        });
    }

    /** {@code <title>} of the currently loaded page. */
    public String title() {
        return driver().getTitle();
    }

    /** Full text of {@code <body>}. Useful for loose substring assertions. */
    public String bodyText() {
        WebElement body = domWait().until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        String text = body.getText();
        return text == null ? "" : text;
    }

    /**
     * True iff {@code selector} matches an element within {@link #DOM_TIMEOUT}. Use
     * this for strict content assertions (e.g. {@code By.cssSelector("h1")} to prove
     * the live page rendered rather than the inline stub HTML).
     */
    // TODO implement this through BasePage.isVisible()
    public boolean hasElement(By selector) {
        try {
            domWait().until(ExpectedConditions.presenceOfElementLocated(selector));
            return true;
        } catch (Exception e) {
            logger.debug("Element not present within {}s: {}", DOM_TIMEOUT.toSeconds(), selector);
            return false;
        }
    }

    public boolean isVisible(By locator) {
        return super.isVisible(locator);
    }

    /* ---------------------------------------------------------------------
     * Generic By-based interaction helpers.
     *
     * These are the webview-domain equivalents of BasePage's native helpers
     * (which take WebElement + @FindBy annotations). Inside a WEBVIEW_* context
     * the underlying driver is chromedriver, so plain W3C locators + explicit
     * waits using {@link #DOM_TIMEOUT} (15s) are the right tool — the PageFactory
     * / Appium annotation machinery doesn't apply.
     *
     * Site-specific page objects (e.g. AppiumProHomePage) extend this class
     * and expose semantic methods on top of these primitives, keeping raw
     * selectors out of step definitions.
     * ------------------------------------------------------------------ */

    /**
     * Clears the matched element and types {@code text} into it. Waits up to
     * {@link #DOM_TIMEOUT} for the element to become clickable (visible +
     * enabled) before interacting.
     */
    public void type(By selector, String text) {
        WebElement el = domWait().until(ExpectedConditions.elementToBeClickable(selector));
        el.clear();
        el.sendKeys(text);
    }

    /** Clicks the matched element once it becomes clickable. */
    public void click(By selector) {
        WebElement el = domWait().until(ExpectedConditions.elementToBeClickable(selector));
        el.click();
    }

    /**
     * Reads the live {@code value} DOM property of the matched element — the
     * current content of an {@code <input>} / {@code <textarea>} as the user
     * would see it, regardless of what the static HTML {@code value} attribute
     * says.
     *
     * <p>Uses {@link WebElement#getDomProperty(String)} deliberately rather than
     * {@link WebElement#getAttribute(String)}: for inputs whose HTML source is
     * {@code <input value>} (empty default) but whose JS-side {@code .value} has
     * been updated by typing, {@code getAttribute} can return the empty HTML
     * attribute rather than falling through to the live property. {@code
     * getDomProperty} always hits the JS property, so it matches what the user
     * sees on screen.
     *
     * <p>Returns empty string rather than {@code null} so callers can compare
     * against {@code ""} without a null-guard.
     */
    public String valueOf(By selector) {
        WebElement el = domWait().until(ExpectedConditions.presenceOfElementLocated(selector));
        String v = el.getDomProperty("value");
        return v == null ? "" : v;
    }
}
