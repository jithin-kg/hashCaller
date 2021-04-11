package com.nibble.hashcaller.view.ui.call.work

import android.util.Log
import android.view.View
import androidx.lifecycle.*
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.nibble.hashcaller.view.ui.call.db.CallLogAndInfoFromServer
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
     var lstOfAllCallLogs: MutableList<CallLogAndInfoFromServer> = mutableListOf()
    var callLogsMutableLiveData:MutableLiveData<MutableList<CallLogAndInfoFromServer>> = MutableLiveData()

//    var callLogTableData: LiveData<List<CallLogTable>>? = repository!!.getAllCallLogLivedata()
    var callLogTableData: LiveData<List<CallLogAndInfoFromServer>>? = repository!!.getAllCallLogLivedata()

    var mutableCalllogTableData : MutableLiveData<MutableList<CallLogAndInfoFromServer>?> = MutableLiveData()


    var markedItems: MutableLiveData<MutableSet<Long>> = MutableLiveData(mutableSetOf())


    init {
    }

    fun clearMarkeditems(){
        markedItems.value?.clear()
    }
    fun addTomarkeditems(id:Long){
        markedItems.value!!.add(id)
        markedItems.value = markedItems.value
    }
    fun removeMarkeditemById(id:Long){
        markedItems.value!!.remove(id)
        markedItems.value = markedItems.value
    }
    fun updateMutableData(list: List<CallLogAndInfoFromServer>) {
        val mutableList: MutableList<CallLogAndInfoFromServer> = mutableListOf()
        mutableList.addAll(list)

        mutableCalllogTableData.value = mutableList
//        return mutableCalllogTableData
    }


    /**
     * called when there is a change in call log in content provider
     */
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
//        callLogsMutableLiveData.value = it
    }

    fun isMarkingStarted(): Boolean {
        return markedIds.size > 0
    }
    fun markItem(id: Long, view: View, pos: Int, address: String) : LiveData<Int> = liveData {
        markedItems.value!!.add(id)

//        var mutableList : MutableList<CallLogTable> = mutableListOf()
//        mutableCalllogTableData.value?.let { mutableList.addAll(it) }
//        var listTwo : MutableList<CallLogTable> = mutableListOf()
////        for (item in mutableList){
////            var obj : CallLogTable ?
////            if(item.id == id){
////                 obj = item.copy(isMarked = true)
////            }else{
////                obj = item.copy()
////            }
////
////            listTwo.add(obj)
////        }
//
//        mutableCalllogTableData.value = listTwo


//
//
//        var listOne: MutableList<CallLogData>  = mutableListOf()
//        var listTwo: MutableList<CallLogData>  = mutableListOf()
//        listOne.addAll(callLogsMutableLiveData.value!!)
//
//
//        for (item in listOne){
//
//            var obj: CallLogData? = null
//            if(item.id == id){
//                if(item.isMarked){
//                    obj = item.copy(isMarked = false)
//                    markedIds.remove(id)
//                    listTwo.add(obj)
//                }else{
//                    obj = item.copy(isMarked = true)
//                    markedIds.add(id)
//                    listTwo.add(obj)
//
//                }
//            }else{
//                listTwo.add(item)
//            }
//
//        }
////        callLogsMutableLiveData.value!!.find {it.id == id }!!.isMarked = true
//        callLogsMutableLiveData.value = listTwo
//        if(markedIds.size == 1){
//            contactAdders = address
//        }
//        emit(markedIds.size)
    }

    /**
     * Deleting process seemed to be slow so I need to delete from the livedata view to show the user quickly
     * and delete in background
     */
    fun deleteThread():LiveData<Int> = liveData {
        emit(SMS_DELETE_ON_PROGRESS)

//        viewModelScope.launch {
//            val as1 = async {
                for(item in markedItems.value!!){
                    repository?.deleteLog(item)
//                    async { repository?.deleteCallLogsFromDBByid(item) }.await()
                    kotlinx.coroutines.delay(500L)
                    Log.d(TAG, "deleteThread: iterating $item")
                }
                 clearMarkeditems()


//            }
//            val as2 = async {
////                repository?.deleteCallLogsFromDBByid(markedItems.value)
//            }
//            as1.await()
//            as2.await()

//        }.join()
        emit(SMS_DELETE_ON_COMPLETED)

    }

    private suspend fun deleteItemsFromView() {
        addAllMarkedItemToDeletedIds(markedIds)
        var listOne: MutableList<CallLogData>  = mutableListOf()
        var listTwo: MutableList<CallLogData>  = mutableListOf()
//        listOne.addAll(callLogsMutableLiveData.value!!)


        for (item in listOne){

            if(!deletedIds.contains(item.id)){
               listTwo.add(item)
            }

        }
//        callLogsMutableLiveData.value!!.find {it.id == id }!!.isMarked = true
//        callLogsMutableLiveData.value = listTwo
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

    fun blockThisAddress(spammerCategory: Int, spammerCategory1: Int) = viewModelScope.launch {
//        threadID
        async {

            repository?.markCallerAsSpamer(formatPhoneNumber(contactAdders),
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

//            list.addAll(callLogsMutableLiveData.value!!)
            list.addAll(res)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                list!!.removeIf { it -> it.id == null }
            }else {
              //  removeDummyItems(list)
            }
//            callLogsMutableLiveData.value = list

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
    fun updateWithNewInfoFromServer(list: List<CallersInfoFromServer>) = viewModelScope.launch {

        repository?.updateWithCallLogWithServerInfo(list)
    }

    fun clearCallLogDB() = viewModelScope.launch {
        repository!!.clearCallersInfoFromServer()
    }

    fun clearMarkedItems() = viewModelScope.launch{
        CallContainerRepository.clearMarkedItems()

        var listOne: MutableList<CallLogData>  = mutableListOf()
        var listTwo: MutableList<CallLogData>  = mutableListOf()
//        listOne.addAll(callLogsMutableLiveData.value!!)


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
//        callLogsMutableLiveData.value = listTwo
    }

    fun updateDatabase(logs: MutableList<CallLogAndInfoFromServer>) = viewModelScope.launch {
        val as1 = async { repository?.updateCallLogDb(logs) }
        val as2 = async { repository?.deleteCallLogs(logs) }
        val as3 = async { getInformationForTheseNumbers() }
        as1.await()
        as2.await()
        as3.await()

    }


    companion object {
        const val TAG = "__SmsContainerViewModel"
    }
}