package com.nibble.hashcaller.view.ui.contacts.IndividualContacts.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Created by Jithin KG on 23,July,2020
 */
class IndividualContactFactory(private val repo: IndividualContactRepository) :ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return IndividualcontactViewModel(repo) as T
    }
//    override fun <T : ViewModel?> create(application: Class<T>): T {
//        return (T) new IndividualContactFactory(application, id);
//    }

}