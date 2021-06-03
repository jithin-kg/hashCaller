package com.nibble.hashcaller.view.ui.search

import android.database.Cursor
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.nibble.hashcaller.view.ui.contacts.ContactsQueryHelper
import com.nibble.hashcaller.view.ui.sms.util.SmsRepositoryHelper
import com.nibble.hashcaller.view.utils.LibPhoneCodeHelper

object AllSearchInjectorUtil {
    fun provideViewModelFactory(
        smsCursor: Cursor?,
        contactsCursor: Cursor?,
        allCallLogsCursor: Cursor?
    ): AllSearchViewmodelFactory {

        val allSearchRepository = AllSearchRepository(smsCursor, contactsCursor,
            allCallLogsCursor,
            ContactsQueryHelper(contactsCursor),
            SmsRepositoryHelper(smsCursor),

        )
        return AllSearchViewmodelFactory(allSearchRepository, LibPhoneCodeHelper(PhoneNumberUtil.getInstance()))
    }
}