Feature: Lyrical Matching based on favorite songs

  Scenario: Lyrical soulmate with similar song preferences
    Given On the favorites page logged in as "mutualC" with pass "mutualC1"
    And I have the most similar lyrics with "mutualA"
    When I click the "Similar" lyrical match button
    Then I should see the user "mutualA"
    And their favorites list should be displayed
    And a "Similar Friend" label should appear at the top

  Scenario: Lyrical enemy with different song preferences
    Given On the favorites page logged in as "Matey" with pass "Matey1"
    And I have the least similar lyrics with "OppUser"
    When I click the "Enemy" lyrical match button
    Then I should see the user "OppUser"
    And their favorites list should be displayed
    And a "Lyrical Enemy" label should appear at the top

  Scenario: Mutual lyrical soulmate match
    Given On the favorites page logged in as "mutualA" with pass "mutualA1"
    And this user has the same favorite songs as "mutualB"
    And they are both each other's top lyrical match
    When I click the "Similar" lyrical match button
    Then I should see the user "mutualB"
    And their favorites list should be displayed
    And a "Similar Friend" label should appear at the top
    And I should see a popup saying "Congrats! You have a mutual soulmate!!"

  Scenario: Mutual lyrical enemy match
    Given On the favorites page logged in as "OppUser" with pass "OppUser1"
    And has only a unique favorite song in different language unlike "mutualA"
    And they are each other's least similar lyrical match
    When I click the "Enemy" lyrical match button
    Then I should see the user "mutualA"
    And their favorites list should be displayed
    And a "Lyrical Enemy" label should appear at the top
    And I should see a popup saying "Oh no! You have a mutual enemy!!"
