package com.nibble.hashcaller.utils.callReceiver

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.util.Log
import com.nibble.hashcaller.utils.callReceiver.Util.scheduleJob


/**
 * JobService to be scheduled by the JobScheduler.
 * start another service
 * https://www.vogella.com/tutorials/AndroidTaskScheduling/article.html
 */
class TestJobService : JobService() {
    override fun onStartJob(params: JobParameters): Boolean {
        val service = Intent(applicationContext, ForegroundService::class.java)
        applicationContext.startService(service)
//        scheduleJob(applicationContext) // reschedule the job
        Log.d(TAG, "onStartJob: ")


        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        return false
    }

    companion object {
        private const val TAG = "SyncService"
    }
}