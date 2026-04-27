# Mobile Automation Framework (Java · Appium · TestNG · Cucumber)
A portfolio-style mobile automation framework demonstrating production-grade structure, reporting, and stability patterns using a **realistic login flow** with multiple outcomes.

## Features Under Test
The suite covers two flows in TheApp:

**Login** — chosen because it is simple in scope but rich in behaviour:
* Valid credentials → user reaches the secret area and identity is verified.
* Invalid credentials → error/alert is shown.
* Wrong password → error/alert is shown.
* Empty username → error/alert is shown.

**WebView Demo** — exercises the harder parts of the framework (native ↔ WebView context switching, URL allow-list validation, hybrid form interaction):
* Navigate to an allowed URL → WebView loads and DOM/title are asserted from inside the WebView context.
* Whitelist match is case-insensitive.
* Rejected URL variants (disallowed host, wrong scheme, trailing-slash mismatch) trigger the not-allowed native alert.
* Clear button empties the URL input.
* Type into a WebView form field and read the value back through the inspector.
* Recovery: a valid navigation succeeds after dismissing a previously-blocked alert.

A standalone Failsafe IT (`WebViewContextSanityIT`) also lives outside the Cucumber flow as a pre-flight diagnostic — it proves the native ↔ WebView context toggle works end-to-end before any feature runs.

The result is a compact suite that still exercises synchronisation, page flows, robust assertions, and hybrid-app context handling.

## Highlights
* **Page Object Model + flow orchestration** for clean separation of concerns, and maintainable test architecture.
* **Behavior-Driven Development (BDD) with Cucumber** for readable, business-readable scenarios.
* **TestNG orchestration** with extensible runners.
* **CI/CD integration** for automated test execution in pipelines for iOS (macOS) and Android (Linux).
* **Allure reporting** for clear test insights.
* **Structured logging** for traceability and debugging.
* **Failure-proofing techniques** to increase test stability and reduce flakiness.
* **Defensive UI handling** for variable login outcomes (alerts, invalid states).
* **Extensible configuration** via JSON device profiles.
* **Modular design** with POM + flows + steps.
* **Parallel execution** *(available on the `development` branch)*.

## Quick Start
For users who want to run the project quickly:
```bash
# 1. Clone repo
git clone <repo-url>
cd mobile-automation

# 2. Install dependencies
mvn -q -DskipTests install

# 3. Install Appium + driver
npm install -g appium
appium driver install uiautomator2

# 4. Start Appium manually
#    The chromedriver auto-download flag is needed for any Android @webview
#    scenario; harmless for iOS-only runs. Appium 3.x requires the prefixed
#    feature name (the unprefixed `chromedriver_autodownload` is rejected).
appium --log-level warn --allow-insecure uiautomator2:chromedriver_autodownload

#    Or, if you want all installed drivers to share the same trust list:
#    appium --log-level warn --allow-insecure '*:chromedriver_autodownload'

# 5. Provide login credentials
#    The login Cucumber suite reads credentials from env vars (or a
#    gitignored .env file at the project root). For the bundled TheApp,
#    the demo values are alice / mypassword — see `Credentials` below.
cp .env.example .env
# then edit .env (alice / mypassword for TheApp)
```
Run tests on iOS:
```bash
# 5. Run the Cucumber suite on iOS (login + webview features)
mvn -Pcucumber test -Dplatform=ios

# Optional: generate Allure report
allure serve allure-results
```
Run tests on Android:
```bash
# 5. Run the Cucumber suite on Android
mvn -Pcucumber test -Dplatform=android

# Optional: generate Allure report
allure serve allure-results
```

