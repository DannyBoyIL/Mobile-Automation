# Mobile Automation Framework (Java · Appium · TestNG · Cucumber)
This automation project for mobile applications contains example code for the
*Behavior-Driven Java with Cucumber, TestNG & Appium*.


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
appium --log-level warn
```
Run tests on iOS:
```bash
# 5. Run iOS tests
mvn test -Dplatform=ios

# Optional: generate Allure report
allure serve allure-results
```
Run tests on Android:
```bash
# 5. Run Android tests
mvn test -Dplatform=android

# Optional: generate Allure report
allure serve allure-results
```


## Repository Purpose
This project serves as a comprehensive example of a modern mobile automation framework. It demonstrates a full end-to-end implementation of best practices used in professional QA automation, including:

* __Page Object Model (POM)__ for clean, maintainable test architecture.
* __Behaviour-Driven Development (BDD)__ using readable, business-oriented scenarios.
* __TestNG runner integration__ for suite orchestration and reporting.
* __CI/CD integration__ for automated test execution in pipelines.
* __Allure Reporting__ for rich, visual test reports.
* __Extensive logging__ to support debugging and traceability.
* __Failure-proofing techniques__ to increase test stability and reduce flakiness.

The repository provides reference implementations for all these components. Use this project as guidance when building or improving your own automation framework. Instead of copying code directly, explore the structure, patterns, and design decisions to understand how each part contributes to a scalable and robust automation setup.

### Project Structure
```text
mobile-automation/
│
├── .github/                                # CI/CD configuration
│   └── workflows/                          # CI pipelines
├── allure-results/                         # Allure result files
├── apps/                                   # Demo apps (iOS/Android)
│   ├── android/
│   └── ios/
├── logs/                                   # Local logs (if enabled)
├── src/
│   ├── main/
│   └── test/
│       ├── java/
│       │   └── com/mobileAutomation/
│       │       ├── assertions/             # Custom assertions
│       │       ├── config/                 # Config loaders
│       │       ├── driver/                 # Driver factory and manager
│       │       ├── flows/                  # Flow orchestration
│       │       ├── hooks/                  # Cucumber hooks
│       │       ├── pages/                  # Page Object Model
│       │       ├── runners/                # TestNG runners
│       │       └── steps/                  # Cucumber step definitions
│       └── resources/
│           ├── config/                     # Device configs
│           ├── features/                   # BDD .feature files
│           ├── logback-test.xml            # Logging config (tests)
│           ├── testng-mobile-parallel.xml  # TestNG config for parallel execution
│           └── users-data.json             # Test data
├── .gitignore
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
This project uses Maven, TestNG, and Cucumber to run the automation suite.

### iOS Execution
Update iOS device config:
```bash
cat src/test/resources/config/ios.json
```
Run:
```bash
mvn test -Dplatform=ios
```

### Android Execution
Update Android device config:
```bash
cat src/test/resources/config/android.json
```
Run:
```bash
mvn test -Dplatform=android
```

### Parallel Execution
Parallel execution uses the `mobile-parallel` Maven profile, which runs the suite defined in `src/test/resources/testng-mobile-parallel.xml`.

Before running in parallel, start one Appium server per device. The current parallel setup expects multiple servers on ports `10000`, `10001`, `10002`.

Example (3 servers):
```bash
appium --port 10000 &
appium --port 10001 &
appium --port 10002
```

Run parallel iOS:
```bash
mvn test -Pmobile-parallel
```

Note: `src/test/resources/testng-mobile-parallel.xml` defines how devices are distributed across threads. Update that file to add or remove devices, and make sure each device is mapped to a unique Appium port.

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

__Where to find WDA logs__:
* Local Appium logs: `logs/appium-test.log` (if enabled)
* CI Appium logs: uploaded as workflow artifacts when a job fails
* Simulator system logs: `Console.app` → filter by `WebDriverAgent` or `XCTest`

### Allure Report Empty
__Symptom__: Allure opens a report with 0 tests.

__Fix__:
* Ensure tests ran before generating the report
* Verify `allure-results` exists

```bash
ls -la allure-results
```
