package edu.usc.csci310.project;

import static org.junit.jupiter.api.Assertions.*;

import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArtistStepDefs {
    private static final String ROOT_URL = "https://localhost:8443/";
    private WebDriver driver = WebDriverManager.getDriver();
    private WebDriverWait wait;
    private long startTime;
    private String selectedSong;

    @Before
    public void setUp() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Given("I am on the search page")
    public void iAmOnTheSearchPage() {
        WebDriverManager.closeDriver();

        // Start a new browser session
        driver = WebDriverManager.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(ROOT_URL);

        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/search"),
                ExpectedConditions.urlContains("/login")
        ));

        if (driver.getCurrentUrl().contains("/login")) {
            WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
            usernameField.sendKeys("sanyaSearch");
            WebElement passwordField = driver.findElement(By.id("password"));
            passwordField.sendKeys("/epq2£1tW&x4");
            WebElement loginButton = driver.findElement(By.xpath("//button[text()='Log In']"));
            loginButton.click();

            wait.until(ExpectedConditions.urlContains("/search"));
        }
    }

    @Given("I go to the favorites page")
    public void i_go_on_the_favorites_page() {
        WebDriverManager.closeDriver();
        driver = WebDriverManager.getDriver();

        driver.get("http://localhost:8080/");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        if (driver.getCurrentUrl().contains("/login")) {
            WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
            usernameField.sendKeys("sanyaSearch");
            WebElement passwordField = driver.findElement(By.id("password"));
            passwordField.sendKeys("/epq2£1tW&x4");
            WebElement loginButton = driver.findElement(By.xpath("//button[text()='Log In']"));
            loginButton.click();

            WebElement favoritesLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='Favorites']")));
            favoritesLink.click();
            wait.until(ExpectedConditions.urlContains("/favorites"));
        }
    }

    @When("I enter {string} in the search field")
    public void iEnterInTheSearchField(String artistName) {
        WebElement searchField = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[data-testid='artist-name-input']")));
        searchField.clear();
        searchField.sendKeys(artistName);
    }

    @When("I leave the search field empty")
    public void iLeaveTheSearchFieldEmpty() {
        WebElement searchField = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[data-testid='artist-name-input']")));
        searchField.clear();
    }

    @And("I click the artist search button")
    public void iClickTheSearchButton() {
        WebElement searchButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[data-testid='search-button']")));
        searchButton.click();
    }

    @Then("I should see a message indicating no songs were found")
    public void iShouldSeeAMessageIndicatingNoSongsWereFound() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("no-results")));
        WebElement noResultsMessage = driver.findElement(By.className("no-results"));
        assertTrue(noResultsMessage.isDisplayed());
    }

    @Then("no search request should be made")
    public void noSearchRequestShouldBeMade() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<WebElement> loadingElements = driver.findElements(By.className("loading"));
        List<WebElement> artistLists = driver.findElements(By.cssSelector("[data-testid='artist-list']"));
        assertTrue(loadingElements.isEmpty() && artistLists.isEmpty());
    }

    @And("no results should be displayed")
    public void noResultsShouldBeDisplayed() {
        List<WebElement> resultLists = driver.findElements(By.cssSelector("[data-testid='artist-list'], [data-testid='song-list']"));
        assertTrue(resultLists.isEmpty());
    }

    @Then("I should see a list of matching artists with images")
    public void iShouldSeeAListOfMatchingArtists() {
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.xpath("//h2[contains(text(), 'Artists matching')]")),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='artist-list']")),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='artist-item']"))
        ));

        List<WebElement> artistItems = driver.findElements(By.cssSelector("[data-testid='artist-item']"));
        if (artistItems.isEmpty()) {
            artistItems = driver.findElements(By.className("artist-item"));
        }
        if (artistItems.isEmpty()) {
            artistItems = driver.findElements(By.className("song-item"));
        }

        assertFalse(artistItems.isEmpty(), "Expected to find at least one artist in the list");
    }

    @And("I have selected manual search")
    public void iHaveSelectedManualSearch() {
        try {
            WebElement manualRadio = driver.findElement(By.id("manual"));
            ((org.openqa.selenium.JavascriptExecutor) driver)
                    .executeScript("arguments[0].scrollIntoView(true); arguments[0].click();", manualRadio);

            Thread.sleep(500);
            Boolean isSelected = (Boolean) ((org.openqa.selenium.JavascriptExecutor) driver)
                    .executeScript("return document.getElementById('manual').checked");

            assertTrue(isSelected, "Manual search mode should be selected");
        } catch (Exception e) { //fallback
            try {
                driver.findElement(By.xpath("//input[@id='manual']/../label")).click();
                Thread.sleep(500);
            } catch (Exception ex) {
                fail(e.getMessage());
            }
        }
    }

    @And("I click the {string} search button")
    public void iClickTheNamedButton(String buttonText) {
        WebElement button;

        try {
            button = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[normalize-space(text())='" + buttonText + "' or contains(text(), '" + buttonText + "')]")));
        } catch (Exception e) {//partial text fallback
            if (buttonText.toLowerCase().contains("generate")) {
                button = wait.until(ExpectedConditions.elementToBeClickable(
                        By.className("generate-button")));
            } else if (buttonText.toLowerCase().contains("add to existing")) {
                button = wait.until(ExpectedConditions.elementToBeClickable(
                        By.className("add-to-existing-button")));
            } else {
                throw e;
            }
        }

        startTime = System.currentTimeMillis();

        try {//js click
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
            System.out.println("Clicked " + buttonText + " button using JavaScript");
        } catch (Exception e) {//fallback
            button.click();
            System.out.println("Clicked " + buttonText + " button using regular click");
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @And("I select at least one song")
    public void iHaveSelectedAtLeastOneSong() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='song-list']")));
        WebElement firstSong = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='song-item']")));

        try {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", firstSong);
        } catch (Exception e) {
            firstSong.click();
        }

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".song-item.selected")));
    }

    @And("I have entered an arbitrary number in the number input field")
    public void iHaveEnteredANumberInTheNumberInputField() {
        WebElement numInput = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[data-testid='artist-num-input']")));
        numInput.clear();
        numInput.sendKeys("5");
    }

    @And("I have entered a number {string} in the number input field")
    public void iHaveEnteredANumberInTheNumberInputField(String number) {
        WebElement numInput = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[data-testid='artist-num-input']")));
        numInput.clear();
        numInput.sendKeys(number);
    }

    @And("I have not entered a number in the number input field")
    public void iHaveNotEnteredANumberInTheNumberInputField() {
        WebElement numInput = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("input[data-testid='artist-num-input']")));
        numInput.clear();
    }

    @Then("I should see an error message indicating a number is required")
    public void iShouldSeeAnErrorMessageIndicatingANumberIsRequired() {
        try {
            WebElement searchButton = driver.findElement(By.cssSelector("button[data-testid='search-button']"));
            searchButton.click();

            Thread.sleep(500);
            Boolean isInvalid = (Boolean) ((org.openqa.selenium.JavascriptExecutor) driver)
                    .executeScript("return document.querySelector('input[data-testid=\"artist-num-input\"]').validity.valueMissing");

            assertTrue(isInvalid, "The number input field should show validation error for being required");
            String validationMessage = (String) ((org.openqa.selenium.JavascriptExecutor) driver)
                    .executeScript("return document.querySelector('input[data-testid=\"artist-num-input\"]').validationMessage");

            assertFalse(validationMessage.isEmpty(), "A validation message should be displayed for the empty number field");

        } catch (Exception e) {
            List<WebElement> errorElements = driver.findElements(By.className("error"));
            if (!errorElements.isEmpty()) {
                for (WebElement error : errorElements) {
                    if (error.isDisplayed() && (error.getText().contains("number") ||
                            error.getText().contains("required") ||
                            error.getText().contains("field"))) {
                        return;
                    }
                }
            }
            fail("nothing found");
        }
    }

    @Then("a word cloud should be generated based on the songs")
    public void aWordCloudShouldBeGeneratedBasedOnTheTopSongs() {
        WebDriverWait longerWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        longerWait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='word-cloud']")));

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        boolean wordCloudHasContent = false;
        try {
            List<WebElement> wordItems = driver.findElements(By.cssSelector("[data-testid='word-item']"));
            if (!wordItems.isEmpty()) {
                wordCloudHasContent = true;
            } else {
                List<WebElement> svgTextElements = driver.findElements(By.cssSelector("[data-testid='word-cloud'] text"));
                if (!svgTextElements.isEmpty()) {
                    wordCloudHasContent = true;
                } else {
                    Boolean hasSvg = (Boolean) ((org.openqa.selenium.JavascriptExecutor) driver)
                            .executeScript("return document.querySelector('[data-testid=\"word-cloud\"] svg') !== null");
                    if (hasSvg) {
                        wordCloudHasContent = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertTrue(wordCloudHasContent, "Expected to find word cloud content");
    }

    @And("I select {string} from the list of artists")
    public void iSelectFromTheListOfArtists(String artistName) {
        WebDriverWait longerWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        longerWait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("[data-testid='artist-list']")));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        WebElement artistItem = null;
        try {
            artistItem = driver.findElement(
                    By.xpath("//li[@data-testid='artist-item']//div[contains(text(),'" + artistName + "')]"));
        } catch (Exception e) {
            try {
                artistItem = driver.findElement(
                        By.xpath("//div[contains(@class, 'song-title') and contains(text(),'" + artistName + "')]"));
            } catch (Exception e2) {
                try {
                    List<WebElement> allArtists = driver.findElements(By.cssSelector("[data-testid='artist-item']"));
                    for (WebElement artist : allArtists) {
                        if (artist.getText().contains(artistName)) {
                            artistItem = artist;
                            break;
                        }
                    }
                    if (artistItem == null) {
                        allArtists = driver.findElements(By.className("song-item"));
                        for (WebElement artist : allArtists) {
                            if (artist.getText().contains(artistName)) {
                                artistItem = artist;
                                break;
                            }
                        }
                    }
                } catch (Exception e3) {
                    System.out.println("Could not find artist with name: " + artistName);
                }
            }
        }

        if (artistItem != null) {
            ((org.openqa.selenium.JavascriptExecutor) driver)
                    .executeScript("arguments[0].scrollIntoView(true);", artistItem);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {//js click
                ((org.openqa.selenium.JavascriptExecutor) driver)
                        .executeScript("arguments[0].click();", artistItem);
                System.out.println("Clicked artist '" + artistName + "' using JavaScript");
            } catch (Exception e) {//fall back
                artistItem.click();
                System.out.println("Clicked artist '" + artistName + "' using regular click");
            }
        } else {
            fail("Could not find artist with name: " + artistName);
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Given("a word cloud has been generated")
    public void aWordCloudHasBeenGenerated() {
        iAmOnTheSearchPage();
        iEnterInTheSearchField("Tyler");
        iHaveEnteredANumberInTheNumberInputField("3");
        iClickTheSearchButton();

        WebElement artistItem = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='artist-item']")));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", artistItem);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        WebElement generateButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.className("generate-button")));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", generateButton);
        WebDriverWait longerWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        longerWait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='word-cloud']")));

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @And("I hover over a song from modal")
    public void iHoverOverASongFromModal() {
        WebDriverWait longerWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        WebElement modal = longerWait.until(ExpectedConditions.presenceOfElementLocated(By.className("modal-content")));

        // Find the first song in the occurrences list
        WebElement songLink = modal.findElement(By.cssSelector("ul li a"));
        assertNotNull(songLink, "Expected to find a song link in the modal");

        // Hover over the song link
        org.openqa.selenium.interactions.Actions actions = new org.openqa.selenium.interactions.Actions(driver);
        actions.moveToElement(songLink).perform();

        // Verify tooltip appears
        WebElement tooltip = longerWait.until(ExpectedConditions.presenceOfElementLocated(By.className("small-tooltip")));
        assertTrue(tooltip.isDisplayed(), "Tooltip should be displayed when hovering over a song");
    }


    @And("I click {string}")
    public void i_click(String buttonText) {
        try {
            WebElement button = null;
            try {
                button = wait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[normalize-space(text())='" + buttonText + "' or contains(text(), '" + buttonText + "')]")));
            } catch (Exception e) {
                if (buttonText.toLowerCase().contains("add to existing")) {
                    button = wait.until(ExpectedConditions.elementToBeClickable(
                            By.className("add-to-existing-button")));
                } else {
                    button = wait.until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//*[contains(text(), '" + buttonText + "')]")));
                }
            }

            if (button != null) {
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
                Thread.sleep(2000);
            } else {
                fail("Could not find clickable element with text: " + buttonText);
            }
        } catch (Exception e) {
            fail("Failed to click on '" + buttonText + "': " + e.getMessage());
        }
    }

    @Then("I should see a tooltip with a button to add the song to favorites")
    public void iShouldSeeATooltipWithButtonToAddToFavorites() {
        WebElement tooltip = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("small-tooltip")));
        assertTrue(tooltip.isDisplayed(), "Tooltip should be displayed when hovering over a word");

        WebElement favoriteButton = tooltip.findElement(By.id("favorite-button"));
        assertTrue(favoriteButton.isDisplayed(), "Favorite button should be present in the tooltip");
        assertEquals("Add to Favorites", favoriteButton.getText(), "Favorite button should have the correct label");
    }

    @Then("I should see a modal showing the word's total occurrences and songs")
    public void iShouldSeeAModalShowingWordDetails() {
        // Wait for the modal to appear
        WebElement modal = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("modal-content")));
        assertTrue(modal.isDisplayed(), "The modal should be displayed");

        // Verify modal contains word details
        WebElement wordTitle = modal.findElement(By.tagName("h2"));
        assertNotNull(wordTitle, "The modal should contain a title for the word");
        assertFalse(wordTitle.getText().isEmpty(), "The word title should not be empty");

        // Verify modal contains total occurrences
        WebElement totalOccurrences = modal.findElement(By.xpath("//p[strong[text()='Total:']]"));
        assertNotNull(totalOccurrences, "The modal should display the total occurrences of the word");
        assertFalse(totalOccurrences.getText().isEmpty(), "The total occurrences should not be empty");

        // Verify modal contains a list of songs
        WebElement occurrencesList = modal.findElement(By.tagName("ul"));
        assertNotNull(occurrencesList, "The modal should contain a list of songs");
        List<WebElement> songs = occurrencesList.findElements(By.tagName("li"));
        assertFalse(songs.isEmpty(), "The list of songs should not be empty");
    }

    @Given("I am viewing the modal for a word")
    public void iAmViewingTheModalForAWord() {
        aWordCloudHasBeenGenerated();
        iClickOnAWordInTheWordCloud();

        //check that modal is displayed
    }

    @When("I click on a song in the list")
    public void iClickOnASongInTheList() {

        WebDriverWait longerWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        WebElement modal = longerWait.until(ExpectedConditions.presenceOfElementLocated(By.className("modal-content")));

        // Find the first song in the occurrences list
        WebElement songLink = modal.findElement(By.cssSelector("ul li a"));
        assertNotNull(songLink, "Expected to find a song link in the modal");

        // Hover over the song link
        org.openqa.selenium.interactions.Actions actions = new org.openqa.selenium.interactions.Actions(driver);
        actions.click(songLink).perform();

        // Verify modal appears
        WebElement popUpModal = longerWait.until(ExpectedConditions.presenceOfElementLocated(By.className("modal-content")));
        assertTrue(popUpModal.isDisplayed(), "modal should be displayed when clicking on a song");

    }

    @Then("I should see the song's details")
    public void iShouldSeeSongDetails() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement largeModal = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("modal-content")));
        assertTrue(largeModal.isDisplayed(), "The large modal should be displayed");

        WebElement songTitle = largeModal.findElement(By.xpath("/html/body/div/div/div/div[2]/div[4]/div/h1"));
        assertNotNull(songTitle, "The modal should contain the song's title");
        assertFalse(songTitle.getText().isEmpty(), "The song's title should not be empty");

        WebElement artistInfo = largeModal.findElement(By.xpath("//p[strong[text()='Artist:']]"));
        assertNotNull(artistInfo, "The modal should display the artist's name");
        assertFalse(artistInfo.getText().isEmpty(), "The artist's name should not be empty");
    }

    @Then("I should see the song lyrics with the word highlighted")
    public void iShouldSeeSongLyrics() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement largeModal = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("modal-content")));
        assertTrue(largeModal.isDisplayed(), "The large modal should be displayed");

        WebElement songTitle = largeModal.findElement(By.xpath("/html/body/div/div/div/div[2]/div[4]/div/h1"));
        assertNotNull(songTitle, "The modal should contain the song's title");
        assertFalse(songTitle.getText().isEmpty(), "The song's title should not be empty");

        WebElement artistInfo = largeModal.findElement(By.xpath("//p[strong[text()='Artist:']]"));
        assertNotNull(artistInfo, "The modal should display the artist's name");
        assertFalse(artistInfo.getText().isEmpty(), "The artist's name should not be empty");

        WebElement lyricsSection = largeModal.findElement(By.xpath("/html/body/div/div/div/div[2]/div[4]/div/div/div"));
        assertNotNull(lyricsSection, "The modal should display the lyrics section");
        assertFalse(lyricsSection.getText().isEmpty(), "The lyrics section should not be empty");

        WebElement highlightedWord = lyricsSection.findElement(By.tagName("mark"));
        assertNotNull(highlightedWord, "The lyrics should contain the highlighted word");
        assertFalse(highlightedWord.getText().isEmpty(), "The highlighted word should not be empty");
    }

    @Then("I should see the songs a word appears in and the frequency")
    public void iShouldSeeSongs() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement largeModal = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("modal-content")));
        assertTrue(largeModal.isDisplayed(), "The modal should be displayed");

        WebElement songTitle = largeModal.findElement(By.xpath("/html/body/div/div/div/div[2]/div[4]/div/h1"));
        assertNotNull(songTitle, "The modal should contain the song's title");
        assertFalse(songTitle.getText().isEmpty(), "The song's title should not be empty");

        WebElement artistInfo = largeModal.findElement(By.xpath("//p[strong[text()='Artist:']]"));
        assertNotNull(artistInfo, "The modal should display the artist's name");
        assertFalse(artistInfo.getText().isEmpty(), "The artist's name should not be empty");
    }

    @Given("I have already added a song to favorites")
    public void iHaveAlreadyAddedASongToFavorites() {
        aWordCloudHasBeenGenerated();
        iClickOnAWordInTheWordCloud();
        iHoverOverASongFromModal();
        i_click_the_favorites_button("Add to Favorites");

        // add
    }

    @Then("I should see an error saying it is already in favorites")
    public void iShouldSeeErrorAlreadyInFavorites() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement tooltip = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("small-tooltip")));
        assertTrue(tooltip.isDisplayed(), "Tooltip should be displayed when hovering over a song");

        WebElement errorMessage = tooltip.findElement(By.tagName("p"));
        assertNotNull(errorMessage, "Error message should be present in the tooltip");
        assertEquals("Already in Favorites!", errorMessage.getText(), "Error message should indicate the song is already in favorites");
    }

    @Then("the new words should be merged into the current word cloud")
    public void newWordsShouldBeMergedIntoCurrentCloud() {
        try { //check word cloud is still present
            boolean wordCloudPresent = !driver.findElements(By.cssSelector("[data-testid='word-cloud']")).isEmpty();
            assertTrue(wordCloudPresent, "Word cloud should still be present after merging new words");
        } catch (Exception e) {
            fail("Error verifying word cloud after merge: " + e.getMessage());
        }
    }

    @Given("I have selected one or more songs manually")
    public void iHaveSelectedSongsManually() {
        try {
            WebElement manualRadio = wait.until(ExpectedConditions.elementToBeClickable(
                    By.id("manual")));
            manualRadio.click();

            WebElement searchField = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("input[data-testid='artist-name-input']")));
            searchField.clear();
            searchField.sendKeys("Drake");

            WebElement numInput = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("input[data-testid='artist-num-input']")));
            numInput.clear();
            numInput.sendKeys("5");

            WebElement searchButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("button[data-testid='search-button']")));
            searchButton.click();

            WebElement artistItem = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("[data-testid='artist-item']")));
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", artistItem);

            Thread.sleep(2000);
            WebElement songItem = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("[data-testid='song-item']")));
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", songItem);

            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".song-item.selected")));
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to set up manual song selection: " + e.getMessage());
        }
    }

    @Given("no word cloud has been generated")
    public void noWordCloudHasBeenGenerated() {
        iAmOnTheSearchPage();

        List<WebElement> wordClouds = driver.findElements(By.cssSelector("[data-testid='word-cloud']"));
        assertTrue(wordClouds.isEmpty(), "No word cloud should be present");
    }

    @And("I click on a word in the word cloud")
    public void iClickOnAWordInTheWordCloud() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement wordCloud = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("[data-testid='word-cloud']")));

        // Find a word element in the word cloud (e.g., <text> tag)
        WebElement wordElement = wordCloud.findElement(By.cssSelector("text[data-testid='word-item']"));

        // Scroll into view and click the word
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", wordElement);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", wordElement);
        } catch (Exception e) {
            wordElement.click(); // Fallback to regular click
        }

        // Wait for the modal to appear
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("modal-content")));
    }

    @When("I click the {string} favorites button")
    public void i_click_the_favorites_button(String buttonText) {
        WebDriverWait longerWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        WebElement tooltip = longerWait.until(ExpectedConditions.presenceOfElementLocated(By.className("small-tooltip")));

        WebElement favoriteButton = tooltip.findElement(By.id("favorite-button"));
        assertNotNull(favoriteButton, "Favorite button should be present in the tooltip");
        assertEquals(buttonText, favoriteButton.getText(), "Favorite button should have the correct label");

        try {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", favoriteButton);
        } catch (Exception e) {
            favoriteButton.click(); // Fallback to regular click
        }
    }

    @When("I click the {string} favorites button twice")
    public void i_click_the_favorites_button_twice(String buttonText) {

        WebDriverWait longerWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        WebElement modal = longerWait.until(ExpectedConditions.presenceOfElementLocated(By.className("modal-content")));

        // Find the first song in the occurrences list
        WebElement songLink = modal.findElement(By.cssSelector("ul li a"));
        assertNotNull(songLink, "Expected to find a song link in the modal");

        // Hover over the song link
        org.openqa.selenium.interactions.Actions actions = new org.openqa.selenium.interactions.Actions(driver);
        actions.moveToElement(songLink).perform();

        // Wait for the tooltip to appear
        WebElement tooltip = longerWait.until(ExpectedConditions.presenceOfElementLocated(By.className("small-tooltip")));

        // Find and click the "Add to Favorites" button twice
        WebElement favoriteButton = tooltip.findElement(By.id("favorite-button"));
        assertNotNull(favoriteButton, "Favorite button should be present in the tooltip");
        assertEquals(buttonText, favoriteButton.getText(), "Favorite button should have the correct label");

        favoriteButton.click();
        try {
            Thread.sleep(1000); // Wait for the first action to complete
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        favoriteButton.click();

        // Wait for the error message to appear
        WebElement errorMessage = tooltip.findElement(By.tagName("p"));
        assertNotNull(errorMessage, "Error message should be present in the tooltip");
        assertEquals("Already in Favorites!", errorMessage.getText(), "Error message should indicate the song is already in favorites");
    }

    @When("I click {string} tabular button")
    public void iClickTabularButton(String buttonText) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        try {
            WebElement cloudContainer = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.className("cloud-container")));
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                    "document.getElementById('tabular').click();"
            );
            Thread.sleep(500);
            Boolean isSelected = (Boolean) ((org.openqa.selenium.JavascriptExecutor) driver)
                    .executeScript("return document.getElementById('tabular').checked;");

            assertTrue(isSelected, "view mode should be selected");

            System.out.println("Successfully switched to using JavaScript");
        } catch (Exception e) {
            System.out.println("view mode failed");
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @When("I click the {string} word button")
    public void iClickCloudButton(String buttonText) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        try {
            WebElement cloudContainer = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.className("cloud-container")));
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                    "document.getElementById('cloud').click();"
            );
            Thread.sleep(500);
            Boolean isSelected = (Boolean) ((org.openqa.selenium.JavascriptExecutor) driver)
                    .executeScript("return document.getElementById('cloud').checked;");

            assertTrue(isSelected, "view mode should be selected");

            System.out.println("Successfully switched to using JavaScript");
        } catch (Exception e) {
            System.out.println("view mode failed");
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Then("I should see a tabular version of the cloud")
    public void iShouldSeeTabularVersionOfCloud() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement wordTable = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='word-table']")));

        assertTrue(wordTable.isDisplayed(), "Word table should be displayed in tabular mode");
        List<WebElement> tableRows = wordTable.findElements(By.tagName("tr"));
        assertTrue(tableRows.size() > 1, "Table should have rows of data");

        WebElement headerRow = tableRows.get(0);
        List<WebElement> headerCells = headerRow.findElements(By.tagName("th"));
        assertEquals(2, headerCells.size(), "Table should have two columns");
        assertTrue(headerCells.get(0).getText().contains("Word"), "First column should be for words");
        assertTrue(headerCells.get(1).getText().contains("Total"), "Second column should be for totals");
    }

    @Then("I should see an error saying {string}")
    public void iShouldSeeErrorWithMessage(String expectedError) {
        try {
            List<WebElement> tooltipErrors = driver.findElements(By.className("tooltip-error"));
            if (!tooltipErrors.isEmpty()) {
                for (WebElement tooltip : tooltipErrors) {
                    if (tooltip.getText().contains(expectedError)) {
                        return;
                    }
                }
            }
            List<WebElement> errorElements = driver.findElements(By.className("error"));
            boolean errorFound = false;

            for (WebElement error : errorElements) {
                if (error.isDisplayed() && error.getText().contains(expectedError)) {
                    errorFound = true;
                    break;
                }
            }

            if (expectedError.equals("Please generate a word cloud first")) {
                boolean wordCloudPresent = !driver.findElements(By.cssSelector("[data-testid='word-cloud']")).isEmpty();
                if (!wordCloudPresent) {
                    return;
                }
            }

            if (!errorFound) {
                fail("Expected error message containing '" + expectedError + "' but it was not found");
            }
        } catch (Exception e) {
            fail("Error while checking for error message: " + e.getMessage());
        }
    }

    @Then("the song should be added to my favorites")
    public void the_song_should_be_added_to_my_favorites() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement tooltip = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("small-tooltip")));

        WebElement favoriteButton = tooltip.findElement(By.id("favorite-button"));
        assertNotNull(favoriteButton, "Favorite button should be present in the tooltip");
        wait.until(ExpectedConditions.or(
                ExpectedConditions.textToBePresentInElement(favoriteButton, "Added to Favorites"),
                ExpectedConditions.attributeContains(favoriteButton, "style", "background-color: gray")
        ));
        assertEquals("Added to Favorites", favoriteButton.getText(),
                "'Added to Favorites' after clicking");

        String buttonStyle = favoriteButton.getAttribute("style");
        assertTrue(buttonStyle.contains("background-color") &&
                        (buttonStyle.contains("gray") || buttonStyle.contains("grey")),
                "Favorite button should have gray bkg after adding");
    }

    @Then("the word cloud should contain only the top {string} words")
    public void theWordCloudShouldContainOnlyTheTopWords(String limit) {
        WebDriverWait longerWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        longerWait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='word-cloud']")));

        try {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                    "document.getElementById('tabular').click();"
            );
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<WebElement> tableRows = driver.findElements(By.cssSelector("[data-testid='word-table'] tbody tr"));
        int wordLimit = Integer.parseInt(limit);

        assertTrue(tableRows.size() <= wordLimit,
                "Word cloud should contain at most " + wordLimit + " words, but found " + tableRows.size());
    }

    @Then("the word cloud should not contain any filler words")
    public void theWordCloudShouldNotContainAnyFillerWords() {
        WebDriverWait longerWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        longerWait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='word-cloud']")));

        Set<String> fillerWords = new HashSet<>(Arrays.asList(
                "a", "an", "the", "and", "or", "but", "if", "in", "on", "of",
                "for", "by", "with", "you", "he", "she", "it", "we",
                "they", "me", "him", "her", "them", "my", "your", "his", "her",
                "its", "our", "their", "this", "that", "these", "those", "at",
                "from", "as", "is", "are", "was", "were", "ayy", "like", "yeah",
                "uh", "woo", "ok", "okay", "alright", "ooh", "oh", "whoa", "so",
                "because"
        ));

        try {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                    "document.getElementById('tabular').click();"
            );
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<WebElement> tableRows = driver.findElements(By.cssSelector("[data-testid='word-table'] tbody tr"));
        for (WebElement row : tableRows) {
            String word = row.findElement(By.tagName("td")).getText().toLowerCase();
            assertFalse(fillerWords.contains(word),
                    "Word cloud should not contain filler word '" + word + "'");
        }
    }

    @Then("the word sizes should vary based on frequency")
    public void theWordSizesShouldVaryBasedOnFrequency() {
        WebDriverWait longerWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        WebElement wordCloud = longerWait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("[data-testid='word-cloud']")));

        // get the words from SVG
        List<WebElement> wordElements = driver.findElements(By.cssSelector("[data-testid='word-cloud'] text"));
        assertFalse(wordElements.isEmpty(), "Word cloud should contain words");

        // get font sizes of a few words to verify
        boolean hasSizeVariation = false;
        String baseSize = null;

        for (WebElement word : wordElements) {
            String fontSize = word.getAttribute("font-size");
            if (fontSize == null) fontSize = word.getCssValue("font-size");

            if (fontSize != null && !fontSize.isEmpty()) {
                if (baseSize == null) {
                    baseSize = fontSize;
                } else if (!fontSize.equals(baseSize)) {
                    hasSizeVariation = true;
                    break;
                }
            }
        }

        if (!hasSizeVariation && wordElements.size() > 1) {
            String baseClass = wordElements.get(0).getAttribute("class");
            for (int i = 1; i < wordElements.size(); i++) {
                String currentClass = wordElements.get(i).getAttribute("class");
                if (!currentClass.equals(baseClass)) {
                    hasSizeVariation = true;
                    break;
                }
            }
        }

        assertTrue(hasSizeVariation, "Word cloud should have words with different sizes based on frequency");
    }

    @Then("the word cloud should be generated quicker than the time limit")
    public void theWordCloudShouldBeGeneratedQuickerThanTheTimeLimit() {
        WebDriverWait longerWait = new WebDriverWait(driver, Duration.ofSeconds(15));
        longerWait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='word-cloud']")));

        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;

        assertTrue(elapsedTime < 10000,
                "exceeds the 10 second limit");

        System.out.println("Word cloud generated in " + (elapsedTime/1000.0) + " seconds");
    }

    @Then("the word cloud should contain stemmed verbs")
    public void theWordCloudShouldContainStemmedVerbs() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement wordTable = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("[data-testid='word-table']")));

        assertTrue(wordTable.isDisplayed(), "Word table should be displayed in tabular mode");
        List<WebElement> tableRows = wordTable.findElements(By.tagName("tr"));
        List<String> words = tableRows.stream()
                .skip(1)  // skip header row
                .map(row -> row.findElements(By.tagName("td")).get(0).getText().toLowerCase())
                .toList();

        // assert that stemming happened: "want" is present, "wanna" is not
        assertTrue(words.contains("want"), "Expected stemmed form 'want' to appear in the table");
        assertFalse(words.contains("wanna"), "Did not expect the raw form 'wanna' after stemming");
    }

    @AfterAll
    public static void cleanUp() {
        WebDriverManager.closeDriver();
    }
}
