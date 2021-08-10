package com.hashcaller.view.ui.auth.getinitialInfos

import android.content.Context
import com.hashcaller.datastore.DataStoreRepository
import com.hashcaller.local.db.HashCallerDatabase
import com.hashcaller.repository.user.UserNetworkRepository
import com.hashcaller.utils.auth.TokenHelper
import com.hashcaller.utils.auth.TokenManager
import com.hashcaller.utils.notifications.tokeDataStore
import com.hashcaller.view.ui.auth.getinitialInfos.db.UserHasehdNumRepository
import com.hashcaller.view.utils.CountrycodeHelper
import com.hashcaller.view.utils.imageProcess.ImageCompressor

object UserInfoInjectorUtil {
    fun provideUserInjectorUtil(context: Context, tokenHelper: TokenHelper?) : UserViewModelFactory {
        val userInfoDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).userInfoDAo() }
        val senderInfoFromServerDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callersInfoFromServerDAO() }
        val userHashedNumDAo = context?.let { HashCallerDatabase.getDatabaseInstance(it).userHashedNumDAO() }
        val imageCompressor = ImageCompressor(context)
        val countryCodeHelper = CountrycodeHelper(context)
        val userHashedNumRepository = UserHasehdNumRepository(userHashedNumDAo, countryCodeHelper)

        val userNetworkRepository = UserNetworkRepository(
            TokenManager( DataStoreRepository(context.tokeDataStore)),
            userInfoDAO,
            senderInfoFromServerDAO,
            imageCompressor,
            tokenHelper
        )

        return UserViewModelFactory(
            userNetworkRepository,
            userHashedNumRepository
        )
    }
}