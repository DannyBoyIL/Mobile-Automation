package com.mobileAutomation.steps;

import com.mobileAutomation.driver.ContextManager;
import com.mobileAutomation.flows.WebViewFlow;
import com.mobileAutomation.flows.WebViewResult;
import com.mobileAutomation.pages.webPages.AppiumProHomePage;
import com.mobileAutomation.pages.WebViewContent;
import com.mobileAutomation.pages.WebViewPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import static com.mobileAutomation.assertions.ResultAssertions.assertInstanceOf;

public class WebViewSteps {

    private final WebViewFlow webViewFlow = new WebViewFlow();
    private WebViewPage webViewPage;
    private WebViewResult result;

    private static final String APPIUM_PRO_HOME = "https://appiumpro.com";
    private static final Logger logger = LoggerFactory.getLogger(WebViewSteps.class);

    /**
     * Lazily-created because it's only used by the framework-sanity scenario.
     * Page objects are stateless selector-holders so a single instance is safe to share between the enter-email and
     * verify-email steps.
     */
    private AppiumProHomePage appiumProHomePage;

    private AppiumProHomePage appiumProHomePage() {
        if (appiumProHomePage == null) {
            appiumProHomePage = new AppiumProHomePage();
        }
        return appiumProHomePage;
    }

    @Given("the user is on the Webview Demo screen")
    public void openScreen() {
        logger.info("STEP: Open Webview Demo screen");
        webViewPage = webViewFlow.openWebViewScreen();
        Assert.assertTrue(webViewPage.isVisible(), "Webview Demo screen did not open");
    }

    @When("the user navigates to {string}")
    public void navigate(String url) {
        logger.info("STEP: Navigate WebView to '{}'", url);
        result = webViewFlow.navigate(url);
    }

    @When("the user types {string} into the URL field")
    public void typeIntoUrlField(String text) {
        logger.info("STEP: Type '{}' into URL field", text);
        webViewPage.enterUrl(text);
    }

    @And("the user taps Clear")
    public void tapClear() {
        logger.info("STEP: Tap Clear");
        webViewPage.tapClear();
    }

    @Then("the WebView should load the Appium Pro homepage")
    public void verifyAppiumProLoaded() {
        logger.info("STEP: Expect Loaded result, assert DOM + URL inside webview");
        WebViewResult.Loaded loaded = assertInstanceOf(WebViewResult.Loaded.class, result);
        WebViewContent content = loaded.content();

        ContextManager.runInWebView(() -> {
            String currentUrl = content.waitForUrlStartingWith(APPIUM_PRO_HOME);
            logger.info("WebView reports currentUrl='{}', title='{}'", currentUrl, content.title());
            Assert.assertTrue(
                    content.hasElement(By.cssSelector("h1")),
                    "Expected at least one <h1> on appiumpro.com homepage (stub HTML has none)"
            );
        });
    }

    @Then("the not-allowed alert should be shown")
    public void verifyBlocked() {
        logger.info("STEP: Expect Blocked result, assert alert is visible");
        WebViewResult.Blocked blocked = assertInstanceOf(WebViewResult.Blocked.class, result);
        Assert.assertTrue(blocked.alert().isVisible(), "Not-allowed alert was not visible");
    }

    @And("dismissing the alert should keep the WebView on the initial page")
    public void dismissAlertAndVerifyStubShown() {
        logger.info("STEP: Dismiss alert, assert WebView still shows stub HTML");
        WebViewResult.Blocked blocked = assertInstanceOf(WebViewResult.Blocked.class, result);
        blocked.alert().dismiss();

        Assert.assertFalse(
                blocked.alert().isVisible(),
                "Not-allowed alert should no longer be visible after dismissal"
        );

        WebViewContent content = new WebViewContent();
        ContextManager.runInWebView(() -> {
            String body = content.bodyText();
            logger.info("WebView body after dismissal: '{}'", body);
            Assert.assertTrue(
                    body.toLowerCase().contains("please navigate to a webpage"),
                    "Expected stub HTML after dismissal but body was: " + body
            );
        });

        result = null;
    }

    @Then("the URL input should be empty")
    public void verifyEmpty() {
        logger.info("STEP: Assert URL input is empty");
        Assert.assertEquals(
                webViewPage.urlFieldValue(),
                "",
                "URL field should be empty"
        );
    }

    /* ---------- Framework-sanity (WebView interaction) ---------- */

    @And("the user enters {string} into the WebView email field")
    public void typeEmailInWebView(String email) {
        logger.info("STEP: Enter '{}' into WebView email field", email);
        // Sanity-guard: if the prior navigate was blocked, there is no page to type into.
        // Failing fast here gives a clearer message than letting the type() timeout.
        assertInstanceOf(WebViewResult.Loaded.class, result);

        ContextManager.runInWebView(() -> {
            appiumProHomePage().enterEmail(email);
        });
    }

    @Then("the WebView email field should contain {string}")
    public void verifyEmailValue(String expected) {
        logger.info("STEP: Assert WebView email field contains '{}'", expected);
        String actual = ContextManager.runInWebView(() -> appiumProHomePage().emailValue());
        Assert.assertEquals(
                actual,
                expected,
                "WebView email field value mismatch"
        );
    }
}
