package com.nibble.hashcaller.view.ui.call.work

import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.nibble.hashcaller.view.ui.call.CallFragment.Companion.fullDataFromCproviderFetched
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServer
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServerDAO
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogData
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogLiveData
import com.nibble.hashcaller.view.ui.call.repository.CallContainerRepository
import com.nibble.hashcaller.view.ui.call.repository.CallContainerRepository.Companion.addAllMarkedItemToDeletedIds
import com.nibble.hashcaller.view.ui.call.repository.CallContainerRepository.Companion.deletedIds
import com.nibble.hashcaller.view.ui.call.repository.CallContainerRepository.Companion.markedIds
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.getMarkedContactAddress
import com.nibble.hashcaller.view.ui.contacts.utils.OPERATION_COMPLETED
import com.nibble.hashcaller.view.ui.contacts.utils.OPERATION_PENDING
import com.nibble.hashcaller.view.ui.sms.individual.util.*
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.async
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
        fullDataFromCproviderFetched = true
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
//            callLogsMutableLiveData.value = logs

        }
    }

    fun updateLiveDataWithFlow(it: MutableList<CallLogData>) = viewModelScope.launch{
//        var lst: MutableList<CallLogData> = mutableListOf()
//        lst.addAll(lst)
//        lstOfAllCallLogs.add(it)
        callLogsMutableLiveData.value = it
    }

    fun isMarkingStarted(): Boolean {
        return markedIds.size > 0
    }
    fun markItem(id: Long, view: View, pos: Int, address: String) : LiveData<Int> = liveData {

        var listOne: MutableList<CallLogData>  = mutableListOf()
        var listTwo: MutableList<CallLogData>  = mutableListOf()
        listOne.addAll(callLogsMutableLiveData.value!!)


        for (item in listOne){

            var obj: CallLogData? = null
            if(item.id == id){
                if(item.isMarked){
                    obj = item.copy(isMarked = false)
                    markedIds.remove(id)
                    listTwo.add(obj)
                }else{
                    obj = item.copy(isMarked = true)
                    markedIds.add(id)
                    listTwo.add(obj)

                }
            }else{
                listTwo.add(item)
            }

        }
//        callLogsMutableLiveData.value!!.find {it.id == id }!!.isMarked = true
        callLogsMutableLiveData.value = listTwo
        emit(markedIds.size)

//        if(idContainsInList(id)){ // if id already exist in list,remove from the list and unMark view
//            IndividualMarkedItemHandlerCall.removeFromMarkedItemsById(id)
//            if(getMarkedItemSize() == 1){
//                setMarkedContactAddress(address)
//            }
////            emit(CALL_ITEM_UN_MARKED)
//
//        }else{
//
//            if(getMarkedItemSize() == 0){
//                setMarkedContactAddress(address)
//            }
//            addTomarkedItemsById(id)
//            addToMarkedViews(view)
//
//
//            if(!isMarkedViewsEmpty()){
//                for(view in getMarkedViews()){
////               markedViewsLiveData.value = view
////                    emit(CALL_NEW_ITEM_MARKED)
//                }
//            }
//
//
//        }

    }

    /**
     * Deleting process seemed to be slow so I need to delete from the livedata view to show the user quickly
     * and delete in background
     */
    fun deleteThread():LiveData<Int> = liveData {
        emit(SMS_DELETE_ON_PROGRESS)
        viewModelScope.launch {

            //deleting from livedata
           val as1=  async {
                deleteItemsFromView()

            }

            //deleting from repository
           val as2 =  async {
                val numRowsDeleted =  repository!!.deleteLogs()
            }

            as1.await().apply {
                emit(SMS_DELETE_ON_COMPLETED)
            }

            as2.await()


        }



//        numRowsDeletedLiveData.value = numRowsDeleted
    }

    private fun deleteItemsFromView() {
        addAllMarkedItemToDeletedIds(markedIds)
        var listOne: MutableList<CallLogData>  = mutableListOf()
        var listTwo: MutableList<CallLogData>  = mutableListOf()
        listOne.addAll(callLogsMutableLiveData.value!!)


        for (item in listOne){

            if(!deletedIds.contains(item.id)){
               listTwo.add(item)
            }

        }
//        callLogsMutableLiveData.value!!.find {it.id == id }!!.isMarked = true
        callLogsMutableLiveData.value = listTwo
//        emit(markedIds.size)
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

    /**
     * called for the first time to get 10 results, then updated with and followed by full livedata
     */

    fun fetchCallLogFlow(activity: FragmentActivity) = viewModelScope.launch{
      val res =   repository!!.fetchFirst10()

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

    fun getNextPage() = viewModelScope.launch {
        val res = async {    repository!!.getSMSByPage() }.await()

        var list : MutableList<CallLogData> = mutableListOf()
        if(callLogsMutableLiveData.value!= null){

            list.addAll(callLogsMutableLiveData.value!!)
            list.addAll(res)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                list!!.removeIf { it -> it.id == null }
            }else {
                removeDummyItems(list)
            }
            callLogsMutableLiveData.value = list

        }
    }

    private fun removeDummyItems(list: MutableList<CallLogData>) {
        //using iterator i can delet item while iterating throgh the list
        val iterator = list.iterator()
        while (iterator.hasNext()){
            if(iterator.next().id == null){
                iterator.remove()
            }
        }
//        list.addAll(list)
//        for (item in newlist){
//            if(item.id == null){
//
//            }
//        }
    }

    fun getCallLogFromServer() : LiveData<List<CallersInfoFromServer>>  {
        return  repository!!.getCallLogLiveDAtaFromDB()
    }

    /**
     * called when info about a caller comes from server, or db changes
     */
    fun updateWithNewInfoFromServer() = viewModelScope.launch {
         repository!!.getFullCallLogs().apply {
             callLogsMutableLiveData.value = this
         }
    }

    fun clearCallLogDB() = viewModelScope.launch {
        repository!!.clearCallersInfoFromServer()
    }

    fun clearMarkedItems() = viewModelScope.launch{
        CallContainerRepository.clearMarkedItems()

        var listOne: MutableList<CallLogData>  = mutableListOf()
        var listTwo: MutableList<CallLogData>  = mutableListOf()
        listOne.addAll(callLogsMutableLiveData.value!!)


        for (item in listOne){

            var obj: CallLogData? = null
            obj = item.copy(isMarked = false)
            if(item.isMarked){
                listTwo.add(obj)
            }else{
                listTwo.add(item)
            }

        }
//        callLogsMutableLiveData.value!!.find {it.id == id }!!.isMarked = true
        callLogsMutableLiveData.value = listTwo
    }


    companion object {
        const val TAG = "__SmsContainerViewModel"
    }
}