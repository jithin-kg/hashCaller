package com.nibble.hashcaller.view.ui.sms.util

import android.content.SharedPreferences
import androidx.lifecycle.*
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServer
import com.nibble.hashcaller.network.spam.ReportedUserDTo
import com.nibble.hashcaller.view.ui.contacts.utils.pageOb
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

    var mapofAddressAndPos: HashMap<String, Long> = hashMapOf() // for findin duplicate sms in list
//    private  var smsSenersInfoFromDB : LiveData<List<SMSSendersInfoFromServer>> = repository!!.getSmsSenderInforFromDB()

//    var smsLive:SMSLiveData = SMS //assigning SMS live data to smslive
    var smsLiveData:MutableLiveData<MutableList<SMS>> = MutableLiveData()
     var smsLIst:MutableList<SMS>? = mutableListOf()


    private lateinit var sharedPreferences: SharedPreferences






    fun getSmsSendersInfoFromServer(): LiveData<List<SMSSendersInfoFromServer>> {
        return repository!!.getSmsSenderInforFromDB()
//        return smsSenersInfoFromDB
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
            smsLiveData.value = this

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
    fun updateLiveData(smsList: MutableList<SMS>?) = viewModelScope.launch  {

        smsLiveData.value = smsList



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

    fun deleteThread() = viewModelScope.launch {
        val numRowsDeleted =  repository!!.deleteSmsThread()
        pageOb.page = 0
        numRowsDeletedLiveData.value = numRowsDeleted
    }

    /**
     * called when there is a change in sms data
     * to get information abount a sender
     */
    fun getInformationForTheseNumbers(
        smslist: List<SMS>?,
        packageName: String
    ) = viewModelScope.launch {

        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(SmsHashedNumUploadWorker::class.java).build()
        WorkManager.getInstance().enqueue(oneTimeWorkRequest)

    }




    fun getFirstPageOfSMS() =viewModelScope.launch{
        val res = async { repository!!.fetchFirstPageOfSMS() }.await()
        updateLiveData(res)
    }


    override fun onCleared() {
        super.onCleared()
    }

    fun mark(id: Long, address: String) = viewModelScope.launch{


//        smsLiveData.value?.find{it.addressString == address}?.kaatam = "true"
//        smsLiveData.value = smsLiveData.value
        var list1: MutableList<SMS> = mutableListOf()
        list1.addAll(smsLiveData.value!!)
        var list2: MutableList<SMS> = mutableListOf()
        val item = list1.find { it.addressString == address }

        for(item in list1){
            var secondObj : SMS ? = null
            if(item.addressString == address){
                if(item.isMarked){
                    secondObj = item.copy(isMarked = false)
                    repository?. markedThreadIds?.remove(id)
                }else{
                    secondObj = item.copy(isMarked = true)
                    repository?.markedThreadIds?.add(id)
                }
                list2.add(secondObj!!)
            }else{
                if(!item.isMarked && item.addressString !=address){
                    repository?. markedThreadIds?.remove(id)
                }

                list2.add(item)
            }
        }
        smsLiveData.value = list2


//        val item = smsLiveData.value!!.find { it.addressString == address }
//        val sms = SMS()
//        sms.isMarked = true

//        Log.d(TAG, "mark: $item")
//        smsLiveData.value!!.replace(newValue = SMS()){it.addressString ==address}

//        var list:MutableList<SMS> = mutableListOf()
//        list.addAll(smsLiveData.value!!)
//        var copyList:MutableList<SMS> = mutableListOf()

//        for (item in list){
//            var obj = SMS()
//                obj.isMarked = true
//                obj.readState = item.readState
//                obj.spamCount  = item.spamCount
//                obj.addresStringNonFormated = item.addresStringNonFormated
//                obj.sub = item.sub
//                obj.subject = item.subject
//                obj.ct_t = item.ct_t
//                obj.read_status = item.read_status
//                obj.reply_path_present = item.reply_path_present
//                obj.body = item.body
//                obj.addressString = item.addressString
//                obj.address = item.address
//                obj.readState = item.readState
//                obj.relativeTime = item.relativeTime
//                obj. senderInfoFoundFrom= item.senderInfoFoundFrom
//                obj.nameForDisplay = item.nameForDisplay
//                obj.msgString = item.msgString
//                obj.isMarked = true
//
//                if(obj.addressString == address){
//                    Log.d(TAG, "mark: address equal")
//                    obj.kaatam = "kaatam"
//
//                }
//
//            copyList.add(obj)
//        }

//        for(item in list){
//            copyList.add(item.clone)
//        }
//        smsLiveData.value = copyList

//        smsLiveData.value = smsLiveData.value
       // emit(0)
        
    }
    fun <T> List<T>.replace(newValue: T, block: (T) -> Boolean): MutableList<T> {
        return map {
            if (block(it)) newValue else it
        }.toMutableList()
    }
    companion object
    {
        //todo save color of round in this map, so that color does not change for miner change of sms
        private const val TAG ="__SMSViewModel"
        var isLoading:MutableLiveData<Boolean> = MutableLiveData(false)

    }
}
