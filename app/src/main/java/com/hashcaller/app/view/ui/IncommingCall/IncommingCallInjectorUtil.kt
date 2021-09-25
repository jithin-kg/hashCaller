package com.hashcaller.app.view.ui.IncommingCall

import android.app.Application
import android.content.Context
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.hashcaller.app.datastore.DataStoreRepository
import com.hashcaller.app.local.db.HashCallerDatabase
import com.hashcaller.app.repository.incomingcall.IncomingCallRepository
import com.hashcaller.app.repository.spam.SpamNetworkRepository
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.utils.notifications.tokeDataStore
import com.hashcaller.app.view.utils.CountrycodeHelper
import com.hashcaller.app.view.utils.LibPhoneCodeHelper

object IncommingCallInjectorUtil {
    fun provideUserInjectorUtil(context:Context) : IncommingCallViewModelFactory {
        val spamListDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).spamListDAO() }
        val spamNetworkRepository = SpamNetworkRepository(context, spamListDAO, DataStoreRepository(context.tokeDataStore))
        return IncommingCallViewModelFactory(spamNetworkRepository)
    }

    fun provideFactory(application: Application,token: TokenHelper?) : IncommingCallViewUpdatedModel.Factory {
        val callerInfoFromServerDAO = application?.let { HashCallerDatabase.getDatabaseInstance(it).callersInfoFromServerDAO() }
        val libCountryHelper: LibPhoneCodeHelper = LibPhoneCodeHelper(PhoneNumberUtil.getInstance())
        val countryCodeIso = CountrycodeHelper(application).getCountryISO()

        val repo = IncomingCallRepository(token, callerInfoFromServerDAO, libCountryHelper, countryCodeIso)
        return IncommingCallViewUpdatedModel.Factory(application, repo)
    }
}