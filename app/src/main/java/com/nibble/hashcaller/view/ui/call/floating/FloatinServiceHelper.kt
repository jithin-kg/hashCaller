package com.nibble.hashcaller.view.ui.call.floating

import android.content.Context
import android.util.Log
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.datastore.PreferencesKeys
import com.nibble.hashcaller.network.StatusCodes
import com.nibble.hashcaller.network.search.model.CntctitemForView
import com.nibble.hashcaller.utils.callReceiver.InCommingCallManager
import com.nibble.hashcaller.utils.callReceiver.InCommingCallManager.Companion.REASON_BLOCK_BY_PATTERN
import com.nibble.hashcaller.utils.callReceiver.InCommingCallManager.Companion.REASON_BLOCK_NON_CONTACT
import com.nibble.hashcaller.utils.callReceiver.InCommingCallManager.Companion.REASON_BLOCK_TOP_SPAMMER
import com.nibble.hashcaller.utils.callReceiver.InCommingCallManager.Companion.REASON_FOREIGN
import com.nibble.hashcaller.view.ui.contacts.utils.DATE_THREASHOLD
import com.nibble.hashcaller.view.ui.contacts.utils.SPAM_THREASHOLD
import com.nibble.hashcaller.view.ui.contacts.utils.isCurrentDateAndPrevDateisGreaterThanLimit
import kotlinx.coroutines.*

class FloatinServiceHelper(
    private val inComingCallManager: InCommingCallManager,
    private val hashedNum: String,
    private val supervisorScope: CoroutineScope,
    private val window: Window?,
    private val phoneNumber: String,
    private val context: Context,
    private val isCallScreeningRoleHeld: Boolean,
    private val dataStoreRepository: DataStoreRepository,
) {

    suspend fun  handleCall(){
    var isInfoFoundInCprovider = false
        supervisorScope.launch {
            val isBlockCommonSpammersEnabled =  dataStoreRepository.getSharedPreferencesBoolean(PreferencesKeys.KEY_BLOCK_COMMONG_SPAMMERS)

                var isSpam = false
                val defBlockedByPattern = async { inComingCallManager.isBlockedByPattern() }
                val defNonContactsBlocked = async { inComingCallManager.isNonContactsCallsAllowed() }
                var defServerHandling:Deferred<CntctitemForView?>? = null
                val definfoFromDb = async { inComingCallManager.getAvailbleInfoInDb() }
                val defredInfoFromCprovider = async { inComingCallManager.infoFromContentProvider() }

                val deferedBlockForeignCountry = async { inComingCallManager.isBlockForeignCountryEnabled() }
                try {
                    val contactInCprovider = defredInfoFromCprovider.await()
//                    window.open()
                    if(contactInCprovider!=null){
                        //the caller is in contact, so set information in db as caller information
                        isInfoFoundInCprovider = true
                        window?.updateWithcontentProviderInfo(contactInCprovider)
                    }

                    val infoAvailableInDb = definfoFromDb.await()
                    if(infoAvailableInDb!=null){
                        if(infoAvailableInDb.spammCount?:0L > SPAM_THREASHOLD && isBlockCommonSpammersEnabled){
                            isSpam = true
                            endCall(
                                inComingCallManager,
                                phoneNumber,
                                REASON_BLOCK_TOP_SPAMMER
                                )

                        }
                        if(!isInfoFoundInCprovider){
                            window?.updateWithServerInfo(infoAvailableInDb, phoneNumber)
                        }
                        if(isCurrentDateAndPrevDateisGreaterThanLimit(infoAvailableInDb.informationReceivedDate, DATE_THREASHOLD)
                            && !isCallScreeningRoleHeld){
                                //if data is out of date then get new updated data from server
                            defServerHandling =  async {  inComingCallManager.searchInServerAndHandle(
                                hashedNum
                            ) }

                        }
                    }else{
                        //todo check date of the info received from server, if today - date >0 search in server
                        if(!isCallScreeningRoleHeld){
                                defServerHandling =  async {  inComingCallManager.searchInServerAndHandle(hashedNum) }

                        }
                    }
                }catch (e:Exception){
                    Log.d(TAG, "handleCall: $e")
                }
                try {
                    Log.d(TAG, "onReceive: firsttry")
                    val isBlockedByPattern  = defBlockedByPattern.await()
                    if(isBlockedByPattern){
                        isSpam = true
                        endCall(
                            inComingCallManager,
                            phoneNumber,
                            REASON_BLOCK_BY_PATTERN
                            )
                    }
                }catch (e: Exception){
                    Log.d(TAG, "onReceive: $e")
                }
                try {

                    Log.d(TAG, "onReceive: second try")
                    val resFromServer = defServerHandling?.await()
                    if(resFromServer?.statusCode == StatusCodes.STATUS_OK){
                        window?.updateWithServerInfo(resFromServer, phoneNumber)
                    }

                    if(resFromServer?.spammCount?:0 > SPAM_THREASHOLD && isBlockCommonSpammersEnabled){

                        isSpam = true
                        endCall(
                            inComingCallManager,
                            phoneNumber,
                            REASON_BLOCK_TOP_SPAMMER
                            )
                    }
                    if(resFromServer!=null){
                        inComingCallManager.saveInfoFromServer(resFromServer, phoneNumber)
                    }

                }catch (e: Exception){
                    Log.d(TAG, "onReceive: $e ")
                }
                try {
                    Log.d(TAG, "onReceive: third try ")
                    val r = defNonContactsBlocked.await()
                    if(r){
                        endCall(
                            inComingCallManager,
                            phoneNumber,
                            REASON_BLOCK_NON_CONTACT
                            )
                    }
                }catch (e: Exception){
                    Log.d(TAG, "onReceive: $e")
                }

            try {
                if(deferedBlockForeignCountry.await()){
                    endCall(
                        inComingCallManager,
                        phoneNumber,
                        REASON_FOREIGN
                    )
                }
            }catch (e:Exception){
                Log.d(TAG, "handleCall: exception country ${e}")
            }
//            stopSelf();

        }.join()
//        context.stopFloatingService(incomingNumber = "incomingNumber")

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
        phoneNumber: String,
        reason:Int
    ) {
        window?.setwindowSpamColor()
        inComingCallManager.endIncommingCall(reason)
//        notificationHelper.showNotificatification(true, phoneNumber)
    }

    companion object{
        const val TAG = "__FloatinServiceHelper"
    }
}