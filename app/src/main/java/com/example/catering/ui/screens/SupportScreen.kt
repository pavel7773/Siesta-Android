package com.example.catering.ui.screens

import com.example.catering.R

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.catering.ui.components.common.*
import com.example.catering.ui.theme.*

@Composable
fun SupportScreen(onBack: () -> Unit, onCallSupportClick: () -> Unit) {
    val texts = androidx.compose.ui.platform.LocalContext.current.resources

    SiestaPage(texts.getString(R.string.ui_support_423), onBack, footer = { SiestaButton(texts.getString(R.string.ui_call_support_466), onCallSupportClick, secondary = true) }) {
        SiestaHeading(texts.getString(R.string.ui_we_are_here_to_help_467))
        SiestaBody(texts.getString(R.string.ui_contact_the_restaurant_about_your_order_468))
        SupportAnswer(texts.getString(R.string.ui_where_can_i_find_my_order_469), texts.getString(R.string.ui_open_orders_and_select_the_order_number_470), Icons.Outlined.ReceiptLong)
        SupportAnswer(texts.getString(R.string.ui_how_do_i_reorder_471), texts.getString(R.string.ui_open_an_order_and_tap_reorder_available_dishes_will_be_add_472), Icons.Outlined.Refresh)
        SupportAnswer(texts.getString(R.string.ui_how_do_i_cancel_an_order_473), texts.getString(R.string.ui_open_the_order_card_if_cancellation_is_still_available_474), Icons.Outlined.Info)
        SiestaMessage(texts.getString(R.string.ui_ask_the_restaurant_about_delivery_preparation_time_and_pay_475))
        Text(texts.getString(R.string.ui_support_phone_7_928_555_77_73_476), color = SiestaTextPrimary)
    }
}

@Composable
private fun SupportAnswer(title: String, answer: String, icon: ImageVector) {
    SiestaCard {
        Row {
            Text(title, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium, color = SiestaTextPrimary)
            Icon(icon, null, tint = SiestaTextPrimary)
        }
        SiestaBody(answer)
    }
}
