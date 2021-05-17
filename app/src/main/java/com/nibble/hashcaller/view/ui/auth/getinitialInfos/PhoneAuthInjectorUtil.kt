package com.nibble.hashcaller.view.ui.auth.getinitialInfos

import android.content.Context
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.repository.user.UserNetworkRepository
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.utils.notifications.tokeDataStore
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.db.UserHasehdNumRepository
import com.nibble.hashcaller.view.utils.CountrycodeHelper
import com.nibble.hashcaller.view.utils.imageProcess.ImageCompressor

object PhoneAuthInjectorUtil {

    fun provideUserInjectorUtil(context:Context) : UserViewModelFactory {

        val userInfoDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).userInfoDAo() }
        val senderInfoFromServerDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callersInfoFromServerDAO() }
        val userHashedNumDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).userHashedNumDAO() }
        val imageCompressor = ImageCompressor(context)
        val countryCodeHelper = CountrycodeHelper(context)
        val userNetworkRepository = UserNetworkRepository(
            TokenManager( DataStoreRepository(context.tokeDataStore)),
            userInfoDAO,
            senderInfoFromServerDAO,
            imageCompressor,
            null
        )
        val userHashedNumRepository = UserHasehdNumRepository(userHashedNumDAO, countryCodeHelper)
        return UserViewModelFactory(
            userNetworkRepository,
            userHashedNumRepository
        )
    }
}