`mvn test` by itself is intentionally a no-op — see [Test Suites and Build Phases](#test-suites-and-build-phases) for why.

## Example Test
The following step definitions show the full login flow — success and failure paths — using the sealed interface pattern for typed outcomes:

```java
@When("the user logs in with username {string} and password {string}")
public void login(String user, String pw) {
    loginResult = loginFlow.login(user, pw);
}

@Then("the secret area should be displayed")
public void secretArea() {
    // LoginResult is a sealed interface — assertInstanceOf enforces the expected outcome type
    LoginResult.Success success = assertInstanceOf(LoginResult.Success.class, loginResult);
    secretPage = success.page();
    Assert.assertTrue(secretPage.isVisible());
}

@And("the logged in user should be {string}")
public void verifyUser(String username) {
    Assert.assertTrue(
        secretPage.loggedInUserText().contains(username),
        "Expected username to be displayed"
    );
    secretPage.logOut();
}

@Then("an invalid login alert should be shown")
public void invalidLogin() {
    // Wrong outcome type at runtime = instant, descriptive failure
    LoginResult.Invalid invalid = assertInstanceOf(LoginResult.Invalid.class, loginResult);
    InvalidLoginAlert alert = invalid.alert();
    Assert.assertTrue(alert.isVisible());
    alert.dismiss();
}
```

`LoginResult` is a Java 17 sealed interface with two permitted subtypes — `Success` and `Invalid`. If the app returns the wrong outcome, `assertInstanceOf` fails with a clear message naming both types, rather than a null pointer or a silent wrong-state assertion.

## Project Structure
```text
mobile-automation/
│
├── .github/workflows/                          # CI pipelines (ios-tests.yml, android-tests.yml)
├── apps/                                       # Demo apps (iOS .app, Android .apk)
├── allure-results/                             # Allure result files
├── logs/                                       # Local logs (if enabled)
├── src/test/java/com/mobileAutomation/
│   ├── assertions/                             # Custom assertions
│   ├── config/                                 # Config loaders
│   ├── credentials/                            # Alias-based credentials store (env / .env)
│   ├── driver/                                 # Driver factory and manager
│   ├── flows/                                  # Flow orchestration
│   ├── hooks/                                  # Cucumber hooks
│   ├── pages/                                  # Page Object Model
│   ├── runners/                                # TestNG runners
│   ├── steps/                                  # Cucumber step definitions
│   └── tests/                                  # Standalone Failsafe ITs (e.g. WebViewContextSanityIT)
├── src/test/resources/
│   ├── config/                                 # Device configs (android.json, ios.json)
│   └── features/                               # BDD .feature files
│
├── .env.example                                # Template for local credentials (copy to .env)
├── pom.xml
└── README.md
```

## Java & Appium Setup
This section documents how to set up your machine for Appium-based mobile test automation development.
<details> <summary><strong>Read more..</strong></summary>

### Java and Maven
This project requires Java 17 and Maven.

Verify versions:
```bash
java -version
mvn -version
```

### Node.js and Appium
Appium is installed via npm:
```bash
node -v
npm -v
npm install -g appium
```
Install the Android driver:
```bash
appium driver install uiautomator2
```

### iOS Requirements
macOS is required for iOS automation.
You must have:
* Xcode installed
* Command Line Tools installed
* iOS Simulator available

Verify Xcode:
```bash
xcodebuild -version
xcrun simctl list devices
```

### Android Requirements
Android automation requires the Android SDK and an emulator or device.
Verify SDK tools:
```bash
adb version
emulator -version
```
</details>

## Credentials
The login Cucumber suite never embeds real credentials in `login.feature`. Each scenario speaks in **aliases** (`"the valid user"`, `"the unknown user"`, `"the wrong-password user"`, `"the empty-username user"`), and `CredentialsStore` resolves each alias to a username/password pair at step time.

**What's externalized vs. inline.** Only the valid user is treated as a secret — its values are loaded from environment variables / `.env`. The three negative-test aliases live as inline fixtures in `CredentialsStore`:
* `the unknown user` → `"foo"` / `"bar"` (synthetic, no real account)
* `the wrong-password user` → `LOGIN_VALID_USER` / `"wrong"` (reuses the valid username so the fixture stays in sync)
* `the empty-username user` → `""` / `LOGIN_VALID_PASSWORD` (test condition: valid pw, no user)

That keeps the .env file scoped to one real key pair and avoids hiding values that aren't credentials at all.

**Resolution order for the valid user's keys:**
1. Process environment (`System.getenv`)
2. Project-root `.env` file (gitignored; loaded via `dotenv-java` with `ignoreIfMissing`)

Missing keys are fatal at first lookup. Empty values are allowed.

### Test-app credentials (TheApp)
The framework code stays free of embedded credentials, but `.env` still needs values to run. For the bundled [TheApp](https://github.com/cloudgrey-io/the-app) demo app, the publicly-documented sample account is:

| Key | Value |
|---|---|
| `LOGIN_VALID_USER` | `alice` |
| `LOGIN_VALID_PASSWORD` | `mypassword` |

Drop those into your local `.env` to run the suite as-is. `.env.example` carries the same hint block so a fresh clone has the values within reach without grepping the upstream repo. If you point the framework at a different app, replace these with that app's real credentials (in CI: GitHub Secrets), and never commit them.

### Local dev
```bash
cp .env.example .env
# fill in LOGIN_VALID_USER and LOGIN_VALID_PASSWORD, then run the suite normally
mvn -Pcucumber test -Dplatform=ios
```

### CI
Both `.github/workflows/ios-tests.yml` and `.github/workflows/android-tests.yml` inject the same two keys as job-level env vars, sourced from GitHub Actions Secrets. Configure these once at *Settings → Secrets and variables → Actions*:

| Secret | Used for |
|---|---|
| `LOGIN_VALID_USER` | the valid user (positive scenario, reused by wrong-password fixture) |
| `LOGIN_VALID_PASSWORD` | the valid user (positive scenario, reused by empty-username fixture) |

### Adding a new alias
1. Add a new branch to the `switch` in `CredentialsStore.get(...)` (and add the alias name to the `ALIASES` list used in error messages).
2. If the alias needs a new sensitive value, add a new env key, document it in `.env.example`, and add a matching GitHub Actions Secret + workflow env line. If it's purely synthetic test data, just inline the literal in the new `case`.
3. Reference the alias from the `.feature` file. No step-definition changes are needed.

## Allure Setup
Allure Reporting provides rich, visual test reports generated from your framework.
<details> <summary><strong>Read more..</strong></summary>

### Install Allure
macOS (Homebrew):
```bash
brew install allure
```

Windows: Download the Allure ZIP from the official distribution page, extract it, and add the bin folder to your system path.

Linux:
```bash
sudo apt-add-repository ppa:qameta/allure
sudo apt-get update
sudo apt-get install allure
```

Verify installation:
```bash
allure --version
```

### Viewing Test Reports
After running your tests, an Allure results folder will be created. To generate and view the report:
```bash
allure serve allure-results
```
This command builds the report and opens it in your browser.
</details>

## Running Tests
This project uses Maven, TestNG, and Cucumber to run the automation suite. Two complementary entry points exist — the Cucumber suite (Surefire, opt-in via the `cucumber` profile) and a standalone WebView context sanity test (Failsafe, runs in the `verify` phase).

### Test Suites and Build Phases
By design, **`mvn test` runs nothing**. The top-level Surefire configuration sets `<skipTests>true</skipTests>` so a default build never spins up an Appium session. Each suite is opt-in:

| What you want | Command |
|---|---|
| Cucumber suite, iOS | `mvn -Pcucumber test -Dplatform=ios` |
| Cucumber suite, Android | `mvn -Pcucumber test -Dplatform=android` |
| Filter by tag | `mvn -Pcucumber test -Dplatform=ios -Dcucumber.filter.tags=@login` |
| WebView context sanity (IT) | `mvn verify -Dit.test=WebViewContextSanityIT -Dplatform=ios` |
| Parallel multi-device (TestNG XML) | `mvn -Pmobile-parallel test` |

The reason for the split: Surefire's default include pattern (`**/Test*.java`) would otherwise pick up `TestRunner.java` and bring up an Appium session on every `mvn test` or `mvn verify`. The `cucumber` profile re-enables Surefire for the Cucumber runner; the `*IT.java` naming convention routes the WebView sanity test to Failsafe, which only runs in the `integration-test` phase.

### Parallel Execution (Development Branch)
**Parallel test execution is implemented on the `development` branch.** If you want parallel runs, switch to that branch and follow the branch-specific README instructions.

### iOS Execution
Update iOS device config:
```bash
cat src/test/resources/config/ios.json
```
Run:
```bash
mvn -Pcucumber test -Dplatform=ios
```

### Android Execution
Update Android device config:
```bash
cat src/test/resources/config/android.json
```
Run:
```bash
mvn -Pcucumber test -Dplatform=android
```

Android additionally needs Appium started with chromedriver auto-download enabled so the Java WebView's chromedriver is fetched at runtime to match the emulator's WebView Chrome version (Appium 3.x requires the prefixed feature flag):

```bash
appium --log-level warn --allow-insecure uiautomator2:chromedriver_autodownload
```

If you want to pin a specific chromedriver binary instead of using auto-download, pass `-Dchromedriver.path=/abs/path/to/chromedriver` when running Maven.

### CI Execution
GitHub Actions workflows live under:
```bash
.github/workflows/
```
They run iOS on macOS runners and Android on Ubuntu runners.

## Troubleshooting
A collection of common issues and quick fixes for running the mobile automation project.

### Appium Server Not Found
__Symptom__: `SessionNotCreatedException` or `ECONNREFUSED` to Appium server.

__Fix__:
```bash
appium --log-level warn --port 4723
```

### Android Emulator Offline
__Symptom__: `adb: device offline` or boot never completes.

__Fix__:
```bash
adb kill-server
adb start-server
adb devices
```

### iOS WDA Slow Startup
__Symptom__: WDA session takes several minutes.

__Fix__:
* Ensure Xcode is installed and set as active
* Pre-boot the simulator before running tests

```bash
xcrun simctl boot "<device-name>" || true
xcrun simctl bootstatus "<device-name>" -b
```

### Allure Report Empty
__Symptom__: Allure opens a report with 0 tests.

__Fix__:
* Ensure tests ran before generating the report
* Verify `allure-results` exists

```bash
ls -la allure-results
```

### Android `NoSuchContextException: chromedriver only supports Chrome version N / current browser is M`
__Symptom__: A `@webview` scenario fails on Android the first time the suite tries to switch into the WebView, with a chromedriver/WebView version mismatch in the message.

__Cause__: The Play Store updates Android System WebView in the background; the chromedriver shipped with the UiAutomator2 driver is older than the device's current WebView. Pinning a chromedriver version is brittle because the version drift happens silently.

__Fix__: Start Appium with chromedriver auto-download (Appium 3.x requires the prefixed feature flag), and let `DriverFactory` pass `chromedriverAutodownload: true`:

```bash
appium --log-level warn --allow-insecure uiautomator2:chromedriver_autodownload
```

To pin a specific binary (e.g. an offline CI runner), pass `-Dchromedriver.path=/abs/path/to/chromedriver` when running Maven.

### iOS Login `Expected result type <Success> but was <Invalid>` (hardware-keyboard related)
__Symptom__: The valid-credentials login scenario fails intermittently or consistently on certain iOS Simulators, with characters appearing to be dropped or routed to the wrong field. We observed this on **iPhone 17 / iOS 26.1**; the issue can surface on any simulator whose "Connect Hardware Keyboard" preference is set ON, and conversely simulators with it OFF (e.g. our iPhone 16e at the time of the incident) won't reproduce it.

__Cause__: iOS Simulators store the "Connect Hardware Keyboard" preference per device, and the default depends on simulator version, model, and whatever state the prefs file was last left in (Erase All Content & Settings, fresh CI runner image, manual toggle in Hardware → Keyboard, etc.). When the hardware keyboard is connected, iOS hides the on-screen keyboard and `sendKeys` drives the Mac keyboard at a speed that `secureTextEntry` fields can't always commit. Two simulators of identical model and OS can disagree on this.

__Fix__: Already wired into `DriverFactory.createIOSDriver()` via two capabilities — `appium:connectHardwareKeyboard=false` and `appium:forceTurnOnSoftwareKeyboard=true` — which neutralize the per-device drift. If you see this regress, verify those caps are still set, and that you have **not** added `appium:autoDismissAlerts=true` (which causes `@negative` login flakes by racing XCUITest's alert monitor against the test's own alert assertion).

### `mvn test` ran nothing
__Symptom__: `mvn test` completes in seconds, no Appium session, no Cucumber output.

__Cause__: This is intentional — see [Test Suites and Build Phases](#test-suites-and-build-phases). Use `mvn -Pcucumber test -Dplatform=ios` for the Cucumber suite, or `mvn verify -Dit.test=WebViewContextSanityIT -Dplatform=ios` for the standalone IT.
