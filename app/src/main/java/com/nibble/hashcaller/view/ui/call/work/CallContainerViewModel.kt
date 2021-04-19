package com.nibble.hashcaller.view.ui.call.work

import android.util.Log
import android.view.View
import androidx.lifecycle.*
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.nibble.hashcaller.local.db.blocklist.BlockedListPattern
import com.nibble.hashcaller.repository.BlockListPatternRepository
import com.nibble.hashcaller.view.ui.call.db.CallLogAndInfoFromServer
import com.nibble.hashcaller.view.ui.call.db.CallLogTable
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
import com.nibble.hashcaller.view.ui.sms.db.NameAndThumbnail
import com.nibble.hashcaller.view.ui.sms.individual.util.*
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class CallContainerViewModel(
    val callLogs: CallLogLiveData,
    val repository: CallContainerRepository?,
    val SMSSendersInfoFromServerDAO: CallersInfoFromServerDAO?,
    private val blockListPatternRepository: BlockListPatternRepository
) :ViewModel(){
    var contactAddress = ""
     var lstOfAllCallLogs: MutableList<CallLogAndInfoFromServer> = mutableListOf()
    var callLogsMutableLiveData:MutableLiveData<MutableList<CallLogAndInfoFromServer>> = MutableLiveData()
//    var callLogTableData: LiveData<List<CallLogTable>>? = repository!!.getAllCallLogLivedata()
    var callLogTableData: LiveData<MutableList<CallLogTable>>? = repository!!.getAllCallLogLivedata()

    var mutableCalllogTableData : MutableLiveData<MutableList<CallLogTable>?> = MutableLiveData()


    var markedItems: MutableLiveData<MutableSet<Long>> = MutableLiveData(mutableSetOf())
    var markedItemsPositions: HashSet<Int> = hashSetOf()


    init {
    }

    fun clearMarkeditems(){
        markedItems.value?.clear()
    }
    fun addTomarkeditems(id: Long, position: Int, number: String){
        markedItems.value!!.add(id)
        markedItemsPositions.add(position)
        markedItems.value = markedItems.value
        contactAddress = number
    }
    fun removeMarkeditemById(id: Long, position: Int){
        markedItems.value!!.remove(id)
        markedItemsPositions.remove(position)
        markedItems.value = markedItems.value
    }
    fun updateMutableData(list: List<CallLogTable>) {
        val mutableList: MutableList<CallLogTable> = mutableListOf()
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
                  val  info =  async {  repository!!.getCallerInfoForAddressFromDB(log.number!!) }.await()
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
        emit(ON_PROGRESS)

        viewModelScope.launch {
//            val as1 = async {
            for(item in markedItems.value!!){
                async { repository?.deleteCallLogsFromDBByid(item) }.await()
            }
            emit(ON_COMPLETED)
            for (item in markedItems.value!!) {
                repository?.deleteLog(item)
//                async { repository?.deleteCallLogsFromDBByid(item) }
//                    kotlinx.coroutines.delay(500L)
                Log.d(TAG, "deleteThread: iterating $item")
            }
            clearMarkeditems()

        }.join()

//            }
//            val as2 = async {
////                repository?.deleteCallLogsFromDBByid(markedItems.value)
//            }
//            as1.await()
//            as2.await()

//        }.join()
//        emit(SMS_DELETE_ON_COMPLETED)

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
            contactAddress = async { repository!!.muteContactAddress(address) }.await()

        }.join()

        emit(OPERATION_COMPLETED)


    }


    /**
     * called from snackbar
     */
    fun unmute() = viewModelScope.launch {
        repository!!.unmuteByAddress(contactAddress)
    }
    fun unmuteByAddress() :LiveData<Int> = liveData {
            emit(OPERATION_PENDING)
            var address = getMarkedContactAddress()!!
            address = formatPhoneNumber(address)
            viewModelScope.launch {
                contactAddress = async { repository!!.unmuteByAddress(address) }.await()

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

    fun blockThisAddress(spammerCategory: Int, spammerCategory1: Int) : LiveData<Int> = liveData {
//        threadID
        viewModelScope.launch {
            async {

                repository?.marAsReportedByUser(contactAddress)

                blockListPatternRepository.insert(
                    BlockedListPattern(
                        null,
                        formatPhoneNumber(contactAddress),
                        "",
                        EXACT_NUMBER
                    )
                )
            }
            async {
//            repository?.report(
//                ReportedUserDTo(
//                    formatPhoneNumber(contactAddress), " ",
//                    spammerType.toString(), spammerCategory.toString()
//                )
//            )
            }

        }.join()
        emit(ON_COMPLETED)

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
          for(item in list){
             val res =  async { repository?.findFromCallLogTable(item.contactAddress)  }.await()
              if(res!=null){
                  if(res.nameFromServer!= item.title || res.spamCount < item.spamReportCount){
                      repository?.updateCallLogWithServerInfo(item)
                  }
              }
          }

    }

    fun clearCallLogDB() = viewModelScope.launch {
        repository!!.clearCallersInfoFromServer()

    }

    fun clearMarkedItems() = viewModelScope.launch{
        markedItems.value?.clear()
        markedItems.value = markedItems.value
//        var list = mutableListOf<CallLogAndInfoFromServer>()
//        var list2 = mutableListOf<CallLogAndInfoFromServer>()
//        mutableCalllogTableData.value?.let { list.addAll(it) }
//
//        repository?.getAllCallLog().apply {
//            mutableCalllogTableData.value = this
//        }



    }

    fun updateDatabase(logs: MutableList<CallLogTable>) = viewModelScope.launch {
        val as1 = async {
           repository?.insertIntoCallLogDb(logs)
        }
        val as2 = async { repository?.deleteCallLogs(logs) }
        val as3 = async { getInformationForTheseNumbers() }
        val as5 = async { updateNameAndSpamCount(logs) }
        as2.await()
        as1.await()
        as3.await()

    }

    private suspend fun updateNameAndSpamCount(logs: MutableList<CallLogTable>) {
        var numbersSet: HashSet<String> = hashSetOf()
        var numberCallLogTableHashMap: HashMap<String, CallLogTable> = hashMapOf()
        numbersSet.addAll(logs.map { it.number })

        for (num in numbersSet){
            val nameAndThumbnailFromCp: NameAndThumbnail? =  repository?.getNameForAddressFromContentProvider(num)
            if(nameAndThumbnailFromCp!=null){
            val callLogTableInfo=   repository?.findOneFromCallLogTable(num)
             val serverInfo:CallersInfoFromServer? =  repository?.getCallerInfoForAddressFromDB(num)

            if(callLogTableInfo!=null  ){
                if(serverInfo!=null){
                    if(callLogTableInfo.nameFromServer != serverInfo.title || callLogTableInfo.spamCount < serverInfo.spamReportCount){
                        repository?.updateCallLogWithServerInfo(serverInfo)
                    }
                }
                if(nameAndThumbnailFromCp!=null){
                    if(callLogTableInfo.name!= nameAndThumbnailFromCp.name || callLogTableInfo.thumbnailFromCp!= nameAndThumbnailFromCp.thumbnailUri){
                        repository?.updateWithCproviderInfo(callLogTableInfo.number, nameAndThumbnailFromCp)
                    }
                }
            }


//                numberNamehashMap.put(num, nameAndThumbnailFromCp.name)
            }
        }
//        for(item in logs){
//            if(numberNamehashMap.containsKey(item.number)){
//                item.name = numberNamehashMap[item.number]
//                item.callerInfoFoundFrom = SENDER_INFO_FROM_CONTENT_PROVIDER
//            }
//        }
    }



    fun clearMarkedItemPositions() = viewModelScope.launch{
        markedItemsPositions.clear()
    }

    fun getmarkedItemSize(): Int {

        var size = markedItems.value?.size
        return size ?: 0
    }

    fun getFirst10Logs() :LiveData<MutableList<CallLogTable>> = liveData {
        repository?.getFirst10Logs()?.let {
            it.add(CallLogTable(null))
            emit(it)

        }
    }




    companion object {
        const val TAG = "__SmsContainerViewModel"
    }
}