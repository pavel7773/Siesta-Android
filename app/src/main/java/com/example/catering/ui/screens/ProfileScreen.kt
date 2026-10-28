
package com.example.catering.ui.screens


import com.example.catering.ui.theme.SiestaAmber
import com.example.catering.ui.theme.SiestaBackgroundDark
import com.example.catering.ui.theme.SiestaBgBottom
import com.example.catering.ui.theme.SiestaBgMiddle
import com.example.catering.ui.theme.SiestaBgTop
import com.example.catering.ui.theme.SiestaCardBottom
import com.example.catering.ui.theme.SiestaCardMiddle
import com.example.catering.ui.theme.SiestaCardTop
import com.example.catering.ui.theme.SiestaCtaGoldDark
import com.example.catering.ui.theme.SiestaCtaGoldLight
import com.example.catering.ui.theme.SiestaNavOutline
import com.example.catering.ui.theme.SiestaNavUnselected
import com.example.catering.ui.theme.SiestaSearchTop
import com.example.catering.ui.theme.SiestaTextPrimary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.res.stringResource
import com.example.catering.R
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.catering.domain.model.User
import com.example.catering.ui.components.common.*
import com.example.catering.ui.theme.SiestaManrope

private val ProfileGold = SiestaAmber
private val ProfileWhite = SiestaTextPrimary
private val ProfileMuted = SiestaNavUnselected
private val ProfileBorder = SiestaNavOutline

private val ProfileBackground = Brush.verticalGradient(
    listOf(
        SiestaBgTop,
        SiestaBgMiddle,
        SiestaBgBottom
    )
)

private val ProfileCardBackground = Brush.verticalGradient(
    listOf(
        SiestaCardTop,
        SiestaCardMiddle,
        SiestaCardBottom
    )
)

private val ProfileButtonBackground = Brush.verticalGradient(
    listOf(
        SiestaSearchTop,
        SiestaCardMiddle
    )
)

private val ProfileGoldBackground = Brush.horizontalGradient(
    listOf(
        SiestaCtaGoldLight,
        SiestaAmber,
        SiestaCtaGoldDark
    )
)

