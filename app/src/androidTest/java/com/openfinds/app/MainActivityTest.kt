package com.openfinds.app

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies the real Activity + Hilt dependency graph launches cleanly end to
 * end. This is intentionally light — a full Compose UI walkthrough of the
 * onboarding flow requires the on-device permission dialogs to be handled,
 * which is left to manual QA (see README's testing notes).
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @Test
    fun mainActivity_launchesWithoutCrashing() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                assert(!activity.isFinishing) { "MainActivity finished unexpectedly during launch" }
            }
        }
    }
}
