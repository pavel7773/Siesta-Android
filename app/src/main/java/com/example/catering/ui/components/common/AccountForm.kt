package com.example.catering.ui.components.common

import com.example.catering.R

import com.example.catering.ui.theme.SiestaAmber
import com.example.catering.ui.theme.SiestaBackgroundDark
import com.example.catering.ui.theme.SiestaBgBottom
import com.example.catering.ui.theme.SiestaBgMiddle
import com.example.catering.ui.theme.SiestaBgTop
import com.example.catering.ui.theme.SiestaError
import com.example.catering.ui.theme.SiestaNavOutline
import com.example.catering.ui.theme.SiestaNavUnselected
import com.example.catering.ui.theme.SiestaSearchMiddle
import com.example.catering.ui.theme.SiestaSearchTop
import com.example.catering.ui.theme.SiestaTextPrimary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.catering.ui.theme.SiestaManrope

private val FormGold = SiestaAmber
private val FormText = SiestaTextPrimary

@Composable
fun AccountForm(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(SiestaBgTop, SiestaBgMiddle, SiestaBgBottom)
                )
            )
            .imePadding()
    ) {
        ScreenHeader(
            title, onBack, compact = true, titleFontSize = 24.sp,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
        )
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp), content = content
        )
    }
}

@Composable
fun AccountCopy(text: String, error: Boolean = false) {
    Text(
        text, fontFamily = SiestaManrope, fontSize = 14.sp, lineHeight = 21.sp,
        color = if (error) SiestaError else FormText
    )
}

@Composable
fun AccountField(
    value: String, onChange: (String) -> Unit, label: String,
    enabled: Boolean = true, keyboard: KeyboardType = KeyboardType.Text,
    password: Boolean = false, externalLabel: Boolean = false,
    isError: Boolean = false
) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    var visible by remember { mutableStateOf(false) }
    if (externalLabel) {
        Text(label, fontFamily = SiestaManrope, fontSize = 13.sp, color = SiestaNavUnselected)
        Spacer(Modifier.height(6.dp))
    }
    OutlinedTextField(
        value, onChange, modifier = Modifier
            .fillMaxWidth()
            .semantics { if (externalLabel) contentDescription = label }
            .background(
                Brush.verticalGradient(
                    listOf(
                        SiestaSearchTop,
                        SiestaSearchMiddle,
                        com.example.catering.ui.theme.SiestaSearchBottom
                    )
                ), RoundedCornerShape(20.dp)
            ), enabled = enabled,
        isError = isError,
        singleLine = true, label = if (externalLabel) null else {
            { Text(label, fontFamily = SiestaManrope) }
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        visualTransformation = if (password && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (password) {
            {
                TextButton(onClick = { visible = !visible }, enabled = enabled) {
                    Text(
                        if (visible) texts.getString(R.string.ui_hide_114) else texts.getString(R.string.ui_show_115),
                        color = FormGold
                    )
                }
            }
        } else null,
        shape = RoundedCornerShape(20.dp),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = SiestaManrope,
            fontSize = 16.sp
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = FormText,
            unfocusedTextColor = FormText,
            disabledTextColor = FormText,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
            errorBorderColor = SiestaError,
            errorTextColor = FormText,
            focusedBorderColor = FormGold,
            unfocusedBorderColor = SiestaNavOutline,
            focusedLabelColor = FormGold,
            unfocusedLabelColor = SiestaNavUnselected,
            cursorColor = FormGold
        ))
}

@Composable
fun AccountAction(
    text: String,
    enabled: Boolean = true,
    secondary: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick, modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp), enabled = enabled,
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (secondary) SiestaSearchTop else FormGold,
            contentColor = if (secondary) FormText else SiestaBackgroundDark,
            disabledContainerColor = SiestaNavOutline, disabledContentColor = SiestaNavUnselected
        )
    ) {
        Text(text, fontFamily = SiestaManrope, fontSize = 14.sp)
    }
}
