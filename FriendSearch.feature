Feature: Friend search functionality

  Scenario: comparison table for one friend
    Given I am on the friend search page
    And I search for an exisiting user "daniel"
    And I select the existing user "daniel"
    Then I should see my "daniel" on the selected table
    And I click the "Compare Friends" button
    And I should see the comparison table for "daniel"

  Scenario: comparison table for two friends
    Given I am on the friend search page
    And I search for an exisiting user "daniel"
    And I select the existing user "daniel"
    And I search for an exisiting user "yared"
    And I select the existing user "yared"
    Then I should see my "yared" and "daniel" on the selected table
    And I click the "Compare Friends" button
    And I should see the comparison table for "daniel" and "yared"

  Scenario: search for user that doesnt exist
    Given I am on the friend search page
    And I search for a non exisiting user "fakeFriend"
    Then I should see an error message "no user found with name: fakeFriend"

  Scenario: initial ordering of comparison list
    Given I am on the friend search page
    And I search for an exisiting user "daniel"
    And I select the existing user "daniel"
    And I search for an exisiting user "yared"
    And I select the existing user "yared"
    Then I should see my "yared" and "daniel" on the selected table
    And I click the "Compare Friends" button
    And I should see a comparison table in decreasing order

  Scenario: reverse the comparison order
    Given I am on the friend search page
    And I search for an exisiting user "daniel"
    And I select the existing user "daniel"
    And I click the "Compare Friends" button
    And there exists a comparison table
    Then I should see the comparison table for "daniel"
    And I click the reverse order button
    And the order of the comparison table is reversed

  Scenario: display the song details modal
    Given I am on the friend search page
    And I search for an exisiting user "daniel"
    And I select the existing user "daniel"
    And I click the "Compare Friends" button
    And there exists a comparison table
    When I click on the "song-title-button" in the comparison table
    Then I should see the "song-modal"

  Scenario: display the favorited users modal
    Given I am on the friend search page
    And I search for an exisiting user "daniel"
    And I select the existing user "daniel"
    And I click the "Compare Friends" button
    And there exists a comparison table
    When I click on the "favorited-users-button" in the comparison table
    Then I should see the "favorited-users-modal"

  Scenario: search for a private friend
    Given I am on the friend search page
    And I search for an exisiting user "privateuser"
    And I select the existing user "privateuser"
    And I click the "Compare Friends" button
    Then I should see an error message "unable to compare songs with privateuser"


