package com.nibble.hashcaller.view.ui.sms.util

import android.content.Context
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.LinkedHashSet

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


    fun getInfoForPhoneNumbers(){

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

    fun changelist(smsLIst: List<SMS>, context:Context)  = viewModelScope.launch{

//        smsLive = SMSLiveData(context)
//        smsLive.value = smsLIst as List<SMS>
    }

    /**
     * @param smslist list of sms from content provider
     * function to get name for sms senders
     * if the address is a number(5555, 801238312) not a name('jio, vodafone-pay,etc')
     * then we need to get info for that number in locally
     * for address that is not in contact we need to search for that in server
     * even if that is of type number(5555, 801238312) or a name('jio, vodafone-pay,etc')
     *
     */
    fun getNameForUnknownSender(smslist: List<SMS>) = viewModelScope.launch {
//        getInfoFromContacts(smslist)
//        getInfoFromDB(smslist)


    }

    private suspend fun getInfoFromDB(smslist: List<SMS>) {
        GlobalScope.launch {
            repository!!.getInfoFromLocalDb(smslist)
        }
    }


    private suspend fun getInfoFromContacts(smslist: List<SMS>) {
        //I dont need to update smsLive.value because list is passed as reference,
        //since I am updating the list with name in getInfoFromContacts(smslist) from repository
        //automatically update the value
        //******Here list is passed as Reference ********

        repository!!.getInfoFromContacts(smslist)

    }

    /**
     * called when there is a change in table sender_infor_from_server changes
     */
    fun updateWithNewSenderInfo(
        dataFromDB: List<SMSSendersInfoFromServer>?,
        smsLIst: MutableList<SMS>?
    ) {



         viewModelScope.launch {
                   val r = async {
                        repository!!.getSMSForViewModel(null, requestinfromSpamlistFragment = false, isFullSmsNeeded = true)
                    }

                     val lst = r.await()


             smsLiveData.value = sortedSMSByTime()



         }



    }

    private fun sortedSMSByTime(): MutableList<SMS> {
        val lstofSMS =  mapofAddressAndSMS.values
        val sorted = lstofSMS.sortedByDescending { it.time }
        val lt:MutableList<SMS> = mutableListOf()
        lt.addAll(sorted)
        return lt
    }

    private fun sortAndSet(listOfMessages: MutableList<SMS>): ArrayList<SMS> {
        val s: Set<SMS> = LinkedHashSet(listOfMessages)
        val data = ArrayList(s)

        return data
    }

    fun deleteAllSmsindb() = viewModelScope.launch{
        repository!!.deleteAllSMmsendersINo()
    }


    fun updateLiveDataByFlow(lst: MutableList<SMS>) {
        smsLiveData.value = sortedSMSByTime()
    }

    /**
     * function called when there is a change in sms from content provider
     */
    fun updateLiveData(smsList: MutableList<SMS>?) = viewModelScope.launch  {
        //remove duplicates
        var lst:MutableList<SMS> = mutableListOf()
        //remove delted sms from hashmap
        var lstt:MutableList<SMS>?  = mutableListOf()


//        smsLiveData.value = lstt
//        val l:MutableList<SMS> = mutableListOf()
//        l.addAll(mapofAddressAndSMS.values)
        smsLiveData.value = async { removeDeletedSMS(smsList) }.await()







    }

    /**
     * function to make sure that deleted sms in contentprovider are delted from mapofAddressAndSMS
     */
    private suspend fun removeDeletedSMS(smsList: MutableList<SMS>?): MutableList<SMS>? {
        var newSMSHashmap: HashMap<String, SMS> = hashMapOf()
        if (smsList != null) {
            for(sms in smsList){
                //create new hashmap of updated list
                newSMSHashmap.put(sms.addressString!!, sms)
            }
        }
        mapofAddressAndSMS = newSMSHashmap

        return sortedSMSByTime()
    }

    fun getNextSmsPage()  = viewModelScope.launch{
        val res = async { repository!!.fetchSMS(null) }
        val newpage = res.await()

        var prevSize = 0
        if(smsLiveData.value !=null){
            prevSize = smsLiveData.value!!.size
        }

//        smsLiveData.value!!.addAll(newpage)
//        var sizeAfterAddingPage = smsLiveData.value!!.size
//        Log.d(TAG, "getNextSmsPage: prevSize $prevSize sizeAfterAddingPage $sizeAfterAddingPage  ")
//        isSizeEqual = prevSize == sizeAfterAddingPage

        smsLiveData.value = sortedSMSByTime()
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

    fun updateFlowList(sms: Flow<SMS>?) = viewModelScope.launch{
        sms!!.collect {
            if(it!=null){
                smsLIst!!.add(it!!)
            }
        }
        if(smsLIst!=null){
            var lst = sortAndSet(smsLIst!!)
            this@SMSViewModel.smsLiveData.value = lst
        }

    }

    fun updateLiveDataFromFlow(lst: MutableList<SMS>) {

    }


    companion object
    {
        //todo save color of round in this map, so that color does not change for miner change of sms
        private const val TAG ="__SMSViewModel"
        var isLoading:MutableLiveData<Boolean> = MutableLiveData(false)

    }
}
