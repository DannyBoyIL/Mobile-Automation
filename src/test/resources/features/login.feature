Feature: Login to protected area

  As a user
  I want to log in
  So that I can access the secret area

  Background:
    Given the application is launched
    And the user navigates to the login screen
    And the user is on the login screen

  @login @positive
  Scenario: Login with valid credentials
    When the user logs in with username "alice" and password "mypassword"
    Then the secret area should be displayed
    And the logged in user should be "alice"

  @login @negative
  Scenario Outline: Login with invalid credentials
    When the user logs in with username "<username>" and password "<password>"
    Then an invalid login alert should be shown

    Examples:
      | username | password   |
      | foo      | bar        |
      | alice    | wrong      |
      |          | mypassword |
