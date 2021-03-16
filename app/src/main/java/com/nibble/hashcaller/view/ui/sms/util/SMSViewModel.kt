package com.nibble.hashcaller.view.ui.sms.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.*
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServer
import com.nibble.hashcaller.network.spam.ReportedUserDTo
import com.nibble.hashcaller.view.ui.contacts.utils.isSizeEqual
import com.nibble.hashcaller.view.ui.contacts.utils.pageOb
import com.nibble.hashcaller.view.ui.sms.list.SMSLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * Created by Jithin KG on 22,July,2020
 */
class SMSViewModel(
    val SMS: SMSLiveData,
    val repository: SMSLocalRepository?
): ViewModel() {
    var numRowsDeletedLiveData: MutableLiveData<Int> = MutableLiveData(0)

//    private  var smsSenersInfoFromDB : LiveData<List<SMSSendersInfoFromServer>> = repository!!.getSmsSenderInforFromDB()

//    var smsLive:SMSLiveData = SMS //assigning SMS live data to smslive
    var smsLiveData:MutableLiveData<MutableList<SMS>> = MutableLiveData()
     var smsLIst:MutableList<SMS>? = null


    private lateinit var sharedPreferences: SharedPreferences


    companion object
    {
        private const val TAG ="__SMSViewModel"
        var isLoading:MutableLiveData<Boolean> = MutableLiveData(false)
    }



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
        SMS.value = sms

    }

    fun getUnrealMsgCount() = viewModelScope.launch{
        val count = repository?.getUnreadMsgCount()
        unreadMSCount?.value = count
    }

    fun update(address: String)  = viewModelScope.launch{
     SMS?.update(address)
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

//             var mapofAddressAndValues: HashMap<String, SMS> = HashMap() //hold all sms sender
//             // info found fom db
//
//             for(sms in dataFromDB!!){
//                 //creating a map of senderinfo that is received from DB
//                 val obj = SMS()
//                 obj.name = sms.name
//                 obj.type = sms.spammerType
//                 obj.spamCount = sms.spamReportCount
//                 mapofAddressAndValues.put(sms.contactAddress!!, obj)
//
//             }
//             var lst:MutableList<SMS>  = mutableListOf()
//
//
////                 repository!!.fetchSmsForWorker().apply {
//                     if(smsLiveData.value!=null)
//                     for(sms in smsLiveData.value!!){
//                         val res = mapofAddressAndValues.get(sms.addressString)
//                         val obj = sms
//
//                         if(res!=null){
//                             sms.name = res.name
//                             sms.senderInfoFoundFrom = SENDER_INFO_FROM_DB
//                             if(res.spamCount <1){
//                                 lst.add(obj)
//                             }
//                         }else{
//
//                             lst.add(sms)
//
//                         }
//                     }
                   val r = async {
                        repository!!.getSMSForViewModel(null, requestinfromSpamlistFragment = false, isFullSmsNeeded = true)
                    }

                     val lst = r.await()
                     smsLiveData.value = lst
//                 }


//            smsLive.value = lst

         }
//            smsLiveData.value = smsLiveData.value


    }

    fun updateLiveData(sms: MutableList<SMS>?)  {
        this.smsLiveData.value = sms
        getNameForUnknownSender(sms!!)

    }

    fun getNextSmsPage()  = viewModelScope.launch{
        val res = async { repository!!.fetchSMS(null) }
        val newpage = res.await()
        var prevSize = 0
        if(smsLiveData.value !=null){
            prevSize = smsLiveData.value!!.size
        }

        smsLiveData.value!!.addAll(newpage)
        var sizeAfterAddingPage = smsLiveData.value!!.size
        Log.d(TAG, "getNextSmsPage: prevSize $prevSize sizeAfterAddingPage $sizeAfterAddingPage  ")
        isSizeEqual = prevSize == sizeAfterAddingPage
        smsLiveData.value = smsLiveData.value
    }

    fun blockThisAddress(contactAddress: String,
                         threadID: Long, spammerType: Int,
                         spammerCategory: Int) = viewModelScope.launch {

        async {

            repository?.save(contactAddress, 1, "", "" )
        }

        async {
            repository?.report(
                ReportedUserDTo(contactAddress, " ",
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


}
//class ContactsViewModel(application: Application): AndroidViewModel(application) {
//
//    val contacts =
//        ContactLiveData(application.applicationContext)
//
//}