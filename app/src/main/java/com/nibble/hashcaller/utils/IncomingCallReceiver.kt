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
import androidx.lifecycle.Observer
import com.nibble.hashcaller.Secrets
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.local.db.blocklist.BlockedLIstDao
import com.nibble.hashcaller.network.search.model.Cntct
import com.nibble.hashcaller.repository.BlockListPatternRepository
import com.nibble.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import com.nibble.hashcaller.view.ui.IncommingCall.ActivityIncommingCallView
import com.nibble.hashcaller.view.ui.contacts.search.utils.SearchViewModel
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
                TAG,
                String.format(
                    "IncomingCallReceiver called with incorrect intent action: %s",
                    intent.action
                )
            )
            return
        }
        Log.d(TAG, "call recieved")

        val newState = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        Log.d(
            TAG,
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
                    TAG,
                    "Ignoring call; for some reason every state change is doubled"
                )
                return
            }
            Log.i(
                TAG,
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

//            var obj = cntcts[0]
//            i.putExtra("SerachRes" ,obj)

            searchForNumberInServer(phoneNumber, viewModel, context)









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

    private fun searchForNumberInServer(
        phoneNumber: String?,
        viewModel: SearchViewModel,
        context: Context
    ) {
        Log.d(TAG, "searchForNumberInServer: ")
        var num = formatPhoneNumber(phoneNumber!!)
        num = Secrets().managecipher(context.packageName, num!!)//encoding the number with my algorithm
      
        CoroutineScope(Dispatchers.IO).launch {
            val res = SearchNetworkRepository(context).search(num)
            if(!res?.body()?.cntcts.isNullOrEmpty()){
                val result = res?.body()?.cntcts?.get(0)
                Log.d(TAG, "searchForNumberInServer: result $result")
                if(result!!.spammCount > 0){
                    val inComingCallManager: InCommingCallManager = InCommingCallManager(blockListPatternRepository, context, phoneNumber)
                    inComingCallManager.endIncommingCall(context)
                }
                val i = Intent(context, ActivityIncommingCallView::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                i.putExtra("name", result.name)
                i.putExtra("phoneNumber", phoneNumber)
                i.putExtra("spamcount", result.spammCount)
                i.putExtra("carrier", result.carrier)
                i.putExtra("location", result.location)
                context.startActivity(i)
            }else{
                //if there is no info about the caller in server db
                val i = Intent(context, ActivityIncommingCallView::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                i.putExtra("name", "")
                i.putExtra("phoneNumber", phoneNumber)
                i.putExtra("spamcount", "")
                i.putExtra("carrier", "")
                i.putExtra("location", "")
                context.startActivity(i)
            }

        }
       
    }

    private fun observeSearchLiveData(
        viewModel: SearchViewModel,
        context: Context
    ) {
//        viewModel.searchResultLiveData.observeForever(Observer {
//            Log.d(TAG, "observeSearchLiveData: ")
//            if(it!=null){
//                Log.d(TAG, "observeSearchLiveData: ${it}")
//            }
//        })

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
//        private const val LOG_TAG = "__IncommingCallReceiver"
        private const val MyPREFERENCES = "onlyIncCallFromContact"
        private const val TAG = "__IncomingCallReceiver"
    }
}
