package com.nibble.hashcaller.stubs

import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import androidx.annotation.Keep

/**
 * Created by Jithin KG on 21,July,2020
 */
@Keep
class Contact(
    val id: Long,
    val name: String,
    var phoneNumber: String = "",
    var photoThumnail: String?,
    var photoURI: String = "",
    var drawable: Drawable? = null,
    var nameSpann: SpannableStringBuilder? = null,
    var phoneSpann: SpannableStringBuilder? = null

)  {




}