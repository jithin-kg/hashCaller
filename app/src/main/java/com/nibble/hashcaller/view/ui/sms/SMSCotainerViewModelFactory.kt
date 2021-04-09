package com.nibble.hashcaller.view.ui.sms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServerDAO
import com.nibble.hashcaller.local.db.sms.mute.IMutedSendersDAO
import com.nibble.hashcaller.view.ui.sms.list.SMSLiveData2


/**
 * Created by Jithin KG on 29,July,2020
 */
class SMSCotainerViewModelFactory(
    private val SMSLiveData: SMSLiveData2?,
    private val repository: SMScontainerRepository?,
    private val SMSSendersInfoFromServerDAO: SMSSendersInfoFromServerDAO?,
    private val mutedSendersDAO: IMutedSendersDAO?
) :
    ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        //preparing view model

        return SmsContainerViewModel(this!!.SMSLiveData!!,
            repository,
            SMSSendersInfoFromServerDAO) as T
    }
}