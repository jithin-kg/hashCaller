package com.nibble.hashcaller.view.ui.contacts.IndividualContacts.utils

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds
import android.util.Log
import com.nibble.hashcaller.view.ui.contacts.utils.ContentProviderLiveData


/**
 * Created by Jithin KG on 23,July,2020
 * retrieving data using live data
 */
class IndividualContactRepository(private  val context: Context, private var id:Long)
    :
    ContentProviderLiveData<IndividualContact>(context,
        URI
    ){
    private val idString:String = id.toString()

    companion object{
        val URI: Uri = ContactsContract.Contacts.CONTENT_URI
    }

    @SuppressLint("LongLogTag")
    private fun getIndividualContact(context: Context): IndividualContact {

        val contactId = idString
        val cContactIdString = ContactsContract.Contacts._ID
        val cCONTACT_CONTENT_URI = ContactsContract.Contacts.CONTENT_URI
        val cDisplayNameColumn = ContactsContract.Contacts.DISPLAY_NAME

        var nameToDisplay = ""
        var phoneNumber = ""

//        ContactsContract.Contacts.PHOTO_URI


        val selection = "$cContactIdString = ? "
        val selectionArgs =
            arrayOf<String>(java.lang.String.valueOf(contactId))

        val cursor: Cursor? = context.contentResolver
            .query(cCONTACT_CONTENT_URI, null, selection, selectionArgs, null)
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst()
            while (cursor != null && !cursor.isAfterLast()) {
                if (cursor.getColumnIndex(cContactIdString) >= 0) {
                    if (contactId == cursor.getString(cursor.getColumnIndex(cContactIdString))) {
                        nameToDisplay =
                            cursor.getString(cursor.getColumnIndex(cDisplayNameColumn))
                        Log.d("__IndividualContactRepository"," getIndividualContact: $nameToDisplay")
                   
                        break
                    }
                }
                cursor.moveToNext()
            }

        }
        cursor?.close()
         phoneNumber = getPhoneNumber()


        return IndividualContact(
            id,
            nameToDisplay,
             phoneNumber
        )
    }

    @SuppressLint("LongLogTag")
    private fun getPhoneNumber(): String {
        val contactId = idString
//        val cContactIdString = ContactsContract.Contacts._ID
        val cContactIdString = CommonDataKinds.Phone.CONTACT_ID
        var phoneNum = ""

        val selection = "$cContactIdString = ? "
        val selectionArgs =
            arrayOf<String>(java.lang.String.valueOf(contactId))

        val cursor: Cursor? = context.contentResolver
            .query(CommonDataKinds.Phone.CONTENT_URI, null, selection, selectionArgs, null)
        if (cursor != null && cursor.count > 0) {
            cursor.moveToFirst()
            while (cursor != null && !cursor.isAfterLast) {
                if (cursor.getColumnIndex(cContactIdString) >= 0) {
                    if (contactId == cursor.getString(cursor.getColumnIndex(cContactIdString))) {

                       phoneNum =  cursor.getString(cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER))
                        Log.d("__IndividualContactRepository", "getPhoneNumber: $phoneNum")
//                        val name: String =
//                            cursor.getString(cursor.getColumnIndex(cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER)))
////                        Log.d("__IndividualContactRepository"," getIndividualContact: $name")
                       
                        break
                    }
                }
                cursor.moveToNext()
            }

        }
        cursor?.close()

        return phoneNum
    }
    override fun getContentProviderValue() = getIndividualContact(context)
}