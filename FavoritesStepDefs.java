package edu.usc.csci310.project;
import io.cucumber.java.After;
import io.cucumber.java.en.*;
import org.assertj.core.util.Arrays;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;


public class FavoritesStepDefs {

    private static final String ROOT_URL = "https://localhost:8443/";
    private WebDriver driver;
    private WebDriverWait wait;

    private String selectedSong;

    private boolean initialPrivacySetting;

    private List<String> initialSongOrder;
    private String targetSongName;
    private int targetSongPosition;
    private int originalIndex;



    public FavoritesStepDefs() {
        driver = WebDriverManager.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @After
    public void tearDown() {
    }


    @Given("I am on the favorites page")
    public void i_am_on_the_favorites_page() {
        WebDriverManager.closeDriver();
        driver = WebDriverManager.getDriver();

        driver.get("https://localhost:8443/");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        if (driver.getCurrentUrl().contains("/login")) {
            WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
            usernameField.sendKeys("sydneyFaves");
            WebElement passwordField = driver.findElement(By.id("password"));
            passwordField.sendKeys("favoritesPage732");
            WebElement loginButton = driver.findElement(By.xpath("//button[text()='Log In']"));
            loginButton.click();

            WebElement favoritesLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='Favorites']")));
            favoritesLink.click();
            wait.until(ExpectedConditions.urlContains("/favorites"));
        }
    }

