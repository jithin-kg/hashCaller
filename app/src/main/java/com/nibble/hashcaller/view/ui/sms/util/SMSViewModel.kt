package com.nibble.hashcaller.view.ui.sms.util

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.*
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServer
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
    private  var smsSenersInfoFromDB : LiveData<List<SMSSendersInfoFromServer>> = repository!!.getSmsSenderInforFromDB()

//    var smsLive:SMSLiveData = SMS //assigning SMS live data to smslive
    var smsLiveData:MutableLiveData<MutableList<SMS>> = MutableLiveData()
     var smsLIst:MutableList<SMS>? = null


    private lateinit var sharedPreferences: SharedPreferences


    companion object
    {
        private const val TAG ="__DialerViewModel"
        var isLoading:MutableLiveData<Boolean> = MutableLiveData(false)
    }



    fun getSmsSendersInfoFromServer(): LiveData<List<SMSSendersInfoFromServer>> {
        return smsSenersInfoFromDB
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
     * called when there is a change in sender_infor_from_server changes
     */
    fun updateWithNewSenderInfo(
        dataFromDB: List<SMSSendersInfoFromServer>?,
        smsLIst: MutableList<SMS>?
    ) {
//------

//        if (this.smsLiveData.value != null) {
//            for(sms in this.smsLiveData.value!!){
//                sms.name = "sammm"
//
//            }
//            val result:MutableList<SMS> = mutableListOf()
//            result.addAll(this.smsLiveData.value!!)
//            this.smsLiveData.value = result
//
//        }

        //-------

        var mapofAddressAndValues: HashMap<String, SMS> = HashMap()

        for(sms in dataFromDB!!){
            val obj = SMS()
            obj.name = sms.name
            obj.type = sms.spammerType
            obj.spamCount = sms.spamReportCount
            mapofAddressAndValues.put(sms.contactAddress!!, obj)
        }
        var lst:MutableList<SMS>  = mutableListOf()
        if(smsLiveData.value!=null){
            for(sms in smsLiveData.value!!){
                val res = mapofAddressAndValues.get(sms.addressString)
                val obj = sms

                if(res!=null){
                    sms.name = res.name

                }
                lst.add(obj)
            }
//            smsLive.value = lst
            smsLiveData.value = smsLiveData.value
        }
    }

    fun updateLiveData(sms: MutableList<SMS>?)  {
        this.smsLiveData.value = sms
        getNameForUnknownSender(sms!!)

    }

    fun getNextSmsPage()  = viewModelScope.launch{
        val res = async { repository!!.fetchSMS(null) }
        val newpage = res.await()
        smsLiveData.value!!.addAll(newpage)
        smsLiveData.value = smsLiveData.value
    }


}
//class ContactsViewModel(application: Application): AndroidViewModel(application) {
//
//    val contacts =
//        ContactLiveData(application.applicationContext)
//
//}