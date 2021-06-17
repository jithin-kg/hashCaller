package com.nibble.hashcaller.utils.notifications

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import com.nibble.hashcaller.datastore.PreferencesKeys
import com.nibble.hashcaller.view.ui.auth.ActivityPhoneAuth
import com.nibble.hashcaller.view.ui.contacts.utils.USER_PREFERENCES_NAME
import com.nibble.hashcaller.view.ui.contacts.utils.USER_PREFERENCES_BLOCK
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.signal.argon2.Argon2
import org.signal.argon2.MemoryCost
import org.signal.argon2.Type
import org.signal.argon2.Version

/**
 * This class which extends from Application represents our whole application with all its
 * components like Activities and services, if we want to setup at the start of our application
 * not for in a particular place like activity this is the right place to do it.
 *
 * !!!!Warning you have to re install the application when you make change in
 * channel to take effect
 */
val Context.tokeDataStore by preferencesDataStore(name = USER_PREFERENCES_NAME)
val Context.blockPreferencesDataStore by preferencesDataStore(name = USER_PREFERENCES_BLOCK)

class HashCaller : Application(){
    /**This will be called before any start of activity, right when our app will start.
     * This is the perfect place to setup our channels
     */

    companion object{
        private lateinit var activeFragment:Fragment

        fun setActiveFragment(fragment: Fragment){
            activeFragment = fragment
        }
        fun getActiveFragment(): Fragment {
            return activeFragment
        }
        const val CHANNEL_1_ID = "channel1";
        const val CHANNEL_2_ID = "channel2";

        //CALL SERVICE
        const val CHANNEL_3_CALL_SERVICE_ID ="chanel3"
        const val NOTIFICATION_CHANNEL_NAME = "callerId"
        const val NOTIFICATION_ID = 1
        private lateinit var argon2:Argon2
        fun getArgon2(): Argon2 {
            return argon2
        }


//          var callFragment: CallFragment? = null
//          var messagesFragment: SMSContainerFragment? = null
//          var blockConfigFragment: BlockConfigFragment? = null
//          var contactFragment: ContactsContainerFragment? = null
//          var dialerFragment: DialerFragment? = null
//          var ft: FragmentTransaction? = null

    }


    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        createNotificationChannels()
        instantiateAllFragment()
        buildArgon()
    }

    private fun buildArgon() {
         argon2 = Argon2.Builder(Version.V13)
            .type(Type.Argon2id)
            .memoryCost(MemoryCost.MiB(32))
            .parallelism(1)
            .iterations(2)
            .build()
    }



    private fun instantiateAllFragment() {

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

            val channel3 = NotificationChannel(CHANNEL_3_CALL_SERVICE_ID, NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW)
            channel3.description = "Caller id active"

            val notificationManager = getSystemService(NotificationManager::class.java)

            notificationManager.createNotificationChannel(channel1)
            notificationManager.createNotificationChannel(channel2)
            notificationManager.createNotificationChannel(channel3)



         }
    }


}