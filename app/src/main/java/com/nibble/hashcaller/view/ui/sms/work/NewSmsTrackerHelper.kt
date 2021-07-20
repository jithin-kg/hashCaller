package com.nibble.hashcaller.view.ui.sms.work

import android.util.Log
import com.nibble.hashcaller.Secrets
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServerDAO
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
    val SMSSendersInfoFromServerDAO: CallersInfoFromServerDAO?
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
                        var secret:String?  = smssender.contactAddress?.let { Secrets().managecipher(packageName, it) }
//                        secret = hashUsingArgon(secret)
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
        var numberToBeUploadedOfSize10:MutableList<String> =  mutableListOf()
        if(!smWithoutSendersInformation.isNullOrEmpty())
            if(smWithoutSendersInformation.size > 20)
             numberToBeUploadedOfSize10.addAll(smWithoutSendersInformation.slice(0..9))
            else
                numberToBeUploadedOfSize10.addAll(smWithoutSendersInformation)

        return numberToBeUploadedOfSize10

    }

    /**
     * @param informationReceivedDate : date at which the data is inserted in db
     * @param limit : number of day in which a lookup for the current number should perform
     */
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