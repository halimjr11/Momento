package com.nurhaqhalim.momento.view.auth

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.nurhaqhalim.momento.R
import com.nurhaqhalim.momento.utils.MoIdlingResource
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4ClassRunner::class)
class LoginActivityTest {

    private val dummyEmail = "dodo@gmail.com"
    private val dummyPassword = "papa1234"

    @Before
    fun setup() {
        ActivityScenario.launch(LoginActivity::class.java)
        IdlingRegistry.getInstance().register(MoIdlingResource.idlingResource)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(MoIdlingResource.idlingResource)
    }

    @Test
    fun openAppAndtryToLoginWithEmailAndPassword() {
        onView(withId(R.id.login_title)).check(matches(isDisplayed()))
        onView(withId(R.id.login_title)).check(matches(withText(R.string.app_name)))
        onView(withId(R.id.login_description)).check(matches(isDisplayed()))
        onView(withId(R.id.login_description)).check(matches(withText(R.string.login_title_text)))
        onView(withId(R.id.ed_login_email)).check(matches(isDisplayed()))
        onView(withId(R.id.ed_login_password)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_login)).check(matches(withText(R.string.login)))
        onView(withId(R.id.login_register)).check(matches(isDisplayed()))
        onView(withId(R.id.ed_login_email)).perform(typeText(dummyEmail), closeSoftKeyboard())
        onView(withId(R.id.ed_login_password)).perform(typeText(dummyPassword), closeSoftKeyboard())
        onView(withId(R.id.btn_login)).perform(click())
    }

}