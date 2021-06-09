package com.nibble.hashcaller.view.ui.call.utils

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.repository.BlockListPatternRepository
import com.nibble.hashcaller.utils.auth.TokenHelper
import com.nibble.hashcaller.utils.notifications.tokeDataStore
import com.nibble.hashcaller.view.ui.blockConfig.GeneralBlockRepository
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogLiveData
import com.nibble.hashcaller.view.ui.call.repository.CallContainerRepository
import com.nibble.hashcaller.view.ui.sms.db.ISMSThreadsDAO
import com.nibble.hashcaller.view.utils.CountrycodeHelper
import com.nibble.hashcaller.view.utils.LibPhoneCodeHelper


/**
 * Created by Jithin KG on 29,July,2020
 */
object CallContainerInjectorUtil {
    private const val TAG = "__CallContainerInjectorUtil"
    fun provideViewModelFactory(
        context: Context?,
        lifecycleScope: LifecycleCoroutineScope,
        tokenHelper: TokenHelper?
    ): CallContainerViewModelFactory {



        val callerInfoFromServerDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callersInfoFromServerDAO() }
        val mutedCallersDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).mutedCallersDAO() }
        val callLogDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callLogDAO() }
        val blockListDao = context?.let { HashCallerDatabase.getDatabaseInstance(it).blocklistDAO() }
        val mutedCallersDao = context?.let { HashCallerDatabase.getDatabaseInstance(it).mutedCallersDAO() }
         val libCountryHelper: LibPhoneCodeHelper = LibPhoneCodeHelper(PhoneNumberUtil.getInstance())
         val countryCodeIso = CountrycodeHelper(context!!).getCountryISO()

        Log.d(TAG, "provideViewModelFactory countryIso: $countryCodeIso")

        val blockListPatternRepository = BlockListPatternRepository(
            blockListDao!!,
            mutedCallersDAO!!,
            libCountryHelper,
            countryCodeIso
        )
        val smsThreadsDAO: ISMSThreadsDAO? = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsThreadsDAO() }

        val repository = context?.let {
            CallContainerRepository(
                it,
                callerInfoFromServerDAO!!,
                mutedCallersDAO,
                callLogDAO,
                DataStoreRepository(context.tokeDataStore),
                tokenHelper,
                smsThreadsDAO,
                LibPhoneCodeHelper(PhoneNumberUtil.getInstance()),
                CountrycodeHelper(context).getCountryISO()

            )
        }


        val callLogLiveData =
            CallLogLiveData(context!!, repository, lifecycleScope)

        val generalBlockRepository = GeneralBlockRepository(
            callLogDAO,
            smsThreadsDAO,
            blockListDao,
            LibPhoneCodeHelper(PhoneNumberUtil.getInstance()),
            CountrycodeHelper(context).getCountryISO()
        )

        return CallContainerViewModelFactory(
            callLogLiveData!!,
            repository,
            callerInfoFromServerDAO,
            blockListPatternRepository,
            generalBlockRepository
            )
    }

}