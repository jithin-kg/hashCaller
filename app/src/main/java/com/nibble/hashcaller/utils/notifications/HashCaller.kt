package com.nibble.hashcaller.utils.notifications

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

/**
 * This class which extends from Application represents our whole application with all its
 * components like Activities and services, if we want to setup at the start of our application
 * not for in a particular place like activity this is the right place to do it.
 *
 * !!!!Warning you have to re install the application when you make change in
 * channel to take effect
 */
class HashCaller : Application(){
    /**This will be called before any start of activity, right when our app will start.
     * This is the perfect place to setup our channels
     */
    companion object{
        const val CHANNEL_1_ID = "channel1";
        const val CHANNEL_2_ID = "channel2";
    }


    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O ){
             val channel1 = NotificationChannel(
                 CHANNEL_1_ID,
                 "Incoming messages",
                    NotificationManager.IMPORTANCE_HIGH)
             channel1.description = "Channel 1 description"
            val channel2 = NotificationChannel(
                CHANNEL_2_ID,
                "This is channel 2",
                NotificationManager.IMPORTANCE_LOW)
            channel1.description = "Channel 2 description"

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel1)
            notificationManager.createNotificationChannel(channel2)



         }
    }
}