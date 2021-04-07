package com.nibble.hashcaller.view.ui.notifications

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.CompoundButton
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.contacts.isReceiveNotificationForSpamCallEnabled
import com.nibble.hashcaller.view.ui.contacts.isReceiveNotificationForSpamSMSEnabled
import com.nibble.hashcaller.view.ui.contacts.writeBoolToSharedPref
import com.nibble.hashcaller.view.ui.sms.individual.util.IS_CALL_BLOCK_NOTIFICATION_ENABLED
import com.nibble.hashcaller.view.ui.sms.individual.util.IS_SMS_BLOCK_NOTIFICATION_ENABLED
import com.nibble.hashcaller.view.ui.sms.individual.util.SHARED_PREF_NOTIFICATOINS_CONFIGURATIONS
import kotlinx.android.synthetic.main.activity_manage_notifications.*


class ManageNotificationsActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {
    private var isRecieveCallNotificationEnabled = false
    private var isRecieveSMSNotificationEnabled = false
    private lateinit var sharedpreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_notifications)
        initListeners()
        getSharedPrefrenceValues()
        setToggleButton()

    }

    private fun initListeners() {
        switchCallBlkNotification.setOnCheckedChangeListener(this)
        switchSMSBlkNotifications.setOnCheckedChangeListener(this)
    }

    private fun setToggleButton() {
        switchCallBlkNotification.isChecked = isRecieveCallNotificationEnabled
        switchSMSBlkNotifications.isChecked = isRecieveSMSNotificationEnabled
    }

    override fun onPostResume() {
        super.onPostResume()
        setToggleButton()
    }
    private fun getSharedPrefrenceValues() {
        sharedpreferences = getSharedPreferences(SHARED_PREF_NOTIFICATOINS_CONFIGURATIONS, Context.MODE_PRIVATE) ?: return
       isRecieveCallNotificationEnabled = isReceiveNotificationForSpamCallEnabled()
       isRecieveSMSNotificationEnabled = isReceiveNotificationForSpamSMSEnabled()

    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when(buttonView?.id){
            R.id.switchCallBlkNotification ->{
                writeBoolToSharedPref(IS_CALL_BLOCK_NOTIFICATION_ENABLED,
                                   isChecked,
                                   SHARED_PREF_NOTIFICATOINS_CONFIGURATIONS
                                )
            }
            R.id.switchSMSBlkNotifications ->{
                writeBoolToSharedPref(IS_SMS_BLOCK_NOTIFICATION_ENABLED,
                                    isChecked,
                                SHARED_PREF_NOTIFICATOINS_CONFIGURATIONS
                                    )
            }
        }
    }
}