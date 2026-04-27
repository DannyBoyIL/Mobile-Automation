package com.mobileAutomation.driver;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.remote.SupportsContextSwitching;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Set;
import java.util.function.Supplier;

public final class ContextManager {

    public static final String NATIVE = "NATIVE_APP";

    private static final Duration WEBVIEW_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration POLL = Duration.ofMillis(500);

    private static final Logger logger = LoggerFactory.getLogger(ContextManager.class);

    private ContextManager() {
    }

    /**
     * Switches into the first {@code WEBVIEW_*} context, runs the supplier, and
     * restores the original context even if the supplier throws.
     */
    public static <T> T runInWebView(Supplier<T> action) {
        SupportsContextSwitching contexts = contexts();
        String originalContext = contexts.getContext();

        String webContext = waitForWebContext(contexts, WEBVIEW_TIMEOUT);
        if (webContext == null) {
            throw new IllegalStateException(
                    "No WEBVIEW_* context available after " + WEBVIEW_TIMEOUT.toSeconds() +
                            "s. Known handles: " + contexts.getContextHandles() +
                            ". Check chromedriver version / WebView debuggability."
            );
        }

        logger.info("Switching context: {} -> {}", originalContext, webContext);
        contexts.context(webContext);
        try {
            return action.get();
        } finally {
            logger.info("Switching context back: {} -> {}", webContext, originalContext);
            try {
                contexts.context(originalContext);
            } catch (Exception e) {
                logger.warn("Failed to switch back to context '{}' — next test may start in the wrong context", originalContext, e);
            }
        }
    }

    /**
     * Runnable overload for callers that don't produce a value.
     */
    public static void runInWebView(Runnable action) {
        runInWebView(() -> {
            action.run();
            return null;
        });
    }

    /**
     * Diagnostic hook — returns all current context handles (NATIVE_APP plus any WEBVIEW_*).
     */
    public static Set<String> currentContextHandles() {
        return contexts().getContextHandles();
    }

    /**
     * Diagnostic hook — returns the currently active context name.
     */
    public static String currentContext() {
        return contexts().getContext();
    }

    /* ---------- internals ---------- */

    private static String waitForWebContext(SupportsContextSwitching contexts, Duration timeout) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        Set<String> lastSeen = Set.of();
        while (System.currentTimeMillis() < deadline) {
            Set<String> handles = contexts.getContextHandles();
            if (!handles.equals(lastSeen)) {
                logger.debug("Context handles now: {}", handles);
                lastSeen = handles;
            }
            for (String handle : handles) {
                if (handle != null && handle.startsWith("WEBVIEW_")) {
                    return handle;
                }
            }
            try {
                Thread.sleep(POLL.toMillis());
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return null;
    }

    private static SupportsContextSwitching contexts() {
        AppiumDriver driver = DriverManager.getDriver();
        if (driver == null) {
            throw new IllegalStateException("Driver not initialized. Check Hooks.");
        }
        return (SupportsContextSwitching) driver;
    }
}
