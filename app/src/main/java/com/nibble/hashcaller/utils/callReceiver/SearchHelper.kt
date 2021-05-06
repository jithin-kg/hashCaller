package com.nibble.hashcaller.utils.callReceiver

import android.content.Context
import android.content.Intent
import android.util.Log
import com.nibble.hashcaller.Secrets
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.utils.notifications.tokeDataStore
import com.nibble.hashcaller.view.ui.IncommingCall.ActivityIncommingCallView
import com.nibble.hashcaller.work.formatPhoneNumber

class SearchHelper{
    companion object{
        suspend fun searchForNumberInServer(
            phoneNumber: String?,
            context: Context
        ) {

            var num = formatPhoneNumber(phoneNumber!!)
            num = Secrets().managecipher(context.packageName, num!!)//encoding the number with my algorithm

                try {
                    val searchRepository = SearchNetworkRepository(TokenManager( DataStoreRepository(context.tokeDataStore)))
                    val res = searchRepository.search(num)
//                    if(!res?.body()?.cntcts.()){
//                        val result = res?.body()?.cntcts?.get(0)
//                        Log.d(TAG, "searchForNumberInServer: result $result")

//                        if(result!!.spammCount?:0 > 0){


//                        }
//                        val i = Intent(context, ActivityIncommingCallView::class.java)
//                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                        i.putExtra("name", result.firstName)
//                        i.putExtra("phoneNumber", phoneNumber)
//                        i.putExtra("spamcount", result.spammCount)
//                        i.putExtra("carrier", result.carrier)
//                        i.putExtra("location", result.location)
//                        context.startActivity(i)
//                    }else{
//                        //if there is no info about the caller in server db
//                        val i = Intent(context, ActivityIncommingCallView::class.java)
//                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                        i.putExtra("name", "")
//                        i.putExtra("phoneNumber", phoneNumber)
//                        i.putExtra("spamcount", "")
//                        i.putExtra("carrier", "")
//                        i.putExtra("location", "")
//                        context.startActivity(i)
//                    }
                }catch (e:Exception){
                    Log.d(TAG, "searchForNumberInServer: exception $e")
                }



        }
        const val TAG = "__SearchHelper"
    }
}