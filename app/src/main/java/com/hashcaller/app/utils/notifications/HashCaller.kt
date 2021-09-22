package com.hashcaller.app.utils.notifications

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.preferences.preferencesDataStore
import com.hashcaller.app.datastore.DataStoreRepository
import com.hashcaller.app.datastore.PreferencesKeys
import com.hashcaller.app.view.ui.contacts.utils.USER_PREFERENCES_NAME
import com.hashcaller.app.view.ui.contacts.utils.USER_PREFERENCES_BLOCK
import kotlinx.coroutines.coroutineScope

/**
 * This class which extends from Application represents our whole application with all its
 * components like Activities and services, if we want to setup at the start of our application
 * not for in a particular place like activity this is the right place to do it.
 *
 * !!!!Warning you have to re install the application when you make change in
 * notification channel to take effect
 */
val Context.tokeDataStore by preferencesDataStore(name = USER_PREFERENCES_NAME)

class HashCaller : Application(){
    /**This will be called before any start of activity, right when our app will start.
     * This is the perfect place to setup our channels
     */

    companion object{


        const val CHANNEL_1_ID = "channel1";
        const val CHANNEL_2_ID = "channel2";

        //CALL SERVICE
        const val CHANNEL_3_CALL_SERVICE_ID ="chanel3"
        const val NOTIFICATION_CHANNEL_NAME = "callerId"
        const val NOTIFICATION_ID = 1

        var THRESHOLD = 112


    }


    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        createNotificationChannels()



//        https://www.freecodecamp.org/news/how-to-log-more-efficiently-with-timber-a3f41b193940/
//        if(BuildConfig.DEBUG){
//            //timber only works in debug build
//            Timber.plant(Timber.DebugTree())
//        }
    }







    private fun createNotificationChannels() {

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O ){
             val channel1 = NotificationChannel(
                 CHANNEL_1_ID,
                 "Incoming messages",
                    NotificationManager.IMPORTANCE_HIGH)
             channel1.description = "Notification for incoming SMS"
            val channel2 = NotificationChannel(
                CHANNEL_2_ID,
                "Blocked calls",
                NotificationManager.IMPORTANCE_HIGH)

            channel2.description = "Notification for blocked calls"

            val notificationManager = getSystemService(NotificationManager::class.java)

            notificationManager.createNotificationChannel(channel1)
            notificationManager.createNotificationChannel(channel2)
         }
    }


}