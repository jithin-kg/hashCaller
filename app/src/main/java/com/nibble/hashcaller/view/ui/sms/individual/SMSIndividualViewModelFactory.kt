package com.nibble.hashcaller.view.ui.sms.individual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.local.db.sms.SmsOutboxListDAO
import com.nibble.hashcaller.repository.spam.SpamNetworkRepository
import com.nibble.hashcaller.view.ui.sms.util.SMSLocalRepository


/**
 * Created by Jithin KG on 29,July,2020
 */
class SMSIndividualViewModelFactory(
    private val SMSLiveData: SMSIndividualLiveData?,
    private val repository: SMSLocalRepository?,
    private val smsDAODAO: SmsOutboxListDAO?,
    private val spamNetworkRepository: SpamNetworkRepository?,
    private val smsLocalRepository: SMSLocalRepository
) :
    ViewModelProvider.NewInstanceFactory(){
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        //preparing view model

        return SMSIndividualViewModel(this!!.SMSLiveData!!, repository,
            smsDAODAO, spamNetworkRepository, smsLocalRepository) as T
    }
}