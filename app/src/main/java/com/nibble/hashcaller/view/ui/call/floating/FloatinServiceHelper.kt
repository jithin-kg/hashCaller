package com.nibble.hashcaller.view.ui.call.floating

import android.content.Context
import android.util.Log
import com.nibble.hashcaller.network.StatusCodes
import com.nibble.hashcaller.network.search.model.CntctitemForView
import com.nibble.hashcaller.utils.callReceiver.InCommingCallManager
import com.nibble.hashcaller.view.ui.contacts.stopFloatingService
import com.nibble.hashcaller.view.ui.contacts.utils.SPAM_THREASHOLD
import kotlinx.coroutines.*

class FloatinServiceHelper(
    private val inComingCallManager: InCommingCallManager,
    private val hashedNum: String,
    private val supervisorScope: CoroutineScope,
    private val window: Window,
    private val phoneNumber: String,
    private val context:Context
) {

    suspend fun  handleCall(){
    var isInfoFoundInCprovider = false
        supervisorScope.launch {
                //start operations iff screening role not avaialble
                Log.d(TAG, "onReceive: role not held")

                var isSpam = false


                val defBlockedByPattern = async { inComingCallManager.isBlockedByPattern() }
                val defNonContactsBlocked = async { inComingCallManager.isNonContactsCallsAllowed() }
                var defServerHandling:Deferred<CntctitemForView>? = null
                val definfoFromDb = async { inComingCallManager.getAvailbleInfoInDb() }
                val defredInfoFromCprovider = async { inComingCallManager.infoFromContentProvider() }
                try {
                    val contactInCprovider = defredInfoFromCprovider.await()
//                    window.open()
                    if(contactInCprovider!=null){
                        //the caller is in contact, so set information in db as caller information
                        isInfoFoundInCprovider = true
                        window.updateWithcontentProviderInfo(contactInCprovider)
                    }
                    val infoAvailableInDb = definfoFromDb.await()
                    if(infoAvailableInDb!=null){
                        if(!isInfoFoundInCprovider){
                            window.updateWithServerInfo(infoAvailableInDb, phoneNumber)
                        }
                    }else{
                        //todo check date of the info received from server, if today - date >0 search in server
                         defServerHandling =  async {  inComingCallManager.searchInServerAndHandle(
                            hashedNum
                        ) }
                    }
                }catch (e:Exception){
                    Log.d(TAG, "handleCall: $e")
                }
                try {
                    Log.d(TAG, "onReceive: firsttry")
                    val isBlockedByPattern  = defBlockedByPattern.await()
                    if(isBlockedByPattern){
                        isSpam = true
                        endCall(inComingCallManager,
                            phoneNumber, )
                    }
                }catch (e: Exception){
                    Log.d(TAG, "onReceive: $e")
                }
                try {

                    Log.d(TAG, "onReceive: second try")
                    val resFromServer = defServerHandling?.await()
                    if(resFromServer?.statusCode == StatusCodes.STATUS_OK){
                        window.updateWithServerInfo(resFromServer, phoneNumber)

                    }
                    if(resFromServer?.spammCount?:0 > SPAM_THREASHOLD){
                        isSpam = true
                        endCall(inComingCallManager,
                            phoneNumber)
                    }
                    inComingCallManager.saveInfoFromServer(resFromServer, phoneNumber)
                }catch (e: Exception){
                    Log.d(TAG, "onReceive: $e ")
                }
                try {
                    Log.d(TAG, "onReceive: third try ")
                    val r = defNonContactsBlocked.await()
                    if(r){
                        endCall(inComingCallManager,
                            phoneNumber,)
                    }
                }catch (e: Exception){
                    Log.d(TAG, "onReceive: $e")
                }




//            stopSelf();

        }.join()
        context.stopFloatingService(incomingNumber = "incomingNumber")

    }


//    private fun getIncomminCallManager(phoneNumber: String, context: Context): InCommingCallManager {
//        val  blockedListpatternDAO: BlockedLIstDao = HashCallerDatabase.getDatabaseInstance(context).blocklistDAO()
//
//        searchRepository = SearchNetworkRepository(TokenManager(DataStoreRepository(context.tokeDataStore)))
//        val internetChecker = InternetChecker(context)
//        val contactAdressesDAO = HashCallerDatabase.getDatabaseInstance(context).contactAddressesDAO()
//
//        return  InCommingCallManager(
//            context,
//            phoneNumber, context.isBlockNonContactsEnabled(),
//            null, searchRepository,
//            internetChecker, blockedListpatternDAO,
//            contactAdressesDAO
//        )
//    }


    private fun endCall(
        inComingCallManager: InCommingCallManager,
        phoneNumber: String
    ) {
        inComingCallManager.endIncommingCall()
//        notificationHelper.showNotificatification(true, phoneNumber)
    }

    companion object{
        const val TAG = "__FloatinServiceHelper"

    }
}