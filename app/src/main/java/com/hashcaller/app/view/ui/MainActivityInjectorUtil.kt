package com.hashcaller.app.view.ui

import android.content.Context
import com.hashcaller.app.datastore.DataStoreRepository
import com.hashcaller.app.local.db.HashCallerDatabase
import com.hashcaller.app.repository.user.UserNetworkRepository
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.utils.auth.TokenManager
import com.hashcaller.app.utils.notifications.tokeDataStore
import com.hashcaller.app.view.ui.auth.getinitialInfos.db.UserHasehdNumRepository
import com.hashcaller.app.view.ui.hashworker.HashViewmodelFactory
import com.hashcaller.app.view.ui.hashworker.HashedDataRepository
import com.hashcaller.app.view.utils.CountrycodeHelper
import com.hashcaller.app.view.utils.imageProcess.ImageCompressor

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