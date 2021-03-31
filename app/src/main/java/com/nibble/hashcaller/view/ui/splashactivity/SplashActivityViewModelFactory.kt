package com.nibble.hashcaller.view.ui.splashactivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServerDAO
import com.nibble.hashcaller.local.db.sms.mute.IMutedSendersDAO
import com.nibble.hashcaller.view.ui.sms.list.SMSLiveData


/**
 * Created by Jithin KG on 29,July,2020
 */
class SplashActivityViewModelFactory(
  private val reposistory:SplashActivityRepository
) :
    ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        //preparing view model

        return SplashActivityViewModel(reposistory) as T
    }
}