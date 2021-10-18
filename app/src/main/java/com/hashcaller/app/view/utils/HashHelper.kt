package com.hashcaller.app.view.utils

import android.content.Context
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import java.security.MessageDigest
import kotlin.experimental.and
private const val TAG = "__HashHelper"
//fun hashPhoneNum(phoneNumber: String): String {
//    val md: MessageDigest = MessageDigest.getInstance("SHA-256")
//    md.update(phoneNumber.toByteArray());
//    val bytes = md.digest()
//    val sb = StringBuilder()
//    for (element in bytes) {
//        sb.append(
//            ((element and 0xff.toByte()) + 0x100).toString(16)
//                .substring(1)
//        )
//    }
//    val hashedPhone = sb.toString()
//    return hashedPhone
//}

fun Context.showKeyboard(editText: EditText) {
    val inputMethodManager: InputMethodManager =
        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.toggleSoftInputFromWindow(
        editText.applicationWindowToken,
        InputMethodManager.SHOW_IMPLICIT, 0
    )
    editText.requestFocus()
    editText.setSelection(editText.text.length)
}

fun Context.hideKeyboard(editText: EditText) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(editText.windowToken, 0)
}