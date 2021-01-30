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

        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(SmsHashedNumUploadWorker::class.java).build()
        WorkManager.getInstance().enqueue(oneTimeWorkRequest)

    }

    companion object{
        const val TAG = "__SmsContainerViewModel"
    }

}