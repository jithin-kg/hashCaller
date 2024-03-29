package com.hashcaller.app.view.ui.sms.individual

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.telephony.SmsManager
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.*
import com.klinker.android.send_message.Message
import com.klinker.android.send_message.Settings
import com.klinker.android.send_message.Transaction
import com.hashcaller.app.local.db.blocklist.SpammerInfo
import com.hashcaller.app.local.db.sms.SmsOutboxListDAO
import com.hashcaller.app.network.spam.ReportedUserDTo
import com.hashcaller.app.repository.spam.SpamNetworkRepository
import com.hashcaller.app.utils.SmsStatusDeliveredReceiver
import com.hashcaller.app.utils.SmsStatusSentReceiver
import com.hashcaller.app.view.ui.sms.individual.util.*
import com.hashcaller.app.view.ui.sms.individual.util.IndividualMarkedItemHandler.addToMarkedViews
import com.hashcaller.app.view.ui.sms.individual.util.IndividualMarkedItemHandler.addTomarkedItemsById
import com.hashcaller.app.view.ui.sms.individual.util.IndividualMarkedItemHandler.getMarkedViews
import com.hashcaller.app.view.ui.sms.individual.util.IndividualMarkedItemHandler.isMarkedViewsEmpty
import com.hashcaller.app.view.ui.sms.util.SMS
import com.hashcaller.app.view.ui.sms.util.SMSLocalRepository
import com.hashcaller.app.work.formatPhoneNumber
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*


/**
 * Created by Jithin KG on 22,July,2020
 */
