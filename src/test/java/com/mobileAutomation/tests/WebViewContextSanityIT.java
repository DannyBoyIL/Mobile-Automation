package com.mobileAutomation.tests;

import com.mobileAutomation.driver.DriverFactory;
import com.mobileAutomation.driver.DriverManager;
import com.mobileAutomation.driver.Platform;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.remote.SupportsContextSwitching;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

/**
 * Layer 2 diagnostic — proves Appium can see and switch into the WebView context.
 *
 * <p>This test is intentionally <b>outside</b> the Cucumber flow. It exists only to verify the
 * native ↔ WebView toggle works end-to-end before we write any POM / flow / feature code for
 * the WebView screen. Once this is green, framework development can begin.
 *
 * <p>Run with:
 * <pre>{@code
 *   # Android
 *   mvn verify -Dplatform=android -Dit.test=WebViewContextSanityIT
 *
 *   # iOS Simulator
 *   mvn verify -Dplatform=ios -Dit.test=WebViewContextSanityIT
 * }</pre>
 *
 * <p>Note: this is a Maven Failsafe integration test (the {@code IT} suffix matches
 * the include pattern in {@code pom.xml} → {@code maven-failsafe-plugin}). It is
 * deliberately not run by {@code mvn test} so a default build does not bring up
 * an Appium session.
 *
 * <p>Android prerequisites:
 * <ul>
 *   <li>Emulator booted, TheApp (debuggable) installed.</li>
 *   <li>Appium running with chromedriver autodownload enabled, or the
 *     {@code chromedriverExecutable} capability pinned in
 *     {@link com.mobileAutomation.driver.DriverFactory}:
 *     {@code appium --log-level warn --allow-insecure chromedriver_autodownload}</li>
 * </ul>
 *
 * <p>iOS prerequisites:
 * <ul>
 *   <li>iOS Simulator booted (matching {@code deviceName} / {@code platformVersion}
 *     in {@code ios.json}), TheApp (debug build) installed.</li>
 *   <li>WDA buildable against the installed Xcode toolchain (WDA is rebuilt on first run).</li>
 *   <li>The app's WKWebView must be remote-debuggable — for iOS 16.4+ this means
 *     {@code WKWebView.isInspectable = true} in the debug build. TheApp's debug
 *     build already satisfies this; release builds do not and are not supported
 *     by this sanity check.</li>
 *   <li>No {@code ios_webkit_debug_proxy} needed on simulator — Appium talks to
 *     WebKit's remote inspector directly.</li>
 * </ul>
 *
 * <p>Success criterion: the context list printed in the logs contains both
 * {@code NATIVE_APP} and a {@code WEBVIEW_*} entry, and after switching into the
 * WebView context an {@code executeScript} call can read the DOM back — confirmed
 * by finding the WebviewScreen's static placeholder text in {@code document.body}.
 */
public class WebViewContextSanityIT {

    private static final Logger log = LoggerFactory.getLogger(WebViewContextSanityIT.class);

    private static final Duration NATIVE_WAIT  = Duration.ofSeconds(10);
    private static final Duration WEBVIEW_WAIT = Duration.ofSeconds(30);

    private AppiumDriver driver;
    /** Concrete driver cast to the context-switch mixin.
     *  In Appium Java Client 9.x, context methods live on SupportsContextSwitching,
     *  which AndroidDriver/IOSDriver implement — but AppiumDriver itself doesn't. */
    private SupportsContextSwitching contexts;
    /** Which platform we're sanity-checking against. Only affects how we scroll
     *  "Webview Demo" into view from the home list — everything else (context
     *  handle discovery, data: URL navigation, title/URL readback) is identical
     *  on Android (chromedriver / WebView) and iOS (WebKit remote inspector). */
    private Platform platform;

    @BeforeClass
    public void setUp() {
        platform = Platform.fromString(System.getProperty("platform"));
        log.info("========== SANITY: WebView context switch ({}) ==========", platform);
        DriverManager.setDriver(DriverFactory.createDriver());
        driver   = DriverManager.getDriver();
        contexts = (SupportsContextSwitching) driver;
    }

