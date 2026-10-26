package com.example.catering.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.core.view.WindowCompat
import androidx.appcompat.app.AppCompatActivity
import com.example.catering.R
import com.example.catering.ui.screens.MainScreen
import com.example.catering.ui.theme.CateringTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import android.graphics.Color as AndroidColor

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = AndroidColor.rgb(26, 36, 41)

        WindowCompat.getInsetsController(
            window,
            window.decorView
        ).isAppearanceLightStatusBars = false

        setContent {
            CateringTheme {

                var showSplash by remember {
                    mutableStateOf(true)
                }

                LaunchedEffect(Unit) {
                    delay(1500)
                    showSplash = false
                }
                Crossfade(
                    targetState = showSplash,
                    animationSpec = tween(durationMillis = 600),
                    label = "SiestaSplash"
                ) { isSplashVisible ->

                    if (isSplashVisible) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF0A1014)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(
                                    id = R.drawable.siesta_splash
                                ),
                                contentDescription = "СИЕСТА",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    } else {
                        MainScreen()
                    }
                }
            }
        }
    }
}
