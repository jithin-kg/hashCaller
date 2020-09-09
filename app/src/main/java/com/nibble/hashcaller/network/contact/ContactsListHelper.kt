package com.nibble.hashcaller.network.contact

import com.google.gson.annotations.SerializedName
import com.nibble.hashcaller.repository.contacts.ContactUploadDTO
import com.nibble.hashcaller.stubs.Contact


/**
 * Created by Jithin KG on 25,July,2020
 * This class helps to send list of data while performing retrofit request
 */

data class ContactsListHelper(
   @SerializedName("contacts")
    val contacts:MutableList<ContactUploadDTO>) {

//    private var contactsList:MutableList<ContactUploadDTO> = mutableListOf<ContactUploadDTO>()
//
//    fun addAllContacts(contacts:List<ContactUploadDTO>){
//        contactsList.addAll(contacts)
//    }
}