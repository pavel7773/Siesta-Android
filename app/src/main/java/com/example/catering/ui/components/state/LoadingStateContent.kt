package com.example.catering.ui.components.state

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.example.catering.ui.components.common.*
import com.example.catering.ui.theme.*

@Composable
fun LoadingStateContent(message: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().background(SiestaScreenBrush)
        .verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LoadingCards(message)
    }
}

@Composable
fun LoadingCards(message: String) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
        SiestaBody(message)
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = SiestaAmber, trackColor = SiestaNavOutline)
        repeat(3) {
            SiestaCard(Modifier.clearAndSetSemantics {}) {
                listOf(0.35f, 0.72f, 0.52f).forEach { fraction ->
                    Spacer(Modifier.fillMaxWidth(fraction).height(14.dp).background(SiestaNavOutline, RoundedCornerShape(7.dp)))
                }
            }
        }
    }
}
