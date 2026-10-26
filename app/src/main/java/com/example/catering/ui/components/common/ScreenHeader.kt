package com.example.catering.ui.components.common

import com.example.catering.R

import androidx.compose.foundation.layout.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.catering.ui.theme.SiestaManrope
import com.example.catering.ui.theme.SiestaTextPrimary

@Composable
fun ScreenHeader(title: String, onBack: () -> Unit, modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null, compact: Boolean = false,
    centerTitle: Boolean = false, titleFontSize: TextUnit = 18.sp) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp).semantics { contentDescription = texts.getString(R.string.ui_back_116) }) {
                Text("<", fontFamily = SiestaManrope, fontSize = 32.sp, color = SiestaTextPrimary)
            }
            if (compact) Text(title, modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                fontFamily = SiestaManrope, fontWeight = FontWeight.Bold, fontSize = titleFontSize,
                color = SiestaTextPrimary, textAlign = if (centerTitle) TextAlign.Center else TextAlign.Start)
            else Spacer(Modifier.weight(1f))
            if (trailing != null) trailing() else if (centerTitle) Spacer(Modifier.width(48.dp))
        }
        if (!compact) {
            Spacer(Modifier.height(12.dp))
            Text(title, Modifier.fillMaxWidth(), fontFamily = SiestaManrope, fontWeight = FontWeight.Bold,
                fontSize = 24.sp, color = SiestaTextPrimary,
                textAlign = if (centerTitle) TextAlign.Center else TextAlign.Start)
            Spacer(Modifier.height(16.dp))
        }
    }
}
