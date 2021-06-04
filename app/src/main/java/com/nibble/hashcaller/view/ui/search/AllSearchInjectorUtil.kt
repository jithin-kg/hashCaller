package com.nibble.hashcaller.view.ui.search

import android.content.Context
import android.database.Cursor
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.nibble.hashcaller.view.ui.contacts.ContactsQueryHelper
import com.nibble.hashcaller.view.ui.sms.util.SmsRepositoryHelper
import com.nibble.hashcaller.view.utils.LibPhoneCodeHelper

object AllSearchInjectorUtil {
    fun provideViewModelFactory(
        smsCursor: Cursor?,
        context: Context,
        allCallLogsCursor: Cursor?
    ): AllSearchViewmodelFactory {

        val allSearchRepository = AllSearchRepository(smsCursor, context,
            allCallLogsCursor,
            ContactsQueryHelper(context),
            SmsRepositoryHelper(smsCursor)
        )
        return AllSearchViewmodelFactory(allSearchRepository, LibPhoneCodeHelper(PhoneNumberUtil.getInstance()))
    }
}