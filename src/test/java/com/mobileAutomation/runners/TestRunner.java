package com.mobileAutomation.runners;

import org.testng.annotations.DataProvider;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
        features = "src/test/resources/features",
        glue = {
                "com.mobileAutomation.steps",
                "com.mobileAutomation.hooks"
        },
        plugin = {
                "pretty",
                "summary"
        },
        tags = "@login"
)
public class TestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
