package com.hashcaller.app.view.ui.call.work

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import androidx.work.*
import com.hashcaller.app.local.db.blocklist.BlockTypes.Companion.BLOCK_TYPE_FROM_CALL_LOG
import com.hashcaller.app.network.search.model.CntctitemForView
import com.hashcaller.app.repository.BlockListPatternRepository
import com.hashcaller.app.view.ui.blockConfig.GeneralBlockRepository
import com.hashcaller.app.view.ui.call.db.CallLogAndInfoFromServer
import com.hashcaller.app.view.ui.call.db.CallLogTable
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServer
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServerDAO
import com.hashcaller.app.view.ui.call.dialer.util.CallLogData
import com.hashcaller.app.view.ui.call.dialer.util.CallLogLiveData
import com.hashcaller.app.view.ui.call.repository.CallContainerRepository
import com.hashcaller.app.view.ui.call.repository.CallContainerRepository.Companion.addAllMarkedItemToDeletedIds
import com.hashcaller.app.view.ui.call.repository.CallContainerRepository.Companion.deletedIds
import com.hashcaller.app.view.ui.call.repository.CallContainerRepository.Companion.markedIds
import com.hashcaller.app.view.ui.contacts.startSpamReportWorker
import com.hashcaller.app.view.ui.contacts.utils.OPERATION_COMPLETED
import com.hashcaller.app.view.ui.contacts.utils.OPERATION_PENDING
import com.hashcaller.app.view.ui.sms.db.NameAndThumbnail
import com.hashcaller.app.view.ui.sms.individual.util.*
import com.hashcaller.app.work.SpamThresholdUpdateWorker
import kotlinx.coroutines.*
import java.lang.Exception

