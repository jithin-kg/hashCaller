package com.nibble.hashcaller.view.ui.extensions

import android.widget.TextView
import androidx.core.content.ContextCompat
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.contacts.utils.TYPE_SPAM
import java.util.*

fun TextView.setColorForText(textColor: Int) {
    this.setTextColor(ContextCompat.getColor(context,textColor))
}


fun TextView.setCount(count:Int){
    if(count > 99){
        text = "99+"
    }
    else{
        text = count.toString()
    }
}

fun TextView.setRandomBackgroundCircle(color: Int? =null): Int {
    val rand = Random()
    var num = rand.nextInt(5 - 1) + 1
    if(color!=null){
        num = color
    }

    when (num) {
        1 -> {
            this.background = ContextCompat.getDrawable(context, R.drawable.contact_circular_background)

        }
        2 -> {
            this.background = ContextCompat.getDrawable(context, R.drawable.contact_circular_background2)

        }
        3 -> {
            this.background = ContextCompat.getDrawable(context, R.drawable.contact_circular_background3)


        }
        TYPE_SPAM->{
            this.background = ContextCompat.getDrawable(context, R.drawable.contact_circular_background_spam)

        }

        else -> {
            this.background = ContextCompat.getDrawable(context, R.drawable.contact_circular_background4)
        }
    }
    return num
}