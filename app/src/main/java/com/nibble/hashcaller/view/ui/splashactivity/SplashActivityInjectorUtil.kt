package com.nibble.hashcaller.view.ui.splashactivity

import android.content.Context
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.local.db.blocklist.SMSSendersInfoFromServerDAO
import com.nibble.hashcaller.utils.auth.TokenManager
import com.nibble.hashcaller.view.ui.auth.getinitialInfos.db.UserInfoDAO
import com.nibble.hashcaller.view.ui.contacts.utils.SHARED_PREFERENCE_TOKEN_NAME
import com.nibble.hashcaller.view.ui.sms.list.SMSLiveData


/**
 * Created by Jithin KG on 29,July,2020
 */
object SplashActivityInjectorUtil {
    fun provideViewModelFactory(context: Context?):SplashActivityViewModelFactory{


        val userInfoDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).userInfoDAo() }
        val senderInfoFromServerDAO = context?.let { HashCallerDatabase.getDatabaseInstance(it).smsSenderInfoFromServerDAO() }
        val sp = context!!.getSharedPreferences(SHARED_PREFERENCE_TOKEN_NAME, Context.MODE_PRIVATE)
        val tokenManager: TokenManager = TokenManager(sp)

        val repository = context?.let { SplashActivityRepository(tokenManager, userInfoDAO!!,senderInfoFromServerDAO!!) }


        return SplashActivityViewModelFactory(repository)
    }

}