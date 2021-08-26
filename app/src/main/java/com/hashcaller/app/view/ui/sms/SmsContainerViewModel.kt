package com.hashcaller.app.view.ui.sms
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashcaller.app.network.spam.ReportedUserDTo
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServerDAO
import com.hashcaller.app.view.ui.sms.list.SMSLiveData2
import com.hashcaller.app.view.ui.sms.util.SMS
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class SmsContainerViewModel(
    val SMS: SMSLiveData2,
    val repository: SMScontainerRepository?,
    val SMSSendersInfoFromServerDAO: CallersInfoFromServerDAO?

) :ViewModel(){

    var numRowsDeletedLiveData: MutableLiveData<Int> = MutableLiveData(0)
    fun getInformationForTheseNumbers(
        smslist: List<SMS>?,
        packageName: String
    ) = viewModelScope.launch {
//
//        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(SmsHashedNumUploadWorker::class.java).build()
//        WorkManager.getInstance().enqueue(oneTimeWorkRequest)

    }

    fun deleteThread() = viewModelScope.launch {
       val numRowsDeleted =  repository!!.deleteSmsThread()
        numRowsDeletedLiveData.value = numRowsDeleted
    }

    fun muteMarkedSenders() = viewModelScope.launch {
        repository!!.muteSenders()
    }

    fun blockThisAddress(contactAddress: String, threadID: Long, spammerType: Int) = viewModelScope.launch {

        async {
            repository?.save(contactAddress, 1, "", "" )
        }
    }

    fun deleteAllSmsindb() = viewModelScope.launch{
        SMSSendersInfoFromServerDAO!!.deleteAll()
    }

    companion object{
        const val TAG = "__SmsContainerViewModel"
    }

}