package com.nibble.hashcaller.view.ui.contacts.utils

import android.content.Context

/**
 * Created by Jithin KG on 29,July,2020
 */
object ContacInjectorUtil {
    fun provideContactsViewModelFactory(context: Context?):ContactsViewModelFactory{
        val contactsLiveData = context?.let { ContactLiveData(it) }
        return ContactsViewModelFactory(contactsLiveData!!)
    }
}