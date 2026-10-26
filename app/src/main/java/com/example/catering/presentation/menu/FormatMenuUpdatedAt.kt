package com.example.catering.presentation.menu

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatMenuUpdatedAt(lastUpdatedAtMillis: Long?): String {
    if (lastUpdatedAtMillis == null) {
        return "время обновления неизвестно"
    }

    val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(lastUpdatedAtMillis))
}