class CallContainerViewModel(
    val callLogs: CallLogLiveData,
    val repository: CallContainerRepository?,
    val SMSSendersInfoFromServerDAO: CallersInfoFromServerDAO?,
    private val blockListPatternRepository: BlockListPatternRepository,
    private val generalBlockRepository: GeneralBlockRepository
) :ViewModel(){

    private var showDfltCallerIdLayout = false
    var contactAddress = ""
    var lstOfAllCallLogs: MutableList<CallLogAndInfoFromServer> = mutableListOf()
    var callLogsMutableLiveData:MutableLiveData<MutableList<CallLogAndInfoFromServer>> = MutableLiveData()
//    var callLogTableData: LiveData<List<CallLogTable>>? = repository!!.getAllCallLogLivedata()


    var callLogTableData: LiveData<MutableList<CallLogTable>>? = repository!!.getAllCallLogLivedata()
    var callersInfoFromDBLivedta:LiveData<List<CallersInfoFromServer>>  = repository!!.getCallLogLiveDAtaFromDB()
    var mutableCalllogTableData : MutableLiveData<MutableList<CallLogTable>?> = MutableLiveData()
    var expandedLayoutId: Long? = null
//    var expandedLayoutPositin:Int? = null

//    var markeditemsHelper = MarkeditemsHelper()

    fun setShowDfltCallerIdLayout(value:Boolean){
        showDfltCallerIdLayout = value
    }
    fun getShowDfltCallerIdLayout(): Boolean {
        return showDfltCallerIdLayout
    }
    init {

    }


//    fun clearMarkeditems(){
////            markeditemsHelper.clearMarkeditems()
////        markeditemsHelper.markedItems.value?.clear()
////        markeditemsHelper.markedAddres.clear()
//
//    }
//    fun addTomarkeditems(id: Long, position: Int, number: String){
//            markeditemsHelper.addTomarkeditems(id, position, number)
//
//    }
//    fun removeMarkeditemById(id: Long, position: Int, number: String){
//        markeditemsHelper.removeMarkeditemById(id, position, number)
//    }
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
//    fun markItem(id: Long, view: View, pos: Int, address: String) : LiveData<Int> = liveData {
//        markeditemsHelper.markedItems.value!!.add(id)
//
////        var mutableList : MutableList<CallLogTable> = mutableListOf()
////        mutableCalllogTableData.value?.let { mutableList.addAll(it) }
////        var listTwo : MutableList<CallLogTable> = mutableListOf()
//////        for (item in mutableList){
//////            var obj : CallLogTable ?
//////            if(item.id == id){
//////                 obj = item.copy(isMarked = true)
//////            }else{
//////                obj = item.copy()
//////            }
//////
//////            listTwo.add(obj)
//////        }
////
////        mutableCalllogTableData.value = listTwo
//
//
////
////
////        var listOne: MutableList<CallLogData>  = mutableListOf()
////        var listTwo: MutableList<CallLogData>  = mutableListOf()
////        listOne.addAll(callLogsMutableLiveData.value!!)
////
////
////        for (item in listOne){
////
////            var obj: CallLogData? = null
////            if(item.id == id){
////                if(item.isMarked){
////                    obj = item.copy(isMarked = false)
////                    markedIds.remove(id)
////                    listTwo.add(obj)
////                }else{
////                    obj = item.copy(isMarked = true)
////                    markedIds.add(id)
////                    listTwo.add(obj)
////
////                }
////            }else{
////                listTwo.add(item)
////            }
////
////        }
//////        callLogsMutableLiveData.value!!.find {it.id == id }!!.isMarked = true
////        callLogsMutableLiveData.value = listTwo
////        if(markedIds.size == 1){
////            contactAdders = address
////        }
////        emit(markedIds.size)
//    }



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

    fun blockThisAddress(
        spammerType: Int,
        applicationContext: Context?,
        markedItems: List<String>,
        markedItemContactDetails: HashMap<String, CntctitemForView>
    ) : LiveData<Int> = liveData {

        if (markedItems.isNotEmpty()) {
            viewModelScope.launch {
                supervisorScope {
                    val as1 = async { repository?.marAsReportedByUser(markedItems) }

                    val as2 = async {
                        addAddressToPatternsTable(markedItems, markedItemContactDetails)
                    }
//                    val as4 = async { repository?.markAsSpamInSMS(contactAddress) }
                    val as3 = async { startSpamReportWorker(markedItems, applicationContext, spammerType) }
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
//                    try{
//                        as4.await()
//                    }catch (e:Exception){
//                        Log.d(TAG, "blockThisAddress: $e")
//                    }

                    generalBlockRepository.marAsReportedByUserInCall(contactAddress)
                    generalBlockRepository.marAsReportedByUserInSMS(contactAddress)
                }


//            }.join()
            }.join()

            emit(ON_COMPLETED)

        }
    }

    private suspend fun startSpamReportWorker(
        markedItems: List<String>,
        applicationContext: Context?,
        spammerType: Int
    ) {
        var commanSeperatedNumbers = ""
        for((count, num) in markedItems.withIndex()){
            if(count == markedItems.size -1){
                commanSeperatedNumbers += "$num"
            }else {
                commanSeperatedNumbers += "$num,"
            }
        }
        val list = commanSeperatedNumbers.split(",")
        applicationContext?.startSpamReportWorker(commanSeperatedNumbers, spammerType)
    }

    private suspend fun addAddressToPatternsTable(
        markedItems: List<String>,
        markedItemContactDetails: HashMap<String, CntctitemForView>
    ) {
        for (num in markedItems){
            var name = markedItemContactDetails[num]?.nameForblockListPattern
            blockListPatternRepository.insertPattern(
                num,
                BLOCK_TYPE_FROM_CALL_LOG,
                name?:num
                )
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
        for(infoFromServer in list){
             val resCallLog =  repository?.findFromCallLogTable(infoFromServer.contactAddress)
            var fullName: String? = null
            var nameFromServer = ""
            if(!infoFromServer.firstName.isNullOrEmpty()){
                fullName = infoFromServer.firstName
                if(!infoFromServer.lastName.isNullOrEmpty()){
                    fullName += " " + infoFromServer.lastName
                }
            }
            if(resCallLog?.nameFromServer != fullName || resCallLog?.nameFromServer != infoFromServer.nameInPhoneBook){
                nameFromServer = fullName?:infoFromServer.nameInPhoneBook
            }

              if(resCallLog!=null){
//                  Log.d(TAG, "updateWithNewInfoFromServer:  nameFromServer ${resCallLog.nameFromServer} fullName $fullName")
//                  Log.d(TAG, "updateWithNewInfoFromServer:  nameInPhoneBook ${resCallLog.nameFromServer} nameInPhoneBook ${infoFromServer.nameInPhoneBook}")
//                  Log.d(TAG, "updateWithNewInfoFromServer:  avatarGoogle ${resCallLog.avatarGoogle} avatarGoogle ${infoFromServer.avatarGoogle}")
//                  Log.d(TAG, "updateWithNewInfoFromServer:  imageFromDb ${resCallLog.imageFromDb} imageFromDb ${infoFromServer.thumbnailImg}")
//                  Log.d(TAG, "updateWithNewInfoFromServer:  isVerifiedUser ${resCallLog.isVerifiedUser} isVerifiedUser ${infoFromServer.isVerifiedUser}")
//                  Log.d(TAG, "updateWithNewInfoFromServer:  spamCount ${resCallLog.spamCount} spamCount ${infoFromServer.spamReportCount}")
//                  Log.d(TAG, "updateWithNewInfoFromServer:  hUid ${resCallLog.hUid} hUid ${infoFromServer.hUid}")
//                  if(
//                        res.nameFromServer!= fullName  || res.nameInPhoneBook != item.nameInPhoneBook ||
//                        res.avatarGoogle != item.avatarGoogle || res.imageFromDb != item.thumbnailImg  ||
//                        res.isVerifiedUser != item.isVerifiedUser || res.spamCount != item.spamReportCount  ||
//                        res.hUid != item.hUid
//                          ){
                      if(resCallLog.nameFromServer != nameFromServer ||
                          resCallLog.avatarGoogle != infoFromServer.avatarGoogle || resCallLog.imageFromDb != infoFromServer.thumbnailImg  ||
                          resCallLog.isVerifiedUser != infoFromServer.isVerifiedUser || resCallLog.spamCount != infoFromServer.spamReportCount  ||
                          resCallLog.hUid != infoFromServer.hUid
                      ){
                      repository?.updateCallLogWithServerInfo(infoFromServer)
                  }
              }
          }
    }


    fun updateDatabase(logs: MutableList<CallLogTable>, applicationContext: Context?) = viewModelScope.launch {

        withContext(Dispatchers.IO){
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
                    if(callLogTableInfo.nameInPhoneBook!= nameAndThumbnailFromCp.name || callLogTableInfo.thumbnailFromCp!= nameAndThumbnailFromCp.thumbnailUri){
                        repository?.updateWithCproviderInfo(callLogTableInfo.number, nameAndThumbnailFromCp)
                    }
                }
            }

        }
        }
    }



    fun getFirst10Logs() :LiveData<MutableList<CallLogTable>> = liveData {
        repository?.getFirst10Logs()?.let {
            it.add(CallLogTable(id = null, hUid = ""))
            emit(it)

        }
    }

    fun startHashWorker(applicationContext: Context?) = viewModelScope.launch {

//        repository?.startHashWork(applicationContext)
    }

    fun removeScreeningRoleItemFromList() = viewModelScope.launch{
//        callLogTableData?.let { liveData ->
//            liveData.value?.let { list->
//                if(list[0].id == ID_SHOW_SCREENING_ROLE){
//                    list.removeAt(0)
//                    callLogTableData.value = list
//                }
//
//            }
//        }
    }

    fun updateSpamThreshold(applicationContext:Context) = viewModelScope.launch {

        applicationContext?.let{
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            val oneTimeWorkRequest = OneTimeWorkRequest.Builder(SpamThresholdUpdateWorker::class.java)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(it).enqueue(oneTimeWorkRequest)
        }
    }


    companion object {
        const val TAG = "__CallContainerViewModel"
    }
}