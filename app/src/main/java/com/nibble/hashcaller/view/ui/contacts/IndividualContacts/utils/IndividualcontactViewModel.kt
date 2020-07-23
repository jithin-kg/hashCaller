package com.nibble.hashcaller.view.ui.contacts.IndividualContacts.utils

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.nibble.hashcaller.view.ui.contacts.IndividualContacts.utils.IndividualContactRepository

/**
 * Created by Jithin KG on 23,July,2020
 */
class IndividualcontactViewModel(application: Application, id:Long)
    : AndroidViewModel(application)  {
   val contact =
       IndividualContactRepository(
           application.applicationContext,
           id
       )
}