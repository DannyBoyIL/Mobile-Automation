package com.mobileAutomation.runners;

import com.mobileAutomation.config.DeviceConfig;
import com.mobileAutomation.config.IOSConfig;
import com.mobileAutomation.dataproviders.UserDataProvider;
import com.mobileAutomation.driver.DriverManager;
import com.mobileAutomation.flows.LoginFlow;
import com.mobileAutomation.flows.LoginResult;
import com.mobileAutomation.pages.InvalidLoginDialog;
import com.mobileAutomation.pages.SecretPage;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.Assert;
import org.testng.annotations.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class IOSRunner extends DriverManager {

    private LoginFlow loginFlow;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(IOSRunner.class);

    @BeforeClass
    @Parameters({"appiumPort", "udid", "wdaLocalPort"})
    public void setup(String appiumPort, String udid, String wdaLocalPort) throws MalformedURLException {
        IOSConfig config = DeviceConfig.load("ios.json", IOSConfig.class);

        DesiredCapabilities options = new DesiredCapabilities();
        options.setCapability("platformName", "iOS");
        options.setCapability("appium:automationName", "XCUITest");

        options.setCapability("appium:udid", udid);
        options.setCapability("appium:deviceName", udid);
        options.setCapability("appium:platformVersion", config.platformVersion);

        // installs app ONCE at session start
        options.setCapability("appium:app",
                System.getProperty("user.dir") + config.app);

        /* ---------- PARALLEL STABILITY (CRITICAL) ---------- */

        options.setCapability("appium:wdaLocalPort", Integer.parseInt(wdaLocalPort));
        options.setCapability("appium:derivedDataPath",
                System.getProperty("user.dir") + "/wda/" + udid);

        /* ---------- THE REAL FIX ---------- */

        // DO NOT allow WDA rebuild mid-suite
        options.setCapability("appium:usePrebuiltWDA", true);

        // prevents Xcode internal deadlocks in parallel runs
        options.setCapability("appium:shouldUseSingletonTestManager", false);

        // removes long idle waits that freeze sessions
        options.setCapability("appium:waitForQuiescence", false);

        // we handle resets manually
        options.setCapability("appium:noReset", false);
        options.setCapability("appium:fullReset", false);

        /* ---------- QUALITY OF LIFE ---------- */

        options.setCapability("appium:autoAcceptAlerts", true);
        options.setCapability("appium:newCommandTimeout", 300);


        setDriver(new IOSDriver(new URL("http://127.0.0.1:" + appiumPort), options));

        loginFlow = new LoginFlow();
    }

    @BeforeMethod
    public void prepareState() {
        // optional: implement a logout/home navigation flow here if needed
        logger.info("Preparing app state for next test");
        terminateApp();
        activateApp();
    }

    private void terminateApp() {
        getDriver().executeScript("mobile: terminateApp", Map.of("bundleId", IOS_BUNDLE_ID));
    }

    private void activateApp() {
        getDriver().executeScript("mobile: activateApp", Map.of("bundleId", IOS_BUNDLE_ID));
    }

    @Test(dataProvider = "usersData", dataProviderClass = UserDataProvider.class)
    public void runTests(String username, String password) {

        logger.info("STEP: User logs in as '{}'", username);
        LoginResult loginResult = loginFlow.login(username, password);

        if (loginResult instanceof LoginResult.Invalid invalid) {
            logger.info("STEP: Expect invalid login alert");

            InvalidLoginDialog dialog = invalid.dialog();
            if (dialog.isVisible()) {
                dialog.accept();
            } else {
                logger.info("Invalid login alert not visible; staying on login page");
            }
        } else {
            logger.info("STEP: Verifying secret area is displayed");

            LoginResult.Success success = (LoginResult.Success) loginResult;
            SecretPage secretPage = success.page();
            Assert.assertTrue(secretPage.isVisible());
            Assert.assertTrue(secretPage.loggedInUserText().contains(username), "Expected username to be displayed");
            secretPage.logOut();
        }
    }

    @AfterClass
    public void teardown() {
        quit();
    }
}
