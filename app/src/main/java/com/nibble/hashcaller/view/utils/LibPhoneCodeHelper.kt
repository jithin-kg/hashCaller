package com.nibble.hashcaller.view.utils

import android.util.Log
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.*

class LibPhoneCodeHelper(private val phoneUtil: PhoneNumberUtil) {

    suspend fun getCountryIso(phoneNum: String, countryIso: String): String {
        var regionCode = ""
        try {
            val numberProto =phoneUtil.parse(phoneNum, countryIso)
             regionCode = phoneUtil.getRegionCodeForNumber(numberProto)
            Log.d(TAG+"iso", "getCopuntryIso: regionCode:$regionCode")
        }catch (e:Exception){
            Log.d(TAG+"iso", "getCopuntryIso: $e")
        }
        return regionCode
    }
    suspend fun getCountryName(phoneNum:String): String = withContext(Dispatchers.IO) {
        var countryName = ""
        try {
            val numberProto =phoneUtil.parse(phoneNum, "IN")

//             countryCode = numberProto.countryCode.toString()
//            PhoneNumberUtil.REGION_CODE_FOR_NON_GEO_ENTITY
//            PhoneNumberOfflineGeocoder
           val geocoder =  PhoneNumberOfflineGeocoder.getInstance()
            countryName= geocoder.getDescriptionForNumber(numberProto, Locale.ENGLISH)
            val regionCode = phoneUtil.getRegionCodeForNumber(numberProto)

            Log.d(TAG, "getCountryCode: $countryName")

        }catch (e:Exception){
            Log.d(TAG, "getCountryCode: exception $phoneNum  $e")
        }
        Log.d(TAG, "getCountryCode: returning $countryName")
        return@withContext countryName
    }

    /**
     * https://github.com/google/libphonenumber/blob/10c40f6a583d97318449b3204a26f917dc6e308e/java/demo/src/com/google/phonenumbers/PhoneNumberParserServlet.java#L248
     * https://libphonenumber.appspot.com/phonenumberparser?number=8086176335&country=IN
     *
     * formats a phone number to ES164 standard
     */
     fun getES164Formatednumber(phoneNum: String, countryIso:String): String {
        var formatedNumber = phoneNum

       try {
           val numProto = phoneUtil.parseAndKeepRawInput(phoneNum, countryIso)
           val e164Formated = phoneUtil.format(numProto, PhoneNumberUtil.PhoneNumberFormat.E164)
           val formatedNumProto = phoneUtil.parse(e164Formated, countryIso)
           val isValidNumberForRegion = phoneUtil.isValidNumberForRegion(formatedNumProto, countryIso)

           if(isValidNumberForRegion){
               formatedNumber = e164Formated
           }
//           val numProto = phoneUtil.parse("+$phoneNum", "91")

//           val isValid = phoneUtil.isValidNumberForRegion(numProto, "IN")
//           Log.d(TAG, "getNumberWithCountryCode: $isValid")
//           val numProt2 = phoneUtil.parseAndKeepRawInput("918086176336", "IN")
//           Log.d(TAG, "getNumberWithCountryCode: ${numProt2.countryCode}")
//           Log.d(TAG, "getNumberWithCountryCode: ${numProt2.nationalNumber}")


       }catch (e:Exception){
           Log.d(TAG, "getNumberWithCountryCode: $e")
       }
        return formatPhoneNumber(formatedNumber)
    }

    companion object{
        const val TAG = "__LibCoutryCodeHelper"
    }
}