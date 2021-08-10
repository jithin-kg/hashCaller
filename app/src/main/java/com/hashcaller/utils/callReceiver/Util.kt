package com.hashcaller.utils.callReceiver

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.util.Log


object Util {
    private var  phoneNum = ""

    const val TAG = "__Util"
    /***
     * Called when incomming call comes
     *
     */
    fun scheduleIncommingJob(context: Context, numFromReceiver: String?) {
        Log.d(TAG, "scheduleIncommingJob: called")
        if(!numFromReceiver.isNullOrEmpty()){
            if(phoneNum!=numFromReceiver){
                Log.d(TAG, "scheduleIncommingJob: $phoneNum")
                IncommingCallJobService.setPhoneNumber(phoneNum)
                val serviceComponent = ComponentName(context, IncommingCallJobService::class.java)
                val builder = JobInfo.Builder(0, serviceComponent)
                builder.setMinimumLatency((1 * 1).toLong()) // wait at least
                builder.setOverrideDeadline((1 * 10).toLong()) // maximum delay
                //builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // require unmetered network
                //builder.setRequiresDeviceIdle(true); // device should be idle
                //builder.setRequiresCharging(false); // we don't care if the device is charging or not

                val jobScheduler: JobScheduler = context.getSystemService(JobScheduler::class.java)
                jobScheduler.schedule(builder.build())
            }
        }


    }

    /**
     * called when the call ends
     * @param phoneNumber can be null because TelephonyManager.EXTRA_INCOMING_NUMBER is deprecated
     * so, if null get number from call logs, or if we have screening role, get last called number
     * saved in local DB
     */
    fun scheduleCallFeedbackJob(context: Context, phoneNumber: String?) {
        CallFeedbackJobService.setPhoneNumber(phoneNumber!!)
        val serviceComponent = ComponentName(context, CallFeedbackJobService::class.java)
        val builder = JobInfo.Builder(0, serviceComponent)
        builder.setMinimumLatency((1 * 1).toLong()) // wait at least
        builder.setOverrideDeadline((1 * 10).toLong()) // maximum delay
        //builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
        val jobScheduler: JobScheduler = context.getSystemService(JobScheduler::class.java)
        jobScheduler.schedule(builder.build())
    }

    /**
     * To make phone number empty, because onReceive is called multiple times
     * for a single call in BroadcastReceiver, so the job get called multiple time
     */
    fun setPhoneNumInUtil(numFromReceiver: String){
        phoneNum = ""
    }

}

