package com.mobileAutomation.hooks;

import com.mobileAutomation.config.DeviceConfig;
import com.mobileAutomation.config.IOSConfig;
import com.mobileAutomation.driver.DriverFactory;
import com.mobileAutomation.driver.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.Map;

public class Hooks {

    private static final Logger logger = LoggerFactory.getLogger(Hooks.class);
    private static final boolean CI_SINGLE_SESSION = "true".equalsIgnoreCase(System.getenv("CI_SINGLE_SESSION"));
    private static String cachedIosBundleId;

    @Before
    public void setUp(Scenario scenario) {

        logger.info("========== TEST START ==========");
        logger.info("Scenario: {}", scenario.getName());

        if (DriverManager.getDriver() == null) {
            logger.info("Creating mobile driver session...");
            DriverManager.setDriver(DriverFactory.createDriver());
            logger.info("Driver session created successfully");
        } else {
            logger.warn("Driver already exists! Reusing session");
            if (shouldReuseSession()) {
                activateIosAppIfNeeded();
            }
        }
    }

    @AfterStep
    public void attachArtifacts(Scenario scenario) {

        if (!scenario.isFailed()) {
            return;
        }

        logger.error("Step failed! Capturing diagnostic artifacts...");

        var driver = DriverManager.getDriver();
        if (driver == null) {
            logger.error("Driver is null — cannot capture artifacts");
            return;
        }

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

        if (DriverManager.getDriver() != null) {
            if (shouldReuseSession()) {
                logger.info("Reusing session (CI iOS). Resetting app state...");
                resetIosAppState();
            } else {
                logger.info("Closing mobile driver session...");
                DriverManager.getDriver().quit();
                DriverManager.unload();
                logger.info("Driver session closed");
            }
        } else {
            logger.warn("Driver already null during teardown");
        }

        logger.info("========== TEST END ==========\n");
    }

    @AfterAll
    public static void tearDownSuite() {
        if (!shouldReuseSession()) {
            return;
        }
        if (DriverManager.getDriver() == null) {
            return;
        }
        try {
            logger.info("Suite finished. Closing reused iOS driver session...");
            DriverManager.getDriver().quit();
            logger.info("Reused iOS driver session closed");
        } catch (Exception e) {
            logger.warn("Failed to close reused iOS session at suite end", e);
        } finally {
            DriverManager.unload();
        }
    }

    private static boolean shouldReuseSession() {
        return CI_SINGLE_SESSION && isIosPlatform();
    }

    private static boolean isIosPlatform() {
        String platform = System.getProperty("platform");
        if (platform == null || platform.isBlank()) {
            platform = System.getenv("PLATFORM");
        }
        return platform != null && platform.equalsIgnoreCase("ios");
    }

    private static String iosBundleId() {
        if (cachedIosBundleId != null) {
            return cachedIosBundleId;
        }
        IOSConfig config = DeviceConfig.load("ios.json", IOSConfig.class);
        cachedIosBundleId = config.bundleId;
        return cachedIosBundleId;
    }

    private static void activateIosAppIfNeeded() {
        try {
            String bundleId = iosBundleId();
            if (bundleId == null || bundleId.isBlank()) {
                logger.warn("iOS bundleId not configured; cannot activate app");
                return;
            }
            DriverManager.getDriver().executeScript("mobile: activateApp", Map.of("bundleId", bundleId));
        } catch (Exception e) {
            logger.warn("Failed to activate iOS app before scenario", e);
        }
    }

    private static void resetIosAppState() {
        try {
            String bundleId = iosBundleId();
            if (bundleId == null || bundleId.isBlank()) {
                logger.warn("iOS bundleId not configured; cannot reset app");
                return;
            }
            DriverManager.getDriver().executeScript("mobile: terminateApp", Map.of("bundleId", bundleId));
            DriverManager.getDriver().executeScript("mobile: activateApp", Map.of("bundleId", bundleId));
        } catch (Exception e) {
            logger.warn("Failed to reset iOS app state", e);
        }
    }
}
