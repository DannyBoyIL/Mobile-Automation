
# Credentials never appear in this file. Each alias below resolves to a username/password pair via CredentialsStore —
# read from process env vars first, then from a gitignored .env at the project root. See .env.example for the canonical
# key list, and CredentialsStore.java for the alias map.

Feature: Login to protected area

  As a user
  I want to log in
  So that I can access the secret area

  @login @positive
  Scenario: Login with valid credentials
    When the user logs in as "the valid user"
    Then the secret area should be displayed
    And the logged in user should be "the valid user"

  @login @negative
  Scenario Outline: Login with invalid credentials
    When the user logs in as "<user>"
    Then an invalid login alert should be shown

    Examples:
      | user                    |
      | the unknown user        |
      | the wrong-password user |
      | the empty-username user |
