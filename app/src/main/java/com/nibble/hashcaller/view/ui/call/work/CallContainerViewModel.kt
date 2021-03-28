package com.nibble.hashcaller.view.ui.call.work

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServerDAO
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogData
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogLiveData
import com.nibble.hashcaller.view.ui.call.repository.CallContainerRepository
import com.nibble.hashcaller.view.ui.sms.util.SMS
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class CallContainerViewModel(
    val callLogs: CallLogLiveData,
    val repository: CallContainerRepository?,
    val SMSSendersInfoFromServerDAO: CallersInfoFromServerDAO?
) :ViewModel(){
     var lstOfAllCallLogs: MutableList<CallLogData> = mutableListOf()
    var callLogsMutableLiveData:MutableLiveData<MutableList<CallLogData>> = MutableLiveData()

    init {

    }

    fun updateCAllLogLivedata(list:MutableList<CallLogData>) = viewModelScope.launch {
        callLogsMutableLiveData.value = list

        getInformationForTheseNumbers()

    }


    fun getInformationForTheseNumbers() = viewModelScope.launch {

        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(CallNumUploadWorker::class.java).build()
        WorkManager.getInstance().enqueue(oneTimeWorkRequest)

//
    }


    /**********from callhistory frgment************/
    private var phoneNumber: MutableLiveData<String>? = null


    fun getPhoneNumber(): MutableLiveData<String>? {
        if (phoneNumber == null) {
            phoneNumber = MutableLiveData<String>()
            return phoneNumber
        }
        return phoneNumber

    }

    fun setAdditionalInfo(logs: MutableList<CallLogData>?) = viewModelScope.launch{
        if (logs != null) {
            for(log in logs){
                if(log.name.isNullOrEmpty()){
                    //get detail from db
                  val  info =  async {  repository!!.getNameForAddress(log.number!!) }.await()
                  if(info!=null){
                      if(log.name.isNullOrEmpty()){
                         log.name = info.title
                      }
                      log.spamCount = info.spamReportCount
                  }
                }
            }
            callLogsMutableLiveData.value = logs

        }
    }

    fun updateLiveDataWithFlow(it: CallLogData) = viewModelScope.launch{
//        var lst: MutableList<CallLogData> = mutableListOf()
//        lst.addAll(lst)
        lstOfAllCallLogs.add(it)
        callLogsMutableLiveData.value = lstOfAllCallLogs
    }

    companion object{
        const val TAG = "__SmsContainerViewModel"
    }

}