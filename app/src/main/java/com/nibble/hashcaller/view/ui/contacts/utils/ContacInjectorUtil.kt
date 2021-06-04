package com.nibble.hashcaller.view.ui.contacts.utils

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.nibble.hashcaller.repository.contacts.ContactsNetworkRepository
import com.nibble.hashcaller.repository.search.ContactSearchRepository
import com.nibble.hashcaller.utils.auth.TokenHelper
import com.nibble.hashcaller.view.ui.contacts.ContactsQueryHelper
import com.nibble.hashcaller.view.ui.contacts.getAllContactsCursor

/**
 * Created by Jithin KG on 29,July,2020
 */
object ContacInjectorUtil {
    fun provideContactsViewModelFactory(
        context: Context?,
        lifecycleScope: LifecycleCoroutineScope,
        tokenHelper: TokenHelper
    ):ContactsViewModelFactory{

        val contactQueryHelper = ContactsQueryHelper(context)
        val contactsLiveData = context?.let { ContactLiveData(
            it,
            lifecycleScope,
            contactQueryHelper
        ) }
        val contactLisDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).contactInformationDAO() }

        //passing necessory elements ( dao, since we are
        // creating the dao here we can easily pass context just right here)
        // for ContactLocalSyncReposirorty

        val contactLocalSyncRepository = ContactLocalSyncRepository(contactLisDAO, context!!)
        val contactNetworkRepository  = context?.let { ContactsNetworkRepository(it, tokenHelper) }
        val contactsRepository = context?.let { ContactSearchRepository(it) }



        return ContactsViewModelFactory(contactsLiveData!!,
            contactLocalSyncRepository,
            contactsRepository,
            contactNetworkRepository
            )
    }

}