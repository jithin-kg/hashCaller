package com.nibble.hashcaller.stubs

import android.graphics.drawable.Drawable
import android.telephony.PhoneNumberUtils
import android.text.SpannableStringBuilder
import androidx.annotation.Keep
import com.nibble.hashcaller.view.ui.call.dialer.util.normalizePhoneNumber

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

    fun doesContainPhoneNumber(text: String): Boolean {
        return if (text.isNotEmpty()) {
            val normalizedText = text.normalizePhoneNumber()
            if (normalizedText.isEmpty()) {
                    phoneNumber.contains(text)
            } else {
                PhoneNumberUtils.compare(phoneNumber.normalizePhoneNumber(), normalizedText) ||
                        phoneNumber.contains(text) ||
                        phoneNumber.normalizePhoneNumber().contains(normalizedText) ||
                        phoneNumber.contains(normalizedText)
            }
        } else {
            false
        }
    }
}