package com.nibble.hashcaller.view.ui.search

import android.content.Context
import com.google.firebase.auth.FirebaseUser
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import com.nibble.hashcaller.utils.auth.TokenHelper
import com.nibble.hashcaller.view.utils.CountrycodeHelper
import com.nibble.hashcaller.view.utils.LibPhoneCodeHelper

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