package com.nibble.hashcaller.view.ui.contacts.individualContacts.utils

import android.content.Context
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.view.ui.contacts.individualContacts.IndividualContactLiveData

object IndividualContactInjectorUtil {
    var phoneNumber:String = ""
    fun provideUserInjectorUtil(context:Context) : IndividualContactFactory {

        val contactsListDAO = HashCallerDatabase.getDatabaseInstance(context).contactInformationDAO()
        val mutedContactsDAO = HashCallerDatabase.getDatabaseInstance(context).mutedCallersDAO()
        val callersInfoFromServer = HashCallerDatabase.getDatabaseInstance(context).callersInfoFromServerDAO()
        val repository = IndividualContactRepository(contactsListDAO, context)

        IndividualContactLiveData.phoneNumber = phoneNumber
        val individualContacLiveData = IndividualContactLiveData(context)


        return IndividualContactFactory(repository, individualContacLiveData, mutedContactsDAO, callersInfoFromServer)
    }
}