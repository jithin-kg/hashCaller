package com.hashcaller.view.ui.contacts.search

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import androidx.lifecycle.LifecycleCoroutineScope
import com.hashcaller.stubs.Contact
import com.hashcaller.view.ui.contacts.utils.ContentProviderLiveData

/**
 * Created by Jithin KG on 31,July,2020
 */
class SearchContact(private val context: Context, private val scope: LifecycleCoroutineScope) :

    ContentProviderLiveData<List<Contact>>(context, URI, scope) {

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
                    if(name!=null){
                        listOfContacts.add(Contact(
                            id,
                            name,
                            photoThumnailServer = "photoThumnail",
                            photoURI = "photoURI"
                        ))
                        val c = Contact(
                            id,
                            name,
                            photoThumnailServer = "photoThumnail",
                            photoURI = "photoURI"
                        )

                    }



                }while (cursor.moveToNext())
                cursor.close()
            }

            return listOfContacts

        }
        // so if there is any change in data this function will query and get latest data
        override suspend fun getContentProviderValue(text: String?): List<Contact> = getContacts(context)
}