    @Test
    public void nativeToWebViewContextSwitch() {
        WebDriverWait shortWait = new WebDriverWait(driver, NATIVE_WAIT);

        /* ---------- 1. Navigate Home -> Webview Demo ---------- */
        log.info("STEP 1: Tap 'Webview Demo' from Home");

        // Prove we're on Home by waiting for the known-good "Login Screen" marker first.
        try {
            shortWait.until(ExpectedConditions.presenceOfElementLocated(
                    AppiumBy.accessibilityId("Login Screen")));
        } catch (Exception e) {
            log.error("Home-screen marker 'Login Screen' not visible. Page source follows:\n{}",
                    truncate(driver.getPageSource(), 4000));
            throw e;
        }

        // The 'Webview Demo' row may be below the fold; scroll-into-view works whether
        // the entry is on-screen or not. The scroll mechanism is platform-specific:
        //   Android: UiScrollable.scrollIntoView(...) via the UiAutomator strategy.
        //   iOS    : XCUITest's `mobile: scroll` with a predicate string.
        // Both locate the final element by the same accessibility id because the RN
        // testProp ("Webview Demo") is rendered as content-description on Android and
        // as accessibility id on iOS.
        WebElement webViewEntry = scrollToWebViewDemoEntry();
        webViewEntry.click();

        // Confirm the tap actually landed on the Webview Demo screen before we spend
        // 30s waiting for a WEBVIEW_* handle. On iOS in particular, if the 'Webview Demo'
        // accessibility id resolves to a label rather than the tappable row, we can
        // end up stranded on Home and every subsequent diagnostic becomes ambiguous.
        try {
            shortWait.until(ExpectedConditions.presenceOfElementLocated(
                    AppiumBy.accessibilityId("urlInput")));
        } catch (Exception e) {
            log.error("Tap did not reach the Webview Demo screen ('urlInput' not visible). "
                    + "We're not where we think we are. Page source follows:\n{}",
                    truncate(driver.getPageSource(), 4000));
            throw e;
        }

        /* ---------- 2. Wait for WEBVIEW_* context to appear ---------- */
        // The Webview Demo screen creates its WebView widget on mount (loaded with a
        // placeholder "Please navigate to a webpage" HTML), so the WEBVIEW_* handle
        // is available as soon as the screen is on. We don't need to tap Go first.
        log.info("STEP 2: Wait for WEBVIEW_* context (timeout = {}s)", WEBVIEW_WAIT.toSeconds());
        String webContext = waitForWebContext(WEBVIEW_WAIT);

        log.info("All context handles: {}", contexts.getContextHandles());
        if (webContext == null) {
            // Rich diagnostic dump — pageSource confirms we're on the right screen,
            // and the detailed contexts list (iOS) or the handles set (Android) tells
            // us what Appium is seeing.
            log.error("No WEBVIEW_* context after {}s. Native page source follows:\n{}",
                    WEBVIEW_WAIT.toSeconds(), truncate(driver.getPageSource(), 4000));
            if (platform == Platform.IOS) {
                try {
                    Object detailed = driver.executeScript("mobile: getContexts",
                            Map.of("waitForWebviewMs", 0));
                    log.error("iOS mobile: getContexts (detailed) = {}", detailed);
                } catch (Exception e) {
                    log.error("mobile: getContexts failed", e);
                }
            }
            Assert.fail(
                    "Expected a WEBVIEW_* context to appear but only NATIVE_APP was present.\n" +
                    (platform == Platform.IOS
                        ? "On iOS this usually means WKWebView is not remote-debuggable. Since iOS 16.4, " +
                          "WKWebView is only inspectable when the app sets `webView.isInspectable = true` " +
                          "in native code. Verify the TheApp build you're running has this set (check the " +
                          "debug scheme of the RN host). A release build will never surface a WEBVIEW_* " +
                          "handle on iOS 16.4+, regardless of Appium timing."
                        : "On Android this usually means a chromedriver version mismatch or a non-debuggable " +
                          "WebView. Check `chromedriverExecutable` in DriverFactory.")
            );
        }
        log.info("WebView context detected: {}", webContext);

        /* ---------- 3. Switch into the WebView context ---------- */
        log.info("STEP 3: Switch into WebView context");
        contexts.context(webContext);

        /* ---------- 4. Drive the WebView directly via the remote inspector ---------- */
        // This is the core of the sanity test: proving Appium can control the WebView
        // DOM after the context switch. We deliberately do NOT call driver.get() here:
        //
        //   (a) Top-frame data: URL navigation is silently rejected by WKWebView's
        //       navigation delegate on iOS 14+ regardless of inspectability. The
        //       remote inspector's Page.navigate goes through the same delegate, so
        //       there's no way to bypass that restriction from the test side.
        //   (b) Driving the app's native URL-input + Go button isn't this layer's
        //       job — this is an Appium/WebView integration sanity check, not a
        //       UI-flow test. (And there's a separate React Native Elements bug
        //       where the Go button's onPress doesn't fire for programmatic taps;
        //       we'll chase that in the feature-level suite.)
        //
        // Instead, we assert against the WebView content the screen *already* has
        // mounted: the WebviewScreen calls loadHTMLString with a static placeholder
        // ("Please navigate to a webpage") on mount. Reading that back via
        // executeScript proves the full path we care about — native context →
        // WEBVIEW context → JS execution in that context → DOM readback through
        // the inspector — with zero network dependency and no fake/synthetic data.
        final String expectedSnippet = "Please navigate to a webpage";
        log.info("STEP 4: Read DOM from the WebView via executeScript");
        Object bodyTextRaw = driver.executeScript("return document.body && document.body.innerText;");
        String bodyText = bodyTextRaw == null ? "" : bodyTextRaw.toString();
        String currentUrl = driver.getCurrentUrl();
        log.info("WebView body text: '{}'", truncate(bodyText, 200));
        log.info("WebView URL      : '{}'", currentUrl);

        // If the placeholder isn't there, dump the page source for diagnosis.
        if (!bodyText.contains(expectedSnippet)) {
            log.error("WebView body did not contain expected snippet '{}'. Page source:\n{}",
                    expectedSnippet, truncate(driver.getPageSource(), 4000));
        }

        /* ---------- 5. Switch back to NATIVE_APP ---------- */
        log.info("STEP 5: Switch back to NATIVE_APP");
        contexts.context("NATIVE_APP");
        log.info("Active context after switch back: {}", contexts.getContext());

        /* ---------- Assertions ---------- */
        Assert.assertTrue(
                bodyText.contains(expectedSnippet),
                "WebView did not return the expected placeholder content. Context switch " +
                "succeeded but DOM readback through the inspector did not surface the " +
                "WebviewScreen's static loadHTMLString. Check appium / chromedriver logs. " +
                "Got: '" + truncate(bodyText, 200) + "'"
        );
        Assert.assertEquals(
                contexts.getContext(),
                "NATIVE_APP",
                "Failed to return to NATIVE_APP context after the webview probe."
        );

        log.info("========== SANITY PASSED: native ↔ webview toggle works ==========");
    }

