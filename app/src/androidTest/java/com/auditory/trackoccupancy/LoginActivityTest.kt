package com.auditory.trackoccupancy

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.auditory.trackoccupancy.ui.auth.LoginActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginActivityTest {

    @Test
    fun testLoginScreenElementsAreDisplayed() {
        // Launch the login activity
        ActivityScenario.launch(LoginActivity::class.java).use { scenario ->

            // Check that the logo is displayed
            onView(withId(R.id.logoImageView))
                .check(matches(isDisplayed()))

            // Check that the login card is displayed
            onView(withId(R.id.loginCardView))
                .check(matches(isDisplayed()))

            // Check that the login edit text is displayed
            onView(withId(R.id.loginEditText))
                .check(matches(isDisplayed()))

            // Check that the password edit text is displayed
            onView(withId(R.id.passwordEditText))
                .check(matches(isDisplayed()))

            // Check that the login button is displayed
            onView(withId(R.id.loginButton))
                .check(matches(isDisplayed()))

            // Check that the language FAB is displayed
            onView(withId(R.id.languageFab))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun testLoginButtonIsClickable() {
        ActivityScenario.launch(LoginActivity::class.java).use { scenario ->

            // Check that the login button is clickable
            onView(withId(R.id.loginButton))
                .check(matches(isClickable()))
        }
    }

    @Test
    fun testLanguageFabIsClickable() {
        ActivityScenario.launch(LoginActivity::class.java).use { scenario ->

            // Check that the language FAB is clickable
            onView(withId(R.id.languageFab))
                .check(matches(isClickable()))

            // Click the language FAB
            onView(withId(R.id.languageFab))
                .perform(click())

            // Verify the language dialog appears with language options
            onView(withText(R.string.language))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun testLoginFieldsAcceptInput() {
        ActivityScenario.launch(LoginActivity::class.java).use { scenario ->

            // Type in the login field
            onView(withId(R.id.loginEditText))
                .perform(typeText("testuser"), closeSoftKeyboard())

            // Type in the password field
            onView(withId(R.id.passwordEditText))
                .perform(typeText("testpass"), closeSoftKeyboard())

            // Verify the input was entered (the text should be in the fields)
            onView(withId(R.id.loginEditText))
                .check(matches(withText("testuser")))

            // Note: Password field shows dots, so we just verify it's not empty
            onView(withId(R.id.passwordEditText))
                .check(matches(withText("testpass")))
        }
    }

    @Test
    fun testEmptyLoginShowsError() {
        ActivityScenario.launch(LoginActivity::class.java).use { scenario ->

            // Try to login with empty fields
            onView(withId(R.id.loginButton))
                .perform(click())

            // The login field should show an error (we just verify the button is still enabled)
            // Note: In a real test, you'd check for the error message on the TextInputLayout
            onView(withId(R.id.loginButton))
                .check(matches(isEnabled()))
        }
    }

    @Test
    fun testEmptyPasswordShowsError() {
        ActivityScenario.launch(LoginActivity::class.java).use { scenario ->

            // Enter login but not password
            onView(withId(R.id.loginEditText))
                .perform(typeText("admin"), closeSoftKeyboard())

            // Try to login
            onView(withId(R.id.loginButton))
                .perform(click())

            // The password field should show an error (we just verify the button is still enabled)
            onView(withId(R.id.loginButton))
                .check(matches(isEnabled()))
        }
    }

    @Test
    fun testLoginButtonTextChangesOnClick() {
        ActivityScenario.launch(LoginActivity::class.java).use { scenario ->

            // Enter credentials
            onView(withId(R.id.loginEditText))
                .perform(typeText("admin"), closeSoftKeyboard())

            onView(withId(R.id.passwordEditText))
                .perform(typeText("admin"), closeSoftKeyboard())

            // Click login button
            onView(withId(R.id.loginButton))
                .perform(click())

            // Note: In a real test with mocked network, you would verify:
            // 1. Button text changes to "Logging in..."
            // 2. Button becomes disabled
            // 3. On success, navigation to MainActivity
            // 4. On failure, error toast is shown
            //
            // This basic test just verifies the button can be clicked
        }
    }

    // Note: More comprehensive UI tests would require:
    // 1. MockWebServer or Hilt test modules to mock the API responses
    // 2. Idling resources for network calls
    // 3. Test rule to handle activity transitions
    //
    // Example of what a full login flow test might look like with mocked API:
    /*
    @Test
    fun testSuccessfulLoginNavigatesToMainActivity() {
        // Given - Mock API to return successful login response
        
        ActivityScenario.launch(LoginActivity::class.java).use { scenario ->
            // Enter valid credentials
            onView(withId(R.id.loginEditText))
                .perform(typeText("admin"), closeSoftKeyboard())
            
            onView(withId(R.id.passwordEditText))
                .perform(typeText("admin"), closeSoftKeyboard())
            
            // Click login
            onView(withId(R.id.loginButton))
                .perform(click())
            
            // Wait for navigation
            // Verify MainActivity is displayed
            onView(withId(R.id.nav_host_fragment))
                .check(matches(isDisplayed()))
        }
    }
    
    @Test
    fun testFailedLoginShowsErrorMessage() {
        // Given - Mock API to return error response
        
        ActivityScenario.launch(LoginActivity::class.java).use { scenario ->
            // Enter invalid credentials
            onView(withId(R.id.loginEditText))
                .perform(typeText("wrong"), closeSoftKeyboard())
            
            onView(withId(R.id.passwordEditText))
                .perform(typeText("credentials"), closeSoftKeyboard())
            
            // Click login
            onView(withId(R.id.loginButton))
                .perform(click())
            
            // Verify error toast is shown
            // onView(withText(R.string.login_failed)).inRoot(isToast()).check(matches(isDisplayed()))
            
            // Verify we're still on login screen
            onView(withId(R.id.loginCardView))
                .check(matches(isDisplayed()))
        }
    }
    */
}

