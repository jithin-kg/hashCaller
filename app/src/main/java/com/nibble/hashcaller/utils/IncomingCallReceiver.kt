package com.nibble.hashcaller.utils

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.local.db.blocklist.BlockedLIstDao
import com.nibble.hashcaller.network.search.model.Cntct
import com.nibble.hashcaller.network.user.Status
import com.nibble.hashcaller.repository.BlockListPatternRepository
import com.nibble.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import com.nibble.hashcaller.view.ui.IncommingCall.ActivityIncommingCallView
import com.nibble.hashcaller.view.ui.contacts.search.utils.SearchViewModel
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


/**
 * Created by Jithin KG on 20,July,2020
 * this class recieves the broadcast intent about call state
 */
class IncomingCallReceiver : BroadcastReceiver(){

    private lateinit var  blockedLIstDao:BlockedLIstDao
    private lateinit var blockListPatternRepository: BlockListPatternRepository


    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingPermission") // P`ermissions checked when app opened; just fail here if missing
    override fun onReceive(context: Context, intent: Intent) {
        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED != intent.action) {

//            val i = Intent(context, ActivityIncommingCallView::class.java)
//                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            context.startActivity(i)
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
            val inComingCallManager: InCommingCallManager = InCommingCallManager(blockListPatternRepository, context, phoneNumber)
            inComingCallManager.getBLockedLists()



//            inComingCallManager.getCallerInfo()
            /**
             * geting caller info from server
             */
            val serchNetworkRepo = SearchNetworkRepository(context)
            val contactsListDAO = HashCallerDatabase.getDatabaseInstance(context).contactInformationDAO()
            val contactLocalSyncRepository = ContactLocalSyncRepository(contactsListDAO)
            val viewModel = SearchViewModel(serchNetworkRepo, contactLocalSyncRepository)
            val i = Intent(context, ActivityIncommingCallView::class.java)
//            var obj = cntcts[0]
//            i.putExtra("SerachRes" ,obj)
//            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)

//            viewModel.search(phoneNumber!!).observeForever( androidx.lifecycle.Observer {
//                it.let {
//                        resource ->
//                    when(resource.status){
//                        Status.SUCCESS->{
//                            Log.d(TAG, " mhan: $it")
//                            resource.data?.let {
//                                    searchResult->
//                                Log.d(TAG, "getCallerInfo: $searchResult")
//                                Log.d(TAG, "getCallerInfo: ${searchResult.cntcts[0]}")
//                                //start Caller Info activity
//                              startCallerInfoActivity(context, searchResult.cntcts)
//
//                            }
//                        }
//                        Status.LOADING->{
//                            //show loading
//
//                            Log.d(TAG, "onQueryTextChange: Loading....")
//                        }
//                        else ->{
//                            Log.d(TAG, "onQueryTextChange: Error ${resource}")
//
//                            Toast.makeText(context.applicationContext, it.message, Toast.LENGTH_LONG).show()
//                        }
//                    }
//                }
//
//            })








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

    private fun startCallerInfoActivity(
        context: Context,
        cntcts: List<Cntct>
    ) {
        val i = Intent(context, ActivityIncommingCallView::class.java)
        var obj = cntcts[0]
        i.putExtra("SerachRes" ,obj)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(i)
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
