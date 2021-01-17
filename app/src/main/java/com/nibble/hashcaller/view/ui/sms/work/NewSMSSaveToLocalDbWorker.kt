package com.nibble.hashcaller.view.ui.sms.work

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nibble.hashcaller.Secrets
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServer
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServerDAO
import com.nibble.hashcaller.view.ui.sms.util.SMSLocalRepository
import java.lang.Exception
import java.util.*

/**
 * class to save new Sms senders numbers in local DB (SMSSendersInfoFromServer) table,
 * so that I can search for these number in server and
 * get information for these unknown sender
 * This should be called before SmsHashedNumUploadWorker, because then only the SMSSendersInfoFromServer table
 * have sms senders number that is to be uploaded to server
 */
class NewSMSSaveToLocalDbWorker (private val context: Context, private val params: WorkerParameters) :
    CoroutineWorker(context, params){
    private val spamListDAO = HashCallerDatabase.getDatabaseInstance(context).spamListDAO()
    private val sMSSendersInfoFromServerDAO: SMSSendersInfoFromServerDAO = HashCallerDatabase.getDatabaseInstance(context).spammerInfoFromServerDAO()
    @SuppressLint("LongLogTag")
    override suspend fun doWork(): Result {
        try {
            val smsrepoLocal = SMSLocalRepository(context, spamListDAO) // to get content provided sms
            val allsmsincontentProvider = smsrepoLocal.fetchSMS(null)
            var sms : MutableList<SMSSendersInfoFromServer> = mutableListOf()


//            for (sms in allsms){
//                val secret = Secrets().managecipher(context?.packageName!!, sms.addressString.toString()) // encoding the
//                //phone number using my secret algorithm
//                if(!sms.addressString.isNullOrEmpty())
//                    if(sms.addressString!!.length >4){
//                        var firstFiveDigitsOfAddress = sms.addressString!!.substring(0, 4)
//                        val smsSendersInfoFromServerObj = SMSSendersInfoFromServer(null, secret,
//                            -1, sms.addressString!!, Date(),
//                            -1L, firstFiveDigitsOfAddress)
//
//                    }
////
//            }
            return Result.success()
        }catch (e:Exception){
            return Result.retry()
            Log.d(TAG, "doWork: ")
        }

    }
    companion object {const val TAG = "__NewSMSSaveToLocalDbWorker"}
}