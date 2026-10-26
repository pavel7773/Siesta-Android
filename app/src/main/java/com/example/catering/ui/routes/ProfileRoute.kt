
package com.example.catering.ui.routes

import com.example.catering.R

import androidx.compose.runtime.Composable
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.os.LocaleListCompat
import com.example.catering.presentation.user.UserViewModel
import com.example.catering.ui.components.state.LoadingStateContent
import com.example.catering.ui.screens.ProfileScreen

@Composable
fun ProfileRoute(
    onBack: () -> Unit,
    userViewModel: UserViewModel,
    onGoLogin: () -> Unit,
    onGoRegister: () -> Unit,
    onLogout: () -> Unit,
    onGoContacts: () -> Unit,
    onGoSupport: () -> Unit,
    onGoSecurity: () -> Unit,
    onGoPersonalData: () -> Unit,
    onGoCart: () -> Unit,
    badgeCartCount: Int,
    onGoAddress: () -> Unit
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    val selectedLanguage = LocalConfiguration.current.locales[0].language
    if (userViewModel.isSessionRestoring) {
        LoadingStateContent(
            message = texts.getString(R.string.ui_checking_your_account_33)
        )
        return
    }

    ProfileScreen(
        selectedLanguage = if (selectedLanguage == "en") "en" else "ru",
        onLanguageChange = { tag ->
            if (tag == "ru" || tag == "en") {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
            }
        },
        onBack = onBack,
        user = userViewModel.currentUser,
        onGoCart = onGoCart,
        badgeCartCount = badgeCartCount,
        onGoLogin = onGoLogin,
        onGoRegister = onGoRegister,

        onLogout = {
            userViewModel.logout(
                onSuccess = onLogout
            )
        },
        onGoContacts = onGoContacts,
        onGoSupport = onGoSupport,
        onGoSecurity = onGoSecurity,
        onGoPersonalData = onGoPersonalData,
        onGoAddress = onGoAddress,

        isLoggingOut = userViewModel.isLoggingOut
    )
}
