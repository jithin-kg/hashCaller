package com.nibble.hashcaller.view.ui.auth.getinitialInfos

import android.content.Context
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.repository.user.UserNetworkRepository
import com.nibble.hashcaller.utils.auth.TokenHelper
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.utils.notifications.tokeDataStore
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.db.UserHasehdNumRepository
import com.nibble.hashcaller.view.utils.CountrycodeHelper
import com.nibble.hashcaller.view.utils.imageProcess.ImageCompressor

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