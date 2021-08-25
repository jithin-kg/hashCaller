package com.hashcaller.app.view.ui.call.dialer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PhoneNumber : ViewModel() {
    private var phoneNumber: MutableLiveData<String>? = null

    //    public void setPhoneNumber(String number){
    //       phoneNumber = new MutableLiveData<>();
    //        phoneNumber.setValue(number);
    //    }
    fun getPhoneNumber(): MutableLiveData<String>? {
        if (phoneNumber == null) {
            phoneNumber = MutableLiveData<String>()
            return phoneNumber
        }
        return phoneNumber
    }
}