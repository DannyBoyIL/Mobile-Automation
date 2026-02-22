package com.mobileAutomation.driver;

import com.mobileAutomation.config.AndroidConfig;
import com.mobileAutomation.config.DeviceConfig;
import com.mobileAutomation.config.IOSConfig;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.MalformedURLException;
import java.net.URL;

public class DriverFactory {

    // TODO remove or change for the parallel jobs
    private static final String APPIUM_URL = "http://localhost:4723";

    public static AppiumDriver createDriver() {
        Platform platform = Platform.fromString(System.getProperty("platform"));

        try {
            return switch (platform) {
                case ANDROID -> createAndroidDriver();
                case IOS -> createIOSDriver();
            };
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Appium server URL", e);
        }
    }

    private static AppiumDriver createAndroidDriver() throws MalformedURLException {
        AndroidConfig config = DeviceConfig.load("android.json", AndroidConfig.class);

        UiAutomator2Options options = new UiAutomator2Options()
                .setAutomationName(config.automationName)
                .setDeviceName(config.deviceName)
                .setPlatformVersion(config.platformVersion)
                .setApp(System.getProperty("user.dir") + config.app)
                .setDisableSuppressAccessibilityService(true)
                .setDisableWindowAnimation(true)
                .setAutoGrantPermissions(true)
                .amend("disableAutofill", true)
                .amend("uiautomator2ServerInstallTimeout", 120000)
                .amend("adbExecTimeout", 120000);

        return new AndroidDriver(new URL(APPIUM_URL), options);
    }

    private static AppiumDriver createIOSDriver() throws MalformedURLException {
        IOSConfig config = DeviceConfig.load("ios.json", IOSConfig.class);

        DesiredCapabilities options = new DesiredCapabilities();
        options.setCapability("platformName", config.platformName);
        options.setCapability("appium:automationName", config.automationName);
        options.setCapability("appium:deviceName", config.deviceName);
        options.setCapability("appium:platformVersion", config.platformVersion);
        options.setCapability("appium:app", System.getProperty("user.dir") + config.app);

        // CI capabilities
        options.setCapability("appium:wdaLaunchTimeout", 180000);
        options.setCapability("appium:wdaStartupRetries", 2);
        options.setCapability("appium:wdaStartupRetryInterval", 20000);

        options.setCapability("noReset", false);
        options.setCapability("fullReset", false);

        return new IOSDriver(new URL(APPIUM_URL), options);
    }
}
