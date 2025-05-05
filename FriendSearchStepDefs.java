package edu.usc.csci310.project;

import static org.junit.jupiter.api.Assertions.*;

import io.cucumber.java.AfterAll;
import io.cucumber.java.en.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;

public class FriendSearchStepDefs {
    private WebDriver driver = WebDriverManager.getDriver();
    private List<Integer> originalCounts = new ArrayList<>();

    @Given("I am on the friend search page")
    public void i_am_on_the_friend_search_page() {
        WebDriverManager.closeDriver();
        driver = WebDriverManager.getDriver();

        //driver.get("http://localhost:8080/");
        driver.get("https://localhost:8443/");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        // log in as admin and navigate using the navbar
        if (driver.getCurrentUrl().contains("/login")) {
            WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
            usernameField.sendKeys("admin");
            WebElement passwordField = driver.findElement(By.id("password"));
            passwordField.sendKeys("Admin1");
            WebElement loginButton = driver.findElement(By.xpath("//button[text()='Log In']"));
            loginButton.click();

            WebElement friendSearchLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='Friend Search']")));
            friendSearchLink.click();
            wait.until(ExpectedConditions.urlContains("/friendSearch"));
        }
    }

    @When("I search for an exisiting user {string}")
    public void i_search_for_an_exisiting_user(String friendName) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));

        WebElement searchInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input.Friend-search-input")));
        searchInput.clear();
        searchInput.sendKeys(friendName);

        WebElement searchButton = driver.findElement(By.cssSelector("button.search-button"));
        searchButton.click();
    }

    @When("I search for a non exisiting user {string}")
    public void i_search_for_a_non_exisiting_user(String friendName) {
        // This step is identical to searching for an existing user,
        // but the error handling will show the appropriate error message.
        i_search_for_an_exisiting_user(friendName);
    }

    @When("I select the existing user {string}")
    public void i_select_the_existing_user(String friendName) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        WebElement friendButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//ul[contains(@class, 'search-results')]//button[text()='" + friendName + "']")));
        friendButton.click();
    }

    @Then("I should see my {string} on the selected table")
    public void i_should_see_my_on_the_selected_table(String friendName) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        WebElement selectedFriendsList = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.selected-friends ul")));
        String listText = selectedFriendsList.getText();
        assertTrue(listText.contains(friendName), "Selected friends list does not contain: " + friendName);
    }

    @Then("I should see my {string} and {string} on the selected table")
    public void i_should_see_my_and_on_the_selected_table(String friend1, String friend2) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        WebElement selectedFriendsList = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.selected-friends ul")));
        String listText = selectedFriendsList.getText();
        assertTrue(listText.contains(friend1), "Selected friends list does not contain: " + friend1);
        assertTrue(listText.contains(friend2), "Selected friends list does not contain: " + friend2);
    }

    @When("I click the {string} button")
    public void i_click_the_button(String buttonText) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[text()='" + buttonText + "']")));
        button.click();
    }

    @Then("I should see the comparison table for {string}")
    public void i_should_see_the_comparison_table_for(String friendName) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        WebElement comparisonTable = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.comparison-table table")));
        assertNotNull(comparisonTable, "Comparison table not displayed");

        // Expected songs and counts for friend "daniel"
        Map<String, Integer> expectedSongs = Map.of("Rolling in the Deep", 2);

        // Locate all rows
        List<WebElement> rows = comparisonTable.findElements(By.xpath(".//tr[position()>1]"));
        Map<String, Integer> actualSongs = new HashMap<>();
        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            if (cells.size() >= 2) {
                String song = cells.get(0).getText().trim();
                int count = Integer.parseInt(cells.get(1).getText().trim());
                actualSongs.put(song, count);
            }
        }

        // For each expected song, verify it exists in the table and the count is correct.
        for (Map.Entry<String, Integer> entry : expectedSongs.entrySet()) {
            String expectedSong = entry.getKey();
            Integer expectedCount = entry.getValue();
            assertTrue(actualSongs.containsKey(expectedSong),
                    "Comparison table does not contain expected song: " + expectedSong);
            assertEquals(expectedCount, actualSongs.get(expectedSong),
                    "Comparison table count mismatch for song " + expectedSong);
        }
    }

    @Then("I should see the comparison table for {string} and {string}")
    public void i_should_see_the_comparison_table_for_two(String friend1, String friend2) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        WebElement comparisonTable = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.comparison-table table")));
        assertNotNull(comparisonTable, "Comparison table not displayed");

        // Expected songs and counts for friend "daniel" and "yared"
        Map<String, Integer> expectedSongs = Map.of(

                "Rolling in the Deep", 3);

        // Locate all rows
        List<WebElement> rows = comparisonTable.findElements(By.xpath(".//tr[position()>1]"));
        Map<String, Integer> actualSongs = new HashMap<>();
        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            if (cells.size() >= 2) {
                String song = cells.get(0).getText().trim();
                int count = Integer.parseInt(cells.get(1).getText().trim());
                actualSongs.put(song, count);
            }
        }

        // For each expected song, verify it exists in the table and the count is correct.
        for (Map.Entry<String, Integer> entry : expectedSongs.entrySet()) {
            String expectedSong = entry.getKey();
            Integer expectedCount = entry.getValue();
            assertTrue(actualSongs.containsKey(expectedSong),
                    "Comparison table does not contain expected song: " + expectedSong);
            assertEquals(expectedCount, actualSongs.get(expectedSong),
                    "Comparison table count mismatch for song " + expectedSong);
        }
    }


    @Then("I should see an error message {string}")
    public void i_should_see_an_error_message(String expectedErrorMessage) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        WebElement errorDiv = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.error")));
        assertEquals(expectedErrorMessage, errorDiv.getText().trim());
    }

    @Then("I should see a comparison table in decreasing order")
    public void i_should_see_a_comparison_table_in_decreasing_order() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        WebElement table = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.comparison-table table")));

        // Get all rows except the header row
        List<WebElement> rows = table.findElements(By.xpath(".//tr[position()>1]"));
        List<Integer> counts = new ArrayList<>();
        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            if (cells.size() >= 2) {
                // Second cell contains the count of appearances
                try {
                    int count = Integer.parseInt(cells.get(1).getText().trim());
                    counts.add(count);
                } catch (NumberFormatException e) {
                    fail("The count value is not a valid number in row: " + row.getText());
                }
            }
        }
        // Verify that the counts are in decreasing order
        for (int i = 1; i < counts.size(); i++) {
            assertTrue(counts.get(i - 1) >= counts.get(i),
                    "Row " + i + " count " + counts.get(i - 1) + " is not >= " + counts.get(i));
        }
    }

    @When("there exists a comparison table")
    public void there_exists_a_comparison_table() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        WebElement table = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.comparison-table table")));

        // Get all rows in the table
        List<WebElement> allRows = table.findElements(By.tagName("tr"));
        List<WebElement> dataRows;

        // Skip header row if it exists
        if (!allRows.isEmpty() && !allRows.get(0).findElements(By.tagName("th")).isEmpty()) {
            dataRows = allRows.subList(1, allRows.size());
        } else {
            dataRows = allRows;
        }

        originalCounts.clear();

        // Capture the counts from the second cell of each data row
        for (WebElement row : dataRows) {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            if (cells.size() >= 2) {
                try {
                    int count = Integer.parseInt(cells.get(1).getText().trim());
                    originalCounts.add(count);
                } catch (NumberFormatException e) {
                    fail("The count value is not a valid number in row: " + row.getText());
                }
            }
        }

        assertFalse(originalCounts.isEmpty(), "Comparison table is empty or no data rows found.");
    }

    @When("I click the reverse order button")
    public void i_click_the_reverse_order_button() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        WebElement reverseButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("OrderButton")));
        reverseButton.click();
    }

    @Then("the order of the comparison table is reversed")
    public void the_order_of_the_comparison_table_is_reversed() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        WebElement table = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.comparison-table table")));
        List<WebElement> rows = table.findElements(By.xpath(".//tr[position()>1]"));
        List<Integer> newCounts = new ArrayList<>();
        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            if (cells.size() >= 2) {
                try {
                    int count = Integer.parseInt(cells.get(1).getText().trim());
                    newCounts.add(count);
                } catch (NumberFormatException e) {
                    fail("The count value is not a valid number in row: " + row.getText());
                }
            }
        }

        List<Integer> expectedReversed = new ArrayList<>(originalCounts);
        Collections.reverse(expectedReversed);

        assertEquals(expectedReversed, newCounts, "The comparison table order is not reversed as expected.");
    }

    @When("I click on the {string} in the comparison table")
    public void i_click_on_the_in_the_comparison_table(String string) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        WebElement modalButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button." + string)));
        modalButton.click();


    }
    @Then("I should see the {string}")
    public void i_should_see_the (String string) {
        // Wait for the modal to be visible
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        WebElement modal = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div." + string)));
        assertTrue(modal.isDisplayed(), "The modal is displayed.");

    }

    @AfterAll
    public static void cleanUp() {
        WebDriverManager.closeDriver();
    }

    @And("I search for a private user {string}")
    public void iSearchForAPrivateUser(String userName) {


    }
}
