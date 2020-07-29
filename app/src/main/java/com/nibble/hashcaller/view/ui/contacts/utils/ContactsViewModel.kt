package com.nibble.hashcaller.view.ui.contacts.utils

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.nibble.hashcaller.view.ui.contacts.utils.ContactLiveData

/**
 * Created by Jithin KG on 22,July,2020
 */
class ContactsViewModel(val contacts: ContactLiveData): ViewModel() {



}
//class ContactsViewModel(application: Application): AndroidViewModel(application) {
//
//    val contacts =
//        ContactLiveData(application.applicationContext)
//
//}