Feature: Login functionality

  Scenario: Login is the first page for the application
    Given the user navigates to the root URL
    Then the user should be redirected to the login page

  Scenario: Successful link to register page
    Given the user is on the login page
    When the user clicks the "Create an Account" link
    Then the user should be redirected to the registration page

  Scenario: Successful Login
    Given the user is on the login page
    When the user enters a valid username in the username field
    And enters a valid password in the password field
    And clicks the "Log In" button
    Then the user should be redirected to the dashboard

  Scenario: Account locks
    Given the user is on the login page
    When the user enters a valid username and fails login three times
    Then the user should see an error message "Account is locked. Try again after 30 seconds."

  Scenario: 30 sec successful login after lockout
    Given the user is on the login page
    When the user locks themself out of their account
    And waits 30 seconds
    And the user enters a valid username in the username field
    And enters a valid password in the password field
    And clicks the "Log In" button
    Then the user should be redirected to the dashboard

  Scenario: Wrong 3 times in 70 seconds no lockout
    Given the user is on the login page
    When the user enters a valid username in the username field
    And enters an incorrect password in the password field three times in 70 sec
    And enters a valid password in the password field
    And clicks the "Log In" button
    Then the user should be redirected to the dashboard

  Scenario: Failed Login with incorrect username
    Given the user is on the login page
    When the user enters an incorrect username in the username field
    And enters a valid password in the password field
    And clicks the "Log In" button
    Then the user should see an error message "User not found"

  Scenario: Failed Login with incorrect password
    Given the user is on the login page
    When the user enters a valid username in the username field
    And enters an incorrect password in the password field
    And clicks the "Log In" button
    Then the user should see an error message "Wrong password"

  Scenario: Failed Login with empty fields
    Given the user is on the login page
    And clicks the "Log In" button
    Then the user should see an empty field validation message
