package com.hashcaller.app.view.ui.contacts.individualContacts.utils

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.hashcaller.app.datastore.DataStoreRepository
import com.hashcaller.app.local.db.HashCallerDatabase
import com.hashcaller.app.repository.BlockListPatternRepository
import com.hashcaller.app.repository.spam.SpamNetworkRepository
import com.hashcaller.app.utils.notifications.tokeDataStore
import com.hashcaller.app.view.ui.blockConfig.GeneralBlockRepository
import com.hashcaller.app.view.ui.contacts.individualContacts.IndividualContactLiveData
import com.hashcaller.app.view.ui.sms.db.ISMSThreadsDAO
import com.hashcaller.app.view.utils.CountrycodeHelper
import com.hashcaller.app.view.utils.LibPhoneCodeHelper

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
        val blockListDao = context?.let { HashCallerDatabase.getDatabaseInstance(it).blocklistDAO() }
        val mutedCallersDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).mutedCallersDAO() }
        val libCountryHelper: LibPhoneCodeHelper = LibPhoneCodeHelper(PhoneNumberUtil.getInstance())
        val countryCodeIso = CountrycodeHelper(context!!).getCountryISO()
        val smsThreadsDAO: ISMSThreadsDAO? = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsThreadsDAO() }

        val repository = IndividualContactRepository(
            contactsListDAO,
            context,
            callersInfoFromServer,
            phoneNum,
            callLogDAO,
            libCountryHelper,
            countryCodeIso
            )

        val spamListDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).spamListDAO() }
        val spamNetworkRepository = context?.let { SpamNetworkRepository(
            it,
            spamListDAO,
            DataStoreRepository(context.tokeDataStore)
        ) }


        IndividualContactLiveData.phoneNumber = phoneNumber
        val individualContacLiveData = IndividualContactLiveData(context, lifecycleScope)
        val blockListPatternRepository = BlockListPatternRepository(
            blockListDao!!,
            mutedCallersDAO!!,
            libCountryHelper,
            countryCodeIso
        )
        val generalBlockRepository = GeneralBlockRepository(
            callLogDAO,
            smsThreadsDAO,
            blockListDao,
            LibPhoneCodeHelper(PhoneNumberUtil.getInstance()),
            CountrycodeHelper(context).getCountryISO()
        )
        return IndividualContactFactory(repository,
            individualContacLiveData,
            mutedContactsDAO,
            callersInfoFromServer,
            spamNetworkRepository,
            blockListPatternRepository,
            generalBlockRepository
            )
    }
}