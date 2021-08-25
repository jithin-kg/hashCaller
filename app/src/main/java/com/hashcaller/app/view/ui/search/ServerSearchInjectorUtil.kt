package com.hashcaller.app.view.ui.search

import android.content.Context
import com.google.firebase.auth.FirebaseUser
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.hashcaller.app.local.db.HashCallerDatabase
import com.hashcaller.app.repository.search.SearchNetworkRepository
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.view.utils.CountrycodeHelper
import com.hashcaller.app.view.utils.LibPhoneCodeHelper

object ServerSearchInjectorUtil {
    fun provideViewModelFactory(firebaseUser: FirebaseUser?, context:Context): ServerSearchViewmodelFactory {
        val callersInfoFromServerDAO = HashCallerDatabase.getDatabaseInstance(context).callersInfoFromServerDAO()

        return ServerSearchViewmodelFactory(SearchNetworkRepository(
            TokenHelper(firebaseUser),
            callersInfoFromServerDAO,
            LibPhoneCodeHelper(PhoneNumberUtil.getInstance()),
            CountrycodeHelper(context).getCountryISO()
        ), LibPhoneCodeHelper(PhoneNumberUtil.getInstance()))
    }
}