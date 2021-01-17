package com.nibble.hashcaller.view.ui.sms.work

import android.util.Log
import com.nibble.hashcaller.Secrets
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServerDAO
import com.nibble.hashcaller.view.ui.sms.SMScontainerRepository
import com.nibble.hashcaller.view.ui.sms.util.SMS
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * helps to get new sms, ie  sms in the inbox without sender info
 * in the local DB.
 *
 */
class NewSmsTrackerHelper(
    val repository: SMScontainerRepository?,
    val SMSSendersInfoFromServerDAO: SMSSendersInfoFromServerDAO?
) {
    /**
     * @param smslist:list of all sms from content provider
     * @param packageName: packagename required for c++ to hash
     */
    suspend fun getUnknownNumbersList(smslist: List<SMS>?,
                                      packageName: String): List<String> {
        var smWithoutSendersInformation:MutableList<String> = mutableListOf()

        Log.d(TAG, "getInformationForTheseNumbers: ")
        // SMS - minus - spmmersInfoListFromLocalDb -> data to be send
        //todo move this whole logic into workmanager, because this takes time
        val smsSendersfromLocalDb =  repository!!.geSmsSendersStoredInLocalDB()
        for(smssender in smsSendersfromLocalDb){
            if(smssender.spamReportCount == -1L ){
                //we havent got the details from server
                //so we need to check teh last sync date, if the last sync date - currentdate is >2  or
                //date value = empty or " ", ie we have not yet cross checked in server
                //we need to upload this number
                val date: String =
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                    if(isCurrentDateAndPrevDateisGreaterThanLimit(smssender.informationReceivedDate, 3)){
                        val secret = smssender.contactAddress?.let { Secrets().managecipher(packageName, it) }
                        secret?.let { smWithoutSendersInformation.add(it) }

                    }
            }else{
                //we already got the info of that number from server ,
                // if the today - prevSyndate >7 we need to update the info of count from server
                if(isCurrentDateAndPrevDateisGreaterThanLimit(smssender.informationReceivedDate, 7)){
                    val secret = smssender.contactAddress?.let { Secrets().managecipher(packageName, it) }
                    secret?.let { smWithoutSendersInformation.add(it) }
                }
            }

        }
        val numberToBeUploadedOfSize10 = smWithoutSendersInformation.slice(0..9)


//        var  hashedPhoneNumbers:MutableList<String> = mutableListOf()
//        var phoneNumbersAvailableInlocalDB:MutableList<String> = mutableListOf()
//
//
//        if (smslist != null) {
//            for (sms in smslist){
//                val secret = sms.addressString?.let { Secrets().managecipher(packageName, it) }
//                val hashedNum = secret?.let { hashPhoneNum(it) }
//                hashedNum?.let { hashedPhoneNumbers.add(it) }
//            }
//        }
//        for (spammer in smsSendersfromLocalDb){
//            if(hashedPhoneNumbers.contains(spammer.contactAddress)){
//                spammer.contactAddress?.let { phoneNumbersAvailableInlocalDB.add(it) }
//            }
//        }
//        var numberToBeUploaded =  hashedPhoneNumbers - phoneNumbersAvailableInlocalDB
        //because sending more than 10 items will slow down server and increases load

//        val numberToBeUploadedOfSize10 = numberToBeUploaded.slice(0..9)

//        val obj = hashednums(numberToBeUploadedOfSize10)//object for transfering or dto
//        obj.hashedPhoneNum.addAll(numberToBeUploadedOfSize10)

//        if(!numberToBeUploadedOfSize10.isNullOrEmpty()){
//            //schedule work
//            val oneTimeWorkRequest = OneTimeWorkRequest.Builder(SmsHashedNumUploadWorker::class.java).build()
//            WorkManager.getInstance().enqueue(oneTimeWorkRequest)
//
//        }
        return numberToBeUploadedOfSize10

    }

    private fun isCurrentDateAndPrevDateisGreaterThanLimit(
        informationReceivedDate: Date,
        limit: Int
    ): Boolean {
        val today = Date()
        val miliSeconds: Long = today.getTime() - informationReceivedDate.getTime()
        val seconds = TimeUnit.MILLISECONDS.toSeconds(miliSeconds)
        val minute = seconds / 60
        val hour = minute / 60
        val days = hour / 24
        if(days > limit)
            return true
        return false
    }

    companion object{
        const val TAG = "__NewSmsTrackerHelper"
    }
}