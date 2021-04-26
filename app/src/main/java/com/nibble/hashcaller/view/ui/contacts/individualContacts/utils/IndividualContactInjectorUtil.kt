package com.nibble.hashcaller.view.ui.contacts.individualContacts.utils

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.repository.spam.SpamNetworkRepository
import com.nibble.hashcaller.view.ui.contacts.individualContacts.IndividualContactLiveData

object IndividualContactInjectorUtil {
    var phoneNumber:String = ""
    fun provideUserInjectorUtil(
        context: Context,
        phoneNum: String?,
        lifecycleScope: LifecycleCoroutineScope
    ) : IndividualContactFactory {

        val contactsListDAO = HashCallerDatabase.getDatabaseInstance(context).contactInformationDAO()
        val mutedContactsDAO = HashCallerDatabase.getDatabaseInstance(context).mutedCallersDAO()
        val callersInfoFromServer = HashCallerDatabase.getDatabaseInstance(context).callersInfoFromServerDAO()
        val callLogDAO = HashCallerDatabase.getDatabaseInstance(context).callLogDAO()
        val repository = IndividualContactRepository(contactsListDAO, context, callersInfoFromServer, phoneNum, callLogDAO)

        val spamListDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).spamListDAO() }
        val spamNetworkRepository = context?.let { SpamNetworkRepository(it, spamListDAO) }


        IndividualContactLiveData.phoneNumber = phoneNumber
        val individualContacLiveData = IndividualContactLiveData(context, lifecycleScope)


        return IndividualContactFactory(repository,
            individualContacLiveData,
            mutedContactsDAO,
            callersInfoFromServer,
            spamNetworkRepository)
    }
}