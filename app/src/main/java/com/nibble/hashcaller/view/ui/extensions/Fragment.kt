package com.nibble.hashcaller.view.ui.extensions

import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.nibble.hashcaller.R

fun Fragment.getSpannableString(msg:String): SpannableString {

    val spannable = SpannableString(msg)
    spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(context!!, R.color.textColor)),
        0 ,msg.length, 0)
    return spannable


//    var titleStr = "Delete this call history ?"
//    val title = SpannableString(titleStr)
//    title.setSpan(ForegroundColorSpan(ContextCompat.getColor(this.requireActivity(), R.color.textColor)), 0 ,title.length, 0)

}