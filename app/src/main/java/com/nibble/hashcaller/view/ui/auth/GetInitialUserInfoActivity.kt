package com.nibble.hashcaller.view.ui.auth

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.R
import com.nibble.hashcaller.network.user.UserUploadHelper
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

    @SuppressLint("LongLogTag")
    override fun onClick(v: View?) {
        Log.d(TAG, "onClick: ")
        when(v?.id){
            btnUserContinue.id ->{
                Log.d(TAG, "onClick: btn")
                sendUserInfo()
            }
        }
    }

    @SuppressLint("LongLogTag")
private fun sendUserInfo() {

    val firstName = editTextFName.text.toString().trim()
    val lastName = editTextLName.text.toString().trim()
    val email = editTextEmail.text.toString().trim()

    editTextFName.error = null
    editTextEmail.error = null
    editTextLName.error = null
    val isValid = validateInput(firstName, lastName, email);
    if(isValid){
        Log.d(TAG, "isvalid: ")
        var userInfo = UserInfoDTO()
        userInfo.firstName = firstName;
        userInfo.lastName =  lastName;
        userInfo.email = email;

        val helper = UserUploadHelper(userInfoViewModel, this, applicationContext)
        helper.upload(userInfo)
    }


}

    @SuppressLint("LongLogTag")
    private fun validateInput(firstName: String, lastName: String, email: String): Boolean {
        var isValid = true;
        if( email.isEmpty()){
            editTextEmail.error = "Enter a valid email"
            isValid = false;
        }
        if( firstName.isEmpty() || firstName.length < 3){
            editTextFName.error = "First name should contain at least 3 characters"
            isValid = false;
        }
        if( lastName.isEmpty() || lastName.length < 3){
            editTextLName.error = "Last name should contain at least 3 characters"
            isValid = false;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmail.error = "Enter a valid email"
            isValid = false;
        }
        return isValid
}

}