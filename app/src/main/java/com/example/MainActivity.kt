package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.AppLanguage
import com.example.core.BengaliStrings
import com.example.core.EnglishStrings
import com.example.core.LocalAppStrings
import com.example.core.LocalCurrencySymbol
import com.example.core.ThemeMode
import com.example.ui.navigation.BizSphereNavShell
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.PinLockScreen
import com.example.ui.theme.BizSphereTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            val useDarkTheme = when (state.prefs.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            val appStrings = if (state.prefs.language == AppLanguage.BN) BengaliStrings else EnglishStrings

            CompositionLocalProvider(
                LocalAppStrings provides appStrings,
                LocalCurrencySymbol provides state.prefs.currencySymbol
            ) {
                BizSphereTheme(darkTheme = useDarkTheme) {
                    Crossfade(
                        targetState = Triple(state.isLoading, state.prefs.isLoggedIn, state.isPinUnlocked),
                        label = "rootGate"
                    ) { (loading, loggedIn, pinUnlocked) ->
                        when {
                            loading -> Box(modifier = Modifier.fillMaxSize())
                            !loggedIn -> AuthScreen(
                                isRegistered = state.prefs.isRegistered,
                                language = state.prefs.language,
                                onToggleLanguage = {
                                    val next = if (state.prefs.language == AppLanguage.EN) AppLanguage.BN else AppLanguage.EN
                                    viewModel.setLanguage(next)
                                },
                                onRegister = { owner, biz, email, pass ->
                                    viewModel.registerAccount(owner, biz, email, pass)
                                },
                                onLogin = { email, pass ->
                                    viewModel.loginWithCredentials(email, pass)
                                },
                                onQuickStart = { viewModel.continueQuickStart() }
                            )
                            !pinUnlocked -> PinLockScreen(
                                isRegistered = state.prefs.isRegistered,
                                onVerifyPin = { pin -> viewModel.verifyPin(pin) },
                                onUnlockWithPassword = { pass -> viewModel.unlockWithPasswordAndDisablePin(pass) }
                            )
                            else -> BizSphereNavShell(state = state, viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
