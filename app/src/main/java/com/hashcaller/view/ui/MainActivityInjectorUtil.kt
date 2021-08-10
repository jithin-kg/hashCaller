package com.hashcaller.view.ui

import android.content.Context
import com.hashcaller.datastore.DataStoreRepository
import com.hashcaller.local.db.HashCallerDatabase
import com.hashcaller.repository.user.UserNetworkRepository
import com.hashcaller.utils.auth.TokenHelper
import com.hashcaller.utils.auth.TokenManager
import com.hashcaller.utils.notifications.tokeDataStore
import com.hashcaller.view.ui.auth.getinitialInfos.db.UserHasehdNumRepository
import com.hashcaller.view.ui.hashworker.HashViewmodelFactory
import com.hashcaller.view.ui.hashworker.HashedDataRepository
import com.hashcaller.view.utils.CountrycodeHelper
import com.hashcaller.view.utils.imageProcess.ImageCompressor

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

    fun provideHashINjectorUtil(context: Context, tokenHelper: TokenHelper?): HashViewmodelFactory {
        val hashedNumDao = context?.let { HashCallerDatabase.getDatabaseInstance(it).hashedNumDAO() }

        val repository = HashedDataRepository(hashedNumDao)
        return HashViewmodelFactory(repository)
    }


}