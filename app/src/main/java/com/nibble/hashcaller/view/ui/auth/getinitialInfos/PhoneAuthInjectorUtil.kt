package com.nibble.hashcaller.view.ui.auth.getinitialInfos

import android.content.Context
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.repository.user.UserNetworkRepository
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.utils.notifications.tokeDataStore
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.db.UserHasehdNumRepository
import com.nibble.hashcaller.view.ui.contacts.utils.SHARED_PREFERENCE_TOKEN_NAME
import com.nibble.hashcaller.view.utils.CountrycodeHelper
import com.nibble.hashcaller.view.utils.imageProcess.ImageCompressor

object PhoneAuthInjectorUtil {
    fun provideUserInjectorUtil(context:Context) : UserViewModelFactory {
        val userInfoDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).userInfoDAo() }
        val senderInfoFromServerDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsSenderInfoFromServerDAO() }
        val userHashedNumDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).userHashedNumDAO() }
        val sp = context.getSharedPreferences(SHARED_PREFERENCE_TOKEN_NAME, Context.MODE_PRIVATE)
        val imageCompressor = ImageCompressor(context)
        val countryCodeHelper = CountrycodeHelper(context)
        val userNetworkRepository = UserNetworkRepository(
            TokenManager(sp, DataStoreRepository(context.tokeDataStore)),
            userInfoDAO,
            senderInfoFromServerDAO,
            imageCompressor
        )
        val userHashedNumRepository = UserHasehdNumRepository(userHashedNumDAO, countryCodeHelper)
        return UserViewModelFactory(
            userNetworkRepository,
            userHashedNumRepository
        )
    }
}