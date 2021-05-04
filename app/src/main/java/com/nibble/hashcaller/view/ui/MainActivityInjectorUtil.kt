package com.nibble.hashcaller.view.ui

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

object MainActivityInjectorUtil {
    fun provideUserInjectorUtil(context:Context) : MainActivityViewModelFactory {
        val userInfoDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).userInfoDAo() }
        val senderInfoFromServerDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsSenderInfoFromServerDAO() }
        val hasehdNumDAo = context?.let { HashCallerDatabase.getDatabaseInstance(it).userHashedNumDAO() }
//        val contactsRepository = ContactRepositoryTwo(context)
        val imageCompressor: ImageCompressor = ImageCompressor(context)
        val countryCodeHelper = CountrycodeHelper(context)


        val userHasehdNumRepository = UserHasehdNumRepository(hasehdNumDAo, countryCodeHelper)

        val userNetworkRepository = UserNetworkRepository(
            TokenManager(DataStoreRepository(context.tokeDataStore)),
            userInfoDAO,
            senderInfoFromServerDAO,
            imageCompressor
        )

        return MainActivityViewModelFactory(
            userNetworkRepository,
            userHasehdNumRepository
        )
    }


}