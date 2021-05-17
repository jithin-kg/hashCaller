package com.nibble.hashcaller.view.ui

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

object MainActivityInjectorUtil {
    fun provideUserInjectorUtil(context: Context, tokenHelper: TokenHelper?) : MainActivityViewModelFactory {
        val userInfoDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).userInfoDAo() }
        val senderInfoFromServerDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).callersInfoFromServerDAO() }
        val hasehdNumDAo = context?.let { HashCallerDatabase.getDatabaseInstance(it).userHashedNumDAO() }
//        val contactsRepository = ContactRepositoryTwo(context)
        val imageCompressor: ImageCompressor = ImageCompressor(context)
        val countryCodeHelper = CountrycodeHelper(context)


        val userHasehdNumRepository = UserHasehdNumRepository(hasehdNumDAo, countryCodeHelper)

        val userNetworkRepository = UserNetworkRepository(
            TokenManager(DataStoreRepository(context.tokeDataStore)),
            userInfoDAO,
            senderInfoFromServerDAO,
            imageCompressor,
            tokenHelper
        )

        return MainActivityViewModelFactory(
            userNetworkRepository,
            userHasehdNumRepository
        )
    }


}