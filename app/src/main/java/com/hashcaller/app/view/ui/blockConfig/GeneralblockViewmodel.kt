package com.hashcaller.app.view.ui.blockConfig

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import androidx.work.*
import com.hashcaller.app.local.db.blocklist.BlockTypes.Companion.BLOCK_TYPE_EXACT_NUMBER
import com.hashcaller.app.local.db.blocklist.BlockTypes.Companion.BLOCK_TYPE_FROM_CALL_LOG
import com.hashcaller.app.local.db.blocklist.BlockTypes.Companion.BLOCK_TYPE_FROM_CONTACTS
import com.hashcaller.app.local.db.blocklist.BlockedListPattern
import com.hashcaller.app.repository.BlockListPatternRepository
import com.hashcaller.app.utils.Constants
import com.hashcaller.app.view.ui.call.work.CallContainerViewModel
import com.hashcaller.app.view.ui.contacts.utils.CONTACT_ADDRES
import com.hashcaller.app.view.ui.sms.individual.util.ON_COMPLETED
import com.hashcaller.app.work.SpamReportWorker
import com.hashcaller.app.work.UnblockWorker
import com.hashcaller.app.work.formatPhoneNumber
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.lang.Exception

/**
 * viewmodel to block and report a number
 * and make as blocked in calllog,smsthread and report to server
 */
class GeneralblockViewmodel(
    val repository: GeneralBlockRepository?, // general block repository to mark as blocked in sms,calllog
    private val blockListPatternRepository: BlockListPatternRepository,

    ):ViewModel() {

    val allBlockListLivedata: LiveData<MutableList<BlockedListPattern>>? =  blockListPatternRepository.getListLiveData()
    val isThisNumberBlocked : MutableLiveData<Boolean> = MutableLiveData(false)


    fun blockThisAddress(
        spammerType: Int,
        contactAddress: String,
        applicationContext: Context,
        intentSource: Int,
        name: String
    ) : LiveData<Int> = liveData {
//        contactAddress = markeditemsHelper.getmarkedAddresAt(0) ?: ""
        val formatedNum = formatPhoneNumber(contactAddress)
        if (contactAddress.isNotEmpty()) {
            viewModelScope.launch {
                supervisorScope {
                    val as1 = async { repository?.marAsReportedByUserInCall(formatedNum) }
//                    val as4 = async { repository?.marAsReportedByUserInSMS(formatedNum) }

                    val as2 = async {
                        blockListPatternRepository.insertPattern(contactAddress, intentSource, name)
                    }

                    val as3 = async {
                       startSpamReportWorker(contactAddress, spammerType,applicationContext)
                    }

                    try {
                        as1.await()
                    } catch (e: Exception) {
                        Log.d(CallContainerViewModel.TAG, "blockThisAddress: $e")
                    }
                    try {
                        as2.await()
                    } catch (e: Exception) {
                        Log.d(CallContainerViewModel.TAG, "blockThisAddress: $e")
                    }
                    try {
                        as3.await()
                    } catch (e: Exception) {
                        Log.d(CallContainerViewModel.TAG, "blockThisAddress: $e")
                    }
//                    try {
//                        as4.await()
//                    }catch (e:Exception){
//                        Log.d(TAG, "blockThisAddress: $e")
//                    }
                }

                //******************************sms**********************//




//            }.join()
            }.join()

            emit(ON_COMPLETED)

        }
    }

    private suspend fun startSpamReportWorker(
        contactAddress: String,
        spammerType: Int,
        applicationContext: Context
    ) {
        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        val data = Data.Builder()
        data.putString(CONTACT_ADDRES, contactAddress)
        data.putInt(Constants.SPAMMER_TYPE, spammerType)

        val oneTimeWorkRequest =
            OneTimeWorkRequest.Builder(SpamReportWorker::class.java)
                .setConstraints(constraints)
                .setInputData(data.build())
                .build()
        WorkManager.getInstance(applicationContext).enqueue(oneTimeWorkRequest)
    }
    private suspend fun startUnblockWorker(contactAddress: String, applicationContext: Context) {
        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        val data = Data.Builder()
        data.putString(CONTACT_ADDRES, contactAddress)
        val oneTimeWorkRequest =
            OneTimeWorkRequest.Builder(UnblockWorker::class.java)
                .setConstraints(constraints)
                .setInputData(data.build())
                .build()
        WorkManager.getInstance(applicationContext).enqueue(oneTimeWorkRequest)
    }


    override fun onCleared() {
        super.onCleared()
    }

    fun updateBlockListOfIndividual(it: List<BlockedListPattern>?, phoneNum: String) = viewModelScope.launch {
       var isNumBlockedByUser = false
        if(allBlockListLivedata?.value!=null){
            for (pattern in allBlockListLivedata.value!!){
                if(pattern.numberPattern == formatPhoneNumber(phoneNum) && (pattern.type == BLOCK_TYPE_EXACT_NUMBER  || pattern.type == BLOCK_TYPE_FROM_CALL_LOG || pattern.type == BLOCK_TYPE_FROM_CONTACTS )){
                    isNumBlockedByUser = true
                    break
                }
            }
        }
        isThisNumberBlocked.value = isNumBlockedByUser

    }

    /**
     * @param numberType :  BlockTypes.BLOCK_TYPE_EXACT_NUMBER, ...
     */
    fun removeFromBlockList(
        phoneNum: String,
        numberType: Int,
        randomColor: Int,
        applicationContext: Context
    )  = viewModelScope.launch{
        val defCall = async { repository?.markAsNotSpamInCalls(phoneNum, randomColor) }
        val defPattern = async { blockListPatternRepository.delete(phoneNum, numberType) }
//        val defSMS = async { repository?.markAsNotSpamInSMS(phoneNum, randomColor) }
        val defWorker = async { startUnblockWorker(phoneNum,applicationContext ) }
        try{
            defPattern.await()
        }catch (e:Exception){
            Log.d(TAG, "removeFromBlockList: $e")
        }
        try{
            defCall.await()

        }catch (e:Exception){
            Log.d(TAG, "removeFromBlockList: $e")
        }
        try{
            defWorker.await()
        }catch (e:Exception){
            Log.d(TAG, "removeFromBlockList: $e")
        }

       /* try{
            defSMS.await()

        }catch (e:Exception){
            Log.d(TAG, "removeFromBlockList: $e")
        }*/
    }


    companion object{
        const val TAG = "__GeneralblockViewmodel"
    }
}