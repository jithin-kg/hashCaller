package com.nibble.hashcaller.view.ui.contacts.IndividualContacts.utils

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Created by Jithin KG on 23,July,2020
 */
class IndividualContactFactory(private val application: Application,private val id:Long) :ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return IndividualcontactViewModel(application, id) as T
    }
//    override fun <T : ViewModel?> create(application: Class<T>): T {
//        return (T) new IndividualContactFactory(application, id);
//    }

}