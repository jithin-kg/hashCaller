package com.nibble.hashcaller.view.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivitySettingsBinding
import com.nibble.hashcaller.view.ui.MainActivityInjectorUtil
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.UserInfoViewModel
import com.nibble.hashcaller.view.ui.manageblock.BlockManageActivity
import com.nibble.hashcaller.view.ui.notifications.ManageNotificationsActivity
import com.nibble.hashcaller.view.ui.sms.individual.util.beInvisible
import com.nibble.hashcaller.view.ui.sms.individual.util.beVisible
import com.nibble.hashcaller.view.utils.getDecodedBytes
import com.nibble.hashcaller.work.formatPhoneNumber


class SettingsActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding:ActivitySettingsBinding
    private lateinit var viewmodel: SettingsViewModel
    private lateinit var sharedUserInfoViewmodel: UserInfoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViewmodel()
        getUserInfo()

        initListeners()
        observeUserInfo()

    }
    private fun observeUserInfo() {
        sharedUserInfoViewmodel.userInfoLivedata.observe(this, Observer {
            if (it != null) {
                val fLetter = formatPhoneNumber(it.firstname)[0].toString()
                binding.tvFirstLetterMain.text = fLetter
                binding.tvFullNameMain.text = "${it.firstname} ${it.lastName}"
                if(!it.photoURI.isNullOrEmpty()){
                    binding.imgViewAvatarMain.setImageBitmap(getDecodedBytes(it.photoURI))
                    binding.tvFirstLetterMain.beInvisible()
                }else{
                    binding.tvFirstLetterMain.beVisible()
                }
            }
        })
    }
    private fun getUserInfo() {
        viewmodel.getUserInfo()
    }

    private fun initViewmodel() {
        viewmodel = ViewModelProvider(
            this, SettingsInjectorUtil.provideContactsViewModelFactory(
                applicationContext
            )
        ).get(
            SettingsViewModel::class.java
        )
        sharedUserInfoViewmodel = ViewModelProvider(
            this, MainActivityInjectorUtil.provideUserInjectorUtil(
                this
            )
        ).get(
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
    companion object{
        const val TAG = "__SettingsActivity"
    }
}