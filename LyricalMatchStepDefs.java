package edu.usc.csci310.project;

import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LyricalMatchStepDefs {

    private WebDriver driver = WebDriverManager.getDriver();
    private WebDriverWait wait;

    public LyricalMatchStepDefs() {
        wait = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    @Given("On the favorites page logged in as {string} with pass {string}")
    public void i_am_on_the_favorites_page_logged_in_as(String username, String pwd) throws InterruptedException {
        WebDriverManager.closeDriver();
        driver = WebDriverManager.getDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        driver.get("https://localhost:8443/login");


        if (driver.getCurrentUrl().contains("/login")) {
            WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
            usernameField.sendKeys(username);
            WebElement passwordField = driver.findElement(By.id("password"));
            passwordField.sendKeys(pwd);
            driver.findElement(By.xpath("//button[text()='Log In']")).click();
        }

        try {
            wait.until(ExpectedConditions.urlContains("/search")); // wait for redirect
            // Find the anchor by exact text and tag
            WebElement favoritesLink = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[@class='nav-link' and text()='Favorites']")
            ));
            favoritesLink.click();

            // Wait until the new route is loaded
            wait.until(ExpectedConditions.urlContains("/favorites"));


        } catch (Exception e) {
            System.out.println("Failed to find or click the Favorites link. Current URL: " + driver.getCurrentUrl());
            throw e;  // Re-throw so test still fails, but with better debug info
        }

    }

    @When("I click the {string} lyrical match button")
    public void i_click_the_button(String label) {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='" + label + "']")));
        button.click();
    }

    @Then("their favorites list should be displayed")
    public void their_favorites_should_be_displayed() {
        List<WebElement> altFavorites = driver.findElements(By.cssSelector(".favorites-list .song-card"));
        assertTrue(!altFavorites.isEmpty(), "Expected at least one song in other user's favorites list but found none.");
    }

    @Then("a {string} label should appear at the top")
    public void a_label_should_appear(String labelText) {
        WebElement label = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".match-banner")));
        assertTrue(label.getText().toLowerCase().contains(labelText.toLowerCase()), "Expected label to contain: " + labelText);
    }

    @And("I have the most similar lyrics with {string}")
    public void iHaveTheMostSimilarLyricsWith(String arg0) {
        // Do nothing, this was only for additional info
    }

    @Then("I should see the user {string}")
    public void iShouldSeeTheUser(String username) {
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(20));
        longWait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.cssSelector(".favorites-title"), username
        ));
        WebElement heading = driver.findElement(By.cssSelector(".favorites-title"));
        assertTrue(
                heading.getText().toLowerCase().contains(username.toLowerCase()),
                "Expected to see user " + username + " but saw: " + heading.getText()
        );
    }

    @And("I have the least similar lyrics with {string}")
    public void iHaveTheLeastSimilarLyricsWith(String arg0) {
        // Do nothing, this was only for additional info
    }

    @And("this user has the same favorite songs as {string}")
    public void thisUserHasTheSameFavoriteSongsAs(String arg0) {
        // Do nothing, this was only for additional info
    }

    @And("they are both each other's top lyrical match")
    public void theyAreBothEachOtherSTopLyricalMatch() {
        // Do nothing, this was only for additional info
    }

    @And("I should see a popup saying {string}")
    public void iShouldSeeAPopupSaying(String expectedText) {
        WebElement popup = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".mutual-popup"))
        );
        assertTrue(
                popup.getText().contains(expectedText),
                "Expected popup text to contain: " + expectedText + " but was: " + popup.getText()
        );
    }

    @And("has only a unique favorite song in different language unlike {string}")
    public void hasOnlyAUniqueFavoriteSongInDifferentLanguageUnlike(String arg0) {
        // Do nothing, this was only for additional info
    }

    @And("they are each other's least similar lyrical match")
    public void theyAreEachOtherSLeastSimilarLyricalMatch() {
        // Do nothing, this was only for additional info
    }
}
