Feature: Artist Search Functionality

  Scenario: Search for an artist with no results
    Given I am on the search page
    When I enter "SanyaNotReal" in the search field
    And I click the artist search button
    Then I should see a message indicating no songs were found

  Scenario: Empty search
    Given I am on the search page
    When I leave the search field empty
    And I click the artist search button
    Then no search request should be made
    And no results should be displayed

  Scenario: Search for an ambiguous artist
    Given I am on the search page
    When I enter "Drake" in the search field
    And I have entered an arbitrary number in the number input field
    And I click the artist search button
    Then I should see a list of matching artists with images

  Scenario: Generate a word cloud from selected songs
    Given I am on the search page
    When I enter "Travis" in the search field
    And I have entered an arbitrary number in the number input field
    And I have selected manual search
    And I click the artist search button
    And I select "Travis Scott" from the list of artists
    And I select at least one song
    And I click the "Generate" search button
    Then a word cloud should be generated based on the songs

  Scenario: Generate a word cloud based on top songs
    Given I am on the search page
    When I enter "Travis" in the search field
    And I have entered a number "2" in the number input field
    And I click the artist search button
    And I select "Travis Scott" from the list of artists
    And I click the "Generate" search button
    Then a word cloud should be generated based on the songs

  Scenario: Attempt to generate a word cloud without entering a number
    Given I am on the search page
    When I enter "Drake" in the search field
    And I have not entered a number in the number input field
    And I click the artist search button
    Then I should see an error message indicating a number is required

  Scenario: Add to favorites from song modal
    Given a word cloud has been generated
    And I click on a word in the word cloud
    And I hover over a song from modal
    When I click the "Add to Favorites" favorites button
    Then the song should be added to my favorites

  Scenario: Click on a word in the word cloud
    Given a word cloud has been generated
    And I click on a word in the word cloud
    Then I should see a modal showing the word's total occurrences and songs

  Scenario: Click on a song in the word modal
    Given I am viewing the modal for a word
    When I click on a song in the list
    Then I should see the song's details

  Scenario: Attempt to add to favorites a song already favorited
    Given a word cloud has been generated
    And I click on a word in the word cloud
    And I hover over a song from modal
    When I click the "Add to Favorites" favorites button twice
    Then I should see an error saying it is already in favorites

  Scenario: Success confirming removal of favorite song
    Given I go to the favorites page
    When I hover over a song
    And I click the delete icon
    Then a confirmation dialog should appear
    When I click the "Confirm" button
    Then the song should be removed from my favorites

  Scenario: Add top songs to an existing word cloud
    Given a word cloud has been generated
    When I enter "Travis" in the search field
    And I have entered a number "2" in the number input field
    And I click the artist search button
    And I select "Travis Scott" from the list of artists
    And I click "Add to Existing Cloud"
    Then the new words should be merged into the current word cloud

  Scenario: Add manually selected songs to an existing word cloud
    Given a word cloud has been generated
    When I enter "Taylor" in the search field
    And I have entered an arbitrary number in the number input field
    And I have selected manual search
    And I click the artist search button
    And I select "Taylor Swift" from the list of artists
    And I select at least one song
    And I click "Add to Existing Cloud"
    Then the new words should be merged into the current word cloud

  Scenario: Attempt to add to existing cloud without any word cloud generated
    Given no word cloud has been generated
    When I enter "Travis" in the search field
    And I have entered a number "2" in the number input field
    And I click the artist search button
    And I select "Travis Scott" from the list of artists
    And I click "Add to Existing Cloud"
    Then I should see an error saying "Please generate a word cloud first"

  Scenario: Tabular word cloud
    Given a word cloud has been generated
    When I click "Tabular" tabular button
    Then I should see a tabular version of the cloud

  Scenario: Back to cloud word cloud
    Given a word cloud has been generated
    When I click "Tabular" tabular button
    And I click the "Cloud" word button
    Then a word cloud should be generated based on the songs

  Scenario: Highlighted lyrics
    Given I am viewing the modal for a word
    When I click on a song in the list
    Then I should see the song lyrics with the word highlighted

  Scenario: Songs a word appears in appears in modal
    Given I am viewing the modal for a word
    When I click on a song in the list
    Then I should see the songs a word appears in and the frequency

  Scenario: Word Limit 100-word cloud
    Given I am on the search page
    When I enter "Travis" in the search field
    And I have entered a number "2" in the number input field
    And I click the artist search button
    And I select "Travis Scott" from the list of artists
    And I click the "Generate" search button
    Then the word cloud should contain only the top "100" words

  Scenario: Filler Word filter word cloud
    Given I am on the search page
    When I enter "Travis" in the search field
    And I have entered a number "2" in the number input field
    And I click the artist search button
    And I select "Travis Scott" from the list of artists
    And I click the "Generate" search button
    Then the word cloud should not contain any filler words

  Scenario: Word cloud generation under 10 seconds
    Given I am on the search page
    When I enter "Travis" in the search field
    And I have entered a number "2" in the number input field
    And I click the artist search button
    And I select "Travis Scott" from the list of artists
    And I click the "Generate" search button
    Then the word cloud should be generated quicker than the time limit

  Scenario: Word is bigger based on frequency
    Given I am on the search page
    When I enter "Travis" in the search field
    And I have entered a number "2" in the number input field
    And I click the artist search button
    And I select "Travis Scott" from the list of artists
    And I click the "Generate" search button
    Then a word cloud should be generated based on the songs
    And the word sizes should vary based on frequency

  Scenario: Stemming in word cloud
    Given I am on the search page
    When I enter "Travis" in the search field
    And I have entered a number "3" in the number input field
    And I click the artist search button
    And I select "Travis Scott" from the list of artists
    And I click the "Generate" search button
    When I click "Tabular" tabular button
    Then the word cloud should contain stemmed verbs

  Scenario: Generate word cloud from favorites
    Given I am on the search page
    When I enter "Travis" in the search field
    And I have entered a number "3" in the number input field
    And I click the artist search button
    And I select "Travis Scott" from the list of artists
    And I click the "Generate" search button
    Then a word cloud should be generated based on the songs
    When I click on a word in the word cloud
    And I hover over a song from modal
    And I click the "Add to Favorites" favorites button
    And I click the "x" button
    When I click the "Generate Cloud from Favorites" button
    Then a word cloud should be generated based on the songs

  Scenario: Add favorites to current word cloud
    Given I am on the search page
    When I enter "Billie" in the search field
    And I have entered a number "3" in the number input field
    And I click the artist search button
    And I select "Billie Eilish" from the list of artists
    And I click the "Generate" search button
    When I click the "Add Favorites to Existing Cloud" button
    Then the new words should be merged into the current word cloud

  Scenario: Success confirming removal of favorite song
    Given I go to the favorites page
    When I hover over a song
    And I click the delete icon
    Then a confirmation dialog should appear
    When I click the "Confirm" button
    Then the song should be removed from my favorites
