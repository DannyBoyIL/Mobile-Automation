# Mobile Automation Framework (Java · Appium · TestNG · Cucumber)
A portfolio-style mobile automation framework demonstrating production-grade structure, reporting, and stability patterns using a **realistic login flow** with multiple outcomes.

This project serves as a comprehensive example of a modern mobile automation framework. It demonstrates a full end-to-end implementation of best practices used in professional QA automation.

## Feature Under Test
**Login** is intentionally chosen because it is simple in scope but rich in behavior. This project validates **four scenarios**:
* Valid credentials → user reaches the secret area and identity is verified.
* Invalid credentials → error/alert is shown.
* Wrong password → error/alert is shown.
* Empty username → error/alert is shown.

The result is a compact suite that still exercises synchronization, page flows, and robust assertions.

## Highlights
* **Page Object Model + flow orchestration** for clean separation of concerns, and maintainable test architecture.
* **Behaviour-Driven Development (BDD) with Cucumber** for readable, business-readable scenarios.
* **TestNG orchestration** with extensible runners.
* **CI/CD integration** for automated test execution in pipelines for iOS (macOS) and Android (Linux).
* **Allure reporting** for clear test insights.
* **Structured logging** for traceability and debugging.
* **Failure-proofing techniques** to increase test stability and reduce flakiness.
* **Defensive UI handling** for variable login outcomes (alerts, invalid states).
* **Extensible configuration** via JSON device profiles.
* **Modular design** with POM + flows + steps.
* **Parallel execution** *(available on the `development` branch)*.

The repository provides reference implementations for all these components. Use this project as guidance when building or improving your own automation framework. Instead of copying code directly, explore the structure, patterns, and design decisions to understand how each part contributes to a scalable and robust automation setup.

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

## Project Structure
```text
mobile-automation/
│
├── .github/workflows/                  # CI pipelines
├── apps/                               # Demo apps (iOS/Android)
├── allure-results/                     # Allure result files
├── logs/                               # Local logs (if enabled)
├── src/test/java/com/mobileAutomation/
│   ├── assertions/                     # Custom assertions
│   ├── config/                         # Config loaders
│   ├── driver/                         # Driver factory and manager
│   ├── flows/                          # Flow orchestration
│   ├── hooks/                          # Cucumber hooks
│   ├── pages/                          # Page Object Model
│   ├── runners/                        # TestNG runners
│   └── steps/                          # Cucumber step definitions
├── src/test/resources/
│   ├── config/                         # Device configs (android.json, ios.json)
│   └── features/                       # BDD .feature files
│
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

### Parallel Execution (Development Branch)
**Parallel test execution is implemented on the `development` branch.** If you want parallel runs, switch to that branch and follow the branch-specific README instructions.

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
