package com.mobileAutomation.flows;

import com.mobileAutomation.pages.HomePage;
import com.mobileAutomation.pages.InvalidLoginDialog;
import com.mobileAutomation.pages.LoginPage;
import com.mobileAutomation.pages.SecretPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginFlow {

    private final HomePage homePage = new HomePage();
    private final LoginPage loginPage = new LoginPage();

    private static final Logger logger = LoggerFactory.getLogger(LoginFlow.class);

    public LoginResult login(String username, String password) {

        logger.info("FLOW: Starting login flow");

        // Ensure we are on the login screen
        if (!loginPage.isVisible()) {
            logger.info("FLOW: Login screen not visible -> navigating from Home");
            homePage.goToLogin();
        }

        logger.info("FLOW: Submitting credentials");
        loginPage.login(username, password);

        InvalidLoginDialog dialog = new InvalidLoginDialog();
        if (dialog.isVisible()) {
            logger.info("FLOW RESULT: Application rejected credentials");
            return new LoginResult.Invalid(dialog);
        }

        SecretPage secret = new SecretPage();
        if (secret.isVisible()) {
            logger.info("FLOW RESULT: Application granted access to secret area");
            return new LoginResult.Success(secret);
        }

        if (loginPage.isVisible()) {
            logger.warn("FLOW RESULT: Still on login page after submit");
            return new LoginResult.Invalid(dialog);
        }

        logger.error("FLOW ERROR: App reached unknown state after login");
        throw new IllegalStateException("Unknown state after login");
    }
}
