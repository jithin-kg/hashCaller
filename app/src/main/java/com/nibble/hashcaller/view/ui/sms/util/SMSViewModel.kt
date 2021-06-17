package com.nibble.hashcaller.view.ui.sms.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.*
import androidx.work.*
import com.nibble.hashcaller.local.db.blocklist.BlockedListPattern
import com.nibble.hashcaller.repository.BlockListPatternRepository
import com.nibble.hashcaller.view.ui.blockConfig.GeneralBlockRepository
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServer
import com.nibble.hashcaller.view.ui.call.spam.MarkeditemsHelper
import com.nibble.hashcaller.view.ui.contacts.startSpamReportWorker
import com.nibble.hashcaller.view.ui.sms.db.SmsThreadTable
import com.nibble.hashcaller.view.ui.sms.individual.util.EXACT_NUMBER
import com.nibble.hashcaller.view.ui.sms.individual.util.ON_COMPLETED
import com.nibble.hashcaller.view.ui.sms.individual.util.ON_PROGRESS
import com.nibble.hashcaller.view.ui.sms.list.SMSLiveData
import com.nibble.hashcaller.view.ui.sms.work.SmsHashedNumUploadWorker
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.*

/**
 * Created by Jithin KG on 22,July,2020
 */
class SMSViewModel(
    val SMS: SMSLiveData,
    val repository: SMSLocalRepository?,
    private val blockListPatternRepository: BlockListPatternRepository,
    private val generalBlockRepository: GeneralBlockRepository
): ViewModel() {

    var numRowsDeletedLiveData: MutableLiveData<Int> = MutableLiveData(-1)
    var smsThreadsLivedata: LiveData<MutableList<SmsThreadTable>>? = repository?.getSMSThreadsLivedata()


//    var smsLive:SMSLiveData = SMS //assigning SMS live data to smslive
    var smsLiveData:MutableLiveData<MutableList<SMS>> = MutableLiveData()
     var smsLIst:MutableList<SMS>? = mutableListOf()

//    var markedItems: MutableLiveData<MutableSet<Long>> = MutableLiveData(mutableSetOf())
    var markeditemsHelper = MarkeditemsHelper()


    private lateinit var sharedPreferences: SharedPreferences






    fun getSmsSendersInfoFromServer(): LiveData<List<CallersInfoFromServer>> {
        return repository!!.getSmsSenderInforFromDB()
//        return smsSenersInfoFromDB
    }

    fun getmarkedItemSize(): Int {
        return markeditemsHelper.getmarkedItemSize()

    }
    fun addTomarkeditems(id: Long, position: Int, address: String){
        markeditemsHelper.addTomarkeditems(id, position, address)

    }
    fun removeMarkeditemById(id: Long, position: Int, address: String){
        markeditemsHelper.removeMarkeditemById(id, position, address)
    }
    var unreadMSCount:MutableLiveData<Int>? = null
     var filteredSms: MutableLiveData<String>? = null


    fun getPhoneNumber(): MutableLiveData<String>? {
        if (filteredSms == null) {
            filteredSms = MutableLiveData<String>()
            return filteredSms
        }
        return filteredSms
    }


    fun search(searchQuery: String?)  = viewModelScope.launch{
      val sms =  repository?.getSms(searchQuery)
//        SMS.value = sms

    }

    fun getUnrealMsgCount() = viewModelScope.launch{
        val count = repository?.getUnreadMsgCount()
        unreadMSCount?.value = count
    }

    /**
     * if addresstring is null then all sms has to be marked as read
     */
    fun markSMSAsRead(address: String?)  = viewModelScope.launch{
     repository!!.markSMSAsRead(address)
    }







    /**
     * called when there is a change in table sender_infor_from_server changes
     */
    fun updateWithNewSenderInfo(list: List<CallersInfoFromServer>) = viewModelScope.launch {
        upadteThreadsWithInfoFromServer()
//        for(item in list){
//            async { repository?.updateThreadsDBWithServerInfo(item) }.await()
//        }
    }

//    private fun sortedSMSByTime(): MutableList<SMS> {
//        val lstofSMS =  mapofAddressAndSMS.values
//        val sorted = lstofSMS.sortedByDescending { it.time }
//        val lt:MutableList<SMS> = mutableListOf()
//        lt.addAll(sorted)
//        return lt
//    }



    fun deleteAllSmsindb() = viewModelScope.launch{
        repository!!.deleteAllSMmsendersINo()
    }



    /**
     * function called when there is a change in sms from content provider
     */

//    fun updateLiveData(smsList: List<SMSThreadANDServerInfo>) = viewModelScope.launch  {
//
//        var mutableList: MutableList<SMSThreadANDServerInfo> = mutableListOf()
//        mutableList.addAll(smsList)
//
//
//
//
//    }




    fun blockThisAddress(spammerType: Int, applicationContext: Context?): LiveData<Int> = liveData  {
        var contactAddress = markeditemsHelper.getmarkedAddresAt(0) ?: ""

        viewModelScope.launch {
            val defLocal = async {
                if (!contactAddress.isNullOrEmpty()) {
//                    for (id in items) {
//                        val id = items[0]
//                        val thread = repository?.findOneThreadById(id)
//                        if (thread != null) {

//                             contactAddress = thread.numFormated
//                            repository?.markAsSpam(contactAddress, 1, "", "")

                            blockListPatternRepository.insertPattern(
                                    contactAddress,
                                    EXACT_NUMBER
                            )
                    blockListPatternRepository?.markAsSpam(contactAddress)
                 }

            }
            val as2 = async { repository?.marAsReportedByUserInCall(contactAddress) }
            val as3 = async {
                applicationContext?.startSpamReportWorker(contactAddress, spammerType)
            }

        try {
            defLocal.await()
        }catch (e:Exception){
            Log.d(TAG, "blockThisAddress: $e")
        }

        try {
            as3.await()
        }catch (e:Exception){
            Log.d(TAG, "blockThisAddress: $e")
        }

        try {
            as2.await()
        }catch (e:Exception){
            Log.d(TAG, "blockThisAddress: $e")
        }
        }.join()

        generalBlockRepository.marAsReportedByUserInSMS(contactAddress)
        generalBlockRepository.marAsReportedByUserInCall(contactAddress)
        emit(ON_COMPLETED)



    }


    fun muteMarkedSenders() = viewModelScope.launch {
        repository!!.muteSenders()
    }

    fun deleteMarkedSMSThreads(): LiveData<Int> = liveData {
        emit(ON_PROGRESS)
        viewModelScope.launch {
        var set: HashSet<Long> = hashSetOf()
            markeditemsHelper.markedItems.value?.let {
            set.addAll(it)
        }
        for (threadId in set) {
           val as1 =  async { repository?.markAsDelete(threadId) }
           val as2 =  async { repository?.deleteSmsThread(threadId) }
            kotlinx.coroutines.delay(300L)
        }
        clearMarkeditems()

    }.join()
        clearMarkeditems()
        emit(ON_COMPLETED)

    }
    fun clearMarkeditems(){
        markeditemsHelper.clearMarkeditems()
//        markeditemsHelper
//        markedItems.value?.clear()
    }

    /**
     * called when there is a change in sms data
     * to get information abount a sender
     */
    fun scheduleWorker(applicationContext: Context) = viewModelScope.launch {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(SmsHashedNumUploadWorker::class.java)
                                 .setConstraints(constraints)
                                .build()
        WorkManager.getInstance(applicationContext).enqueue(oneTimeWorkRequest)

    }




    fun getFirstPageOfSMS() =viewModelScope.launch{
        val res = async { repository!!.fetchSMS(null, false) }.await()
//        updateLiveData(res)
    }


    override fun onCleared() {
        super.onCleared()
    }

    fun mark(id: Long, address: String) = viewModelScope.launch{
        
    }
    fun <T> List<T>.replace(newValue: T, block: (T) -> Boolean): MutableList<T> {
        return map {
            if (block(it)) newValue else it
        }.toMutableList()
    }

    fun updateDatabase(sms: MutableList<SmsThreadTable>, applicationContext: Context?) = viewModelScope.launch {

//        val supervisorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        withContext(Dispatchers.IO){
            sms?.let{
                val as1 =    async {  repository?.insertIntoThreadsDb(it) }
                val as2 = async{repository?.updateThreadContent(it)}
                val as3 = async { applicationContext?.let { it1 -> scheduleWorker(it1) } }
                val as4 = async { repository?.deleteFromDb(it) }
                val as5 = async { generalBlockRepository.updateSMSWithBlockListPattern(sms) }

                try {
                    as1.await()
                }catch (e:Exception){
                    Log.d(TAG, "updateDatabase: $e")
                }

                try{
                    as2.await()
                }catch (e:Exception){
                    Log.d(TAG, "updateDatabase:exception $e")
                }

                try{
                    as3.await()
                }catch (e:Exception){
                    Log.d(TAG, "updateDatabase:exception $e")
                }

                try{
                    as4.await()
                }catch (e:Exception){
                    Log.d(TAG, "updateDatabase:exception $e")
                }
            try{
                as5.await()
            }catch (e:Exception){
                Log.d(TAG, "updateDatabase: $e")
            }



//            as5.await()
            }

        }
        upadteThreadsWithInfoFromServer()

//        updateWithServerInfo()







//        as1.await()
    }

    private fun upadteThreadsWithInfoFromServer() = viewModelScope.launch {
      withContext(Dispatchers.IO){
          val allThreads = repository?.getAllSmsThreads()
          if(allThreads!=null)
              for (threadItem in allThreads){
                  val infoFromServer = repository?.getServerInfoForNumber(threadItem.numFormated)
                  //todo update with contentprovider data from here
                  val infoFromCprovider = repository?.getInfoFromCproviderForNum(threadItem.numFormated)
                  if(infoFromServer!=null){
                      if(threadItem.firstNameFromServer!= infoFromServer.firstName || threadItem.lastNameFromServer !=infoFromServer.lastName ||
                          threadItem.spamCount < infoFromServer.spamReportCount ||threadItem.imageFromDb != infoFromServer.thumbnailImg){
                          //if any of the info exists in threads table related to server info is diff from server ino , update it
                          repository?.updateThreadsDBWithServerInfo(infoFromServer)
                      }
                  }
                  if(infoFromCprovider!=null){
                      if(threadItem.firstName!= infoFromCprovider.firstName || threadItem.thumbnailFromCp != infoFromCprovider.photoThumnailServer){
                          withContext(Dispatchers.Default) {
                              repository?.updateChatThreadWithContentProviderInfo(
                                  infoFromCprovider
                              )
                          }
                      }
                  }
              }
      }


    }

    private fun updateWithServerInfo(threadsFromCprovider: MutableList<SmsThreadTable>) = viewModelScope.launch {
//        for (item in threadsFromCprovider){
//          updateDb(item)
//        }
    }

    private fun updateDb(item: SmsThreadTable) {
        viewModelScope.launch {
            var isInfoTobBeUpdated = false
//            val nameAndThumbnailFromCp = async { repository?.getNameForAddressFromContentProvider(item.contactAddress) }.await()
//            if(nameAndThumbnailFromCp!=null){
//                if(item.firstName != nameAndThumbnailFromCp.name || item.thumbnailFromCp !=nameAndThumbnailFromCp.thumbnailUri){
//                    isInfoTobBeUpdated = true
//                    item.firstName = nameAndThumbnailFromCp.name
//                    item.thumbnailFromCp = nameAndThumbnailFromCp.thumbnailUri
//                }
//            }

            val infoFromServer:CallersInfoFromServer? =  async { repository?.getSenderInfoFromServerForAddres(item.numFormated) }.await()
            val threadInfoInDb = async { repository?.getThreadInfo(item.numFormated) }.await()
            if(infoFromServer!=null && threadInfoInDb !=null){
                if(threadInfoInDb.firstNameFromServer != infoFromServer.firstName){
                    item.firstNameFromServer = infoFromServer.firstName
                    item.lastNameFromServer = infoFromServer.lastName
                    isInfoTobBeUpdated = true
                }
                if(threadInfoInDb.spamCount < infoFromServer.spamReportCount){
                    item.spamCount = infoFromServer.spamReportCount
                    isInfoTobBeUpdated = true
                }

            }
            if(isInfoTobBeUpdated){
                async { repository?.updateThreadSpamCount(item) }.await()
            }
        }

    }

    private fun setName(threads: MutableList<SmsThreadTable>) {
        var numbersSet: HashSet<String> = hashSetOf()
        var numberNamehashMap: HashMap<String, String> = hashMapOf()
        numbersSet.addAll(threads.map { it.contactAddress})

        for (num in numbersSet){
//            val name:String? =  repository?.getNameForAddressFromContentProvider(num)
//            if(name!=null){
//                numberNamehashMap.put(num, name)
//            }
        }
        for(item in threads){
            if(numberNamehashMap.containsKey(item.contactAddress)){
                item.firstName = numberNamehashMap[item.contactAddress] ?: ""
                item.senderInfoFoundFrom = SENDER_INFO_FROM_CONTENT_PROVIDER
            }
        }
    }

    fun clearMarkedPositions() {
        markeditemsHelper.clearMarkedItemPositions()
    }

    fun getmarkeditemPositions() : Iterable<Int> {
        return markeditemsHelper.markedItemsPositions
    }

    fun clearMarkedItems() = viewModelScope.launch {
        markeditemsHelper.clearMarkeditems()
        markeditemsHelper.clearMarkedItemPositions()

    }

    fun clearMarkedItemPositions() = viewModelScope.launch{
        markeditemsHelper.clearMarkedItemPositions()
//        markedItemsPositions.clear()
    }


    companion object
    {
        //todo save color of round in this map, so that color does not change for miner change of sms
        private const val TAG ="__SMSViewModel"
        var isLoading:MutableLiveData<Boolean> = MutableLiveData(false)

    }
}
