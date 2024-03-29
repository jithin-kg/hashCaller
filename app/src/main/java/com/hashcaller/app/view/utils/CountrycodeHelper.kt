package com.hashcaller.app.view.utils

import android.Manifest.permission.READ_PHONE_STATE
import android.annotation.SuppressLint
import android.content.Context
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import com.hashcaller.app.view.utils.spam.OperatorInformationDTO
import com.vmadalin.easypermissions.EasyPermissions
import java.util.*

/**
 * helper class to get sim operator name and country code
 */
class CountrycodeHelper(private val context: Context) {


     fun getCountrycode(): String {

         var countryCode = ""
         if(isPhoneReadStatePermissionGiven()){



//            SubscriptionManager.ACTION_REFRESH_SUBSCRIPTION_PLANS
             countryCode = getCountryCode().toString()

        }


        return countryCode

    }

    @SuppressLint("MissingPermission", "LogNotTimber")
    private fun getCountryCode(): Int {
        val subscriptionInfoList = mutableListOf<OperatorInformationDTO>()
        val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val subscriptionInfos:List<SubscriptionInfo> =subscriptionManager.activeSubscriptionInfoList
        var cCode = 0
        for (element in subscriptionInfos) {
            val lsuSubscriptionInfo: SubscriptionInfo = element
            val operatorDisplayName = lsuSubscriptionInfo.displayName

//                    val tel =  getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager;
//                    val operator = tel.networkOperator
//                    val simOperator = tel.simOperator

            val countryIso = lsuSubscriptionInfo.countryIso.uppercase(Locale.getDefault())
            val countryCode = com.google.i18n.phonenumbers.PhoneNumberUtil.getInstance().getCountryCodeForRegion(countryIso)
//            val countryCode =
//                io.michaelrocks.libphonenumber.android.PhoneNumberUtil.createInstance(context)
//                    .getCountryCodeForRegion(countryIso)
            cCode = countryCode

            subscriptionInfoList.add(
                OperatorInformationDTO(
                    operatorDisplayName.toString(),
                    lsuSubscriptionInfo.countryIso
                )
            )

        }
        return cCode

    }

    private fun isPhoneReadStatePermissionGiven(): Boolean {

       return  EasyPermissions.hasPermissions(context, READ_PHONE_STATE)
//        if (ActivityCompat.checkSelfPermission(
//                context,
//                Manifest.permission.READ_PHONE_STATE
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            return true
//
//        }
//        return false
    }

    fun getcountryCodeOfUser(){

    }

    @SuppressLint("MissingPermission", "LogNotTimber")
    fun getCountryISO(): String {
        var countryIso  = ""
        if (isPhoneReadStatePermissionGiven()) {

            val subscriptionInfoList = mutableListOf<OperatorInformationDTO>()

            val subscriptionManager =
                context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            val subscriptionInfos: List<SubscriptionInfo> =
                subscriptionManager.activeSubscriptionInfoList
            var cCode = 0
            for (element in subscriptionInfos) {
                val lsuSubscriptionInfo: SubscriptionInfo = element
                val operatorDisplayName = lsuSubscriptionInfo.displayName
//                    val tel =  getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager;
//                    val operator = tel.networkOperator
//                    val simOperator = tel.simOperator
                lsuSubscriptionInfo.iccId
                 countryIso = lsuSubscriptionInfo.countryIso.toUpperCase()
            }
        }
        return countryIso

    }
    companion object{
        const val TAG = "__CountrycodeHelper"
    }

}