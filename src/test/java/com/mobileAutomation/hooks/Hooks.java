package com.mobileAutomation.hooks;

import com.mobileAutomation.driver.DriverFactory;
import com.mobileAutomation.driver.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;

public class Hooks {

    @Before
    public void setUp() {
        if (DriverManager.getDriver() == null) {
            DriverManager.setDriver(DriverFactory.createDriver());
        }
    }

    @After
    public void tearDown() {
        if (DriverManager.getDriver() != null) {
            DriverManager.getDriver().quit();
            DriverManager.unload();
        }
    }
}
