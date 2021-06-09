package com.nibble.hashcaller.view.ui.call.work

import android.content.Context
import android.util.Log
import android.view.View
import androidx.lifecycle.*
import androidx.work.*
import com.nibble.hashcaller.local.db.blocklist.BlockedListPattern
import com.nibble.hashcaller.repository.BlockListPatternRepository
import com.nibble.hashcaller.view.ui.blockConfig.GeneralBlockRepository
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
import com.nibble.hashcaller.view.ui.call.spam.MarkeditemsHelper
import com.nibble.hashcaller.view.ui.contacts.startSpamReportWorker
import com.nibble.hashcaller.view.ui.contacts.utils.OPERATION_COMPLETED
import com.nibble.hashcaller.view.ui.contacts.utils.OPERATION_PENDING
import com.nibble.hashcaller.view.ui.sms.db.NameAndThumbnail
import com.nibble.hashcaller.view.ui.sms.individual.util.*
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.lang.Exception

class CallContainerViewModel(
    val callLogs: CallLogLiveData,
    val repository: CallContainerRepository?,
    val SMSSendersInfoFromServerDAO: CallersInfoFromServerDAO?,
    private val blockListPatternRepository: BlockListPatternRepository,
    private val generalBlockRepository: GeneralBlockRepository
) :ViewModel(){
    var contactAddress = ""
     var lstOfAllCallLogs: MutableList<CallLogAndInfoFromServer> = mutableListOf()
    var callLogsMutableLiveData:MutableLiveData<MutableList<CallLogAndInfoFromServer>> = MutableLiveData()
//    var callLogTableData: LiveData<List<CallLogTable>>? = repository!!.getAllCallLogLivedata()
    var callLogTableData: LiveData<MutableList<CallLogTable>>? = repository!!.getAllCallLogLivedata()
    var callersInfoFromDBLivedta:LiveData<List<CallersInfoFromServer>>  = repository!!.getCallLogLiveDAtaFromDB()
    var mutableCalllogTableData : MutableLiveData<MutableList<CallLogTable>?> = MutableLiveData()
    var expandedLayoutId: Long? = null
//    var expandedLayoutPositin:Int? = null

    var markeditemsHelper = MarkeditemsHelper()


    init {
    }

    fun clearMarkeditems(){
            markeditemsHelper.clearMarkeditems()
//        markeditemsHelper.markedItems.value?.clear()
//        markeditemsHelper.markedAddres.clear()

    }
    fun addTomarkeditems(id: Long, position: Int, number: String){
            markeditemsHelper.addTomarkeditems(id, position, number)

    }
    fun removeMarkeditemById(id: Long, position: Int, number: String){
        markeditemsHelper.removeMarkeditemById(id, position, number)
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
    fun getInformationForTheseNumbers(applicationContext: Context?) = viewModelScope.launch {
        applicationContext?.let{
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

            val oneTimeWorkRequest = OneTimeWorkRequest.Builder(CallNumUploadWorker::class.java)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(it).enqueue(oneTimeWorkRequest)
        }
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
                         log.name = info.firstName
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
        markeditemsHelper.markedItems.value!!.add(id)

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
            for(item in markeditemsHelper.markedAddres){
                //first mark as deleted for quickly showing that user that operation done quickly
                //because deleting the item at first make a chance of showing it when deletion happens in
                //content provider in the following loop, so first mark as delete is a better way to go.
                async { repository?.deleteCallLogsFromDBByid(item) }.await()
            }
            emit(ON_COMPLETED)
            for (item in markeditemsHelper.markedAddres) {
                repository?.deleteLog(item)
//                async { repository?.deleteCallLogsFromDBByid(item) }
//                    kotlinx.coroutines.delay(500L)
                Log.d(TAG, "deleteThread: iterating $item")
                //delete the item from call log table as well , because just after this deletion is performed
                //if an incomming call comes, it will not show in call log, since it is marked as deleted previously
                //so delete items which are marked as deleted
                repository?.deleteCallLogFromDb(item)
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
//        var address = getMarkedContactAddress()!!
//        address = formatPhoneNumber(address)
//        viewModelScope.launch {
//            contactAddress = async { repository!!.muteContactAddress(address) }.await()
//
//        }.join()

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
//            var address = getMarkedContactAddress()!!
//            address = formatPhoneNumber(address)
//            viewModelScope.launch {
//                contactAddress = async { repository!!.unmuteByAddress(address) }.await()
//
//            }.join()

            emit(OPERATION_COMPLETED)
    }

    fun checkWhetherMutedOrBlocked() = liveData<Int>{
//        var address = formatPhoneNumber(getMarkedContactAddress()!!)

//        viewModelScope.launch {
//
//        }
//        repository!!.isMmuted(address).apply {
//            if(this){
//                emit(IS_MUTED_ADDRESS)
//            }else{
//                emit(IS_NOT_MUTED_ADDRESS)
//            }
//        }

    }

    fun blockThisAddress(spammerType: Int, applicationContext: Context?) : LiveData<Int> = liveData {

        contactAddress = markeditemsHelper.getmarkedAddresAt(0) ?: ""
        if (contactAddress.isNotEmpty()) {
            viewModelScope.launch {
                supervisorScope {
                    val as1 = async { repository?.marAsReportedByUser(contactAddress) }

                    val as2 = async {
                        blockListPatternRepository.insertPattern(
                           contactAddress,
                            EXACT_NUMBER )
                    }
                    val as4 = async { repository?.markAsSpamInSMS(contactAddress) }
                    val as3 = async { applicationContext?.startSpamReportWorker(contactAddress, spammerType) }


                    try {
                        as1.await()
                    } catch (e: Exception) {
                        Log.d(TAG, "blockThisAddress: $e")
                    }
                    try {
                        as2.await()
                    } catch (e: Exception) {
                        Log.d(TAG, "blockThisAddress: $e")
                    }
                    try {
                        as3.await()
                    } catch (e: Exception) {
                        Log.d(TAG, "blockThisAddress: $e")
                    }
                    try{
                        as4.await()
                    }catch (e:Exception){
                        Log.d(TAG, "blockThisAddress: $e")
                    }

                    generalBlockRepository.marAsReportedByUserInCall(contactAddress)
                    generalBlockRepository.marAsReportedByUserInSMS(contactAddress)
                }


//            }.join()
            }.join()

            emit(ON_COMPLETED)

        }
    }

    fun getNextPage() = viewModelScope.launch {
//        val res = async {    repository!!.getSMSByPage() }.await()

        var list : MutableList<CallLogData> = mutableListOf()
        if(callLogsMutableLiveData.value!= null){

//            list.addAll(callLogsMutableLiveData.value!!)
//            list.addAll(res)
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

//    fun getCallLogFromServer() : LiveData<List<CallersInfoFromServer>>  {
//
//        return  repository!!.getCallLogLiveDAtaFromDB()
//    }

    /**
     * called when info about a caller comes from server, or db changes
     */
    fun updateWithNewInfoFromServer(list: List<CallersInfoFromServer>) = viewModelScope.launch {
        for(item in list){
             val res =  repository?.findFromCallLogTable(item.contactAddress)
              if(res!=null){
                  if(res.nameFromServer!= item.firstName ){
                      repository?.updateCallLogWithServerInfo(item)
                  }else if( res.spamCount < item.spamReportCount ){
                      repository?.updateCallLogWithSpamerDetails(item)
                  }else if(res.imageFromDb!= item.thumbnailImg){
                      repository?.updateCallLogWithImgFromServer(item)
                  }

              }
          }
    }

    fun clearCallLogDB() = viewModelScope.launch {
        repository!!.clearCallersInfoFromServer()
        blockListPatternRepository.clearAll()



    }

    fun clearMarkedItems() = viewModelScope.launch{
        markeditemsHelper.clearMarkeditems()
        markeditemsHelper.clearMarkedItemPositions()

//        markedItems.value?.clear()
//        markedItems.value = markedItems.value
//        var list = mutableListOf<CallLogAndInfoFromServer>()
//        var list2 = mutableListOf<CallLogAndInfoFromServer>()
//        mutableCalllogTableData.value?.let { list.addAll(it) }
//
//        repository?.getAllCallLog().apply {
//            mutableCalllogTableData.value = this
//        }



    }

    fun updateDatabase(logs: MutableList<CallLogTable>, applicationContext: Context?) = viewModelScope.launch {
        val as1 = async {
           repository?.insertIntoCallLogDb(logs)
        }
        val as2 = async { updateCallLogIds(logs) }

        val as3 = async { getInformationForTheseNumbers(applicationContext) }
        val as5 = async { updateNameAndSpamCount(logs) }
        val as4 = async { repository?.deleteCallLogs(logs) }

        val as6 = async { generalBlockRepository.updateCallLogsWithblockListpatterns(logs) }
        as2.await()
        as1.await()
        as3.await()
        as4.await()
        as5.await()
        as6.await()

    }

    private suspend fun updateCallLogIds(logs: MutableList<CallLogTable>) {
        for (item in logs){
            val res = repository?.findOneFromCallLogTable(item.numberFormated)
            if (res!=null){
                if(res.id!= item.id){
                    repository?.updateIdWithContentProviderInfo(item)
                }
            }
        }
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
                    if(callLogTableInfo.nameFromServer != serverInfo.firstName || callLogTableInfo.spamCount < serverInfo.spamReportCount){
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
        markeditemsHelper.clearMarkedItemPositions()
//        markedItemsPositions.clear()
    }

    fun getmarkedItemSize(): Int {

        var size = markeditemsHelper.markedItems.value?.size
        return size ?: 0
    }

    fun getFirst10Logs() :LiveData<MutableList<CallLogTable>> = liveData {
        repository?.getFirst10Logs()?.let {
            it.add(CallLogTable(id = null))
            emit(it)

        }
    }

    fun isThisViewExpanded(id: Long): Boolean {
        return id == markeditemsHelper.getExpandedLayoutId()
    }

    fun setExpandedLayout(id: Long?, position: Int?) {
        markeditemsHelper.setExpandedLayout(id, position)
//        expandedLayoutId = id
//        expandedLayoutPositin = position
    }

    fun getPreviousExpandedLayout(): Long? {
        return markeditemsHelper.getExpandedLayoutId()
    }

    fun getPrevExpandedPosition(): Int? {
        return markeditemsHelper.getExpanedLayoutPosition()
    }

    fun getmarkeditemPositions(): Iterable<Int> {
        return markeditemsHelper.markedItemsPositions
    }

    fun startHashWorker(applicationContext: Context?) = viewModelScope.launch {

//        repository?.startHashWork(applicationContext)
    }


    companion object {
        const val TAG = "__SmsContainerViewModel"
    }
}