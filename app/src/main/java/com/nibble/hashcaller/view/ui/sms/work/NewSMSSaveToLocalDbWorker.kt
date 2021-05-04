package com.nibble.hashcaller.view.ui.sms.work

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServer
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServerDAO
import com.nibble.hashcaller.utils.notifications.tokeDataStore
import com.nibble.hashcaller.view.ui.sms.util.SMSLocalRepository
import java.lang.Exception

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
    private val smssendersInfoDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsSenderInfoFromServerDAO() }
    private val mutedSendersDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).mutedSendersDAO() }
    val smsThreadsDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsThreadsDAO() }

    private val sMSSendersInfoFromServerDAO: SMSSendersInfoFromServerDAO = HashCallerDatabase.getDatabaseInstance(context).smsSenderInfoFromServerDAO()
    @SuppressLint("LongLogTag")
    override suspend fun doWork(): Result {
        try {
            val smsrepoLocal = SMSLocalRepository(
                context,
                spamListDAO,
                smssendersInfoDAO,
                mutedSendersDAO,
                smsThreadsDAO,
                DataStoreRepository(context.tokeDataStore)
            ) // to get content provided sms
            val allsmsincontentProvider = smsrepoLocal.fetchSMSForLivedata(null, false)
            var sms : MutableList<SMSSendersInfoFromServer> = mutableListOf()
            return Result.success()
        }catch (e:Exception){
            return Result.retry()
            Log.d(TAG, "doWork: ")
        }

    }
    companion object {const val TAG = "__NewSMSSaveToLocalDbWorker"}
}