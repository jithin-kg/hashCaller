package com.nibble.hashcaller.view.ui.contacts.search.utils

import android.content.Context
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.repository.BlockListPatternRepository
import com.nibble.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import com.nibble.hashcaller.utils.auth.TokenHelper
import com.nibble.hashcaller.view.ui.IncommingCall.LocalDbSearchRepository
import com.nibble.hashcaller.view.utils.CountrycodeHelper
import com.nibble.hashcaller.view.utils.LibPhoneCodeHelper

object SearchInjectorUtil {
    fun provideUserInjectorUtil(context: Context, tokenHelper: TokenHelper?) : SearchViewModelFactory {
        val callersInfoFromServerDAO = HashCallerDatabase.getDatabaseInstance(context).callersInfoFromServerDAO()
        val searchNetworkRepository = SearchNetworkRepository(
            tokenHelper,
            callersInfoFromServerDAO,
            LibPhoneCodeHelper(PhoneNumberUtil.getInstance()),
            CountrycodeHelper(context).getCountryISO()
            )

        val blockListDao = context?.let { HashCallerDatabase.getDatabaseInstance(it).blocklistDAO() }
        val mutedCallersDao = context?.let { HashCallerDatabase.getDatabaseInstance(it).mutedCallersDAO() }
        val libCountryHelper: LibPhoneCodeHelper = LibPhoneCodeHelper(PhoneNumberUtil.getInstance())
        val countryCodeIso = CountrycodeHelper(context).getCountryISO()
        val blockListPatternRepository: BlockListPatternRepository = BlockListPatternRepository(
            blockListDao,
            mutedCallersDao,
            libCountryHelper,
            countryCodeIso
        )

        val contactsListDAO = HashCallerDatabase.getDatabaseInstance(context).contactInformationDAO()
        val contactLocalSyncRepository = ContactLocalSyncRepository(contactsListDAO, context)
        val localDbSearchRepository = LocalDbSearchRepository(
            callersInfoFromServerDAO,
            LibPhoneCodeHelper(PhoneNumberUtil.getInstance()),
            CountrycodeHelper(context).getCountryISO()
            )

        return SearchViewModelFactory(searchNetworkRepository, contactLocalSyncRepository, localDbSearchRepository, blockListPatternRepository)
    }
}