@Composable
fun ProfileScreen(
    selectedLanguage: String,
    onLanguageChange: (String) -> Unit,
    onBack: () -> Unit,
    user: User?,
    onGoLogin: () -> Unit,
    onGoRegister: () -> Unit,
    onLogout: () -> Unit,
    onGoContacts: () -> Unit,
    onGoSupport: () -> Unit,
    onGoSecurity: () -> Unit,
    onGoPersonalData: () -> Unit,
    onGoAddress: () -> Unit,
    onGoCart: (() -> Unit)? = null,
    badgeCartCount: Int = 0,
    isLoggingOut: Boolean = false
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources


    if (user == null) {
        SiestaPage(texts.getString(R.string.ui_profile_39), onBack, trailing = onGoCart?.let { { SiestaCartButton(badgeCartCount, it) } }) {
            SiestaHeading(texts.getString(R.string.ui_nice_to_meet_you_418))
            SiestaBody(texts.getString(R.string.ui_sign_in_to_see_your_orders_and_save_recipient_details_419))
            SiestaButton(texts.getString(R.string.ui_sign_in_320), onGoLogin)
            SiestaButton(texts.getString(R.string.ui_create_account_329), onGoRegister, secondary = true)
            ProfileLanguageSelector(selectedLanguage, onLanguageChange)
            SiestaCard(Modifier.clip(RoundedCornerShape(20.dp)).clickable(role = Role.Button, onClick = onGoSupport)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(texts.getString(R.string.ui_contact_and_help_420), Modifier.weight(1f), color = ProfileWhite, fontFamily = SiestaManrope)
                    Icon(Icons.Outlined.Phone, null, tint = ProfileWhite)
                }
                SiestaBody(texts.getString(R.string.ui_you_can_contact_us_without_signing_in_421))
            }
        }
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileBackground)
    ) {

        ScreenHeader(
            title = texts.getString(R.string.ui_my_profile_422),
            trailing = onGoCart?.let { { SiestaCartButton(badgeCartCount, it) } },
            onBack = onBack,
            compact = true,
            centerTitle = false,
            titleFontSize = 24.sp,
            modifier = Modifier.padding(
                horizontal = 18.dp,
                vertical = 8.dp
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
        ) {

            Spacer(Modifier.height(22.dp))

            ProfileUserCard(user = user)

            Spacer(Modifier.height(18.dp))
            ProfileLanguageSelector(selectedLanguage, onLanguageChange)

            Spacer(Modifier.height(18.dp))

            ProfileActionPanel {

                if (user != null) {

                    ProfileActionRow(
                        title = texts.getString(R.string.ui_personal_details_413),
                        icon = Icons.Outlined.Person,
                        onClick = onGoPersonalData
                    )

                    ProfileRowDivider()

                    ProfileActionRow(
                        title = texts.getString(R.string.ui_delivery_address_157),
                        icon = Icons.Outlined.Place,
                        onClick = onGoAddress
                    )

                    ProfileRowDivider()

                }

                ProfileActionRow(
                    title = texts.getString(R.string.ui_contact_us_226),
                    icon = Icons.Outlined.Phone,
                    onClick = onGoContacts
                )

                ProfileRowDivider()

                ProfileActionRow(
                    title = texts.getString(R.string.ui_support_423),
                    icon = Icons.Outlined.Info,
                    onClick = onGoSupport
                )

                if (user != null) {

                    ProfileRowDivider()

                    ProfileActionRow(
                        title = texts.getString(R.string.ui_security_424),
                        icon = Icons.Outlined.Lock,
                        onClick = onGoSecurity
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            if (user != null) {

                ProfileSecondaryButton(
                    text = if (isLoggingOut) {
                        texts.getString(R.string.ui_signing_out_425)
                    } else {
                        texts.getString(R.string.ui_sign_out_136)
                    },
                    enabled = !isLoggingOut,
                    onClick = onLogout
                )

            } else {

                ProfileGoldButton(
                    text = texts.getString(R.string.ui_sign_in_426),
                    enabled = true,
                    onClick = onGoLogin
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// КАРТОЧКА ПОЛЬЗОВАТЕЛЯ

@Composable
private fun ProfileLanguageSelector(selectedLanguage: String, onLanguageChange: (String) -> Unit) {
    SiestaCard {
        Text(stringResource(R.string.language_title), color = ProfileWhite, fontFamily = SiestaManrope)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilterChip(selected = selectedLanguage == "ru", onClick = { onLanguageChange("ru") },
                label = { Text(stringResource(R.string.language_russian), fontFamily = SiestaManrope) })
            FilterChip(selected = selectedLanguage == "en", onClick = { onLanguageChange("en") },
                label = { Text(stringResource(R.string.language_english), fontFamily = SiestaManrope) })
        }
    }
}

@Composable
private fun ProfileUserCard(user: User?) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources


    val shape = RoundedCornerShape(20.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(ProfileCardBackground)
            .border(1.dp, ProfileBorder, shape)
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        Box(
            modifier = Modifier
                .size(55.dp)
                .clip(CircleShape)
                .background(SiestaCardTop),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = ProfileGold,
                modifier = Modifier.size(28.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {

            Text(
                text = user?.name ?: texts.getString(R.string.ui_guest_427),
                fontFamily = SiestaManrope,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = ProfileWhite
            )

            Text(
                text = user?.phone ?: texts.getString(R.string.ui_sign_in_to_your_account_428),
                fontFamily = SiestaManrope,
                fontSize = 12.sp,
                color = ProfileMuted
            )
        }
    }
}

// ОБЩАЯ ПАНЕЛЬ

@Composable
private fun ProfileActionPanel(
    content: @Composable ColumnScope.() -> Unit
) {

    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(ProfileCardBackground)
            .border(1.dp, ProfileBorder, shape),
        content = content
    )
}

// ПУНКТ МЕНЮ

@Composable
private fun ProfileActionRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 65.dp)
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 17.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(15.dp)
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ProfileGold,
            modifier = Modifier.size(23.dp)
        )

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontFamily = SiestaManrope,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = ProfileWhite
        )

        Text(
            text = "›",
            color = ProfileMuted,
            fontSize = 29.sp
        )
    }
}

@Composable
private fun ProfileRowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 17.dp),
        color = ProfileBorder.copy(alpha = 0.7f),
        thickness = 1.dp
    )
}

// ЗОЛОТАЯ КНОПКА

@Composable
private fun ProfileGoldButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {

    val shape = RoundedCornerShape(50)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(shape)
            .background(
                if (enabled) {
                    ProfileGoldBackground
                } else {
                    ProfileButtonBackground
                }
            )
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = text,
            fontFamily = SiestaManrope,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) {
                SiestaBackgroundDark
            } else {
                ProfileMuted
            }
        )
    }
}

// ТЁМНАЯ КНОПКА

@Composable
private fun ProfileSecondaryButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {

    val shape = RoundedCornerShape(50)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(shape)
            .background(ProfileButtonBackground)
            .border(1.dp, ProfileBorder, shape)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = text,
            fontFamily = SiestaManrope,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (enabled) {
                ProfileWhite
            } else {
                ProfileMuted
            },
            textAlign = TextAlign.Center
        )
    }
}
