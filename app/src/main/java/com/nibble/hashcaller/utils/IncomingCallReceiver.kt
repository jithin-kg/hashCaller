package com.nibble.hashcaller.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.android.internal.telephony.ITelephony
import com.nibble.hashcaller.data.local.db.HashCallerDatabase
import com.nibble.hashcaller.data.local.db.dao.BlockedLIstDao
import com.nibble.hashcaller.data.repository.BlockListPatternRepository
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

/**
 * Created by Jithin KG on 20,July,2020
 */
class IncomingCallReceiver : BroadcastReceiver(){

    lateinit var  blockedLIstDao:BlockedLIstDao
    lateinit var blockListPatternRepository: BlockListPatternRepository


    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingPermission") // P`ermissions checked when app opened; just fail here if missing
    override fun onReceive(context: Context, intent: Intent) {
        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED != intent.action) {





            Log.e(
                LOG_TAG,
                String.format(
                    "IncomingCallReceiver called with incorrect intent action: %s",
                    intent.action
                )
            )
            return
        }
        Log.d(LOG_TAG, "call recieved")

        val newState = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        Log.d(
            LOG_TAG,
            String.format("Call state changed to %s", newState)
        )
        if (TelephonyManager.EXTRA_STATE_RINGING == newState) {

            val phoneNumber =
                intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            Log.d(
                TAG,
                "extracarrire name " + intent.getStringArrayExtra(TelephonyManager.EXTRA_CARRIER_NAME)
            )
            Log.d(
                TAG,
                "extracarrire carrier " + intent.getStringArrayExtra(TelephonyManager.EXTRA_SPECIFIC_CARRIER_NAME)
            )
            Log.d(
                TAG,
                "extracarrire coutry " + intent.getStringArrayExtra(TelephonyManager.EXTRA_NETWORK_COUNTRY)
            )
            Log.d(
                TAG,
                "extracarrire accountHandler " + intent.getStringArrayExtra(TelephonyManager.EXTRA_PHONE_ACCOUNT_HANDLE)
            )
            val extraNetworkCountry = TelephonyManager.EXTRA_NETWORK_COUNTRY
            Log.i(TAG, extraNetworkCountry)
            val actionNetworkCountryChanged =
                TelephonyManager.ACTION_NETWORK_COUNTRY_CHANGED
            Log.i(TAG, actionNetworkCountryChanged)


            /**
             * check if receive call ony from contacts enabled
             */
            //initialise incomming call manager
//            sharedPreferences = context.getSharedPreferences(
//                MyPREFERENCES,
//                Context.MODE_PRIVATE
//            )


            if (phoneNumber == null) {
                Log.d(
                    LOG_TAG,
                    "Ignoring call; for some reason every state change is doubled"
                )
                return
            }
            Log.i(
                LOG_TAG,
                String.format("Incoming call from %s", phoneNumber)
            )
            blockedLIstDao = HashCallerDatabase.getDatabaseInstance(context).blocklistDAO()
            blockListPatternRepository = BlockListPatternRepository(blockedLIstDao)
            val inComingCallManager: InCommingCallManager = InCommingCallManager(blockListPatternRepository)
            inComingCallManager.getBLockedLists()
//            genratehash(phoneNumber, context)
            /**
             * From here we manage incomming call by IncommingCallManager class
             */
            //TODO CHECK IF internet DATA connection IS ENABLED

            /**
             * identify the caller if incomming numer in contact
             */

        }
    }

    private fun genratehash(
        phoneNumber: String,
        context: Context
    ) {
        val generatedPassword: String? = null
        try {
            val md = MessageDigest.getInstance("md5")
            md.update(phoneNumber.toByteArray())
            val phoneBytes = md.digest()
            val sb = StringBuilder()
            for (i in phoneBytes.indices) {
//                sb.append(
//                    Integer.toString((phoneBytes[i]  0xff) + 0x100, 16)
//                        .substring(1)
//                )
            }
            val hashedPhoneNumber = sb.toString()
            Toast.makeText(context, hashedPhoneNumber, Toast.LENGTH_SHORT).show()
            Log.d(
                TAG,
                "genratehash: $hashedPhoneNumber"
            )
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
    }

    private fun isDataEnabled() {
        //todo tm.isDataEnabled();
    }





    companion object {
        private const val LOG_TAG = "__IncommingCallReceiver"
        private const val MyPREFERENCES = "onlyIncCallFromContact"
        private const val TAG = "IncomingCallReceiver"
    }
}
