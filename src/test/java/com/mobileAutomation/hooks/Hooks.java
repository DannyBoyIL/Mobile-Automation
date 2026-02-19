package com.mobileAutomation.hooks;

import com.mobileAutomation.driver.DriverFactory;
import com.mobileAutomation.driver.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

public class Hooks {

    private static final Logger logger = LoggerFactory.getLogger(Hooks.class);

    @Before
    public void setUp(Scenario scenario) {

        logger.info("========== TEST START ==========");
        logger.info("Scenario: {}", scenario.getName());
        DriverManager.setDriver(DriverFactory.createDriver());
    }

    @AfterStep
    public void attachArtifacts(Scenario scenario) {

        if (!scenario.isFailed()) {
            return;
        }

        logger.error("Step failed! Capturing diagnostic artifacts...");

        var driver = DriverManager.getDriver();

        try {

            /* ---------- Screenshot ---------- */
            logger.info("Capturing screenshot");
            byte[] screenshot = driver.getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment("Failure Screenshot", new ByteArrayInputStream(screenshot));

            /* ---------- Page Source ---------- */
            logger.info("Capturing page source");
            String source = driver.getPageSource();
            Allure.addAttachment("Page Source", "text/xml", source, ".xml");

            /* ---------- Capabilities ---------- */
            logger.info("Capturing device capabilities");
            String caps =
                    "Platform: " + driver.getCapabilities().getPlatformName() + "\n" +
                    "Device: " + driver.getCapabilities().getCapability("deviceName") + "\n" +
                    "Automation: " + driver.getCapabilities().getCapability("automationName");

            Allure.addAttachment("Device Info", caps);

        } catch (Exception e) {
            logger.error("Failed capturing failure artifacts", e);
        }
    }

    @After(order = 0)
    public void tearDown(Scenario scenario) {

        if (scenario.isFailed()) {
            logger.error("SCENARIO FAILED: {}", scenario.getName());
        } else {
            logger.info("SCENARIO PASSED: {}", scenario.getName());
        }

        DriverManager.quit();
        logger.info("========== TEST END ==========\n");
    }
}
