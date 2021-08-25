package com.hashcaller.app.view.ui.contacts.individualContacts.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hashcaller.app.local.db.blocklist.mutedCallers.IMutedCallersDAO
import com.hashcaller.app.repository.spam.SpamNetworkRepository
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServerDAO
import com.hashcaller.app.view.ui.contacts.individualContacts.IndividualContactLiveData

/**
 * Created by Jithin KG on 23,July,2020
 */
class IndividualContactFactory(
    private val repo: IndividualContactRepository,
    private val livedata: IndividualContactLiveData,
    private val mutedContactsDAO: IMutedCallersDAO,
    private val callersInfoFromServer: CallersInfoFromServerDAO,
    private val spamNetworkRepository: SpamNetworkRepository
) :ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return IndividualcontactViewModel(repo,
            livedata,
            mutedContactsDAO,
            callersInfoFromServer,
            spamNetworkRepository

        ) as T
    }
//    override fun <T : ViewModel?> create(application: Class<T>): T {
//        return (T) new IndividualContactFactory(application, id);
//    }

}