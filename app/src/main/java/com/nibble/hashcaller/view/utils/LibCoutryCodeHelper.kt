package com.nibble.hashcaller.view.utils

import android.util.Log
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.*

class LibCoutryCodeHelper(private val phoneUtil: PhoneNumberUtil) {

    suspend fun getCountryCode(phoneNum:String): String = withContext(Dispatchers.IO) {
        var countryCodeIso = ""
        try {
            val numberProto =phoneUtil.parse(phoneNum, "")
//             countryCode = numberProto.countryCode.toString()
//            PhoneNumberUtil.REGION_CODE_FOR_NON_GEO_ENTITY
//            PhoneNumberOfflineGeocoder
           val geocoder =  PhoneNumberOfflineGeocoder.getInstance()
            countryCodeIso= geocoder.getDescriptionForNumber(numberProto, Locale.ENGLISH)
            Log.d(TAG, "getCountryCode: $countryCodeIso")


        }catch (e:Exception){
            Log.d(TAG, "getCountryCode: exception $phoneNum  $e")
        }
        Log.d(TAG, "getCountryCode: returning $countryCodeIso")
        return@withContext countryCodeIso


    }

    companion object{
        const val TAG = "__LibCoutryCodeHelper"
    }
}