package com.nibble.hashcaller.view.ui.notifications

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.CompoundButton
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivityManageNotificationsBinding
import com.nibble.hashcaller.datastore.DataStoreInjectorUtil
import com.nibble.hashcaller.datastore.DataStoreViewmodel
import com.nibble.hashcaller.datastore.PreferencesKeys
import com.nibble.hashcaller.view.ui.contacts.isReceiveNotificationForSpamCallEnabled
import com.nibble.hashcaller.view.ui.contacts.isReceiveNotificationForSpamSMSEnabled
import com.nibble.hashcaller.view.ui.contacts.writeBoolToSharedPref
import com.nibble.hashcaller.view.ui.manageblock.BlockSettingsInjectorUtil
import com.nibble.hashcaller.view.ui.manageblock.BlockSettingsViewModel
import com.nibble.hashcaller.view.ui.sms.individual.util.IS_CALL_BLOCK_NOTIFICATION_ENABLED
import com.nibble.hashcaller.view.ui.sms.individual.util.IS_SMS_BLOCK_NOTIFICATION_ENABLED
import com.nibble.hashcaller.view.ui.sms.individual.util.SHARED_PREF_NOTIFICATOINS_CONFIGURATIONS
import kotlinx.android.synthetic.main.activity_manage_notifications.*


class ManageNotificationsActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener {
    private var isRecieveCallNotificationEnabled = false
    private var isRecieveSMSNotificationEnabled = false
//    private lateinit var sharedpreferences: SharedPreferences
    private lateinit var dataStoreViewmodel: DataStoreViewmodel
    private lateinit var binding:ActivityManageNotificationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initListeners()
        initViewmodel()
        getSharedPrefrenceValues()

    }
    private fun initViewmodel() {
        dataStoreViewmodel = ViewModelProvider(this, DataStoreInjectorUtil.providerViewmodelFactory(applicationContext)).get(DataStoreViewmodel::class.java)
    }

    private fun initListeners() {
        binding.switchCallBlkNotification.setOnCheckedChangeListener(this)
        binding.switchSMSBlkNotifications.setOnCheckedChangeListener(this)
    }



    override fun onPostResume() {
        super.onPostResume()
    }
    private fun getSharedPrefrenceValues() {
//        sharedpreferences = getSharedPreferences(SHARED_PREF_NOTIFICATOINS_CONFIGURATIONS, Context.MODE_PRIVATE) ?: return
//       isRecieveCallNotificationEnabled = isReceiveNotificationForSpamCallEnabled()
//       isRecieveSMSNotificationEnabled = isReceiveNotificationForSpamSMSEnabled()
        dataStoreViewmodel.getBoolean(PreferencesKeys.RCV_NOT_BLK_CALL).observe(this, Observer {
            binding.switchCallBlkNotification.isChecked = it
        })

        dataStoreViewmodel.getBoolean(PreferencesKeys.RCV_NOT_BLK_SMS).observe(this, Observer {
            binding.switchSMSBlkNotifications.isChecked = it
        })

    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when(buttonView?.id){
            R.id.switchCallBlkNotification ->{
                dataStoreViewmodel.setBoolean(PreferencesKeys.RCV_NOT_BLK_CALL, binding.switchCallBlkNotification.isChecked)
//                writeBoolToSharedPref(IS_CALL_BLOCK_NOTIFICATION_ENABLED,
//                                   isChecked,
//                                   SHARED_PREF_NOTIFICATOINS_CONFIGURATIONS
//                                )
            }
            R.id.switchSMSBlkNotifications ->{
                dataStoreViewmodel.setBoolean(PreferencesKeys.RCV_NOT_BLK_SMS, binding.switchSMSBlkNotifications.isChecked)


//                writeBoolToSharedPref(IS_SMS_BLOCK_NOTIFICATION_ENABLED,
//                                    isChecked,
//                                SHARED_PREF_NOTIFICATOINS_CONFIGURATIONS
//                                    )
            }
        }
    }
}