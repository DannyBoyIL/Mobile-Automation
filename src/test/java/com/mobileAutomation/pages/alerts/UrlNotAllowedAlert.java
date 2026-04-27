package com.mobileAutomation.pages.alerts;

/**
 * Native alert raised by the app when the user tries to navigate to a URL that
 * fails the {@code /^https:\/\/appiumpro\.com$/i} whitelist regex.
 *
 * <p>Mechanics (OK button + text-based visibility check) are inherited from
 * {@link MessageAlert}; this subclass only declares the expected message so
 * {@code WebViewResult.Blocked} keeps a distinct type.
 */
public class UrlNotAllowedAlert extends MessageAlert {

    private static final String EXPECTED_MESSAGE = "Sorry, you are not allowed to visit that url";

    public UrlNotAllowedAlert() {
        super(byText(EXPECTED_MESSAGE));
    }
}
