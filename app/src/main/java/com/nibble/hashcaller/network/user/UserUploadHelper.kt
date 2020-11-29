package com.nibble.hashcaller.network.user

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.liveData
import com.nibble.hashcaller.network.contact.NetWorkResponse
import com.nibble.hashcaller.repository.user.UserInfoDTO
import com.nibble.hashcaller.view.ui.MainActivity
import com.nibble.hashcaller.view.ui.SplashActivity
import com.nibble.hashcaller.view.ui.auth.GetInitialUserInfoActivity
import com.nibble.hashcaller.view.ui.auth.viewmodel.UserInfoViewModel
import com.nibble.hashcaller.view.ui.contacts.utils.SHAREDPREF_LOGGEDIN
import com.nibble.hashcaller.view.ui.contacts.utils.SHARED_PREFERENCE_TOKEN_NAME
import kotlinx.coroutines.Dispatchers
import retrofit2.Response

class UserUploadHelper(
    private val userInfoViewModel: UserInfoViewModel,
    private val owner: LifecycleOwner,
    private val applicationContext: Context

) {
    private lateinit var sharedPreferences: SharedPreferences

//    fun upload(userInfo: UserInfoDTO)  = liveData<Resource<Response<NetWorkResponse>>>(Dispatchers.IO) {
//
//        uploadData(userInfo)
//    }

    private fun uploadData(userInfo: UserInfoDTO) {

        userInfoViewModel.upload(userInfo).observe(owner, Observer {
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
                            Log.d(TAG, "upload: user already exist")
//                            val i  = Intent(applicationContext, MainActivity::class.java)
//                            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                            //set userLogedIn = true in shared preferecce
//                            saveToSharedPref(true)
//                            applicationContext.startActivity(i)

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

    companion object{
    const val TAG = "__UserUploadHelper"
}

}