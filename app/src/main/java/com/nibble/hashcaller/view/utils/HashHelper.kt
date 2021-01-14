package com.nibble.hashcaller.view.utils

import android.util.Log
import com.nibble.hashcaller.view.ui.contacts.search.utils.SearchViewModel
import java.security.MessageDigest
import kotlin.experimental.and
const val TAG = "HashHelper"
fun hashPhoneNum(phoneNumber: String): String {
    val md: MessageDigest = MessageDigest.getInstance("SHA-256")
    md.update(phoneNumber.toByteArray());
    val bytes = md.digest()
    val sb = StringBuilder()
    for (element in bytes) {
        sb.append(
            ((element and 0xff.toByte()) + 0x100).toString(16)
                .substring(1)
        )
    }
    val hashedPhone = sb.toString()
    Log.d(TAG, "search: hashed phone is  $hashedPhone")
    return hashedPhone
}