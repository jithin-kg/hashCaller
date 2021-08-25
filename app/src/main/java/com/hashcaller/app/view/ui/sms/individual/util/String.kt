package com.hashcaller.app.view.ui.sms.individual.util

import android.telephony.PhoneNumberUtils
import java.text.Normalizer
val normalizeRegex = "\\p{InCombiningDiacriticalMarks}+".toRegex()


// remove diacritics, for example č -> c
fun String.normalizeString() = Normalizer.normalize(this, Normalizer.Form.NFD).replace(normalizeRegex, "")

fun String.normalizePhoneNumber() = PhoneNumberUtils.normalizeNumber(this)
