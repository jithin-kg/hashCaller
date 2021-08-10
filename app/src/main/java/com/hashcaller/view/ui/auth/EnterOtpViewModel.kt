package com.hashcaller.view.ui.auth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EnterOtpViewModel : ViewModel() {
    var captchRequest:MutableLiveData<Boolean> = MutableLiveData(false)
    init {

    }

    fun sendToken(token: String) = viewModelScope.launch {
        withContext(Dispatchers.IO){
            val retrofitService = RetrofitClientTest.createaService(ICaptchaService::class.java)
            val response =
                retrofitService.sendToken(Data(token))
            if(response!=null){
                captchRequest.value = true
            }
        }

    }
}

