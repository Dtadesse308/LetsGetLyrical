Feature: Favorites List Component

  Scenario: Favorites list appear on favorites page
    Given I am on the favorites page
    Then the favorites list should appear

  Scenario: Success removing a song from favorites
    Given I am on the favorites page
    When I hover over a song
    And I click the delete icon
    Then a confirmation dialog should appear

  Scenario: Success confirming removal of favorite song
    Given I am on the favorites page
    When I hover over a song
    And I click the delete icon
    Then a confirmation dialog should appear
    When I click the "Confirm" button
    Then the song should be removed from my favorites

  Scenario: Success canceling removal confirmation
    Given I am on the favorites page
    When I hover over a song
    And I click the delete icon
    Then a confirmation dialog should appear
    When I click the "Cancel" button
    Then the song should remain in my favorites

  Scenario: View song details when clicking on a favorite song
    Given I am on the favorites page
    When I click on a song in my favorites list
    Then I should see the song details

  Scenario: Favorites list persistent between sessions
    Given I have previously favorited a song
    When I navigate to the favorites page
    Then I should see my favorited songs
    And they should be in the order they were favorited

  Scenario: Changing user from private to public
    Given I am on the favorites page
    When I click the "private" toggle
    Then User profile is "public"

  Scenario: Changing user from public to private
    Given I am on the favorites page
    When I click the "public" toggle
    Then User profile is "private"

  Scenario: Success default profile set to private
    Given I am a newly registered user
    And Navigate to favorites page
    Then User profile is "private"

  Scenario: Success privacy setting of profile is persistent
    Given I am on the favorites page
    And User current profile privacy setting is either private or public
    When User navigates to a different page on site
    And User navigates back to the favorites page
    Then User profile privacy setting should still be the same

  Scenario: Success removing all songs from favorites
    Given I am on the favorites page
    When I click the "Remove All Favorites" button
    Then a confirmation dialog should appear

  Scenario: Success confirming removal of all favorite songs
    Given I am on the favorites page
    When I click the "Remove All Favorites" button
    Then a confirmation dialog should appear
    When I click Confirm button
    Then all the songs would be removed if confirm was clicked

  Scenario: Success canceling remove all confirmation
    Given I am on the favorites page
    When I click the "Remove All Favorites" button
    Then a confirmation dialog should appear
    When I click the "Cancel" button
    Then all the songs should remain in my favorites

  Scenario: Moving a song up in the list
    Given I am on the favorites page
    When I hover over a song that is not at the top of the list
    And I click the "up" arrow button
    Then the song should move one position up in the list

  Scenario: Moving a song down in the list
    Given I am on the favorites page
    When I hover over a song that is not at the bottom of the list
    And I click the "down" arrow button
    Then the song should move one position down in the list

  Scenario: Up arrow does not do anything for the top song
    Given I am on the favorites page
    When I hover over the first song in the list
    Then the "up" arrow button should be disabled

  Scenario: Down arrow does not do anything for the bottom song
    Given I am on the favorites page
    When I hover over the last song in the list
    Then the "down" arrow button should be disabled

  Scenario: Reordered list is persists
    Given I am on the favorites page
    When I reorder my favorites list
    And User navigates to a different page on site
    And User navigates back to the favorites page
    Then the favorites list should maintain the new order


