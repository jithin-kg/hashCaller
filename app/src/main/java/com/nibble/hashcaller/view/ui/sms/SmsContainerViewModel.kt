package com.nibble.hashcaller.view.ui.sms

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServerDAO
import com.nibble.hashcaller.local.db.sms.mute.IMutedSendersDAO
import com.nibble.hashcaller.view.ui.sms.list.SMSLiveData
import com.nibble.hashcaller.view.ui.sms.util.SMS
import com.nibble.hashcaller.view.ui.sms.work.SmsHashedNumUploadWorker
import kotlinx.coroutines.launch

class SmsContainerViewModel(
    val SMS: SMSLiveData,
    val repository: SMScontainerRepository?,
    val SMSSendersInfoFromServerDAO: SMSSendersInfoFromServerDAO?

) :ViewModel(){
    var numRowsDeletedLiveData: MutableLiveData<Int> = MutableLiveData(0)
    fun getInformationForTheseNumbers(
        smslist: List<SMS>?,
        packageName: String
    ) = viewModelScope.launch {

        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(SmsHashedNumUploadWorker::class.java).build()
        WorkManager.getInstance().enqueue(oneTimeWorkRequest)

    }

    fun deleteThread(id: Long) = viewModelScope.launch {
       val numRowsDeleted =  repository!!.deleteSmsThread(id)
        numRowsDeletedLiveData.value = numRowsDeleted
    }

    fun muteMarkedSenders() = viewModelScope.launch {
        repository!!.muteSenders()
    }

    companion object{
        const val TAG = "__SmsContainerViewModel"
    }

}