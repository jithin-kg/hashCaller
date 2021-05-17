package com.nibble.hashcaller.view.ui.sms.work

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.utils.auth.TokenHelper
import com.nibble.hashcaller.utils.notifications.tokeDataStore
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServerDAO
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
    private val smssendersInfoDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callersInfoFromServerDAO() }
    private val mutedSendersDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).mutedSendersDAO() }
    val smsThreadsDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsThreadsDAO() }

    private val sMSSendersInfoFromServerDAO: CallersInfoFromServerDAO = HashCallerDatabase.getDatabaseInstance(context).callersInfoFromServerDAO()
    @SuppressLint("LongLogTag")
    override suspend fun doWork(): Result {
        try {
            val smsrepoLocal = SMSLocalRepository(
                context,
                spamListDAO,
                smssendersInfoDAO,
                mutedSendersDAO,
                smsThreadsDAO,
                DataStoreRepository(context.tokeDataStore),
                TokenHelper( FirebaseAuth.getInstance().currentUser)
            ) // to get content provided sms
            val allsmsincontentProvider = smsrepoLocal.fetchSMSForLivedata(null, false)
            var sms : MutableList<CallersInfoFromServerDAO> = mutableListOf()
            return Result.success()
        }catch (e:Exception){
            return Result.retry()
            Log.d(TAG, "doWork: ")
        }

    }
    companion object {const val TAG = "__NewSMSSaveToLocalDbWorker"}
}