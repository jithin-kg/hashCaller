package com.hashcaller.app.view.ui.call.dialer

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.util.Log
import com.hashcaller.app.view.ui.contacts.utils.ContactLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CursorCreator {
    companion object{

        suspend fun getCursor(context: Context){

        }
        suspend fun createContactsSearchCursor(queryString: String, context:Context): Cursor?  = withContext(Dispatchers.IO){
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

            return@withContext cursor

        }


        suspend fun cursorAllContacts(context: Context): Cursor? = withContext(Dispatchers.IO){
                var cursor: Cursor? = null

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
                        null,
                        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " COLLATE NOCASE ASC LIMIT 20"
                    )
                }catch (e:Exception){
                    Log.d(TAG, "createCursor: $e")
                }

                return@withContext cursor
        }

        const val TAG = "__CursorCreator"
    }
}