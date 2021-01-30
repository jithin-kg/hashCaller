package com.nibble.hashcaller.view.ui.call.work

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServerDAO
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogLiveData
import com.nibble.hashcaller.view.ui.call.repository.CallContainerRepository
import com.nibble.hashcaller.view.ui.sms.util.SMS
import kotlinx.coroutines.launch

class CallContainerViewModel(
    val SMS: CallLogLiveData,
    val repository: CallContainerRepository?,
    val SMSSendersInfoFromServerDAO: CallersInfoFromServerDAO?
) :ViewModel(){
    init {

        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(CallNumUploadWorker::class.java).build()
        WorkManager.getInstance().enqueue(oneTimeWorkRequest)
    }
    fun getInformationForTheseNumbers(
        smslist: List<SMS>?,
        packageName: String
    ) = viewModelScope.launch {

        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(CallNumUploadWorker::class.java).build()
        WorkManager.getInstance().enqueue(oneTimeWorkRequest)

//
    }

    companion object{
        const val TAG = "__SmsContainerViewModel"
    }

}