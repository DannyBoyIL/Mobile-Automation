package com.mobileAutomation.pages.alerts;

/**
 * Native alert raised by the app when login credentials are rejected.
 * See {@link MessageAlert} for the shared visibility/dismiss mechanics; this
 * class exists as a distinct type so {@code LoginResult.Invalid} can convey
 * <em>why</em> the dialog appeared without a string match at the call site.
 */
public class InvalidLoginAlert extends MessageAlert {

    private static final String EXPECTED_MESSAGE = "Invalid login credentials";

    public InvalidLoginAlert() {
        super(byText(EXPECTED_MESSAGE));
    }
}
