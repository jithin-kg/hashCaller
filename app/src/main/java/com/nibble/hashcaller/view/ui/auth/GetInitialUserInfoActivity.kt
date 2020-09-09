package com.nibble.hashcaller.view.ui.auth

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.R
import com.nibble.hashcaller.repository.user.UserInfoDTO
import com.nibble.hashcaller.view.ui.auth.utils.UserInfoInjectorUtil
import com.nibble.hashcaller.view.ui.auth.viewmodel.UserInfoViewModel
import kotlinx.android.synthetic.main.activity_get_initial_user_info.*

class GetInitialUserInfoActivity : AppCompatActivity() , View.OnClickListener{
    private lateinit var sharedPreferences: SharedPreferences
    private val SAMPLE_ALIAS = "SOMETHINGNEW"
    private lateinit var userInfoViewModel:UserInfoViewModel


    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_get_initial_user_info)

        btnUserContinue.setOnClickListener(this)
        userInfoViewModel = ViewModelProvider(this, UserInfoInjectorUtil.provideUserInjectorUtil(this)).get(UserInfoViewModel::class.java)



    }

    companion object{

        const val TAG = "__GetInitialUserInfoActivity"

    }

    override fun onClick(v: View?) {
        when(v?.id){
            btnUserContinue.id ->{
                sendUserInfo()
            }
        }
    }

    private fun sendUserInfo() {
        var userInfo = UserInfoDTO()
        userInfo.firstName = editTextFName.text.toString()
        userInfo.lastName = editTextLName.text.toString()
        userInfo.email = editTextEmail.text.toString()
        userInfoViewModel.upload(userInfo)
    }
}