package com.nibble.hashcaller.view.ui.call.work

import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.nibble.hashcaller.network.spam.ReportedUserDTo
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServerDAO
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogData
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogLiveData
import com.nibble.hashcaller.view.ui.call.repository.CallContainerRepository
import com.nibble.hashcaller.view.ui.call.utils.CallLogFlowHelper
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.addToMarkedViews
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.addTomarkedItemsById
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.getMarkedContactAddress
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.getMarkedItemSize
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.getMarkedViews
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.idContainsInList
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.isMarkedViewsEmpty
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.setMarkedContactAddress
import com.nibble.hashcaller.view.ui.contacts.utils.OPERATION_COMPLETED
import com.nibble.hashcaller.view.ui.contacts.utils.OPERATION_PENDING
import com.nibble.hashcaller.view.ui.sms.individual.util.*
import com.nibble.hashcaller.work.formatPhoneNumber
import com.nibble.hashcaller.work.replaceSpecialChars
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class CallContainerViewModel(
    val callLogs: CallLogLiveData,
    val repository: CallContainerRepository?,
    val SMSSendersInfoFromServerDAO: CallersInfoFromServerDAO?
) :ViewModel(){
    var contactAdders = ""
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

    fun updateLiveDataWithFlow(it: MutableList<CallLogData>) = viewModelScope.launch{
//        var lst: MutableList<CallLogData> = mutableListOf()
//        lst.addAll(lst)
//        lstOfAllCallLogs.add(it)
        callLogsMutableLiveData.value = it
    }

    fun markItem(id: Long, view: View, pos: Int, address: String): Flow<Int> = flow {

        if(idContainsInList(id)){ // if id already exist in list,remove from the list and unMark view
            IndividualMarkedItemHandlerCall.removeFromMarkedItemsById(id)
            if(getMarkedItemSize() == 1){
                setMarkedContactAddress(address)
            }
            emit(CALL_ITEM_UN_MARKED)

        }else{

            if(getMarkedItemSize() == 0){
                setMarkedContactAddress(address)
            }
            addTomarkedItemsById(id)
            addToMarkedViews(view)


            if(!isMarkedViewsEmpty()){
                for(view in getMarkedViews()){
//               markedViewsLiveData.value = view
                    emit(CALL_NEW_ITEM_MARKED)
                }
            }


        }

    }

    fun deleteThread():LiveData<Int> = liveData {
        emit(SMS_DELETE_ON_PROGRESS)
        val numRowsDeleted =  repository!!.deleteLogs().apply {
            emit(SMS_DELETE_ON_COMPLETED)
        }


//        numRowsDeletedLiveData.value = numRowsDeleted
    }

    fun muteMarkedCaller() :LiveData<Int> = liveData {
        emit(OPERATION_PENDING)
        var address = getMarkedContactAddress()!!
        address = formatPhoneNumber(address)
        viewModelScope.launch {
            contactAdders = async { repository!!.muteContactAddress(address) }.await()

        }.join()

        emit(OPERATION_COMPLETED)


    }

    fun fetchCallLogFlow(activity: FragmentActivity) = viewModelScope.launch{
      val res =   CallLogFlowHelper.fetchCallLogFlow(activity)

        updateLiveDataWithFlow(res)
    }

    /**
     * called from snackbar
     */
    fun unmute() = viewModelScope.launch {
        repository!!.unmuteByAddress(contactAdders)
    }
    fun unmuteByAddress() :LiveData<Int> = liveData {
            emit(OPERATION_PENDING)
            var address = getMarkedContactAddress()!!
            address = formatPhoneNumber(address)
            viewModelScope.launch {
                contactAdders = async { repository!!.unmuteByAddress(address) }.await()

            }.join()

            emit(OPERATION_COMPLETED)
    }

    fun checkWhetherMutedOrBlocked() = liveData<Int>{
        var address = formatPhoneNumber(getMarkedContactAddress()!!)

//        viewModelScope.launch {
//
//        }
        repository!!.isMmuted(address).apply {
            if(this){
                emit(IS_MUTED_ADDRESS)
            }else{
                emit(IS_NOT_MUTED_ADDRESS)
            }
        }

    }

    fun blockThisAddress(contactAddress: String,
                         threadID: Long, spammerType: Int,
                         spammerCategory: Int) = viewModelScope.launch {

        async {

            repository?.markCallerAsSpamer(formatPhoneNumber(contactAddress),
                spammerCategory, "", "" )
        }

        async {
//            repository?.report(
//                ReportedUserDTo(
//                    formatPhoneNumber(contactAddress), " ",
//                    spammerType.toString(), spammerCategory.toString()
//                )
//            )
        }


    }


    companion object {
        const val TAG = "__SmsContainerViewModel"
    }
}