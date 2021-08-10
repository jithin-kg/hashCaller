package com.hashcaller.utils.callReceiver

import android.annotation.SuppressLint
import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log


/**
 * JobService to be scheduled by the JobScheduler.
 * start another service -> ForegroundService
 * https://www.vogella.com/tutorials/AndroidTaskScheduling/article.html
 * refer Services.java library file for notification information
 */


class IncommingCallJobService : JobService() {

    /**
     * jobFinished() is not a method you override, and the system won’t call it.
     * That’s because you need to be the one to call this method once your service or
     * thread has finished working on the job. If your onStartJob() method kicked off another thread
     * and then returned true, you’ll need to call this method from that thread when the work is complete.
     * This is how to system knows that it can safely release your wakelock.
     * If you forget to call jobFinished(), your app is going to look pretty guilty in the battery stats lineup
     * https://medium.com/google-developers/scheduling-jobs-like-a-pro-with-jobscheduler-286ef8510129
     */
    @SuppressLint("LongLogTag")
    override fun onStartJob(params: JobParameters): Boolean {

        IncommingCallForegroundService.startService(this, "", phoneNum)
//        Log.d(TAG, "onStartJob: ")
        // it is important to call this function so that system can cancel this job , the foreground service
        //will work on its ow
        Log.d(TAG, "onStartJob: ")

        jobFinished(params, false)
        return false
    }

    /**
     * onStopJob() is called by the system if the job is cancelled before being finished.
     */
    @SuppressLint("LongLogTag")
    override fun onStopJob(params: JobParameters): Boolean {
        Log.d(TAG, "onStopJob: ")
        return false // false ->indicates that job should not be restarted when stopped
    }



    companion object {
        fun setPhoneNumber(phoneNumber: String) {
            phoneNum = phoneNumber
        }


        private const val TAG = "__IncommingCallJobService"
        var phoneNum: String = ""

    }
}