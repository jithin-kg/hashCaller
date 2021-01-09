package com.nibble.hashcaller.view.ui.sms.individual

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nibble.hashcaller.local.db.blocklist.SpammerInfo
import com.nibble.hashcaller.local.db.sms.SmsOutboxListDAO
import com.nibble.hashcaller.network.spam.ReportedUserDTo
import com.nibble.hashcaller.repository.spam.SpamNetworkRepository
import com.nibble.hashcaller.utils.SmsStatusSentReceiver
import com.nibble.hashcaller.view.ui.sms.util.SMSLocalRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


/**
 * Created by Jithin KG on 22,July,2020
 */
class SMSIndividualViewModel(
    val SMS: SMSIndividualLiveData,
    private val repository: SMSLocalRepository?,
    private val smsDAODAO: SmsOutboxListDAO?,
    private val spamRepository: SpamNetworkRepository?
): ViewModel() {
//    public var blockedStatusOfThenumber:MutableList<SpammerInfo> =
//        mutableListOf<SpammerInfo>()
    var nameLiveData: MutableLiveData<String> = MutableLiveData("")

    public var blockedStatusOfThenumber:LiveData<List<SpammerInfo>>?= null

    private var applicationContext:Context?=null
    private  var  sendBroadcastReceiver: BroadcastReceiver = SmsStatusSentReceiver()
//    private  var deliveryBroadcastReceiver: BroadcastReceiver = SmsStatusDeliveredReceiver()

    init {

    }
     var filteredSms: MutableLiveData<String>? = null

    fun getContactInfoForNumber(pno:String) = viewModelScope.launch {
        val name = repository?.getConactInfoForNumber(pno)
        Log.d(TAG, "getContactInfoForNumber: name from repository is $name")
        nameLiveData.value = name
    }
    fun getPhoneNumber(): MutableLiveData<String>? {
        if (filteredSms == null) {
            filteredSms = MutableLiveData<String>()
            return filteredSms
        }
        return filteredSms

    }

    fun addMessageToOutBox(
        msg: String,
        contactAddress: String

    ): String? {
        val time = repository?.addMessageToOutBox(msg, contactAddress)
        //save the id in the database so that we can use this id to change type of sms
        // from outbox to sent, only after successfully sending, and we need to delete the id  from
        // database after sending
        Log.d(TAG, "addMessageToOutBox: time $time")
        return time
    }

    fun moveToSent(time: String?, address: String?) = viewModelScope.launch {
        Log.d(TAG, "moveToSent: address $address")
        repository?.moveFromoutBoxToSent(time, address!!)

    }

    /**
     * send sms and manages outbox id
     */
    fun sendSms(
        msg: String,
        applicationContext: Context,
        contactAddress: String
    ) = viewModelScope.launch{
//        sendBroadcastReceiver =
//        deliveryBroadcastReceiver =

        var time = addMessageToOutBox(msg, contactAddress)!!

//        repository?.moveFromoutBoxToSent(id.toString(), contactAddress)
//        if(id!=-1){
//            smsDAODAO?.insert(SMSOutBox(id!!))
//        }
        Log.d(TAG, "sendSms: time $time")
        val SENT = "myhashcallersms"
        val DELIVERED = "SMS_DELIVERED"
//        addMessageToOutBox(msg, contactAddress)
        val sIntent = Intent(SENT)
        var extras = Bundle()
        extras.putString("date", time)
        extras.putString("address", contactAddress)
//        sIntent.putExtra(INTANT_SMS_BRECIEVER_ID, id)
//        sIntent.putExtra("addressNo", contactAddress)
//        repository.moveFromoutBoxToSent(id, contactAddress)
        sIntent.putExtras(extras)
//        Log.d(TAG, "sendSms: called")
//        sIntent.putExtra("date", time)
//        sIntent.putExtra("address", contactAddress)
        /**
         * !!IMPORTANT
         * PendingIntent.FLAG_ONE_SHOT, other wise the intent extras values does not update
         */
        var sendIntent: PendingIntent? = PendingIntent.getBroadcast(applicationContext, 0,sIntent , PendingIntent.FLAG_ONE_SHOT)
        var deliveryIntent: PendingIntent? = PendingIntent.getBroadcast(applicationContext, 0, Intent(DELIVERED), PendingIntent.FLAG_ONE_SHOT)

        applicationContext.registerReceiver(sendBroadcastReceiver, IntentFilter(SENT))

//        applicationContext.registerReceiver(deliveryBroadcastReceiver, IntentFilter(DELIVERED))

        val smsManager: SmsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(
            contactAddress,null , msg ,
            sendIntent, deliveryIntent
        )


    }
    fun unregister(){
        applicationContext?.unregisterReceiver(sendBroadcastReceiver)
//        applicationContext?.unregisterReceiver(deliveryBroadcastReceiver)
    }

    fun update() = viewModelScope.launch {
//        repository.upda
    }

    fun blockThisAddress(
        contactAddress: String,
        threadID: Long,
        spammerType: Int?,
        spammerCategory: Int
    )  = viewModelScope.launch {

       async {
           spamRepository?.report(ReportedUserDTo(contactAddress, " ",
               spammerType.toString(), spammerCategory.toString()
           ))
       }
    async {
        spamRepository?.save(SpammerInfo(null, contactAddress, spammerType!!, spammerCategory, threadID ))
    }

        /**
         * Todo I have to handle non network condition, retry request when the
         * network is available, or add to work manager
         */
    }

    /**
     * @param contactAddress
     * check if the conact address if in spamlist list
     */
    fun getblockedStatusOfThenumber(contactAddress: String)  = viewModelScope.launch{
        val data =spamRepository?.getSpammerInfo(contactAddress)
        blockedStatusOfThenumber = data
    }

    fun unblock(contactAddress: String)  = viewModelScope.launch{
        spamRepository?.delete(contactAddress)
    }


    companion object{
        private const val TAG ="__DialerViewModel"
    }
}
//class ContactsViewModel(application: Application): AndroidViewModel(application) {
//
//    val contacts =
//        ContactLiveData(application.applicationContext)
//
//}