package com.nibble.hashcaller.view.ui.extensions

import android.widget.TextView
import androidx.core.content.ContextCompat
import com.nibble.hashcaller.R

fun TextView.setColorForText(textColor: Int) {
    this.setTextColor(ContextCompat.getColor(context,textColor))
}