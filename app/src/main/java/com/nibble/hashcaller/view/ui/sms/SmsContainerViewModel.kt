package com.nibble.hashcaller.view.ui.sms

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServerDAO
import com.nibble.hashcaller.network.spam.ReportedUserDTo
import com.nibble.hashcaller.view.ui.contacts.utils.pageOb
import com.nibble.hashcaller.view.ui.sms.list.SMSLiveData2
import com.nibble.hashcaller.view.ui.sms.util.SMS
import com.nibble.hashcaller.view.ui.sms.work.SmsHashedNumUploadWorker
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class SmsContainerViewModel(
    val SMS: SMSLiveData2,
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

    fun deleteThread() = viewModelScope.launch {
       val numRowsDeleted =  repository!!.deleteSmsThread()
        pageOb.page = 0
        numRowsDeletedLiveData.value = numRowsDeleted
    }

    fun muteMarkedSenders() = viewModelScope.launch {
        repository!!.muteSenders()
    }

    fun blockThisAddress(contactAddress: String, threadID: Long, spammerType: Int, spammerCategory: Int) = viewModelScope.launch {

        async {

            repository?.save(contactAddress, 1, "", "" )
        }

        async {
            repository?.report(
                ReportedUserDTo(contactAddress, " ",
                spammerType.toString(), spammerCategory.toString()
            )
            )
        }

    }

    fun deleteAllSmsindb() = viewModelScope.launch{
        SMSSendersInfoFromServerDAO!!.deleteAll()
    }

    companion object{
        const val TAG = "__SmsContainerViewModel"
    }

}