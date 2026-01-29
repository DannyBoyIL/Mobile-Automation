package com.mobileAutomation.driver;

import io.appium.java_client.AppiumDriver;

public class DriverManager {

    private static final ThreadLocal<AppiumDriver> DRIVER = new ThreadLocal<>();
    // TODO add Logger helper to support debugging process

    public static void setDriver(AppiumDriver driver) {
        DRIVER.set(driver);
    }

    public static AppiumDriver getDriver() {
        return DRIVER.get();
    }

    public static void unload() {
        DRIVER.remove();
    }
}
