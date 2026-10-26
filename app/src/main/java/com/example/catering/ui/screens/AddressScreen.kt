
package com.example.catering.ui.screens

import com.example.catering.R

import com.example.catering.ui.theme.SiestaAmber
import com.example.catering.ui.theme.SiestaBackgroundDark
import com.example.catering.ui.theme.SiestaBgBottom
import com.example.catering.ui.theme.SiestaBgMiddle
import com.example.catering.ui.theme.SiestaBgTop
import com.example.catering.ui.theme.SiestaCardMiddle
import com.example.catering.ui.theme.SiestaCtaGoldDark
import com.example.catering.ui.theme.SiestaCtaGoldLight
import com.example.catering.ui.theme.SiestaNavOutline
import com.example.catering.ui.theme.SiestaNavUnselected
import com.example.catering.ui.theme.SiestaSearchBottom
import com.example.catering.ui.theme.SiestaSearchMiddle
import com.example.catering.ui.theme.SiestaSearchTop
import com.example.catering.ui.theme.SiestaTextPrimary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.catering.domain.model.User
import com.example.catering.ui.components.common.ScreenHeader
import com.example.catering.ui.theme.SiestaManrope

private val AddressGold = SiestaAmber
private val AddressWhite = SiestaTextPrimary
private val AddressMuted = SiestaNavUnselected
private val AddressOutline = SiestaNavOutline

@Composable
fun AddressScreen(
    user: User?,
    onBack: () -> Unit,
    onSaveAddress: (String) -> Unit,
    isSaving: Boolean = false
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources


    // Черновик адреса привязан к ID аккаунта.
    var editableAddress by rememberSaveable(user?.id) {
        mutableStateOf(user?.address.orEmpty())
    }

    val cleanedAddress = editableAddress.trim()

    val canSave =
        user != null &&
                cleanedAddress != user.address.trim() &&
                !isSaving

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SiestaBgTop,
                        SiestaBgMiddle,
                        SiestaBgBottom
                    )
                )
            )
            .imePadding()
    ) {

        // ШАПКА
        ScreenHeader(
            title = texts.getString(R.string.ui_delivery_address_157),
            onBack = onBack,
            compact = true,
            centerTitle = false,
            titleFontSize = 19.sp,
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 8.dp
            )
        )

        // СОДЕРЖИМОЕ
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {

            Spacer(Modifier.height(25.dp))

            Text(
                text = texts.getString(R.string.ui_delivery_address_157),
                fontFamily = SiestaManrope,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = AddressWhite
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = texts.getString(R.string.ui_save_your_address_to_check_out_faster_next_time_158),
                fontFamily = SiestaManrope,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = AddressMuted
            )

            Spacer(Modifier.height(30.dp))

            if (user == null) {

                Text(
                    text = texts.getString(R.string.ui_sign_in_to_change_your_address_159),
                    fontFamily = SiestaManrope,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = AddressMuted
                )

            } else {

                Text(
                    text = texts.getString(R.string.ui_address_160),
                    fontFamily = SiestaManrope,
                    fontSize = 12.sp,
                    color = AddressMuted
                )

                Spacer(Modifier.height(5.dp))

                val fieldShape = RoundedCornerShape(16.dp)

                OutlinedTextField(
                    value = editableAddress,
                    onValueChange = {
                        editableAddress = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(fieldShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    SiestaSearchTop,
                                    SiestaSearchBottom
                                )
                            )
                        ),
                    placeholder = {
                        Text(
                            text = texts.getString(R.string.ui_street_building_apartment_161),
                            fontFamily = SiestaManrope,
                            fontSize = 13.sp,
                            color = AddressMuted
                        )
                    },
                    minLines = 2,
                    maxLines = 4,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = SiestaManrope,
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        color = AddressWhite
                    ),
                    keyboardOptions = KeyboardOptions(
                        capitalization =
                            KeyboardCapitalization.Sentences
                    ),
                    shape = fieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AddressWhite,
                        unfocusedTextColor = AddressWhite,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = AddressGold,
                        unfocusedBorderColor = AddressOutline,
                        cursorColor = AddressGold
                    )
                )

                Spacer(Modifier.height(7.dp))

                Text(
                    text = texts.getString(R.string.ui_you_can_leave_this_blank_for_pickup_162),
                    fontFamily = SiestaManrope,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = AddressMuted
                )

                Spacer(Modifier.height(25.dp))

                // ОТМЕНА ИЗМЕНЕНИЙ
                AddressSecondaryButton(
                    text = texts.getString(R.string.ui_discard_changes_163),
                    onClick = onBack
                )
            }
        }

        // КНОПКА СОХРАНЕНИЯ ВНИЗУ
        if (user != null) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SiestaCardMiddle)
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = 14.dp,
                        bottom = 16.dp
                    )
            ) {

                AddressGoldButton(
                    text = if (isSaving) {
                        texts.getString(R.string.ui_saving_164)
                    } else {
                        texts.getString(R.string.ui_save_address_165)
                    },
                    enabled = canSave,
                    onClick = {
                        onSaveAddress(cleanedAddress)
                    }
                )
            }
        }
    }
}

// ТЁМНАЯ КНОПКА

@Composable
private fun AddressSecondaryButton(
    text: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(51.dp)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        SiestaSearchTop,
                        SiestaCardMiddle
                    )
                )
            )
            .border(
                width = 1.dp,
                color = AddressOutline,
                shape = shape
            )
            .clickable(
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
            color = AddressWhite,
            textAlign = TextAlign.Center
        )
    }
}

// ЗОЛОТАЯ КНОПКА

@Composable
private fun AddressGoldButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    if (enabled) {
                        listOf(
                            SiestaCtaGoldLight,
                            AddressGold,
                            SiestaCtaGoldDark
                        )
                    } else {
                        listOf(
                            SiestaSearchTop,
                            SiestaSearchMiddle
                        )
                    }
                )
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
                AddressMuted
            }
        )
    }
}
