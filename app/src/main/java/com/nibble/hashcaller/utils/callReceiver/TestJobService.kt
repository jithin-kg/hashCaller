package com.nibble.hashcaller.utils.callReceiver

import android.R
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nibble.hashcaller.Secrets
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.local.db.blocklist.BlockedLIstDao
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import com.nibble.hashcaller.utils.NotificationHelper
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.utils.internet.InternetChecker
import com.nibble.hashcaller.utils.notifications.HashCaller
import com.nibble.hashcaller.utils.notifications.HashCaller.Companion.NOTIFICATION_ID
import com.nibble.hashcaller.utils.notifications.tokeDataStore
import com.nibble.hashcaller.view.ui.MainActivity
import com.nibble.hashcaller.view.ui.contacts.isBlockNonContactsEnabled
import com.nibble.hashcaller.view.ui.contacts.utils.SPAM_THREASHOLD
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.*


/**
 * JobService to be scheduled by the JobScheduler.
 * start another service -> ForegroundService
 * https://www.vogella.com/tutorials/AndroidTaskScheduling/article.html
 * refer Services.java library file for notification information
 */


class TestJobService : JobService() {
    private lateinit var  searchRepository: SearchNetworkRepository

    override fun onStartJob(params: JobParameters): Boolean {

        ForegroundService.startService(this, "", phoneNum)
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        stopSelf();

        return false // false ->indicates that job should not be restarted when stopped
    }

    companion object {
        fun setPhoneNumber(phoneNumber: String) {
            phoneNum = phoneNumber
        }

        private const val TAG = "SyncService"
        var phoneNum: String = ""

    }
}