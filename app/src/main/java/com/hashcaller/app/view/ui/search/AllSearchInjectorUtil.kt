package com.hashcaller.app.view.ui.search

import android.content.Context
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.hashcaller.app.view.ui.contacts.ContactsQueryHelper
import com.hashcaller.app.view.ui.sms.util.SmsRepositoryHelper
import com.hashcaller.app.view.utils.LibPhoneCodeHelper

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