package com.nibble.hashcaller.view.ui.blockConfig

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.nibble.hashcaller.local.db.blocklist.BlockedListPattern
import com.nibble.hashcaller.repository.BlockListPatternRepository
import com.nibble.hashcaller.view.ui.call.work.CallContainerViewModel
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ADDRES
import com.nibble.hashcaller.view.ui.sms.individual.util.EXACT_NUMBER
import com.nibble.hashcaller.view.ui.sms.individual.util.ON_COMPLETED
import com.nibble.hashcaller.view.ui.sms.individual.util.SPAMMER_TYPE
import com.nibble.hashcaller.work.SpamReportWorker
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.lang.Exception

/**
 * viewmodel to block and report a number
 * and make as blocked in calllog,smsthread and report to server
 */
class GeneralblockViewmodel(
    val repository: GeneralBlockRepository?,
    private val blockListPatternRepository: BlockListPatternRepository,


):ViewModel() {


    fun blockThisAddress(spammerType: Int, contactAddress:String) : LiveData<Int> = liveData {
//        contactAddress = markeditemsHelper.getmarkedAddresAt(0) ?: ""
        if (contactAddress.isNotEmpty()) {
            viewModelScope.launch {
                supervisorScope {
                    val as1 = async { repository?.marAsReportedByUserInCall(contactAddress) }

                    val as2 = async {
                        blockListPatternRepository.insertPattern(contactAddress,EXACT_NUMBER )
                    }

                    val as3 = async {
                        val constraints =
                            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                                .build()
                        val data = Data.Builder()
                        data.putString(CONTACT_ADDRES, contactAddress)
                        data.putInt(SPAMMER_TYPE, spammerType)

                        val oneTimeWorkRequest =
                            OneTimeWorkRequest.Builder(SpamReportWorker::class.java)
                                .setConstraints(constraints)
                                .setInputData(data.build())
                                .build()
                        WorkManager.getInstance().enqueue(oneTimeWorkRequest)
                    }
                    val as4 = async { repository?.marAsReportedByUserInSMS(contactAddress) }

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
                    try {
                        as4.await()
                    }catch (e:Exception){
                        Log.d(TAG, "blockThisAddress: $e")
                    }
                }

                //******************************sms**********************//




//            }.join()
            }.join()

            emit(ON_COMPLETED)

        }
    }


    override fun onCleared() {
        super.onCleared()
    }

    companion object{
        const val TAG = "__GeneralblockViewmodel"
    }
}