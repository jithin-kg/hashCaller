package com.nibble.hashcaller.view.ui.sms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServerDAO
import com.nibble.hashcaller.view.ui.sms.list.SMSLiveData
import com.nibble.hashcaller.view.ui.sms.util.SMS
import com.nibble.hashcaller.view.ui.sms.work.SmsHashedNumUploadWorker
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

        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(SmsHashedNumUploadWorker::class.java).build()
        WorkManager.getInstance().enqueue(oneTimeWorkRequest)

    }

    fun deleteThread(id: Long) = viewModelScope.launch {
        repository!!.deleteSmsThread(id)
    }

    companion object{
        const val TAG = "__SmsContainerViewModel"
    }

}