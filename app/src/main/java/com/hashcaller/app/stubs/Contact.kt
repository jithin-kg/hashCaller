package com.hashcaller.app.stubs

import android.telephony.PhoneNumberUtils
import android.text.SpannableStringBuilder
import androidx.annotation.Keep
import com.hashcaller.app.view.ui.call.dialer.util.normalizePhoneNumber
import com.hashcaller.app.view.ui.sms.individual.util.INFO_NOT_FOUND_IN_SERVER

/**
 * Created by Jithin KG on 21,July,2020
 */
@Keep
data class Contact(
    val id: Long,
    var firstName: String="",
    var phoneNumber: String = "",
    var photoThumnailServer: String?="",
    var photoURI: String = "",
    var drawable: Int = 1,
    var nameSpann: SpannableStringBuilder? = null,
    var phoneSpann: SpannableStringBuilder? = null,
    var firstletter:String = "",
    var spanStartPosName: Int = 0,
    var spanEndPosName: Int = 0,
    var spanStartPosNum: Int = 0,
    var spanEndPosNum: Int = 0,
    var country:String = "",
    var location:String = "",
    var carrier:String = "",
    var spamCount:Long = 0L,
    var isInfoFoundInServer:Int = INFO_NOT_FOUND_IN_SERVER,
    var lastName: String = "",
    var thumbnailInCprovider:String = "",
    val nameInPhoneBook:String = "",
    val hUid:String = "",
    val email:String = "",
    val avatarGoogle:String = "",
    val bio:String = "",
    var isVerifiedUser:Boolean = false,
    var nameInLocalPhoneBook :String = "",
    var spamerType:Int?= -1,

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