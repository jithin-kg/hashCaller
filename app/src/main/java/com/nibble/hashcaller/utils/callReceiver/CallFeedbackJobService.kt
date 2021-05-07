package com.nibble.hashcaller.utils.callReceiver

import android.annotation.SuppressLint
import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log

class CallFeedbackJobService : JobService() {
    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(TAG, "onStartJob: ")
        CallFeedbackForegroundService.startService(this, "", phoneNum)

        jobFinished(params, false)
        return false
    }

    /**
     * onStopJob() is called by the system if the job is cancelled before being finished.
     */
    @SuppressLint("LongLogTag")
    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG, "onStopJob: ")
        return false // false ->indicates that job should not be restarted when stopped
    }
    
    companion object{
        fun setPhoneNumber(phoneNumber: String) {
             phoneNum = phoneNumber
        }
        var phoneNum: String = ""

        const val TAG ="__CallFeedbackJobService"
    }
}