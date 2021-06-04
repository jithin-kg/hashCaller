package com.nibble.hashcaller.view.ui.search

import android.content.Context
import android.database.Cursor
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.nibble.hashcaller.view.ui.contacts.ContactsQueryHelper
import com.nibble.hashcaller.view.ui.sms.util.SmsRepositoryHelper
import com.nibble.hashcaller.view.utils.LibPhoneCodeHelper

object AllSearchInjectorUtil {
    fun provideViewModelFactory(
        context: Context,
    ): AllSearchViewmodelFactory {

        val allSearchRepository = AllSearchRepository( context,
            ContactsQueryHelper(context),
            SmsRepositoryHelper(context)
        )
        return AllSearchViewmodelFactory(allSearchRepository, LibPhoneCodeHelper(PhoneNumberUtil.getInstance()))
    }
}