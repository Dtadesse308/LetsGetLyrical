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
import java.time.Instant;

public class LoginRegisterStepDefs {
    private WebDriver driver = WebDriverManager.getDriver();

    @Given("the user is on the login page")
    public void theUserIsOnTheLoginPage() {
        WebDriverManager.closeDriver();

        // Start a new browser session
        driver = WebDriverManager.getDriver();
        driver.get("https://localhost:8443/login");
    }

    @Given("the user is on the registration page")
    public void theUserIsOnTheRegistrationPage() {
        WebDriverManager.closeDriver();
        // Start a new browser session
        driver = WebDriverManager.getDriver();

        driver.get("https://localhost:8443/register");
    }

    @When("the user enters a valid username in the username field")
    public void theUserEntersAValidUsernameInTheField() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        usernameField.clear();
        usernameField.sendKeys("admin");
    }

    @When("enters a valid password in the password field")
    public void entersAValidPasswordInTheField() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        WebElement passwordField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("password")));
        passwordField.clear();
        passwordField.sendKeys("Admin1");
    }

    @When("the user enters an incorrect username in the username field")
    public void theUserEntersAnIncorrectUsernameInTheField() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        usernameField.sendKeys("invalidUser");
    }

    @When("enters an incorrect password in the password field")
    public void entersAnIncorrectPasswordInTheField() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        WebElement passwordField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("password")));
        passwordField.clear();
        passwordField.sendKeys("invalidPassword");
    }

    @When("clicks the {string} button")
    public void clicksTheButton(String button) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement myButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()='" + button + "']")));

        try {
            myButton.click();
        } catch (Exception e) {
            try {
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", myButton);
            } catch (Exception e2) {
                new org.openqa.selenium.interactions.Actions(driver).moveToElement(myButton).click().perform();
            }
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @When("the user enters a valid username and fails login three times")
    public void theUserEntersValidUsernameAndFailsLoginThreeTimes() {
        theUserEntersAValidUsernameInTheField();
        entersAnIncorrectPasswordInTheField();
        clicksTheButton("Log In");
        theUserShouldSeeAnErrorMessage("Wrong password");

        theUserEntersAValidUsernameInTheField();
        entersAnIncorrectPasswordInTheField();
        clicksTheButton("Log In");
        theUserShouldSeeAnErrorMessage("Wrong password");

        theUserEntersAValidUsernameInTheField();
        entersAnIncorrectPasswordInTheField();
        clicksTheButton("Log In");
        theUserShouldSeeAnErrorMessage("Wrong password");

        theUserEntersAValidUsernameInTheField();
        entersAnIncorrectPasswordInTheField();
        clicksTheButton("Log In");
    }

    @When("enters an incorrect password in the password field three times in {int} sec")
    public void entersAnIncorrectPasswordInThePasswordFieldThreeTimesInTimePeriod(int seconds) {
        theUserEntersAValidUsernameInTheField();
        entersAnIncorrectPasswordInTheField();
        clicksTheButton("Log In");
        theUserShouldSeeAnErrorMessage("Wrong password");

        theUserEntersAValidUsernameInTheField();
        entersAnIncorrectPasswordInTheField();
        clicksTheButton("Log In");
        theUserShouldSeeAnErrorMessage("Wrong password");

        waitsSeconds(seconds);
        theUserEntersAValidUsernameInTheField();
        entersAnIncorrectPasswordInTheField();
        clicksTheButton("Log In");
        theUserShouldSeeAnErrorMessage("Wrong password");
    }

    @Then("the user should be redirected to login")
    public void theUserShouldBeRedirectedToLogin() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        boolean urlChanged = wait.until(ExpectedConditions.urlToBe("https://localhost:8443/login"));

        assertTrue(urlChanged, "Redirection to login failed!");
    }

    @Then("the user should be redirected to the dashboard")
    public void theUserShouldBeRedirectedToTheDashboard() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        boolean urlChanged = wait.until(ExpectedConditions.urlToBe("https://localhost:8443/search"));

        assertTrue(urlChanged, "Redirection to dashboard failed!");
    }

    @Then("the user should see an error message {string}")
    public void theUserShouldSeeAnErrorMessage(String expectedErrorMessage) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("error")));

        wait.until(driver -> !errorMessage.getText().trim().isEmpty());

        assertEquals(expectedErrorMessage, errorMessage.getText());
    }

    @Then("the user should see an empty field validation message")
    public void theUserShouldSeeAnErrorMessageForEmptyFields() {
        boolean errorDisplayed = false;
        WebElement usernameField = driver.findElement(By.id("username"));
        WebElement passwordField = driver.findElement(By.id("password"));

        String nameValidationMessage = usernameField.getAttribute("validationMessage");
        String locationValidationMessage = passwordField.getAttribute("validationMessage");

        if (!nameValidationMessage.isEmpty() || !locationValidationMessage.isEmpty()) {
            errorDisplayed = true;
        }

        assertTrue(errorDisplayed, "Expected validation warning message to be displayed.");
    }

    @When("user enters a valid username, password, and confirms password")
    public void theUserEntersValidRegistrationDetails() {
        String uniqueUsername = "user" + Instant.now().getEpochSecond();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username"))).sendKeys(uniqueUsername);

        driver.findElement(By.id("password")).sendKeys("ValidPass123");
        driver.findElement(By.id("password2")).sendKeys("ValidPass123");
    }

    @And("enters a different value in the confirm password field")
    public void entersDifferentConfirmPassword() {
        driver.findElement(By.id("password2")).sendKeys("DifferentPass456");
    }

    @When("the user enters a username that is already taken")
    public void theUserEntersAUsernameThatIsAlreadyTaken() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username"))).sendKeys("admin");
    }

    @When("enters a valid password in the confirm password field")
    public void entersAValidPasswordInTheConfirmationField() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        WebElement passwordField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("password2")));
        passwordField.sendKeys("Admin1");
    }

    @Given("the user has entered {string} in the username field")
    public void theUserHasEnteredInTheUsernameField(String username) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        usernameField.clear();
        usernameField.sendKeys(username);
    }

    @Given("the user has entered {string} in the password field")
    public void theUserHasEnteredInThePasswordField(String password) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        passwordField.clear();
        passwordField.sendKeys(password);
    }

    @Given("the user has entered {string} in the confirm password field")
    public void theUserHasEnteredInTheConfirmPasswordField(String password) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        WebElement confirmField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password2")));
        confirmField.clear();
        confirmField.sendKeys(password);
    }

    @Then("the user should see a confirmation message")
    public void theUserShouldSeeAConfirmationMessage() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));

        WebElement overlay = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div.bg-opacity-50")
        ));

        WebElement msg = overlay.findElement(
                By.xpath(".//p[contains(text(),'Account created successfully')]")
        );
        assertTrue(msg.isDisplayed(), "Confirmation message not displayed");
    }

    @Then("the cancellation confirmation popup should be displayed")
    public void the_cancellation_confirmation_popup_should_be_displayed() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        WebElement overlay = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("div.bg-opacity-50")
        ));
        WebElement msg = overlay.findElement(
                By.xpath(".//p[contains(text(),'Are you sure you want to cancel')]")
        );
        assertTrue(msg.isDisplayed(),
                "Expected cancel-confirmation popup with the proper text");
    }

    @When("the user dismisses the cancellation popup")
    public void the_user_dismisses_the_cancellation_popup() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        WebElement cancelBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[aria-label='dismiss cancel popup']")
        ));
        cancelBtn.click();
    }

    @Then("the username field should still contain {string}")
    public void theUsernameFieldShouldStillContain(String expected) {
        WebElement usernameField = driver.findElement(By.id("username"));
        assertEquals(expected, usernameField.getAttribute("value"));
    }

    @Then("the password field should still contain {string}")
    public void thePasswordFieldShouldStillContain(String expected) {
        WebElement passwordField = driver.findElement(By.id("password"));
        assertEquals(expected, passwordField.getAttribute("value"));
    }

    @Then("the user should remain on the registration page")
    public void theUserShouldRemainOnTheRegistrationPage() {
        assertTrue(driver.getCurrentUrl().contains("/register"), "Expected to remain on registration page");
    }

    @When("enters {string} in the password field")
    public void entersInThePasswordField(String password) {
        theUserHasEnteredInThePasswordField(password);
    }

    @When("enters {string} in the confirm password field")
    public void entersInTheConfirmPasswordField(String password) {
        theUserHasEnteredInTheConfirmPasswordField(password);
    }

    @Then("should see an error message for invalid password")
    public void shouldSeeAnErrorMessageForInvalidPassword() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement errorMessage = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("error")));

        wait.until(driver -> !errorMessage.getText().trim().isEmpty());

        assertEquals(
                "Password must contain at least one uppercase letter, one lowercase letter, and one number.",
                errorMessage.getText()
        );
    }

    @Given("the user navigates to the root URL")
    public void theUserNavigatesToTheRootURL() {
        WebDriverManager.closeDriver();
        driver = WebDriverManager.getDriver();
        driver.get("https://localhost:8443/");
    }

    @Then("the user should be redirected to the login page")
    public void theUserShouldBeRedirectedToTheLoginPage() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        boolean urlChanged = wait.until(ExpectedConditions.urlToBe("https://localhost:8443/login"));

        assertTrue(urlChanged, "Redirection to login page failed!");
    }

    @When("the user clicks the {string} link")
    public void theUserClicksTheLink(String linkText) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.linkText(linkText)));
        link.click();
    }

    @Then("the user should be redirected to the registration page")
    public void theUserShouldBeRedirectedToTheRegistrationPage() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        boolean urlChanged = wait.until(ExpectedConditions.urlToBe("https://localhost:8443/register"));

        assertTrue(urlChanged, "Redirection to registration page failed!");
    }

    @When("the user locks themself out of their account")
    public void locksThemselfOutOfTheirAccount() {
        // this has already happened because the scenario right before this is "user is locked out of their account"
    }

    @When("waits {int} seconds")
    public void waitsSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread was interrupted during wait", e);
        }
    }

    @AfterAll
    public static void cleanUp() {
        WebDriverManager.closeDriver();
    }
}
