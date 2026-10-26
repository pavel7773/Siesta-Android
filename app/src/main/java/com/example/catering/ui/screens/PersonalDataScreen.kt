
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.catering.domain.model.User
import com.example.catering.ui.components.common.ScreenHeader
import com.example.catering.ui.theme.SiestaManrope

private val PersonalGold = SiestaAmber
private val PersonalWhite = SiestaTextPrimary
private val PersonalMuted = SiestaNavUnselected
private val PersonalOutline = SiestaNavOutline

@Composable
fun PersonalDataScreen(
    user: User?,
    onBack: () -> Unit,
    onSaveName: (String) -> Unit,
    isSaving: Boolean = false
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    // Форма привязана к ID текущего аккаунта.
    // При смене пользователя старое имя не переносится.
    var editableName by rememberSaveable(user?.id) {
        mutableStateOf(user?.name.orEmpty())
    }

    val cleanedName = editableName.trim()

    val canSave =
        user != null &&
                cleanedName.isNotBlank() &&
                cleanedName != user.name &&
                !isSaving

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
            .imePadding()
    ) {

        ScreenHeader(
            title = texts.getString(R.string.ui_personal_details_413),
            onBack = onBack,
            compact = true,
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 8.dp
            )
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {

            Spacer(Modifier.height(25.dp))

            Text(
                text = texts.getString(R.string.ui_personal_details_413),
                fontFamily = SiestaManrope,
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                color = PersonalWhite
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = texts.getString(R.string.ui_these_details_will_help_you_check_out_faster_next_time_414),
                fontFamily = SiestaManrope,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = PersonalMuted
            )

            Spacer(Modifier.height(30.dp))

            if (user == null) {

                Text(
                    text = texts.getString(R.string.ui_sign_in_to_view_your_details_415),
                    fontFamily = SiestaManrope,
                    fontSize = 14.sp,
                    color = PersonalMuted
                )

            } else {

                // ИМЯ
                Text(
                    text = texts.getString(R.string.ui_name_215),
                    fontFamily = SiestaManrope,
                    fontSize = 12.sp,
                    color = PersonalMuted
                )

                Spacer(Modifier.height(5.dp))

                OutlinedTextField(
                    value = editableName,
                    onValueChange = {
                        editableName = it
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = SiestaManrope,
                        fontSize = 14.sp,
                        color = PersonalWhite
                    ),
                    shape = RoundedCornerShape(15.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PersonalWhite,
                        unfocusedTextColor = PersonalWhite,
                        focusedContainerColor = SiestaSearchMiddle,
                        unfocusedContainerColor = SiestaSearchMiddle,
                        focusedBorderColor = PersonalGold,
                        unfocusedBorderColor = PersonalOutline,
                        cursorColor = PersonalGold
                    )
                )

                Spacer(Modifier.height(24.dp))

                // ТЕЛЕФОН — ТОЛЬКО ПРОСМОТР
                Text(
                    text = texts.getString(R.string.ui_phone_number_216),
                    fontFamily = SiestaManrope,
                    fontSize = 12.sp,
                    color = PersonalMuted
                )

                Spacer(Modifier.height(5.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(SiestaSearchMiddle)
                        .border(
                            1.dp,
                            PersonalOutline,
                            RoundedCornerShape(15.dp)
                        )
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = user.phone,
                        fontFamily = SiestaManrope,
                        fontSize = 14.sp,
                        color = PersonalWhite
                    )
                }

                Spacer(Modifier.height(7.dp))

                Text(
                    text = texts.getString(R.string.ui_your_phone_number_cannot_be_changed_yet_416),
                    fontFamily = SiestaManrope,
                    fontSize = 11.sp,
                    color = PersonalMuted
                )
            }
        }

        // КНОПКА ЗАКРЕПЛЕНА СНИЗУ
        if (user != null) {

            Box(
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
                val shape = RoundedCornerShape(50)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(shape)
                        .background(
                            Brush.horizontalGradient(
                                if (canSave) {
                                    listOf(
                                        SiestaCtaGoldLight,
                                        PersonalGold,
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
                            enabled = canSave,
                            role = Role.Button,
                            onClick = {
                                onSaveName(cleanedName)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = if (isSaving) {
                            texts.getString(R.string.ui_saving_164)
                        } else {
                            texts.getString(R.string.ui_save_changes_417)
                        },
                        fontFamily = SiestaManrope,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SiestaBackgroundDark
                    )
                }
            }
        }
    }
}
