package com.hashcaller.app.view.ui.notifications

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.hashcaller.app.R
import com.hashcaller.app.databinding.ActivityManageNotificationsBinding
import com.hashcaller.app.datastore.DataStoreInjectorUtil
import com.hashcaller.app.datastore.DataStoreViewmodel
import com.hashcaller.app.datastore.PreferencesKeys


class ManageNotificationsActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener,
    View.OnClickListener {
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
//        binding.switchSMSBlkNotifications.setOnCheckedChangeListener(this)
        binding.imgBtnBacckNotificaions.setOnClickListener(this)
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

//        dataStoreViewmodel.getBoolean(PreferencesKeys.RCV_NOT_BLK_SMS).observe(this, Observer {
////            binding.switchSMSBlkNotifications.isChecked = it
//        })

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
//            R.id.switchSMSBlkNotifications ->{
//                dataStoreViewmodel.setBoolean(PreferencesKeys.RCV_NOT_BLK_SMS, binding.switchSMSBlkNotifications.isChecked)
//
//
////                writeBoolToSharedPref(IS_SMS_BLOCK_NOTIFICATION_ENABLED,
////                                    isChecked,
////                                SHARED_PREF_NOTIFICATOINS_CONFIGURATIONS
////                                    )
//            }
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.imgBtnBacckNotificaions -> {
                finishAfterTransition()
            }
        }
    }

    override fun onBackPressed() {
        finishAfterTransition()
    }
}