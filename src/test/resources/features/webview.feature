Feature: WebView URL navigation

  As a user exploring the Webview Demo screen
  I want only whitelisted URLs to load
  So that the app blocks unexpected external content inside the WebView

  @webview @positive
  Scenario: Navigate to the allowed URL
    Given the user is on the Webview Demo screen
    When the user navigates to "https://appiumpro.com"
    Then the WebView should load the Appium Pro homepage

  @webview @positive
  Scenario: The URL whitelist match is case-insensitive
    Given the user is on the Webview Demo screen
    When the user navigates to "https://APPIUMPRO.COM"
    Then the WebView should load the Appium Pro homepage

  @webview @negative
  Scenario Outline: Rejected URL variants trigger the not-allowed alert
    Given the user is on the Webview Demo screen
    When the user navigates to "<url>"
    Then the not-allowed alert should be shown
    And dismissing the alert should keep the WebView on the initial page

    Examples:
      | url                    |
      | https://example.com    |
      | http://appiumpro.com   |
      | https://appiumpro.com/ |

  @webview @positive
  Scenario: Clear button empties the URL input
    Given the user is on the Webview Demo screen
    When the user types "some garbage text" into the URL field
    And the user taps Clear
    Then the URL input should be empty

  @webview @framework-sanity
  Scenario: WebView elements can be driven from the test suite
    Given the user is on the Webview Demo screen
    When the user navigates to "https://appiumpro.com"
    And the user enters "demo@example.com" into the WebView email field
    Then the WebView email field should contain "demo@example.com"

  @webview @regression
  Scenario: Valid navigation still succeeds after recovering from a blocked URL
    Given the user is on the Webview Demo screen
    When the user navigates to "https://example.com"
    Then the not-allowed alert should be shown
    And dismissing the alert should keep the WebView on the initial page
    When the user navigates to "https://appiumpro.com"
    Then the WebView should load the Appium Pro homepage
