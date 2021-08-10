package com.hashcaller.view.ui.call.dialer.util

import android.telephony.PhoneNumberUtils
import com.hashcaller.view.ui.sms.individual.util.normalizeRegex
import java.text.Normalizer

fun String.normalizeString() = Normalizer.normalize(this, Normalizer.Form.NFD).replace(normalizeRegex, "")
fun String.normalizePhoneNumber() = PhoneNumberUtils.normalizeNumber(this)
