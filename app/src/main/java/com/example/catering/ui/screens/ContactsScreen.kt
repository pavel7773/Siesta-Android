package com.example.catering.ui.screens

import com.example.catering.R

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.example.catering.ui.components.common.*
import com.example.catering.ui.theme.*

@Composable
fun ContactsScreen(onBack: () -> Unit, onCallClick: () -> Unit) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    SiestaPage(texts.getString(R.string.ui_contact_us_226), onBack, footer = { SiestaButton(texts.getString(R.string.ui_call_the_restaurant_227), onCallClick, secondary = true) }) {
        SiestaHeading(texts.getString(R.string.ui_siesta_122))
        SiestaBody(texts.getString(R.string.ui_good_food_starts_with_a_conversation_228))
        SiestaCard {
            Icon(Icons.Outlined.Place, null, tint = SiestaAmber)
            Text(texts.getString(R.string.ui_36_chernikova_st_second_entrance_229), style = MaterialTheme.typography.titleLarge, color = SiestaTextPrimary)
            HorizontalDivider(color = SiestaNavOutline)
            Text(texts.getString(R.string.ui_opening_hours_10_00_23_00_230), color = SiestaTextPrimary)
            Text(texts.getString(R.string.ui_phone_7_918_857_43_78_231), color = SiestaTextPrimary)
        }
    }
}