    /**
     * Scrolls the "Webview Demo" row into view on the home list and returns the element.
     *
     * <p>Android: uses {@code UiScrollable.scrollIntoView(...)} — one atomic UiAutomator
     * call that scrolls and returns the matched node.
     *
     * <p>iOS: tries a direct lookup first (the entry may already be visible on a taller
     * simulator window), then falls back to {@code mobile: scroll} with a predicate
     * string, then re-resolves the element.
     */
    private WebElement scrollToWebViewDemoEntry() {
        switch (platform) {
            case ANDROID:
                try {
                    return driver.findElement(AppiumBy.androidUIAutomator(
                            "new UiScrollable(new UiSelector().scrollable(true).instance(0))"
                            + ".scrollIntoView(new UiSelector().description(\"Webview Demo\"))"
                    ));
                } catch (Exception e) {
                    log.error("Could not locate 'Webview Demo' via UiScrollable. Page source follows:\n{}",
                            truncate(driver.getPageSource(), 4000));
                    throw e;
                }
            case IOS:
                try {
                    return driver.findElement(AppiumBy.accessibilityId("Webview Demo"));
                } catch (Exception firstTry) {
                    log.info("'Webview Demo' not on-screen — scrolling down via mobile: scroll");
                    try {
                        driver.executeScript("mobile: scroll", Map.of(
                                "direction", "down",
                                "predicateString", "name == 'Webview Demo'"
                        ));
                        return driver.findElement(AppiumBy.accessibilityId("Webview Demo"));
                    } catch (Exception e) {
                        log.error("Could not locate 'Webview Demo' on iOS after scrolling. Page source follows:\n{}",
                                truncate(driver.getPageSource(), 4000));
                        throw e;
                    }
                }
            default:
                throw new IllegalStateException("Unsupported platform: " + platform);
        }
    }

