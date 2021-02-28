package com.nibble.hashcaller.view.ui.auth.getinitialInfos

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.R
import com.nibble.hashcaller.network.contact.NetWorkResponse
import com.nibble.hashcaller.network.user.EUserResponse
import com.nibble.hashcaller.network.user.Resource
import com.nibble.hashcaller.network.user.Status
import com.nibble.hashcaller.network.user.UserUploadHelper
import com.nibble.hashcaller.repository.user.UserInfoDTO
import com.nibble.hashcaller.view.ui.MainActivity
import com.nibble.hashcaller.view.ui.contacts.utils.SHARED_PREFERENCE_TOKEN_NAME
import kotlinx.android.synthetic.main.activity_get_initial_user_info.*
import retrofit2.Response

class GetInitialUserInfoActivity : AppCompatActivity() , View.OnClickListener{
    private lateinit var sharedPreferences: SharedPreferences
    private val SAMPLE_ALIAS = "SOMETHINGNEW"
    private lateinit var userInfoViewModel: UserInfoViewModel


    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_get_initial_user_info)

        btnUserContinue.setOnClickListener(this)
        userInfoViewModel = ViewModelProvider(this, UserInfoInjectorUtil.provideUserInjectorUtil(this)).get(
            UserInfoViewModel::class.java)



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



        upload(userInfo)
    }


}



    @SuppressLint("LongLogTag")
    private fun upload(userInfo: UserInfoDTO) {
        userInfoViewModel.upload(userInfo).observe(this, Observer {
            it?.let { resource: Resource<Response<NetWorkResponse>?> ->
                val resMessage = resource.data?.body()?.message
                when (resource.status) {

                    Status.SUCCESS -> {

                        if (resMessage.equals(EUserResponse.NO_SUCH_USER)) { // there is no such user in server
                            Log.d(TAG, "checkIfNewUser: no such user")
                            //This is a new user
                            val i = Intent(applicationContext, GetInitialUserInfoActivity::class.java)
                            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            //set userLoggedIn = false in shared preference

                            saveToSharedPref(false)

                            applicationContext.startActivity(i)

                        }else if(resMessage.equals(EUserResponse.EXISTING_USER)){
                            Log.d(UserUploadHelper.TAG, "upload: user already exist")
//                            val i  = Intent(applicationContext, MainActivity::class.java)
//                            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                            //set userLogedIn = true in shared preferecce
                            saveToSharedPref(true)
                            val i  = Intent(this, MainActivity::class.java)
                            startActivity(i)
                            finish()

                        }
                        Log.d(TAG, "checkIfNewUser: success ${resource.data?.body()?.message}")
                    }
                    Status.LOADING -> {
                        Log.d(TAG, "checkIfNewUser: Loading")
                    }
                    else -> {
                        Log.d(TAG, "checkIfNewUser: else $resource")
                        Log.d(TAG, "checkIfNewUser:error ")
                    }


                }

            }
        })
    }

    private fun saveToSharedPref(b: Boolean) {
        sharedPreferences = applicationContext.getSharedPreferences(
            SHARED_PREFERENCE_TOKEN_NAME, Context.MODE_PRIVATE)

        val editor = sharedPreferences.edit()
        editor.putBoolean("IS_LOGGEDIN", b)
        editor.commit()
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
        if( lastName.isEmpty() || lastName.length < 2){
            editTextLName.error = "Last name should contain at least 3 characters"
            isValid = false;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmail.error = "Enter a valid email"
            isValid = false;
        }
        return isValid
}

    companion object{
        const val TAG = "__GetInitialUserInfoActivity"
    }
}