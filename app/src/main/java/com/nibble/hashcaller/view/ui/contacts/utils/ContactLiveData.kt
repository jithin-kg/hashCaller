package com.nibble.hashcaller.view.ui.contacts.utils

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import com.nibble.hashcaller.stubs.Contact

/**
 * Created by Jithin KG on 22,July,2020
 * To get the list of contacts live data from content provider
 *
 */
class ContactLiveData(private val context: Context):
    ContentProviderLiveData<List<Contact>>(context,
        URI) {

    companion object{
//        val URI: Uri = ContactsContract.Contacts.CONTENT_URI
        val URI: Uri =ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        private const val TAG = "__ContactLiveData"
    }

    private fun getContacts(context: Context):List<Contact>{
        val listOfContacts = mutableListOf<Contact>()

        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME
        )

        val cursor = context.contentResolver.query(
            URI,
            null,
            null,
            null,
            ContactsContract.Contacts.DISPLAY_NAME
        )
        if(cursor != null && cursor.moveToFirst()){
            do{

//                val id = cursor.getLong(0)
//                val name = cursor.getString(1)
                val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.NAME_RAW_CONTACT_ID))
                Log.d(TAG, "id is $id ")
                val name =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val phoneNo =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                Log.d(TAG, "phone num is $phoneNo")
                Log.d(TAG, "name is  $name")
                if(name!=null){
                    listOfContacts.add(Contact(1, name, phoneNo))
                    val c = Contact(1, name)

                }



            }while (cursor.moveToNext())
            cursor.close()
        }

        return listOfContacts

    }
    // so if there is any change in data this function will query and get latest data
    override fun getContentProviderValue(text: String?): List<Contact> = getContacts(context)
}
