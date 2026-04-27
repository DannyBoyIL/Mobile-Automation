package com.mobileAutomation.flows;

import com.mobileAutomation.pages.HomePage;
import com.mobileAutomation.pages.alerts.UrlNotAllowedAlert;
import com.mobileAutomation.pages.WebViewContent;
import com.mobileAutomation.pages.WebViewPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebViewFlow {

    private final HomePage homePage = new HomePage();
    private WebViewPage webViewPage;

    private static final Logger logger = LoggerFactory.getLogger(WebViewFlow.class);

    public WebViewPage openWebViewScreen() {
        if (webViewPage != null && webViewPage.isVisible()) {
            return webViewPage;
        }
        logger.info("FLOW: Opening Webview Demo from Home");
        homePage.goToWebViewDemo();

        webViewPage = new WebViewPage();
        if (!webViewPage.isVisible()) {
            logger.error("FLOW ERROR: Webview Demo screen did not render urlInput");
            throw new IllegalStateException("Webview Demo screen did not open");
        }
        return webViewPage;
    }

    /**
     * Types {@code url} into the native URL input, taps Go, and returns a
     * {@link WebViewResult} describing the branch the app took.
     */
    public WebViewResult navigate(String url) {
        WebViewPage page = openWebViewScreen();

        logger.info("FLOW: Navigate WebView to '{}'", url);
        page.enterUrl(url);
        page.tapGo();

        UrlNotAllowedAlert alert = new UrlNotAllowedAlert();
        if (alert.isVisible()) {
            logger.info("FLOW RESULT: URL blocked by native validation");
            return new WebViewResult.Blocked(alert);
        }

        logger.info("FLOW RESULT: URL accepted, WebView loading");
        return new WebViewResult.Loaded(new WebViewContent());
    }

    /**
     * Returns the cached {@link WebViewPage}, or navigates there if not yet opened.
     * Useful for steps that need to assert on the native field after a branch
     * (e.g. verifying the input was cleared after a blocked-URL alert).
     */
    public WebViewPage page() {
        return openWebViewScreen();
    }
}
