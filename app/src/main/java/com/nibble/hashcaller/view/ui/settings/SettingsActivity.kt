package com.nibble.hashcaller.view.ui.settings

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
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
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.android.synthetic.main.activity_block_manage.*
import kotlinx.android.synthetic.main.activity_settings.*
import java.util.*


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
        sharedUserInfoViewmodel.userInfo.observe(this, Observer {
            if (it != null) {
                val fLetter = formatPhoneNumber(it.firstname)[0].toString()
                binding.tvFirstLetterMain.text = fLetter
                binding.tvFullNameMain.text = "${it.firstname} ${it.lastName}"

//                val base64String =
//                    "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAIAAAACACAYAAADDPmHLAA..."
                val base64String = it.photoURI
                val decodedString: ByteArray =
                    android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                val decodedByte =
                    BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                binding.imgViewAvatarMain.setImageBitmap(decodedByte)

//                val base64Image = base64String.split(",".toRegex()).toTypedArray()[1]
//
//                val decodedString: ByteArray = android.util.Base64.decode(
//                    base64Image,
//                    android.util.Base64.DEFAULT
//                )
//                val decodedByte =
//                    BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
//                binding.imgViewAvatarMain.setImageBitmap(decodedByte)
                Log.d(TAG, "observeUserInfo: $decodedByte")
            }
        })
    }
    private fun getUserInfo() {
        viewmodel.getUserInfo()
    }

    private fun initViewmodel() {
        viewmodel = ViewModelProvider(
            this, SettingsInjectorUtil.provideContactsViewModelFactory(
                this
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