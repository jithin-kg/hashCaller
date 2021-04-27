package com.nibble.hashcaller.stubs

import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import androidx.annotation.Keep

/**
 * Created by Jithin KG on 21,July,2020
 */
@Keep
data class Contact(
    val id: Long,
    val name: String,
    var phoneNumber: String = "",
    var photoThumnail: String?,
    var photoURI: String = "",
    var drawable: Int = 1,
    var nameSpann: SpannableStringBuilder? = null,
    var phoneSpann: SpannableStringBuilder? = null,
    var firstletter:String = "",
    var spanStartPosName: Int = 0,
    var spanEndPosName: Int = 0,
    var spanStartPosNum: Int = 0,
    var spanEndPosNum: Int = 0





)  {


}