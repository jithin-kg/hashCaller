package com.nibble.hashcaller.view.ui.settings

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivitySettingsBinding
import com.nibble.hashcaller.view.ui.MainActivityInjectorUtil
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.UserInfoViewModel
import com.nibble.hashcaller.view.ui.manageblock.BlockManageActivity
import com.nibble.hashcaller.view.ui.notifications.ManageNotificationsActivity
import com.nibble.hashcaller.view.ui.sms.individual.SMSIndividualInjectorUtil
import com.nibble.hashcaller.view.ui.sms.individual.SMSIndividualViewModel
import com.nibble.hashcaller.view.ui.sms.individual.util.SHARED_PREF_BLOCK_CONFIGURATIONS
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.android.synthetic.main.activity_block_manage.*
import kotlinx.android.synthetic.main.activity_settings.*


class SettingsActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding:ActivitySettingsBinding
    private lateinit var viewmodel: SettingsViewModel
    private lateinit var sharedUserInfoViewmodel: UserInfoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViewmodel()
        getUserInfo()

        initListeners()
        observeUserInfo()

    }
    private fun observeUserInfo() {
        sharedUserInfoViewmodel.userInfo.observe(this, Observer {
            if(it!=null){
                val fLetter = formatPhoneNumber(it.firstname)[0]. toString()
                binding.tvFirstLetterMain.text = fLetter
                binding.tvFullNameMain.text = "${it.firstname} ${it.lastName}"
            }
        })
    }
    private fun getUserInfo() {
        viewmodel.getUserInfo()
    }

    private fun initViewmodel() {
        viewmodel = ViewModelProvider(this, SettingsInjectorUtil.provideContactsViewModelFactory(this)).get(
            SettingsViewModel::class.java)
        sharedUserInfoViewmodel = ViewModelProvider(this, MainActivityInjectorUtil.provideUserInjectorUtil(this)).get(
            UserInfoViewModel::class.java
        )
    }


    private fun initListeners() {
        binding.imgBtnBackMain.setOnClickListener(this)
        binding.layoutManageBlocking.setOnClickListener(this)
        binding.layoutNotifications.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.layoutManageBlocking -> {
                startBlockManageActivity()
            }
            R.id.imgBtnBackMain -> {
                finish()
            }
            R.id.layoutNotifications -> {
                val intent = Intent(this, ManageNotificationsActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun startBlockManageActivity() {
        val intent = Intent(this, BlockManageActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }
}