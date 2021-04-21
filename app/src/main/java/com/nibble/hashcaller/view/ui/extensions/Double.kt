package com.nibble.hashcaller.view.ui.extensions

/**
 * round a double to @param decimals number of places
 *
 */
fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return kotlin.math.round(this * multiplier) / multiplier
}