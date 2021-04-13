package com.nibble.hashcaller.view.ui.sms.util

import android.content.SharedPreferences
import androidx.lifecycle.*
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServer
import com.nibble.hashcaller.network.spam.ReportedUserDTo
import com.nibble.hashcaller.view.ui.contacts.utils.pageOb
import com.nibble.hashcaller.view.ui.sms.db.SMSThreadANDServerInfo
import com.nibble.hashcaller.view.ui.sms.db.SmsThreadTable
import com.nibble.hashcaller.view.ui.sms.list.SMSLiveData
import com.nibble.hashcaller.view.ui.sms.work.SmsHashedNumUploadWorker
import com.nibble.hashcaller.work.replaceSpecialChars
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * Created by Jithin KG on 22,July,2020
 */
class SMSViewModel(
    val SMS: SMSLiveData,
    val repository: SMSLocalRepository?
): ViewModel() {

    var numRowsDeletedLiveData: MutableLiveData<Int> = MutableLiveData(-1)
    var smsThreadsLivedata: LiveData<MutableList<SMSThreadANDServerInfo>>? = repository?.getSMSThreadsLivedata()
    var mapofAddressAndPos: HashMap<String, Long> = hashMapOf() // for findin duplicate sms in list
//    private  var smsSenersInfoFromDB : LiveData<List<SMSSendersInfoFromServer>> = repository!!.getSmsSenderInforFromDB()

//    var smsLive:SMSLiveData = SMS //assigning SMS live data to smslive
    var smsLiveData:MutableLiveData<MutableList<SMS>> = MutableLiveData()
     var smsLIst:MutableList<SMS>? = mutableListOf()

    var markedItems: MutableLiveData<MutableSet<Long>> = MutableLiveData(mutableSetOf())
    var markedItemsPositions: HashSet<Int> = hashSetOf()


    private lateinit var sharedPreferences: SharedPreferences






    fun getSmsSendersInfoFromServer(): LiveData<List<SMSSendersInfoFromServer>> {
        return repository!!.getSmsSenderInforFromDB()
//        return smsSenersInfoFromDB
    }

    fun getmarkedItemSize(): Int {

        var size = markedItems.value?.size
        return size ?: 0
    }
    fun addTomarkeditems(id: Long, position: Int){
        markedItems.value!!.add(id)
        markedItemsPositions.add(position)
        markedItems.value = markedItems.value
    }
    fun removeMarkeditemById(id: Long, position: Int){
        markedItems.value!!.remove(id)
        markedItemsPositions.remove(position)
        markedItems.value = markedItems.value
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
    fun updateWithNewSenderInfo() = viewModelScope.launch {



//         viewModelScope.launch {
//                   val r = async {
//                        repository!!.fetchSMS(null, false)
//                    }
//
//                     val lst = r.await()
//
//
////             smsLiveData.value = sortedSMSByTime()
//             smsLiveData.value = lst
//
//
//
//         }
        repository!!.fetchSMS(null, false).apply {
//            smsLiveData.value = this

        }




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
    fun updateLiveData(smsList: List<SMSThreadANDServerInfo>) = viewModelScope.launch  {

        var mutableList: MutableList<SMSThreadANDServerInfo> = mutableListOf()
        mutableList.addAll(smsList)




    }




    fun blockThisAddress(contactAddress: String,
                         threadID: Long, spammerType: Int,
                         spammerCategory: Int) = viewModelScope.launch {

        async {

            repository?.save(replaceSpecialChars(contactAddress), 1, "", "" )
        }

        async {
            repository?.report(
                ReportedUserDTo(
                    replaceSpecialChars(contactAddress), " ",
                    spammerType.toString(), spammerCategory.toString()
                )
            )
        }


    }

    fun muteMarkedSenders() = viewModelScope.launch {
        repository!!.muteSenders()
    }

    fun markThreadAsDeleted() = viewModelScope.launch {
        var set: HashSet<Long> = hashSetOf()
        markedItems.value?.let {
            set.addAll(it)
        }
        for(threadId in set){
            async { repository?.markAsDelete(threadId) }.await()
        }
        clearMarkeditems()
    }
    fun clearMarkeditems(){
        markedItems.value?.clear()
    }

    /**
     * called when there is a change in sms data
     * to get information abount a sender
     */
    fun getInformationForTheseNumbers() = viewModelScope.launch {

        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(SmsHashedNumUploadWorker::class.java).build()
        WorkManager.getInstance().enqueue(oneTimeWorkRequest)

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

    fun updateDatabase(sms: MutableList<SmsThreadTable>) = viewModelScope.launch {

//        val as1 = async {
//            sms?.let { repository?.updateThreadsDb(it) }
//        }

        sms?.let{
         val as1 =    async {  repository?.updateThreadsDb(it) }
         val as2 = async { getInformationForTheseNumbers()}
         val as3 = async { repository?.deleteFromDb(it) }
            as1.await()
            as2.await()
            as3.await()
        }



//        as1.await()
    }

    private fun setName(threads: MutableList<SmsThreadTable>) {
        var numbersSet: HashSet<String> = hashSetOf()
        var numberNamehashMap: HashMap<String, String> = hashMapOf()
        numbersSet.addAll(threads.map { it.contactAddress})

        for (num in numbersSet){
            val name:String? =  repository?.getNameForAddressFromContentProvider(num)
            if(name!=null){
                numberNamehashMap.put(num, name)
            }
        }
        for(item in threads){
            if(numberNamehashMap.containsKey(item.contactAddress)){
                item.name = numberNamehashMap[item.contactAddress]
                item.senderInfoFoundFrom = SENDER_INFO_FROM_CONTENT_PROVIDER
            }
        }
    }

    companion object
    {
        //todo save color of round in this map, so that color does not change for miner change of sms
        private const val TAG ="__SMSViewModel"
        var isLoading:MutableLiveData<Boolean> = MutableLiveData(false)

    }
}
