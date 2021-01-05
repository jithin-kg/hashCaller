package com.nibble.hashcaller.view.ui.auth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nibble.hashcaller.network.RetrofitClient
import kotlinx.coroutines.launch

class EnterOtpViewModel : ViewModel() {
    var captchRequest:MutableLiveData<Boolean> = MutableLiveData(false)
    init {

    }

    fun sendToken(token: String) = viewModelScope.launch {
        val retrofitService = RetrofitClientTest.createaService(ICaptchaService::class.java)
        val response =
            retrofitService.sendToken(Data(token))
        if(response!=null){
            captchRequest.value = true
        }
    }
}

