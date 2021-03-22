package com.nibble.hashcaller.view.ui.contacts

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.nibble.hashcaller.R
import java.util.*

fun Context.generateCircleView(): Drawable? {
    val rand = Random()
    var background : Drawable?= null
    when (rand.nextInt(5 - 1) + 1) {
        1 -> {
            background = ContextCompat.getDrawable(this, R.drawable.contact_circular_background)
        }
        2 -> {
            background = ContextCompat.getDrawable(this, R.drawable.contact_circular_background2)
        }
        3 -> {
            background = ContextCompat.getDrawable(this, R.drawable.contact_circular_background3)
        }
        else -> {
            background = ContextCompat.getDrawable(this, R.drawable.contact_circular_background4)
        }
    }
    return background
}