    /**
     * Polls until a {@code WEBVIEW_*} handle appears or the timeout elapses. Returns the
     * first webview handle found, or {@code null} on timeout.
     *
     * <p>iOS uses {@code mobile: getContexts} in addition to {@code getContextHandles()}
     * because XCUITest's richer endpoint sometimes returns webview entries (with full
     * bundle id + URL) a beat earlier than the W3C {@code GET /contexts} response. At
     * each tick we log what each endpoint sees so a timeout dump shows exactly which
     * layer is blind.
     */
    private String waitForWebContext(Duration timeout) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        Set<String> lastSeen = Set.of();
        while (System.currentTimeMillis() < deadline) {
            Set<String> handles = contexts.getContextHandles();
            if (!handles.equals(lastSeen)) {
                log.debug("Context handles now: {}", handles);
                lastSeen = handles;
            }
            for (String handle : handles) {
                if (handle != null && handle.startsWith("WEBVIEW_")) {
                    return handle;
                }
            }
            // iOS-only second-chance lookup via the XCUITest-native endpoint.
            if (platform == Platform.IOS) {
                String iosHandle = pollIosWebviewHandle();
                if (iosHandle != null) {
                    return iosHandle;
                }
            }
            sleepQuietly(500);
        }
        return null;
    }

    /**
     * Asks XCUITest directly for its context list and returns the first {@code WEBVIEW_*}
     * handle, or {@code null} if none. Swallows errors (the tick loop will retry).
     */
    @SuppressWarnings("unchecked")
    private String pollIosWebviewHandle() {
        try {
            Object raw = driver.executeScript("mobile: getContexts",
                    Map.of("waitForWebviewMs", 0));
            if (!(raw instanceof java.util.List<?> list)) return null;
            for (Object entry : list) {
                String id = null;
                if (entry instanceof String s) {
                    id = s;
                } else if (entry instanceof Map<?, ?> m) {
                    Object v = m.get("id");
                    if (v instanceof String s) id = s;
                }
                if (id != null && id.startsWith("WEBVIEW_")) {
                    return id;
                }
            }
        } catch (Exception ignored) {
            // tick loop will retry
        }
        return null;
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    /** Truncates a string to at most {@code max} chars for logging. */
    private static String truncate(String s, int max) {
        if (s == null) return "<null>";
        return s.length() <= max ? s : s.substring(0, max) + "\n... [truncated " + (s.length() - max) + " chars]";
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        log.info("Tearing down sanity session");
        if (DriverManager.getDriver() != null) {
            try {
                DriverManager.getDriver().quit();
            } catch (Exception e) {
                log.warn("Driver quit threw", e);
            }
            DriverManager.unload();
        }
    }
}
