package com.mobileAutomation.runners;

import com.mobileAutomation.config.AndroidConfig;
import com.mobileAutomation.config.DeviceConfig;
import com.mobileAutomation.dataproviders.UserDataProvider;
import com.mobileAutomation.driver.DriverManager;
import com.mobileAutomation.flows.LoginFlow;
import com.mobileAutomation.flows.LoginResult;
import com.mobileAutomation.pages.InvalidLoginDialog;
import com.mobileAutomation.pages.SecretPage;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AndroidRunner extends DriverManager {

    private LoginFlow loginFlow;

    private static final Logger logger = LoggerFactory.getLogger(AndroidRunner.class);

    @BeforeClass
    @Parameters({"appiumPort", "udid", "systemPort"})
    public void setup(String appiumPort, String udid, String systemPort) throws MalformedURLException {
        AndroidConfig config = DeviceConfig.load("android.json", AndroidConfig.class);

        UiAutomator2Options options = new UiAutomator2Options()
                .setAutomationName(config.automationName)
                .setDeviceName(config.deviceName)
                .setPlatformVersion(config.platformVersion)
                .setUdid(udid)
                .setSystemPort(Integer.parseInt(systemPort))
                .setApp(System.getProperty("user.dir") + config.app)
                .setDisableSuppressAccessibilityService(true)
                .setDisableWindowAnimation(true)
                .setAutoGrantPermissions(true)
                .amend("disableAutofill", true);

        setDriver(new AndroidDriver(
                new URL("http://127.0.0.1:" + appiumPort),
                options
        ));

        loginFlow = new LoginFlow();
        mobile().activateApp(ANDROID_APP_ID);
    }

    @BeforeMethod
    public void resetApp() {
        Map<String, Object> args = new HashMap<>();
        args.put("appId", "com.appiumpro.the_app");
        getDriver().executeScript("mobile: clearApp", args);

        mobile().activateApp(ANDROID_APP_ID);
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
        //unload();
        quit();
    }
}
