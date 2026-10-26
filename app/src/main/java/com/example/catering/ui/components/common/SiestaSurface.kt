package com.example.catering.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.catering.ui.theme.*

val SiestaScreenBrush = Brush.verticalGradient(listOf(SiestaBgTop, SiestaBgMiddle, SiestaBgBottom))
val SiestaCardBrush = Brush.verticalGradient(listOf(SiestaCardTop, SiestaCardMiddle, SiestaCardBottom))

/** Insets from the app scaffold are already applied by AppNavHost. */
@Composable
fun SiestaPage(title: String, onBack: () -> Unit,
               footer: (@Composable () -> Unit)? = null,
               trailing: (@Composable () -> Unit)? = null,
               content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().background(SiestaScreenBrush).imePadding()) {
        ScreenHeader(title, onBack, compact = true, titleFontSize = 18.sp, trailing = trailing,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
        if (footer != null) Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) { footer() }
    }
}

@Composable
fun SiestaCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Column(modifier.fillMaxWidth().background(SiestaCardBrush, shape)
        .border(1.dp, SiestaNavOutline, shape).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
}

@Composable
fun SiestaHeading(text: String) {
    Text(text, fontFamily = SiestaManrope, fontSize = 26.sp, lineHeight = 33.sp,
        fontWeight = FontWeight.Bold, color = SiestaTextPrimary)
}

@Composable
fun SiestaBody(text: String, error: Boolean = false) {
    Text(text, fontFamily = SiestaManrope, fontSize = 14.sp, lineHeight = 21.sp,
        color = if (error) SiestaError else SiestaNavUnselected)
}

@Composable
fun SiestaMessage(text: String, error: Boolean = false) {
    SiestaCard { SiestaBody(text, error) }
}

@Composable
fun SiestaButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier,
                 secondary: Boolean = false, destructive: Boolean = false,
                 enabled: Boolean = true, loading: Boolean = false) {
    val active = enabled && !loading
    val shape = RoundedCornerShape(28.dp)
    val primary = !secondary && !destructive && active
    val brush = when {
        !active || secondary -> Brush.verticalGradient(listOf(SiestaSearchTop, SiestaSearchMiddle, SiestaSearchBottom))
        destructive -> Brush.horizontalGradient(listOf(SiestaTerracotta, SiestaTerracotta))
        else -> Brush.horizontalGradient(listOf(SiestaCtaGoldLight, SiestaCtaGoldMiddle, SiestaCtaGoldDark))
    }
    Button(onClick, enabled = active, shape = shape,
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp).background(brush, shape)
            .then(if (secondary || !active) Modifier.border(1.dp, SiestaNavOutline, shape) else Modifier),
        contentPadding = PaddingValues(start = 18.dp, end = if (primary) 6.dp else 18.dp, top = 6.dp, bottom = 6.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent, disabledContentColor = SiestaNavUnselected,
            contentColor = if (primary) SiestaBgMiddle else SiestaTextPrimary)) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(18.dp), color = SiestaNavUnselected, strokeWidth = 2.dp)
            Spacer(Modifier.width(10.dp))
        }
        Text(text, modifier = if (primary) Modifier.weight(1f) else Modifier,
            fontFamily = SiestaManrope, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp)
        if (primary) Box(Modifier.padding(start = 8.dp).size(40.dp)
            .background(Brush.verticalGradient(listOf(SiestaCtaInsetTop, SiestaCtaInsetBottom)), CircleShape)
            .border(1.dp, SiestaCtaGoldLight, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, tint = SiestaTextPrimary, modifier = Modifier.size(20.dp))
        }
    }
}
