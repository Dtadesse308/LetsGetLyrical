Feature: Registration functionality

  Scenario: Successful Registration
    Given the user is on the registration page
    And user enters a valid username, password, and confirms password
    When clicks the "Register" button
    Then the user should be redirected to login

  Scenario: Successful Registration shows confirmation and redirect
    Given the user is on the registration page
    And user enters a valid username, password, and confirms password
    When clicks the "Register" button
    Then the user should see a confirmation message
    Then the user should be redirected to login

  Scenario: Failed Registration with missing fields
    Given the user is on the registration page
    And clicks the "Register" button
    Then the user should see an empty field validation message

  Scenario: Failed Registration with non-matching passwords
    Given the user is on the registration page
    When the user enters a valid username in the username field
    And enters a valid password in the password field
    And enters a different value in the confirm password field
    And clicks the "Register" button
    Then the user should see an error message "Passwords don't match."

  Scenario: Failed Registration with existing username
    Given the user is on the registration page
    When the user enters a username that is already taken
    And enters a valid password in the password field
    And enters a valid password in the confirm password field
    And clicks the "Register" button
    Then the user should see an error message "Username already exists."

  Scenario: Cancel registration and confirm
    Given the user is on the registration page
    And user enters a valid username, password, and confirms password
    When clicks the "Cancel" button
    And the cancellation confirmation popup should be displayed
    And clicks the "Confirm" button
    Then the user should be redirected to login

  Scenario: Cancel registration and decline cancellation
    Given the user is on the registration page
    And the user has entered "alice" in the username field
    And the user has entered "Password1" in the password field
    When clicks the "Cancel" button
    And the cancellation confirmation popup should be displayed
    And the user dismisses the cancellation popup
    Then the username field should still contain "alice"
    And the password field should still contain "Password1"
    And the user should remain on the registration page

  Scenario: Failed Registration with invalid password format
    Given the user is on the registration page
    When the user enters a valid username in the username field
    And enters "invalidpass" in the password field
    And enters "invalidpass" in the confirm password field
    And clicks the "Register" button
    Then should see an error message for invalid password