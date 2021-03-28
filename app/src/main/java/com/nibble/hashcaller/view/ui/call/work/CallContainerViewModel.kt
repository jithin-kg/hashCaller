package com.nibble.hashcaller.view.ui.call.work

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServerDAO
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogData
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogLiveData
import com.nibble.hashcaller.view.ui.call.repository.CallContainerRepository
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.addToMarkedViews
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.addTomarkedItemsById
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.getMarkedViews
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.isMarkedViewsEmpty
import com.nibble.hashcaller.view.ui.contacts.utils.pageOb
import com.nibble.hashcaller.view.ui.sms.individual.util.IndividualMarkedItemHandler
import com.nibble.hashcaller.view.ui.sms.util.SMS
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.flow
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

    fun markItem(id: Long, view: View, pos: Int):kotlinx.coroutines.flow.Flow<View> = flow {

        addTomarkedItemsById(id)
        addToMarkedViews(view)

        if(!isMarkedViewsEmpty()){
            for(view in getMarkedViews()){
//               markedViewsLiveData.value = view
                emit(view)
            }
        }
    }

    fun deleteThread() {
        val numRowsDeleted =  repository!!.deleteLogs()
//        numRowsDeletedLiveData.value = numRowsDeleted
    }

    companion object{
        const val TAG = "__SmsContainerViewModel"
    }

}