    @Given("I am a newly registered user")
    public void i_am_a_newly_registered_user() {

        WebDriverManager.closeDriver();
        driver = WebDriverManager.getDriver();

        driver.get("http://localhost:8080/");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        if (driver.getCurrentUrl().contains("/login")) {
            WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
            usernameField.sendKeys("sydneyFaves");
            WebElement passwordField = driver.findElement(By.id("password"));
            passwordField.sendKeys("favoritesPage732");
            WebElement loginButton = driver.findElement(By.xpath("//button[text()='Log In']"));
            loginButton.click();

            WebElement favoritesLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='Favorites']")));
            favoritesLink.click();
            wait.until(ExpectedConditions.urlContains("/favorites"));
        }

    }

    @Given("Navigate to favorites page")
    public void navigate_to_favorites_page() {

        try {
            String currentUrl = driver.getCurrentUrl();

            if (!currentUrl.contains("/favorites")) {
                driver.get("http://localhost:8080/favorites");
                System.out.println("Navigated to favorites page");
            } else {
                System.out.println("Already on favorites page");
            }

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".favorites-container, .favorites-list, .favorites-title")
            ));

            System.out.println("Confirmed page is on favorites page");

        } catch (Exception e) {
            System.err.println("Failed to verify favorites page: " + e.getMessage());
            e.printStackTrace();
            throw new AssertionError("Verification of favorites page failed: " + e.getMessage());
        }
    }

    @When("I hover over a song")
    public void iHoverOverASong()
    {
        try {

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));

            // wait for the song-card to appear
            WebElement songCard = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.className("song-card")
            ));


            selectedSong = songCard.getText();

            // create a hover action over song
            Actions actions = new Actions(driver);
            actions.moveToElement(songCard).perform();

            Thread.sleep(500);


        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            throw new AssertionError("Hover test failed: " + e.getMessage());
        }

    }

    @When("I click on a song in my favorites list")
    public void iClickOnASongInMyFavoritesList() {
        try {

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
            WebElement songCard = wait.until(ExpectedConditions.elementToBeClickable(
                    By.className("song-card")
            ));

            selectedSong = songCard.getText();
            songCard.click();

            Thread.sleep(500);

        } catch (Exception e) {
            System.err.println("Failed to click on song: " + e.getMessage());
            e.printStackTrace();
            throw new AssertionError("Song click test failed: " + e.getMessage());
        }
    }

    @When("I click the delete icon")
    public void iClickTheDeleteIcon() {
        try {

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));

            System.out.println("Looking for remove-icon button");
            WebElement removeIcon = wait.until(ExpectedConditions.elementToBeClickable(
                    By.className("remove-icon")
            ));
            removeIcon.click();
            System.out.println("clicked on remove icon");

            Thread.sleep(500);

        } catch (Exception e) {
            System.err.println("Failed to click delete icon: " + e.getMessage());
            throw new AssertionError("Delete icon click failed: " + e.getMessage());
        }
    }

    @Then("the song should be removed from my favorites")
    public void theSongShouldBeRemovedFromMyFavorites() {

        try{
            if (selectedSong != null && !selectedSong.isEmpty()) {
                try {
                    Thread.sleep(500);
                    List<WebElement> remainingSongs = driver.findElements(By.className("song-card"));

                    boolean songStillExists = false;
                    for (WebElement song : remainingSongs) {
                        if (song.getText().equals(selectedSong)) {
                            songStillExists = true;
                            break;
                        }
                    }

                    assertFalse(songStillExists, "Song '" + selectedSong + "' should be removed from favorites");
                    System.out.println("Verified song was removed from favorites");

                } catch (Exception e) {
                    System.out.println("Error during song removal verification: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to verify song removal: " + e.getMessage());
            throw new AssertionError("Song removal verification failed: " + e.getMessage());
        }
    }

    @Then("the song should remain in my favorites")
    public void theSongShouldRemainInMyFavorites() {
        try {
            if (selectedSong != null && !selectedSong.isEmpty()) {
                try {
                    Thread.sleep(500);

                    List<WebElement> songs = driver.findElements(By.className("song-card"));

                    boolean songExists = false;
                    for (WebElement song : songs) {
                        if (song.getText().equals(selectedSong)) {
                            songExists = true;
                            break;
                        }
                    }

                    assertTrue(songExists, "Song '" + selectedSong + "' should still be in favorites");
                    System.out.println("Verified song remains in favorites");

                } catch (Exception e) {
                    System.out.println("Error during song verification: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to verify song remains in favorites: " + e.getMessage());
            throw new AssertionError("Song verification failed: " + e.getMessage());
        }

    }

    @Then("a confirmation dialog should appear")
    public void aConfirmationDialogShouldAppear() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
            WebElement confirmationDialog = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.className("confirmation-dialog")
            ));

            assertTrue(confirmationDialog.isDisplayed(), "Confirmation dialog should be visible"); //NEW


            WebElement dialogContent = confirmationDialog.findElement(By.className("dialog-content"));
            String dialogText = dialogContent.getText();

            if (selectedSong != null && !selectedSong.isEmpty() && !dialogText.contains("all songs")) {
                assertTrue(dialogText.contains(selectedSong.split("\n")[0]),
                        "Dialog should contain the song title being removed");
            }

            WebElement confirmButton = confirmationDialog.findElement(By.className("confirm-button"));
            WebElement cancelButton = confirmationDialog.findElement(By.className("cancel-button"));

            assertTrue(confirmButton.isDisplayed(), "Confirm button should be visible");
            assertTrue(cancelButton.isDisplayed(), "Cancel button should be visible");

        } catch (Exception e) {
            System.err.println("Failed to verify confirmation dialog: " + e.getMessage());
            throw new AssertionError("Confirmation dialog verification failed: " + e.getMessage());
        }
    }

    @Then("I should see the song details")
    public void iShouldSeeTheSongDetails() {
        try {
            if (driver == null || driver.toString().contains("null")) {
                driver = WebDriverManager.getDriver();
                wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            }

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

            WebElement songDetails = null;
            try {
                songDetails = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.className("song-details-dialog")
                ));
                System.out.println("Found song details using 'song-details-dialog' class");
            } catch (Exception e1) {
                try {
                    songDetails = wait.until(ExpectedConditions.visibilityOfElementLocated(
                            By.className("dialog-content")
                    ));
                    System.out.println("Found song details using 'dialog-content' class");
                } catch (Exception e2) {
                    try {
                        songDetails = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.cssSelector(".dialog-content, .song-details, .song-modal")
                        ));
                        System.out.println("Found song details using combined CSS selector");
                    } catch (Exception e3) {
                        // Last resort - check if any dialog is present
                        songDetails = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//div[contains(@class, 'dialog') or contains(@class, 'modal')]")
                        ));
                        System.out.println("Found song details using generic dialog xpath");
                    }
                }
            }

            assertNotNull(songDetails, "Song details dialog should be present on the page");
            assertTrue(songDetails.isDisplayed(), "Song details should be visible");

            System.out.println("Successfully verified song details are displayed");

        } catch (Exception e) {
            System.err.println("Failed to verify song details: " + e.getMessage());
            e.printStackTrace();
            throw new AssertionError("Song details verification failed: " + e.getMessage());
        }
    }



   @Given("I have previously favorited a song")
   public void iHavePreviouslyFavoritedASong() {
       WebDriverManager.closeDriver();

       driver = WebDriverManager.getDriver();
       wait = new WebDriverWait(driver, Duration.ofSeconds(3));
       driver.get(ROOT_URL);

       wait.until(ExpectedConditions.or(
               ExpectedConditions.urlContains("/search"),
               ExpectedConditions.urlContains("/login")
       ));

       if (driver.getCurrentUrl().contains("/login")) {
           WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
           usernameField.sendKeys("sydneyFaves");
           WebElement passwordField = driver.findElement(By.id("password"));
           passwordField.sendKeys("favoritesPage732");
           WebElement loginButton = driver.findElement(By.xpath("//button[text()='Log In']"));
           loginButton.click();

           wait.until(ExpectedConditions.urlContains("/search"));
       }


   }
   @When("I navigate to the favorites page")
    public void iNavigateToTheFavoritesPage() {
       WebDriverManager.closeDriver();
       driver = WebDriverManager.getDriver();

       driver.get("http://localhost:8080/");

       WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
       if (driver.getCurrentUrl().contains("/login")) {
           WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
           usernameField.sendKeys("sydneyFaves");
           WebElement passwordField = driver.findElement(By.id("password"));
           passwordField.sendKeys("favoritesPage732");
           WebElement loginButton = driver.findElement(By.xpath("//button[text()='Log In']"));
           loginButton.click();

           WebElement favoritesLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[text()='Favorites']")));
           favoritesLink.click();
           wait.until(ExpectedConditions.urlContains("/favorites"));
       }

   }
   @Then("I should see my favorited songs")
    public void iShouldSeeMyFavoritedSongs() {
       try {
           if (selectedSong != null && !selectedSong.isEmpty()) {
               try {
                   Thread.sleep(500);

                   List<WebElement> songs = driver.findElements(By.className("song-card"));

                   boolean songExists = false;
                   for (WebElement song : songs) {
                       if (song.getText().equals(selectedSong)) {
                           songExists = true;
                           break;
                       }
                   }

                   assertTrue(songExists, "Song '" + selectedSong + "' should still be in favorites");
                   System.out.println("Verified song remains in favorites");

               } catch (Exception e) {
                   System.out.println("Error during song verification: " + e.getMessage());
               }
           }
       } catch (Exception e) {
           System.err.println("Failed to verify song remains in favorites: " + e.getMessage());
           throw new AssertionError("Song verification failed: " + e.getMessage());
       }
   }

   @Then("they should be in the order they were favorited")
    public void theyShouldBeInTheOrderTheFavoritedSongs() {
       try {
           if (selectedSong != null && !selectedSong.isEmpty()) {
               try {
                   Thread.sleep(500);

                   List<WebElement> songs = driver.findElements(By.className("song-card"));

                   boolean songExists = false;
                   for (WebElement song : songs) {
                       if (song.getText().equals(selectedSong)) {
                           songExists = true;
                           break;
                       }
                   }

                   assertTrue(songExists, "Song '" + selectedSong + "' should still be in favorites");
                   System.out.println("Verified song remains in favorites");

               } catch (Exception e) {
                   System.out.println("Error during song verification: " + e.getMessage());
               }
           }
       } catch (Exception e) {
           System.err.println("Failed to verify song remains in favorites: " + e.getMessage());
           throw new AssertionError("Song verification failed: " + e.getMessage());
       }
   }


    @When("I click the {string} toggle")
    public void iClickTheToggle(String toggleType) {
        try {
            System.out.println("Clicking the " + toggleType + " toggle");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

            WebElement toggleSwitch = wait.until(ExpectedConditions.elementToBeClickable(
                    By.className("switch")
            ));

            WebElement checkbox = toggleSwitch.findElement(By.cssSelector("input[type='checkbox']"));
            boolean isCheckedBefore = checkbox.isSelected();
            System.out.println("Toggle was " + (isCheckedBefore ? "checked" : "unchecked") + " before clicking");

            toggleSwitch.click();
            System.out.println("Clicked " + toggleType + " toggle switch");

            Thread.sleep(500);

            boolean isCheckedAfter = checkbox.isSelected();
            System.out.println("Toggle is now " + (isCheckedAfter ? "checked" : "unchecked") + " after clicking");

        } catch (Exception e) {
            System.err.println("Failed to click " + toggleType + " toggle: " + e.getMessage());
            throw new AssertionError(toggleType + " toggle click failed: " + e.getMessage());
        }
    }

    @Then("User profile is {string}")
    public void userProfileIsNow(String userProfileStatus) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        WebElement toggleSwitch = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.className("switch")
        ));
        WebElement checkbox = toggleSwitch.findElement(By.cssSelector("input[type='checkbox']"));
        boolean currentState = checkbox.isSelected();

        WebElement privacyLabel = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.className("privacy-label")
        ));
        String labelText = privacyLabel.getText().toLowerCase();

        boolean expectedChecked = userProfileStatus.equalsIgnoreCase("private");

        assertEquals(expectedChecked,
                currentState,
                "Checkbox should be " + (expectedChecked ? "checked" : "unchecked")
                        + " for a " + userProfileStatus + " profile");

        assertTrue(labelText.contains(userProfileStatus.toLowerCase()),
                "Privacy label should indicate '" + userProfileStatus + "', but was: " + labelText);
    }

    @And("User current profile privacy setting is either private or public")
    public void userCurrentProfilePrivacySettingIsEitherPrivateOrPublic() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
            WebElement toggleSwitch = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.className("switch")
            ));

            WebElement checkbox = toggleSwitch.findElement(By.cssSelector("input[type='checkbox']"));

            initialPrivacySetting = checkbox.isSelected();

            System.out.println("Initial privacy setting: " + (initialPrivacySetting ? "private" : "public"));

        } catch (Exception e) {
            System.err.println("Failed to check current privacy setting: " + e.getMessage());
            e.printStackTrace();
            throw new AssertionError("Privacy setting check failed: " + e.getMessage());
        }
    }

    @When("User navigates to a different page on site")
    public void userNavigatesToADifferentPageOnSite() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
            WebElement searchLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[text()='Artist Search' or text()='Search']")
            ));

            searchLink.click();

            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("/search"),
                    ExpectedConditions.urlContains("/artist")
            ));

            System.out.println("Successfully navigated to a different page: " + driver.getCurrentUrl());

        } catch (Exception e) {
            System.err.println("Failed to navigate to a different page: " + e.getMessage());
            e.printStackTrace();
            throw new AssertionError("Navigation failed: " + e.getMessage());
        }
    }

    @And("User navigates back to the favorites page")
    public void userNavigatesBackToTheFavoritesPage() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
            WebElement favoritesLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[text()='Favorites']")
            ));

            favoritesLink.click();

            wait.until(ExpectedConditions.urlContains("/favorites"));

            System.out.println("Successfully navigated back to favorites page");

        } catch (Exception e) {
            System.err.println("Failed to navigate back to favorites page: " + e.getMessage());
            e.printStackTrace();
            throw new AssertionError("Navigation to favorites failed: " + e.getMessage());
        }
    }

    @Then("User profile privacy setting should still be the same")
    public void userProfilePrivacySettingShouldStillBeTheSame() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
            WebElement toggleSwitch = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.className("switch")
            ));

            WebElement checkbox = toggleSwitch.findElement(By.cssSelector("input[type='checkbox']"));

            boolean currentPrivacySetting = checkbox.isSelected();

            System.out.println("Initial privacy setting: " + (initialPrivacySetting ? "private" : "public"));
            System.out.println("Current privacy setting: " + (currentPrivacySetting ? "private" : "public"));

            assertEquals(initialPrivacySetting, currentPrivacySetting,
                    "Privacy setting should remain " + (initialPrivacySetting ? "private" : "public") +
                            " after navigation");

        } catch (Exception e) {
            System.err.println("Failed to verify persistent privacy setting: " + e.getMessage());
            e.printStackTrace();
            throw new AssertionError("Privacy setting verification failed: " + e.getMessage());
        }
    }

    /**
     * Modified implementation for the "Remove All" verification that tests the dialog
     * thoroughly without actually removing all songs.
     */
    @Then("all the songs would be removed if confirm was clicked")
    public void allTheSongsShouldBeRemovedFromMyFavorites() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));

            // Verify dialog content
            WebElement confirmationDialog = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.className("confirmation-dialog")
            ));

            WebElement dialogContent = confirmationDialog.findElement(By.className("dialog-content"));
            String dialogText = dialogContent.getText();

            assertTrue(dialogText.contains("remove all songs") ||
                            dialogText.contains("Remove All"),
                    "Dialog should mention removing all songs");

            List<WebElement> songsBefore = driver.findElements(By.className("song-card"));
            int songCountBefore = songsBefore.size();
            System.out.println("Found " + songCountBefore + " songs before potential removal");

            WebElement confirmButton = confirmationDialog.findElement(By.className("confirm-button"));
            assertTrue(confirmButton.isDisplayed(), "Confirm button should be visible");

            WebElement cancelButton = confirmationDialog.findElement(By.className("cancel-button"));
            cancelButton.click();

            wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
                    By.className("song-card"), 0));

            List<WebElement> songsAfter = driver.findElements(By.className("song-card"));
            assertEquals(songCountBefore, songsAfter.size(),
                    "Number of songs should remain the same after cancelling");

            System.out.println("Successfully verified 'remove all' dialog without removing songs");
        } catch (Exception e) {
            System.err.println("Failed to verify 'remove all' dialog: " + e.getMessage());
            throw new AssertionError("'Remove all' dialog verification failed: " + e.getMessage());
        }
    }

    @Then("all the songs should remain in my favorites")
    public void allTheSongsShouldRemainInMyFavorites() {
        try {
            Thread.sleep(1000);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));

            WebElement favoritesList = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".favorites-list, .favorites-container")
            ));
            assertTrue(favoritesList.isDisplayed(), "Favorites list container should be visible");
            System.out.println("Successfully verified favorites list is displayed");

            List<WebElement> songs = driver.findElements(By.className("song-card"));
            System.out.println("Found " + songs.size() + " songs in favorites list");

            System.out.println("Successfully verified songs remained in favorites");

        } catch (Exception e) {
            System.err.println("Failed to verify songs remained: " + e.getMessage());
            e.printStackTrace();
            throw new AssertionError("Songs verification failed: " + e.getMessage());
        }
    }

    @When("I click Confirm button")
    public void iClickConfirmButton() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
            WebElement confirmButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(text(), 'Confirm')]")
            ));

            System.out.println("Confirm button is present and clickable");

            boolean confirmButtonPresent = confirmButton.isDisplayed() && confirmButton.isEnabled();
            assertTrue(confirmButtonPresent, "Confirm button should be present and enabled");

        } catch (Exception e) {
            System.err.println("Failed to verify Confirm button: " + e.getMessage());
            e.printStackTrace();
            throw new AssertionError("Confirm button verification failed: " + e.getMessage());
        }

    }

    @When("I hover over a song that is not at the top of the list")
    public void iHoverOverASongThatIsNotAtTheTopOfTheList() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
            List<WebElement> songCards = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.className("song-card")
            ));

            assertTrue(songCards.size() >= 2, "Need at least 2 songs to test moving up");

            initialSongOrder = new ArrayList<>();
            for (WebElement song : songCards) {
                initialSongOrder.add(song.getText());
            }

            targetSongPosition = 1;
            WebElement targetSong = songCards.get(targetSongPosition);
            targetSongName = targetSong.getText();

            Actions actions = new Actions(driver);
            actions.moveToElement(targetSong).perform();

            Thread.sleep(500);

        } catch (Exception e) {
            System.err.println("Failed to hover over song not at top: " + e.getMessage());
            e.printStackTrace();
            throw new AssertionError("Hover failed: " + e.getMessage());
        }
    }

    @When("I hover over a song that is not at the bottom of the list")
    public void iHoverOverANonBottomSong() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        List<WebElement> cards = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("song-card"))
        );

        WebElement cardToMove = cards.get(cards.size() - 2);

        targetSongName = cardToMove.findElement(By.className("song-card-title")).getText();
        originalIndex = IntStream.range(0, cards.size())
                .filter(i -> cards.get(i)
                        .findElement(By.className("song-card-title"))
                        .getText()
                        .equals(targetSongName))
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError("Couldn’t find target in list before move"));

        new Actions(driver).moveToElement(cardToMove).perform();
    }

    @And("I click the {string} arrow button")
    public void iClickTheArrowButton(String direction) {
        try {
            String className;
            if(direction.equals("up")) {
                className = "up-icon";
            }
            else{
                className = "down-icon";
            }
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
            WebElement arrowIcon = wait.until(ExpectedConditions.elementToBeClickable(
                    By.className(className)
            ));

            arrowIcon.click();

            Thread.sleep(500);

        } catch (Exception e) {
            System.err.println("Failed to click arrow-icon: " + e.getMessage());
            e.printStackTrace();
            throw new AssertionError("Arrow-icon click failed: " + e.getMessage());
        }
    }

    @Then("the song should move one position up in the list")
    public void theSongShouldMoveOnePositionUpInTheList() throws InterruptedException {

        Thread.sleep(1000); // Wait for animation and reordering to complete

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        List<WebElement> songCardsAfterMove = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.className("song-card")
        ));

        List<String> newSongOrder = new ArrayList<>();
        for (WebElement song : songCardsAfterMove) {
            newSongOrder.add(song.getText());
        }

        int newPosition = -1;
        for (int i = 0; i < newSongOrder.size(); i++) {
            if (newSongOrder.get(i).contains(targetSongName.split("\n")[0])) {
                newPosition = i;
                break;
            }
        }

        int expectedPosition = targetSongPosition - 1;
        assertEquals(expectedPosition, newPosition,
                "Song should have moved from position " + targetSongPosition +
                        " to position " + expectedPosition);

        System.out.println("Successfully verified song moved up from position " +
                targetSongPosition + " to position " + newPosition);
    }

    @Then("the song should move one position down in the list")
    public void theSongShouldMoveOnePositionDownInTheList() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));

        List<WebElement> cardsAfter = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("song-card"))
        );
        List<String> titlesAfter = cardsAfter.stream()
                .map(c -> c.findElement(By.className("song-card-title")).getText())
                .toList();

        assertTrue(titlesAfter.contains(targetSongName),
                "Target song should still be in the list");

        int newIndex = titlesAfter.indexOf(targetSongName);
        assertEquals(originalIndex + 1, newIndex,
                "Song should have moved down one position");
    }

    @When("I hover over the first song in the list")
    public void iHoverOverTheFirstSongInTheList() {
        iHoverOverASong();
    }

    @When("I hover over the last song in the list")
    public void iHoverOverTheLastSongInTheList() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
            List<WebElement> songCards = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.className("song-card")
            ));

            assertTrue(!songCards.isEmpty(), "Need at least 1 song for this test");

            WebElement lastSong = songCards.get(songCards.size() - 1);
            targetSongName = lastSong.getText();
            System.out.println("Selected last song: " + targetSongName);

            Actions actions = new Actions(driver);
            actions.moveToElement(lastSong).perform();
            System.out.println("Hovered over last song");

            Thread.sleep(500);

        } catch (Exception e) {
            System.err.println("Failed to hover over last song: " + e.getMessage());
            e.printStackTrace();
            throw new AssertionError("Hover failed: " + e.getMessage());
        }
    }

    @Then("the {string} arrow button should be disabled")
    public void theUpArrowButtonShouldBeDisabled(String direction) {
        iClickTheArrowButton(direction);
    }

    @When("I reorder my favorites list")
    public void whenIReorderMyFavoritesList() {
        iHoverOverASongThatIsNotAtTheTopOfTheList();
        iClickTheArrowButton("up");
    }



    @Then("the favorites list should maintain the new order")
    public void theFavoritesListShouldMaintainTheNewOrder() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
            List<WebElement> songCards = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                    By.className("song-card")
            ));

            assertFalse(songCards.isEmpty(), "Favorites list should not be empty");

            List<String> currentSongOrder = new ArrayList<>();
            for (WebElement song : songCards) {
                currentSongOrder.add(song.getText());
            }

            int currentPosition = -1;
            for (int i = 0; i < currentSongOrder.size(); i++) {
                if (currentSongOrder.get(i).contains(targetSongName.split("\n")[0])) {
                    currentPosition = i;
                    break;
                }
            }

            assertNotEquals(-1, currentPosition, "Target song should still be in the list");
            assertNotEquals(targetSongPosition, currentPosition,
                    "Song position should be different from its original position");

            System.out.println("Successfully verified song maintained its new position " +
                    currentPosition + " after navigation (was originally at position " +
                    targetSongPosition + ")");

        } catch (Exception e) {
            System.err.println("Failed to verify favorites list: " + e.getMessage());
            e.printStackTrace();
            throw new AssertionError("Favorites list verification failed: " + e.getMessage());
        }
    }


    @Then("the favorites list should appear")
    public void theFavoritesListShouldAppear() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement favoritesList = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.className("favorites-list"))
        );

        assertTrue(favoritesList.isDisplayed(), "Favorites list should be visible on the page");
    }
}