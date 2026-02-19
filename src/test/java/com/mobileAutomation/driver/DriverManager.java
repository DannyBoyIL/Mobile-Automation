package com.mobileAutomation.driver;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.InteractsWithApps;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DriverManager {

    public static final String ANDROID_APP_ID = "com.appiumpro.the_app";
    public static final String IOS_BUNDLE_ID = "com.appiumpro.the_app";

    private static final ThreadLocal<String> platform = ThreadLocal.withInitial(() -> "UNKNOWN");
    private static final ThreadLocal<AppiumDriver> driver = new ThreadLocal<>();
    private static final Logger logger = LoggerFactory.getLogger(DriverManager.class);

    public static void setDriver(AppiumDriver appiumDriver) {
        setPlatform(appiumDriver);
        logger.info("Starting {} driver on thread {}", platform.get(), Thread.currentThread().getId());
        driver.set(appiumDriver);
    }

    public static AppiumDriver getDriver() {
        return driver.get();
    }

    public static InteractsWithApps mobile() {
        return (InteractsWithApps) getDriver();
    }

    public static void quit() {
        if (driver.get() != null) {
            logger.info("Quiting {} driver on thread {}", platform.get(), Thread.currentThread().getId());
            driver.get().quit();
            driver.remove();
        }
    }

    private static void setPlatform(AppiumDriver appiumDriver) {
        if (appiumDriver instanceof AndroidDriver) {
            platform.set("Android");
        } else if (appiumDriver instanceof IOSDriver) {
            platform.set("iOS");
        }
    }
}
