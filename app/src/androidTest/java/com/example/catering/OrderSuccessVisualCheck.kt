package com.example.catering

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.catering.ui.screens.OrderSuccessScreen
import com.example.catering.ui.theme.CateringTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class OrderSuccessVisualCheck {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun screenshotAndActions() {
        var destination = ""
        compose.activityRule.scenario.onActivity {
            it.window.statusBarColor = android.graphics.Color.rgb(26, 36, 41)
            WindowCompat.getInsetsController(it.window, it.window.decorView).isAppearanceLightStatusBars = false
        }
        compose.setContent {
            CateringTheme {
                Scaffold { padding ->
                    Box(Modifier.fillMaxSize().padding(padding)) {
                        OrderSuccessScreen(
                            onGoMenu = { destination = "menu" },
                            onGoToOrdersHistory = { destination = "orders" },
                            onGoHome = { destination = "home" }
                        )
                    }
                }
            }
        }
        compose.onNodeWithText("Заказ принят!").assertIsDisplayed()
        compose.onNodeWithText("Перейти к заказам").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals("orders", destination) }
        compose.onNodeWithText("Вернуться в меню").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals("menu", destination) }
        compose.onNodeWithText("<").performClick()
        compose.runOnIdle { assertEquals("home", destination); destination = "" }
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.runOnIdle { assertEquals("home", destination) }
        compose.waitForIdle()
        Thread.sleep(700)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val file = File(instrumentation.targetContext.getExternalFilesDir(null), "siesta-order-success.png")
        file.outputStream().use { instrumentation.uiAutomation.takeScreenshot().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test fun shortDisplayWithLargeTextKeepsActionsVisible() {
        compose.setContent {
            CateringTheme {
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, 1.3f)) {
                    Box(Modifier.height(600.dp)) {
                        OrderSuccessScreen(onGoMenu = {}, onGoToOrdersHistory = {}, onGoHome = {})
                    }
                }
            }
        }
        compose.onNodeWithText("Перейти к заказам").assertIsDisplayed().performClick()
        compose.onNodeWithText("Вернуться в меню").assertIsDisplayed().performClick()
    }
}
