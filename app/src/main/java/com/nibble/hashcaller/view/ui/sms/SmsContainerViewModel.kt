package com.nibble.hashcaller.view.ui.sms

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.nibble.hashcaller.Secrets
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServerDAO
import com.nibble.hashcaller.network.spam.hashednums
import com.nibble.hashcaller.view.ui.sms.list.SMSLiveData
import com.nibble.hashcaller.view.ui.sms.util.SMS
import com.nibble.hashcaller.view.ui.sms.work.NewSMSSaveToLocalDbWorker
import com.nibble.hashcaller.view.ui.sms.work.SmsHashedNumUploadWorker
import com.nibble.hashcaller.view.utils.hashPhoneNum
import kotlinx.coroutines.launch

class SmsContainerViewModel(
    val SMS: SMSLiveData,
    val repository: SMScontainerRepository?,
    val SMSSendersInfoFromServerDAO: SMSSendersInfoFromServerDAO?
) :ViewModel(){

    fun getInformationForTheseNumbers(
        smslist: List<SMS>?,
        packageName: String
    ) = viewModelScope.launch {

        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(NewSMSSaveToLocalDbWorker::class.java).build()
        WorkManager.getInstance().enqueue(oneTimeWorkRequest)

//        Log.d(TAG, "getInformationForTheseNumbers: ")
        // SMS - minus - spmmersInfoListFromLocalDb -> data to be send
        //todo move this whole logic into workmanager, because this takes time
//       val spammerListfromLocal =  repository!!.geSmsSendersStoredInLocalDB()
//        var  hashedPhoneNumbers:MutableList<String> = mutableListOf()
//        var phoneNumbersAvailableInlocalDB:MutableList<String> = mutableListOf()


//        if (smslist != null) {
//            for (sms in smslist){
//                val secret = sms.addressString?.let { Secrets().managecipher(packageName, it) }
//                val hashedNum = secret?.let { hashPhoneNum(it) }
//                hashedNum?.let { hashedPhoneNumbers.add(it) }
//            }
//        }
//        for (spammer in spammerListfromLocal){
//            if(hashedPhoneNumbers.contains(spammer.contactAddress)){
//                spammer.contactAddress?.let { phoneNumbersAvailableInlocalDB.add(it) }
//            }
//        }
//       var numberToBeUploaded =  hashedPhoneNumbers - phoneNumbersAvailableInlocalDB
//        //because sending more than 10 items will slow down server and increases load
//        //and increases response time
//        if(!numberToBeUploaded.isNullOrEmpty())
//            if(numberToBeUploaded.size > 10){
//                val numberToBeUploadedOfSize10 = numberToBeUploaded.slice(0..9)
//
//                val obj = hashednums(numberToBeUploadedOfSize10)//object for transfering or dto
////        obj.hashedPhoneNum.addAll(numberToBeUploadedOfSize10)
//                if(!numberToBeUploadedOfSize10.isNullOrEmpty()){
//                    //schedule work
//                    Log.d(TAG, "getInformationForTheseNumbers: ")

//                    val oneTimeWorkRequest = OneTimeWorkRequest.Builder(SmsHashedNumUploadWorker::class.java).build()
//                    WorkManager.getInstance().enqueue(oneTimeWorkRequest)
//                    val oneTimeWorkRequest = OneTimeWorkRequest.Builder(NewSMSSaveToLocalDbWorker::class.java).build()
//                    WorkManager.getInstance().enqueue(oneTimeWorkRequest)

//                }
//            }


//        repository.uploadNumbersToGetInfo(obj)
    }

    companion object{
        const val TAG = "__SmsContainerViewModel"
    }

}