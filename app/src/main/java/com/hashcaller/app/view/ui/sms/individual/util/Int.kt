package com.hashcaller.app.view.ui.sms.individual.util

import android.graphics.Color

fun Int.getContrastColor(): Int {
    val DARK_GREY = 0xFF333333.toInt()
    val y = (299 * Color.red(this) + 587 * Color.green(this) + 114 * Color.blue(this)) / 1000
    return if (y >= 149 && this != Color.BLACK) DARK_GREY else Color.WHITE
}