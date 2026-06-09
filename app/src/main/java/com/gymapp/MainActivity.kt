package com.gymapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymapp.domain.model.ThemeMode
import com.gymapp.ui.navigation.GymAppRoot
import com.gymapp.ui.screens.login.LoginScreen
import com.gymapp.ui.theme.GymTheme
import com.gymapp.util.LocalWindowSize
import com.gymapp.util.SessionManager
import com.gymapp.util.ThemeManager
import com.gymapp.util.toWindowSize
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Single-Activity Compose host. Wires the persisted theme + window breakpoint and gates on login. */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var sessionManager: SessionManager

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by themeManager.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
            val signedIn by produceState<Boolean?>(initialValue = null, sessionManager) {
                sessionManager.signedIn.collect { value = it }
            }
            val windowSize = calculateWindowSizeClass(this).widthSizeClass.toWindowSize()
            GymTheme(themeMode = themeMode) {
                CompositionLocalProvider(LocalWindowSize provides windowSize) {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        when (signedIn) {
                            null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                            false -> LoginScreen()
                            true -> GymAppRoot()
                        }
                    }
                }
            }
        }
    }
}
