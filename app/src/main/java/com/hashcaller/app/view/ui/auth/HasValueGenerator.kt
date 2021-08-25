package com.hashcaller.app.view.ui.auth

import android.util.Log
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.experimental.and

/**
 * Created by Jithin KG on 24,July,2020
 */
class HasValueGenerator {
    private val TAG = "HasValueGenerator"
    fun generateHash(phoneNumber: String): String? {
        var hashedPhoneNumber = ""
        val generatedPassword: String? = null
        try {
            val md = MessageDigest.getInstance("md5")
            md.update(phoneNumber.toByteArray())
            val phoneBytes = md.digest()
            val sb = StringBuilder()
            for (i in phoneBytes.indices) {
                sb.append(
                    Integer.toString((phoneBytes[i] and 0xff.toByte()) + 0x100, 16)
                        .substring(1)
                )
            }
            hashedPhoneNumber = sb.toString()
            Log.d(TAG, "genratehash: $hashedPhoneNumber")
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return hashedPhoneNumber
    }
}