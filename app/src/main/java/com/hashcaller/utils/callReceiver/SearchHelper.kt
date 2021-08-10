package com.hashcaller.utils.callReceiver

import android.content.Context
import android.util.Log
import com.hashcaller.Secrets
import com.hashcaller.work.formatPhoneNumber

class SearchHelper{
    companion object{
        suspend fun searchForNumberInServer(
            phoneNumber: String?,
            context: Context
        ) {

            var num = formatPhoneNumber(phoneNumber!!)
            num = Secrets().managecipher(context.packageName, num!!)//encoding the number with my algorithm

                try {
//                    val searchRepository = SearchNetworkRepository(
//                        TokenManager( DataStoreRepository(context.tokeDataStore)),
//                        tokenHelper
//                    )
//                    val res = searchRepository.search(num, token)
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