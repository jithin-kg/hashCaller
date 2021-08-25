package com.hashcaller.app.view.ui.sms.individual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hashcaller.app.local.db.sms.SmsOutboxListDAO
import com.hashcaller.app.repository.spam.SpamNetworkRepository
import com.hashcaller.app.view.ui.sms.util.SMSLocalRepository


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