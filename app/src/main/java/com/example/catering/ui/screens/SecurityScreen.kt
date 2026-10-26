package com.example.catering.ui.screens

import com.example.catering.R

import com.example.catering.ui.theme.SiestaAmber
import com.example.catering.ui.theme.SiestaBgBottom
import com.example.catering.ui.theme.SiestaBgMiddle
import com.example.catering.ui.theme.SiestaBgTop
import com.example.catering.ui.theme.SiestaCardMiddle
import com.example.catering.ui.theme.SiestaCardTop
import com.example.catering.ui.theme.SiestaError
import com.example.catering.ui.theme.SiestaNavOutline
import com.example.catering.ui.theme.SiestaNavUnselected
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.catering.domain.model.User
import com.example.catering.ui.components.common.ScreenHeader

private val SecurityGold = SiestaAmber
private val SecurityWhite = SiestaTextPrimary
private val SecurityMuted = SiestaNavUnselected
private val SecurityOutline = SiestaNavOutline
private val SecurityCardColor = SiestaCardMiddle

@Composable
fun SecurityScreen(
    user: User?,
    onBack: () -> Unit,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit,
    isLoggingOut: Boolean = false,
    biometricEnabled: Boolean = false,
    isBiometricBusy: Boolean = false,
    onEmailClick: () -> Unit,
    onBiometricClick: (() -> Unit)? = null
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    var showBiometricIntro by remember {
        mutableStateOf(false)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        SiestaBgTop,
                        SiestaBgMiddle,
                        SiestaBgBottom
                    )
                )
            )
    ) {

        ScreenHeader(
            title = texts.getString(R.string.ui_security_424),
            onBack = onBack,
            compact = true,
            titleFontSize = 19.sp,
            modifier = Modifier.padding(
                horizontal = 18.dp,
                vertical = 8.dp
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {

            Spacer(Modifier.height(22.dp))

            Text(
                text = texts.getString(R.string.ui_account_protection_450),
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = SecurityWhite
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = texts.getString(R.string.ui_manage_your_password_sign_in_methods_and_account_security_451),
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = SecurityMuted
            )

            Spacer(Modifier.height(28.dp))

            if (user == null) {

                SecurityPanel {
                    Text(
                        text = texts.getString(R.string.ui_sign_in_to_manage_your_security_settings_452),
                        color = SecurityMuted,
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        modifier = Modifier.padding(20.dp)
                    )
                }

            } else {

                SecuritySectionLabel(texts.getString(R.string.ui_sign_in_details_453))

                Spacer(Modifier.height(12.dp))

                SecurityPanel {

                    SecurityActiveRow(
                        icon = Icons.Outlined.Key,
                        title = texts.getString(R.string.ui_change_password_191),
                        subtitle = texts.getString(R.string.ui_update_your_account_password_454),
                        onClick = onChangePassword
                    )

                    SecurityDivider()

                    SecurityActiveRow(
                        icon = Icons.Outlined.Email,
                        title = texts.getString(R.string.ui_email_address_249),
                        subtitle = texts.getString(R.string.ui_add_and_verify_your_email_455),
                        onClick = onEmailClick
                    )
                }

                Spacer(Modifier.height(25.dp))

                SecuritySectionLabel(texts.getString(R.string.ui_quick_sign_in_456))

                Spacer(Modifier.height(12.dp))

                SecurityPanel {

                    if (onBiometricClick == null) {
                        SecurityUnavailableRow(
                            icon = Icons.Outlined.Fingerprint,
                            title = texts.getString(R.string.ui_fingerprint_sign_in_457),
                            subtitle = texts.getString(R.string.ui_biometric_unlock_458),
                            status = texts.getString(R.string.ui_completing_secure_sign_in_setup_459)
                        )
                    } else {
                        SecurityActiveRow(
                            icon = Icons.Outlined.Fingerprint,
                            title = texts.getString(R.string.ui_fingerprint_sign_in_457),
                            subtitle = when {
                                isBiometricBusy -> texts.getString(R.string.ui_verification_in_progress_460)
                                biometricEnabled -> texts.getString(R.string.ui_on_tap_to_turn_off_461)
                                else -> texts.getString(R.string.ui_tap_to_turn_on_462)
                            },
                            enabled = !isBiometricBusy,
                            onClick = {
                                if (biometricEnabled) {
                                    onBiometricClick?.invoke()
                                } else {
                                    showBiometricIntro = true
                                }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(25.dp))

                SecuritySectionLabel(texts.getString(R.string.ui_session_management_463))

                Spacer(Modifier.height(12.dp))

                SecurityPanel {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {

                        SecurityIcon(Icons.Outlined.Lock)

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {

                            Text(
                                text = texts.getString(R.string.ui_current_session_464),
                                color = SecurityWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(
                                text = user.phone,
                                color = SecurityMuted,
                                fontSize = 13.sp
                            )

                            Spacer(Modifier.height(7.dp))

                            Text(
                                text = texts.getString(R.string.ui_you_can_end_your_current_session_on_this_device_465),
                                color = SecurityMuted,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    SecurityDivider()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clickable(
                                enabled = !isLoggingOut,
                                role = Role.Button,
                                onClick = onLogout
                            )
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {

                        Icon(
                            imageVector = Icons.Outlined.Logout,
                            contentDescription = null,
                            tint = SiestaError,
                            modifier = Modifier.size(21.dp)
                        )

                        Spacer(Modifier.width(10.dp))

                        Text(
                            text = if (isLoggingOut) {
                                texts.getString(R.string.ui_signing_out_425)
                            } else {
                                texts.getString(R.string.ui_sign_out_136)
                            },
                            color = SiestaError,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(Modifier.height(30.dp))
        }
    }

    if (
        showBiometricIntro &&
        user != null &&
        onBiometricClick != null &&
        !biometricEnabled
    ) {
        BiometricIntroDialog(
            onContinue = {
                showBiometricIntro = false

                if (!isBiometricBusy) {
                    onBiometricClick()
                }
            },
            onDismiss = {
                showBiometricIntro = false
            }
        )
    }

}

@Composable
private fun SecuritySectionLabel(
    title: String
) {
    Text(
        text = title,
        color = SecurityGold,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp
    )
}

@Composable
private fun SecurityPanel(
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        SiestaCardTop,
                        SecurityCardColor,
                        SiestaCardMiddle
                    )
                )
            )
            .border(
                width = 1.dp,
                color = SecurityOutline,
                shape = shape
            ),
        content = content
    )
}

@Composable
private fun SecurityIcon(
    icon: ImageVector
) {
    Box(
        modifier = Modifier
            .size(43.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(SiestaCardTop),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SecurityGold,
            modifier = Modifier.size(23.dp)
        )
    }
}

@Composable
private fun SecurityActiveRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .padding(17.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {

        SecurityIcon(icon)

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = title,
                color = SecurityWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = subtitle,
                color = SecurityMuted,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = SecurityMuted,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun SecurityUnavailableRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    status: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(17.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {

        SecurityIcon(icon)

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = title,
                color = SecurityWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = subtitle,
                color = SecurityMuted,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = status,
                color = SecurityGold.copy(alpha = 0.8f),
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun SecurityDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 17.dp),
        thickness = 1.dp,
        color = SecurityOutline.copy(alpha = 0.65f)
    )
}
