package com.hashcaller.app.utils.callReceiver

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log


class MyJobService : JobService() {
    var isWorking = false
    var jobCancelled = false

    // Called by the Android system when it's time to run the job
    override fun onStartJob(jobParameters: JobParameters): Boolean {
        Log.d(TAG, "Job started!")
        isWorking = true
        // We need 'jobParameters' so we can call 'jobFinished'
        startWorkOnNewThread(jobParameters) // Services do NOT run on a separate thread
        return isWorking
    }

    private fun startWorkOnNewThread(jobParameters: JobParameters) {
        Thread { doWork(jobParameters) }.start()
    }

    private fun doWork(jobParameters: JobParameters) {
        // 10 seconds of 'working' (1000*10ms)
        for (i in 0..999) {
            // If the job has been cancelled, stop working; the job will be rescheduled.
            if (jobCancelled) return
            try {
                Thread.sleep(10)
            } catch (e: Exception) {
            }
        }
        Log.d(TAG, "Job finished!")
        isWorking = false
        val needsReschedule = false
        jobFinished(jobParameters, needsReschedule)
    }

    // Called if the job was cancelled before being finished
    override fun onStopJob(jobParameters: JobParameters): Boolean {
        Log.d(TAG, "Job cancelled before being completed.")
        jobCancelled = true
        val needsReschedule = isWorking
        jobFinished(jobParameters, needsReschedule)
        return needsReschedule
    }

    companion object {
        private val TAG = "__MyJobService"
    }
}