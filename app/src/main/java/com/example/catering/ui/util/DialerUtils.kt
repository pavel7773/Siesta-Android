package com.example.catering.ui.util

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
fun openDialer(context: Context, phone: String) {
    val phoneUri = "tel:$phone".toUri()
    val intent = Intent(Intent.ACTION_DIAL, phoneUri)
    context.startActivity(intent)
}