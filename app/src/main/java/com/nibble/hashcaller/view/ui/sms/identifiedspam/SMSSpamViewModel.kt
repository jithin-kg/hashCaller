package com.nibble.hashcaller.view.ui.sms.identifiedspam

import android.util.Log
import androidx.lifecycle.*
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServer
import com.nibble.hashcaller.view.ui.contacts.utils.isSizeEqual
import com.nibble.hashcaller.view.ui.sms.util.SMS
import com.nibble.hashcaller.view.ui.sms.util.SMSLocalRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * Created by Jithin KG on 22,July,2020
 */
class SMSSpamViewModel(
    val SMS: SMSSpamLiveData,
    val repository: SMSLocalRepository?
): ViewModel() {
    var smsLiveDataSpam:MutableLiveData<MutableList<SMS>> = MutableLiveData()
    var smsLIstSpam:MutableList<SMS>? = null
    companion object
    {
        const val TAG = "__SMSSpamViewModel"
        var isLoading:MutableLiveData<Boolean> = MutableLiveData(false)
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
        SMS.value = sms

    }

    fun getUnrealMsgCount() = viewModelScope.launch{
        val count = repository?.getUnreadMsgCount()
        unreadMSCount?.value = count
    }

    fun update(address: String)  = viewModelScope.launch{
     SMS?.update(address)
    }

     fun deleteSpamSMS() = viewModelScope.launch {
        repository?.deleteAllSpamSMS()
    }

    fun updateLiveData(sms: MutableList<SMS>?) {
        this.smsLiveDataSpam.value = sms
    }
    fun getSmsSendersInfoFromServer(): LiveData<List<SMSSendersInfoFromServer>> {
        return repository!!.getSmsSenderInforFromDB()
    }

    /**
     * called when there is a change in table sender_infor_from_server changes
     */
    fun updateWithNewSenderInfo(
        dataFromDB: List<SMSSendersInfoFromServer>?
    ) {



        viewModelScope.launch {



            var mapofAddressAndValues: HashMap<String, SMS> = HashMap() //hold all sms sender
            // info found fom db

//            for(sms in dataFromDB!!){
//                //creating a map of senderinfo that is received from DB
//                val obj = SMS()
//                obj.name = sms.name
//                obj.type = sms.spammerType
//                obj.spamCount = sms.spamReportCount
//                mapofAddressAndValues.put(sms.contactAddress!!, obj)
              val r = async {  repository!!.getSMSForViewModel( searchQuery = null, isFullSmsNeeded = true, requestinfromSpamlistFragment = true) }
              val lst =   r.await()

            smsLiveDataSpam.value = lst

//            }
//            var lst:MutableList<SMS>  = mutableListOf()
//
//
////                 repository!!.fetchSmsForWorker().apply {
//            if(smsLiveDataSpam.value!=null)
//                for(sms in smsLiveDataSpam.value!!){
//
//                    val res = mapofAddressAndValues.get(sms.addressString)
//                    val obj = sms
//
//                    if(res!=null){
//                        sms.name = res.name
//                        sms.senderInfoFoundFrom = SENDER_INFO_FROM_DB
//                        if(res.spamCount >0){
//                            lst.add(obj)
//                        }
//                    }
//                }
//            smsLiveDataSpam.value = lst
////                 }


//            smsLive.value = lst

        }
//            smsLiveData.value = smsLiveData.value


    }

    fun getNextSmsPage() = viewModelScope.launch {
       val res =  async { repository!!.getSMSForViewModel(null,true) }
//
        val newpage = res.await()
        var prevSize = 0
        if(smsLiveDataSpam.value !=null){
            prevSize = smsLiveDataSpam.value!!.size
        }

        smsLiveDataSpam.value!!.addAll(newpage)
        var sizeAfterAddingPage = smsLiveDataSpam.value!!.size
        Log.d(TAG, "getNextSmsPage: prevSize $prevSize sizeAfterAddingPage $sizeAfterAddingPage  ")
        isSizeEqual = prevSize == sizeAfterAddingPage
        smsLiveDataSpam.value = smsLiveDataSpam.value
    }


}
//class ContactsViewModel(application: Application): AndroidViewModel(application) {
//
//    val contacts =
//        ContactLiveData(application.applicationContext)
//
//}