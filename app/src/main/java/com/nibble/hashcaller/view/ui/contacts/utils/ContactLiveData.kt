package com.nibble.hashcaller.view.ui.contacts.utils

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import com.nibble.hashcaller.data.stubs.Contact

/**
 * Created by Jithin KG on 22,July,2020
 */
class ContactLiveData(private val context: Context):
    ContentProviderLiveData<List<Contact>>(context,
        URI
    ){

    companion object{
        val URI: Uri = ContactsContract.Contacts.CONTENT_URI
    }

    private fun getContacts(context: Context):List<Contact>{
        val listOfContacts = mutableListOf<Contact>()

        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME
        )

        val cursor = context.contentResolver.query(
            URI,
            projection,
            null,
            null,
            ContactsContract.Contacts.DISPLAY_NAME
        )
        if(cursor != null && cursor.moveToFirst()){
            do{
                val id = cursor.getLong(0)
                val name = cursor.getString(1)

                listOfContacts.add(Contact(id, name))
            }while (cursor.moveToNext())
            cursor.close()
        }
        return listOfContacts

    }
    // so if there is any change in data this function will query and get latest data
    override fun getContentProviderValue() = getContacts(context)
}
