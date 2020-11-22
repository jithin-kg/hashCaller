package com.nibble.hashcaller.view.ui.call.dialer

import android.content.Context
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.nibble.hashcaller.repository.contacts.ContactsNetworkRepository
import com.nibble.hashcaller.repository.search.ContactSearchRepository

/**
 * Created by Jithin KG on 29,July,2020
 */
object DialerInjectorUtil {
    fun provideDialerViewModelFactory(context: Context?):DialerViewModelFactory{


        return DialerViewModelFactory()
    }

}