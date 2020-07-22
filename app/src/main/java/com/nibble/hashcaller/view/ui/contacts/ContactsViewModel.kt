package com.nibble.hashcaller.view.ui.contacts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.nibble.hashcaller.view.ui.contacts.utils.ContactLiveData

/**
 * Created by Jithin KG on 22,July,2020
 */
class ContactsViewModel(application: Application): AndroidViewModel(application) {

    val contacts =
        ContactLiveData(application.applicationContext)


}