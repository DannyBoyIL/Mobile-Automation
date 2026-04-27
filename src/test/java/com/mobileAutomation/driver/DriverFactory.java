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

    private static final String APPIUM_URL =
            System.getProperty("appium.url", "http://localhost:4723");

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
                .amend("adbExecTimeout", 120000)
                .amend("androidInstallTimeout", 180000)
                .amend("uiautomator2ServerLaunchTimeout", 120000)
                .amend("skipServerInstallation", true)
                .amend("skipDeviceInitialization", true)
                .amend("skipInstall", true)
                .amend("chromedriverAutodownload", true);

        String chromedriverPathOverride = System.getProperty("chromedriver.path");
        if (chromedriverPathOverride != null && !chromedriverPathOverride.isBlank()) {
            options.amend("chromedriverExecutable", chromedriverPathOverride);
        }

        return new AndroidDriver(new URL(APPIUM_URL), options);
    }

    private static AppiumDriver createIOSDriver() throws MalformedURLException {
        IOSConfig config = DeviceConfig.load("ios.json", IOSConfig.class);
        boolean ciSingleSession = "true".equalsIgnoreCase(System.getenv("CI_SINGLE_SESSION"));

        DesiredCapabilities options = new DesiredCapabilities();
        options.setCapability("platformName", config.platformName);
        options.setCapability("appium:automationName", config.automationName);
        options.setCapability("appium:deviceName", config.deviceName);
        options.setCapability("appium:platformVersion", config.platformVersion);
        options.setCapability("appium:app", System.getProperty("user.dir") + config.app);
        options.setCapability("appium:bundleId", config.bundleId);

        // CI capabilities
        options.setCapability("appium:wdaLaunchTimeout", 180000);
        options.setCapability("appium:wdaStartupRetries", 2);
        options.setCapability("appium:wdaStartupRetryInterval", 20000);
        options.setCapability("appium:useNewWDA", false);

        // Keep existing local behavior unless CI explicitly enables single-session mode.
        options.setCapability("appium:noReset", ciSingleSession);
        options.setCapability("appium:fullReset", false);

        // Keyboard handling. iOS Simulators store "Connect Hardware Keyboard" per
        // device, and the default differs between models (iPhone 17 / iOS 26.1 ships
        // with it ON; iPhone 16e ships with it OFF). When the hardware keyboard is
        // connected, iOS hides the on-screen keyboard and `sendKeys` drives the Mac
        // keyboard at a speed `secureTextEntry` fields can't always commit fast
        // enough — characters get dropped or routed to the previously-focused field,
        // which surfaces as "Login with valid credentials" being rejected with
        // <Invalid> on iPhone 17 but passing on iPhone 16e (the only difference is
        // this setting).
        //
        // Forcing the software keyboard makes the behavior identical across models:
        //   - connectHardwareKeyboard:false        — XCUITest sets the simulator
        //     pref so the soft keyboard appears regardless of local default.
        //   - forceTurnOnSoftwareKeyboard:true     — belt-and-suspenders for cases
        //     where the prefs file got reset (Erase All Content & Settings, fresh
        //     CI runner image, etc.).
        //
        // NOTE: do NOT set `appium:autoDismissAlerts:true` here. It races XCUITest's
        // alert monitor against the test's own alert assertions: on `@login @negative`
        // scenarios the "Invalid login credentials" native alert is sometimes
        // dismissed by the monitor before LoginFlow can detect it, leaving the flow
        // to throw IllegalStateException after both isVisible() polls expire. If a
        // "Save Password / Not Now" autofill prompt ever does appear between typing
        // and submit, handle it explicitly at the LoginPage level instead.
        options.setCapability("appium:connectHardwareKeyboard", false);
        options.setCapability("appium:forceTurnOnSoftwareKeyboard", true);

        // WebView discovery tuning. XCUITest defaults (10 retries × 5000ms) are often
        // tight for a cold-start simulator where WebKit's inspector takes a beat to
        // attach after the WKWebView mounts. These values only affect how patiently
        // Appium looks for WEBVIEW_* handles — no effect on native-only tests.
        //
        // IMPORTANT: none of this helps if the app's WKWebView isn't inspectable in
        // the first place. Since iOS 16.4, WKWebView is only remote-debuggable when
        // the app sets `webView.isInspectable = true` in native code. Verify the
        // TheApp build you're running has this set for iOS sanity / Cucumber runs.
        options.setCapability("appium:webviewConnectRetries", 30);
        options.setCapability("appium:webviewConnectTimeout", 10_000);
        options.setCapability("appium:includeSafariInWebviews", true);

        // The XCUITest driver matches inspector pages against a hardcoded list of
        // bundle IDs (com.apple.WebKit.WebContent, com.apple.SafariViewService,
        // the app's own bundleId, com.apple.mobilesafari, plus any IDs supplied
        // here). Web Inspector reports React-Native host apps with the bundleId
        // `process-<ExecutableName>` rather than the CFBundleIdentifier — for
        // TheApp that's `process-TheApp` — so without this hint the driver
        // ignores TheApp's pages and latches onto the WebContent helper process,
        // which has no pageArray, leading to "No available web pages after N
        // retries" and only NATIVE_APP being returned. See appium log:
        //   "You may also consider providing more values to
        //    'additionalWebviewBundleIds' capability to match other applications."
        // Using the literal value rather than "*" to avoid grabbing unrelated
        // helper processes.
        options.setCapability("appium:additionalWebviewBundleIds",
                java.util.List.of("process-TheApp"));

        return new IOSDriver(new URL(APPIUM_URL), options);
    }
}
