package com.nibble.hashcaller.view.ui.search

import android.content.Context
import com.google.firebase.auth.FirebaseUser
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import com.nibble.hashcaller.utils.auth.TokenHelper

object ServerSearchInjectorUtil {
    fun provideViewModelFactory(firebaseUser: FirebaseUser?, context:Context): ServerSearchViewmodelFactory {
        val callersInfoFromServerDAO = HashCallerDatabase.getDatabaseInstance(context).callersInfoFromServerDAO()
        return ServerSearchViewmodelFactory(SearchNetworkRepository(TokenHelper(firebaseUser), callersInfoFromServerDAO))
    }
}