package com.nibble.hashcaller.view.ui.sms.util

import android.content.SharedPreferences
import androidx.lifecycle.*
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServer
import com.nibble.hashcaller.network.spam.ReportedUserDTo
import com.nibble.hashcaller.view.ui.contacts.utils.pageOb
import com.nibble.hashcaller.view.ui.sms.SMSContainerFragment.Companion.mapofAddressAndSMS
import com.nibble.hashcaller.view.ui.sms.list.SMSLiveDataFlow
import com.nibble.hashcaller.view.ui.sms.work.SmsHashedNumUploadWorker
import com.nibble.hashcaller.work.replaceSpecialChars
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * Created by Jithin KG on 22,July,2020
 */
class SMSViewModel(
    val SMS: SMSLiveDataFlow,
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

    companion object
    {
        //todo save color of round in this map, so that color does not change for miner change of sms
        private const val TAG ="__SMSViewModel"
        var isLoading:MutableLiveData<Boolean> = MutableLiveData(false)

    }
}
