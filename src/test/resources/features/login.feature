Feature: User login authentication

  As a registered (or unregistered) user
  I want to log in using the login form
  So that I can access restricted areas of the application

  Background:
    Given the application is launched
    And the user navigates to the Login screen

  @login @smoke
  Scenario Outline: User attempts to log in with different credentials
    When the user enters username "<username>" and password "<password>"
    And submits the login form
    Then the login result should be "<result>"

    Examples:
      | username | password | result  |
      | alice    | 123456   | success |
      | bob      | wrong    | error   |
      |          | 123456   | error   |
      | alice    |          | error   |
      |          |          | error   |