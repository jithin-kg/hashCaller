package com.nibble.hashcaller.view.ui.call.dialer

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.util.Log
import com.nibble.hashcaller.view.ui.contacts.utils.ContactLiveData

class CursorCreator {
    companion object{
        fun createContactsSearchCursor(queryString: String, context:Context): Cursor? {
            var cursor: Cursor? = null
            val selectionArgs = arrayOf("%$queryString%", "%$queryString%")

            //phone number like queryString or  display name like query string.

            var selection = "${ContactsContract.CommonDataKinds.Phone.NUMBER} " +
                    "LIKE ? OR ${ContactsContract.Contacts.DISPLAY_NAME}  LIKE ? "
            try {
                val projection = arrayOf(
                    ContactsContract.Contacts.NAME_RAW_CONTACT_ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
                    ContactsContract.Contacts.PHOTO_URI

                )
                cursor = context.contentResolver.query(
                    ContactLiveData.URI,
                    projection,
                    selection,
                    selectionArgs,
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " COLLATE NOCASE ASC LIMIT 20"
                )
            }catch (e:Exception){
                Log.d(TAG, "createCursor: $e")
            }

            return cursor

        }

        const val TAG = "__CursorCreator"
    }
}