package com.nibble.hashcaller.network.user

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.nibble.hashcaller.network.contact.NetWorkResponse
import com.nibble.hashcaller.repository.user.UserInfoDTO
import com.nibble.hashcaller.view.ui.MainActivity
import com.nibble.hashcaller.view.ui.auth.GetInitialUserInfoActivity
import com.nibble.hashcaller.view.ui.auth.viewmodel.UserInfoViewModel
import retrofit2.Response

class UserUploadHelper(
    private val userInfoViewModel: UserInfoViewModel,
    private val owner: LifecycleOwner,
    private val applicationContext: Context
) {

    fun upload(userInfo: UserInfoDTO) {
        userInfoViewModel.upload(userInfo).observe(owner, Observer {
            it?.let { resource: Resource<Response<NetWorkResponse>?> ->
                val resMessage = resource.data?.body()?.message
                when (resource.status) {

                    Status.SUCCESS -> {
                        if (resMessage.equals(EUserResponse.NO_SUCH_USER)) {
                            Log.d(TAG, "checkIfNewUser: no such user")
                            //This is a new user
                            val i = Intent(applicationContext, GetInitialUserInfoActivity::class.java)
                            applicationContext.startActivity(i)

                        }else if(resMessage.equals(EUserResponse.EXISTING_USER)){
                            Log.d(TAG, "upload: user already exist")
                            val i  = Intent(applicationContext, MainActivity::class.java)
                            applicationContext.startActivity(i)
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
companion object{
    const val TAG = "__UserUploadHelper"
}

}