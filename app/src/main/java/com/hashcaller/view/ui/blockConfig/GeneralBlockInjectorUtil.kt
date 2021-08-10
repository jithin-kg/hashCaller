package com.hashcaller.view.ui.blockConfig

import android.content.Context
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.hashcaller.local.db.HashCallerDatabase
import com.hashcaller.repository.BlockListPatternRepository
import com.hashcaller.view.ui.sms.db.ISMSThreadsDAO
import com.hashcaller.view.utils.CountrycodeHelper
import com.hashcaller.view.utils.LibPhoneCodeHelper

object GeneralBlockInjectorUtil {
    fun provideViewModel(
                                context: Context,
                                phoneNum: String) : GeneralBlockViewModelFactory {

        val blockListDao = context?.let { HashCallerDatabase.getDatabaseInstance(it).blocklistDAO() }
        val mutedCallersDao = context?.let { HashCallerDatabase.getDatabaseInstance(it).mutedCallersDAO() }
        val smsThreadsDAO: ISMSThreadsDAO = HashCallerDatabase.getDatabaseInstance(context).smsThreadsDAO()
        val callLogDAO = HashCallerDatabase.getDatabaseInstance(context).callLogDAO()
        val libCountryHelper: LibPhoneCodeHelper = LibPhoneCodeHelper(PhoneNumberUtil.getInstance())
        val countryCodeIso = CountrycodeHelper(context).getCountryISO()
        val blockListPatternRepository = BlockListPatternRepository(
            blockListDao,
            mutedCallersDao,
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

            return GeneralBlockViewModelFactory(
                blockListPatternRepository,
                generalBlockRepository )
    }
}