package com.auditory.trackoccupancy

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.auditory.trackoccupancy.ui.auth.LoginActivity
import com.auditory.trackoccupancy.ui.cities.CitiesAdapter
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {

    @Before
    fun setUp() {
        // Set up a fake auth token to simulate logged-in state
        // This allows MainActivity tests to run without going through login flow
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("auth_token", "test_token_${System.currentTimeMillis()}").apply()
    }

    @Test
    fun testLanguageSwitching() {
        // Launch the main activity (with auth token set in setUp)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->

            // Check that the language FAB is displayed
            onView(withId(R.id.languageFab))
                .check(matches(isDisplayed()))

            // Click the language FAB
            onView(withId(R.id.languageFab))
                .perform(click())

            // Verify the language dialog appears
            onView(withText(R.string.language))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun testNavigationFlowVisibility() {
        // Launch the main activity (with auth token set in setUp)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->

            // Check that we're on the cities fragment initially
            // (This assumes the app starts with cities fragment)
            onView(withId(R.id.citiesRecyclerView))
                .check(matches(isDisplayed()))

            // Check that the language FAB is visible
            onView(withId(R.id.languageFab))
                .check(matches(isDisplayed()))
        }
    }

    @Test
    fun testFullLoginFlowToMainActivity() {
        // Clear any existing auth token to test login flow
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("auth_token").apply()

        // Launch the login activity (app entry point)
        ActivityScenario.launch(LoginActivity::class.java).use { scenario ->

            // Verify we're on login screen
            onView(withId(R.id.loginCardView))
                .check(matches(isDisplayed()))

            // Enter credentials
            onView(withId(R.id.loginEditText))
                .perform(typeText("admin"), closeSoftKeyboard())

            onView(withId(R.id.passwordEditText))
                .perform(typeText("admin"), closeSoftKeyboard())

            // Click login button
            onView(withId(R.id.loginButton))
                .perform(click())

            // Note: In a real integration test with a running server or MockWebServer,
            // you would wait for the network call and verify navigation to MainActivity.
            // This test verifies the login flow can be initiated.
        }
    }

    // Note: More comprehensive UI tests would require:
    // 1. MockWebServer to provide test data
    // 2. Idling resources for network calls
    // 3. Hilt test modules to inject test dependencies
    //
    // Example of what a full navigation test might look like with proper mocking:
    /*
    @Test
    fun testFullNavigationFlow() {
        // Given - Mock API responses with test data

        // Launch app (with auth token set)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->

            // Click on first city
            onView(withId(R.id.citiesRecyclerView))
                .perform(actionOnItemAtPosition<CitiesAdapter.CityViewHolder>(0, click()))

            // Verify we're on buildings screen
            onView(withId(R.id.buildingsRecyclerView))
                .check(matches(isDisplayed()))

            // Click on first building
            onView(withId(R.id.buildingsRecyclerView))
                .perform(actionOnItemAtPosition<BuildingsAdapter.BuildingViewHolder>(0, click()))

            // Verify we're on auditoriums screen
            onView(withId(R.id.auditoriumsRecyclerView))
                .check(matches(isDisplayed()))

            // Click on first auditorium
            onView(withId(R.id.auditoriumsRecyclerView))
                .perform(actionOnItemAtPosition<AuditoriumsAdapter.AuditoriumViewHolder>(0, click()))

            // Verify we're on camera screen
            onView(withId(R.id.cameraImageView))
                .check(matches(isDisplayed()))
        }
    }
    */
}
