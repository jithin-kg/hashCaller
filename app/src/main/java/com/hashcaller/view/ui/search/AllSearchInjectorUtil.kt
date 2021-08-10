package com.hashcaller.view.ui.search

import android.content.Context
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.hashcaller.view.ui.contacts.ContactsQueryHelper
import com.hashcaller.view.ui.sms.util.SmsRepositoryHelper
import com.hashcaller.view.utils.LibPhoneCodeHelper

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