class SMSIndividualViewModel(
    val SMS: SMSIndividualLiveData,
    private val repository: SMSLocalRepository?,
    private val smsDAODAO: SmsOutboxListDAO?,
    private val spamRepository: SpamNetworkRepository?,
    private val smsLocalRepository: SMSLocalRepository
): ViewModel() {
//    public var blockedStatusOfThenumber:MutableList<SpammerInfo> =
//        mutableListOf<SpammerInfo>()
var  smsLiveData:MutableLiveData<MutableList<SMS>> = MutableLiveData()
var markedViewsLiveData:MutableLiveData<View> = MutableLiveData()
    val smsQuee:Queue<SMS> = LinkedList<SMS>()
    val availableSIMCards = ArrayList<SIMCard>()
     var currentSIMCardIndex = 0

    var nameLiveData: MutableLiveData<String> = MutableLiveData("")

    public var blockedStatusOfThenumber:LiveData<List<SpammerInfo>>?= null

    private var applicationContext:Context?=null
    private  var  sendBroadcastReceiver: BroadcastReceiver = SmsStatusSentReceiver()
    var noSimCardException: MutableLiveData<Boolean> = MutableLiveData(false)

//    private  var deliveryBroadcastReceiver: BroadcastReceiver = SmsStatusDeliveredReceiver()

    init {

    }
     var filteredSms: MutableLiveData<String>? = null

    @SuppressLint("LongLogTag")
    fun getContactInfoForNumber(pno:String) = viewModelScope.launch {
        var name = repository?.getNameForNumberFromCprovider(pno)
        Log.d(TAG, "getContactInfoForNumber: name from repository is $name")
        if(name.isNullOrEmpty()){
          val infoFromDb =   async { repository!!.getContactInfoFRomDB(pno) }.await()
            if(infoFromDb!=null){
                name = infoFromDb
            }
        }
        nameLiveData.value = name
    }
    fun getPhoneNumber(): MutableLiveData<String>? {
        if (filteredSms == null) {
            filteredSms = MutableLiveData<String>()
            return filteredSms
        }
        return filteredSms

    }

    @SuppressLint("LongLogTag")
    fun addMessageToOutBox(
        msg: String,
        contactAddress: String

    ): String? {
        var time :String? = ""
        viewModelScope.launch {
             time = repository?.addMessageToOutBox(msg, contactAddress)

        }
        //save the id in the database so that we can use this id to change type of sms
        // from outbox to sent, only after successfully sending, and we need to delete the id  from
        // database after sending
        Log.d(TAG, "addMessageToOutBox: time $time")
        return time
    }

    @SuppressLint("LongLogTag")
    fun moveToSent(time: String?, address: String?) = viewModelScope.launch {
        Log.d(TAG, "moveToSent: address $address")
        repository?.moveFromoutBoxToSent(time, address!!)

    }

    /**
     * send sms and manages outbox id
     */
    @SuppressLint("LongLogTag")
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
        spammerType: Int?
    )  = viewModelScope.launch {

        async {
//            spamRepository?.save(SpammerInfo(null, contactAddress, spammerType!!, spammerCategory, threadID ))
           smsLocalRepository.saveSpamReportedByUser(contactAddress, threadID, spammerType)
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

    @Throws(IndexOutOfBoundsException::class)
    fun sendSmsToClient(
        smsObj: SMS,
        individualSMSActivity: IndividualSMSActivity,
        threadID: Long,
        phoneNum: String?
    ) = viewModelScope.launch {

        try {
            smsQuee.add(smsObj)
            for (item in smsQuee){

                val settings = Settings()
                settings.useSystemSending = true;
                settings.deliveryReports = true //it is importatnt to set this for the sms delivered status
                val msg = item!!.msgString

                //chosing sim card
                val simId = availableSIMCards.get(currentSIMCardIndex).subscriptionId
                if(simId != null){
                    settings.subscriptionId = simId
                }
                val transaction = Transaction(individualSMSActivity, settings)
                val message = Message(msg, phoneNum)
                val smsSentIntent = Intent(individualSMSActivity, SmsStatusSentReceiver::class.java)
                val deliveredIntent = Intent(individualSMSActivity, SmsStatusDeliveredReceiver::class.java)
                transaction.setExplicitBroadcastForSentSms(smsSentIntent)
                transaction.setExplicitBroadcastForDeliveredSms(deliveredIntent)

                transaction.sendNewMessage(message, threadID)
                smsLiveData.value!!.add(smsObj)
                smsLiveData.value = smsLiveData.value
                smsQuee.remove()
                delay(5000)
            }
        }catch (e:Exception){
            when(e){
                is IndexOutOfBoundsException->{
                    noSimCardException.value = true
                }
            }
        }




        }

    @Throws(IndexOutOfBoundsException::class)
    private suspend fun send(
        individualSMSActivity: IndividualSMSActivity,
        phoneNum: String?,
        threadID: Long
    ) {
        //TODO ADD resend sms,for failed sms
        for (item in smsQuee){

            val settings = Settings()
            settings.useSystemSending = true;
            settings.deliveryReports = true //it is importatnt to set this for the sms delivered status
            val msg = item!!.msgString

            //chosing sim card
            val simId = availableSIMCards.get(currentSIMCardIndex).subscriptionId
            if(simId != null){
                settings.subscriptionId = simId
            }
            val transaction = Transaction(individualSMSActivity, settings)
            val message = Message(msg, phoneNum)
//        message.setImage(mBitmap);
            val smsSentIntent = Intent(individualSMSActivity, SmsStatusSentReceiver::class.java)
            val deliveredIntent = Intent(individualSMSActivity, SmsStatusDeliveredReceiver::class.java)
            transaction.setExplicitBroadcastForSentSms(smsSentIntent)
            transaction.setExplicitBroadcastForDeliveredSms(deliveredIntent)

            transaction.sendNewMessage(message, threadID)
            smsQuee.remove()
            delay(5000)
    }


}

    /**
     * searching and creating spannable string for sms based on search query
     * @param query
     */
    fun searchForSMS(query: String?) = viewModelScope.launch {
        SearchUpAndDownHandler.clearStacks()
        SCROLL_TO_POSITION = null
        scrollToPositions.clear()

        val smsList = smsLocalRepository.fetchIndividualSMS(
            IndividualSMSActivity.contact
        )
        var mutableSMSLIst:MutableList<SMS> = mutableListOf()
        mutableSMSLIst.addAll(smsList)
        if(mutableSMSLIst !=null){

            mutableSMSLIst.forEachIndexed {index, sms->
                if(sms.msgString!=null){
                    if(sms.msgString!!.toLowerCase().contains(query!!.toLowerCase())){
//                        scrollToPositions.add(index)
                        //add positions to stack

                        SearchUpAndDownHandler.addToStackOne( index)

                        val msgStr = sms.msgString
                        val spannableStringBuilder =
                            SpannableStringBuilder(msgStr)

                        val startPos = sms.msgString!!.toLowerCase().indexOf(query.toLowerCase())
                        val endPos = startPos + query!!.length
                        val yellow = BackgroundColorSpan(Color.YELLOW)

                        spannableStringBuilder.setSpan(
                            yellow,
                            startPos,
                            endPos,
                            Spanned.SPAN_EXCLUSIVE_INCLUSIVE
                        )
                        val forgroundColor = ForegroundColorSpan(Color.BLACK)
                        spannableStringBuilder.setSpan(forgroundColor, startPos, endPos, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
                        sms.msg = spannableStringBuilder
                        if(SCROLL_TO_POSITION == null)
                        SCROLL_TO_POSITION = index
                    }else{
                        val msgStr = sms.msgString
                        val spannableStringBuilder =
                            SpannableStringBuilder(msgStr)

                        sms.msg = spannableStringBuilder
                    }
                }


            }
            smsLiveData.value = mutableSMSLIst

        }
        if(mutableSMSLIst.isEmpty())
        Toast.makeText(this@SMSIndividualViewModel.applicationContext, "Not found", Toast.LENGTH_SHORT).show()
    }

    fun markItem(
        id: Long,
        view: View,
        pos: Int
    ):kotlinx.coroutines.flow.Flow<View> = flow {
        addTomarkedItemsById(id)
        addToMarkedViews(view)
       if(!isMarkedViewsEmpty()){
           for(view in getMarkedViews()){
//               markedViewsLiveData.value = view
               emit(view)
           }
       }

//        markItemsInView(resources)

    }

//    private fun markItemsInView(resources: Resources) {
//        if(!isMarkedViewsEmpty()){
//            for(view in getMarkedViews() ){
//                ContextCompat.getColor(this, R.color.numbersInnerTextColor)
//               val view = view.findViewById<ConstraintLayout>(R.id.layoutSMSReceivedItem)
//                view.setBackgroundColor(resources.getColor(R.color.numbersInnerTextColor))
//
//            }
//        }
//
//    }

    /**
     * function to create and maintain/update a hashmap of thread Ids of sms
     * when an sms is long pressed
     */
    fun setHashMap(smslist: MutableList<SMS>) {

    }

    fun markAsRead(contactAddress: String) = viewModelScope.launch {
        repository?.marAsReadInDB(contactAddress)
        repository?.markSMSAsRead(contactAddress)

    }


    companion object{
        private const val TAG ="__SMSIndividualViewModel"
    }
}
