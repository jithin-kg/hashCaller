package com.nibble.hashcaller.view.ui.splashactivity

import android.content.Context
import com.nibble.hashcaller.datastore.DataStoreRepository
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.utils.notifications.tokeDataStore
import com.nibble.hashcaller.view.ui.contacts.utils.SHARED_PREFERENCE_TOKEN_NAME


/**
 * Created by Jithin KG on 29,July,2020
 */
object SplashActivityInjectorUtil {
//    fun provideViewModelFactory(context: Context?):SplashActivityViewModelFactory{
//        val userInfoDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).userInfoDAo() }
//        val senderInfoFromServerDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsSenderInfoFromServerDAO() }
//        val tokenManager: TokenManager = TokenManager( DataStoreRepository(context.tokeDataStore))
//        val repository = context?.let { SplashActivityRepository(tokenManager, userInfoDAO!!,senderInfoFromServerDAO!!) }
//
//
////        return SplashActivityViewModelFactory(repository)
//    }

}