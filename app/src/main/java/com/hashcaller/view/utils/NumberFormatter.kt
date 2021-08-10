package com.hashcaller.view.utils

import android.util.Log
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.hashcaller.view.ui.contacts.utils.isNumericOnlyString
import java.lang.Exception

class NumberFormatter {
    private val phoneUtil = PhoneNumberUtil.getInstance()
    fun getES164Formatednumber(phoneNum: String, countryIso:String): String {
        var formatedNumber = phoneNum
        try {
            val numProto = phoneUtil.parseAndKeepRawInput(phoneNum, countryIso)
            val formated = phoneUtil.format(numProto, PhoneNumberUtil.PhoneNumberFormat.E164)
            val formatedNumProto = phoneUtil.parse(formated, countryIso)
            val isValidNumberForRegion = phoneUtil.isValidNumberForRegion(formatedNumProto, countryIso)
            if(isValidNumberForRegion){
                formatedNumber = formated
            }
//           val numProto = phoneUtil.parse("+$phoneNum", "91")

//           val isValid = phoneUtil.isValidNumberForRegion(numProto, "IN")
//           Log.d(TAG, "getNumberWithCountryCode: $isValid")
//           val numProt2 = phoneUtil.parseAndKeepRawInput("918086176336", "IN")
//           Log.d(TAG, "getNumberWithCountryCode: ${numProt2.countryCode}")
//           Log.d(TAG, "getNumberWithCountryCode: ${numProt2.nationalNumber}")


        }catch (e: Exception){
            Log.d(LibPhoneCodeHelper.TAG, "getNumberWithCountryCode: $e")
        }
        return formatPhoneNumber(formatedNumber)
    }

    fun formatPhoneNumber(number:String): String{
    var numberCopy:String = ""
        numberCopy = number
        var fNum = ""
        val numWithoutSpceialChars = numberCopy.replace(Regex("[^A-Za-z0-9]"), "")

        if(isNumericOnlyString(numWithoutSpceialChars)){
            fNum = numberCopy.replace(Regex("[^A-Za-z0-9]"), "")
        }else{
            //not a numberic only type string, contains letters
            fNum = number.trim()

        }
        return fNum.trim()
    }
}