package com.hashcaller.app.view.ui.auth.getinitialInfos

import android.content.Context
import com.hashcaller.app.datastore.DataStoreRepository
import com.hashcaller.app.local.db.HashCallerDatabase
import com.hashcaller.app.repository.user.UserNetworkRepository
import com.hashcaller.app.utils.auth.TokenManager
import com.hashcaller.app.utils.notifications.tokeDataStore
import com.hashcaller.app.view.ui.auth.getinitialInfos.db.UserHasehdNumRepository
import com.hashcaller.app.view.utils.CountrycodeHelper
import com.hashcaller.app.view.utils.imageProcess.ImageCompressor

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