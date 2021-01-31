package com.nibble.hashcaller.view.ui.contactSelector

import android.content.Context
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.nibble.hashcaller.repository.contacts.ContactsNetworkRepository
import com.nibble.hashcaller.repository.search.ContactSearchRepository
import com.nibble.hashcaller.view.ui.contacts.utils.ContactLiveData
import com.nibble.hashcaller.view.ui.contacts.utils.ContactsViewModelFactory

/**
 * Created by Jithin KG on 29,July,2020
 */
object ContactSelectorInjectorUtil {
    fun provideContactsViewModelFactory(context: Context?): ContactsViewModelFactory {

        val contactsLiveData = context?.let { ContactLiveData(it) }
        val contactLisDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).contactInformationDAO() }

        //passing necessory elements ( dao, since we are
        // creating the dao here we can easily pass context just right here)
        // for ContactLocalSyncReposirorty

        val contactLocalSyncRepository = ContactLocalSyncRepository(contactLisDAO)
        val contactNetworkRepository  = context?.let { ContactsNetworkRepository(it) }
        val contactsRepository = context?.let { ContactSearchRepository(it) }



        return ContactsViewModelFactory(contactsLiveData!!,
            contactLocalSyncRepository,
            contactsRepository,
            contactNetworkRepository
            )
    }

}