package com.nibble.hashcaller.view.ui.contacts.IndividualContacts.utils

import android.content.Context
import com.nibble.hashcaller.local.db.HashCallerDatabase

object IndividualContactInjectorUtil {
    fun provideUserInjectorUtil(context:Context) : IndividualContactFactory {

        val contactsListDAO = HashCallerDatabase.getDatabaseInstance(context).contactInformationDAO()
        val repository = IndividualContactRepository(contactsListDAO)


        return IndividualContactFactory(repository)
    }
}