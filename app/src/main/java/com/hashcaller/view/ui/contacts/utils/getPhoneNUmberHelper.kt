package com.hashcaller.view.ui.contacts.utils

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.util.Log

/**
 * Created by Jithin KG on 24,July,2020
 */
@SuppressLint("LongLogTag")
 fun getPhoneNumber(idString:String, context:Context): String {
    val contactId = idString
//        val cContactIdString = ContactsContract.Contacts._ID
    val cContactIdString = ContactsContract.CommonDataKinds.Phone.CONTACT_ID
    var phoneNum = ""

    val selection = "$cContactIdString = ? "
    val selectionArgs =
        arrayOf<String>(java.lang.String.valueOf(contactId))

    val cursor: Cursor? = context.contentResolver
        .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, selection, selectionArgs, null)
    if (cursor != null && cursor.count > 0) {
        cursor.moveToFirst()
        while (cursor != null && !cursor.isAfterLast) {
            if (cursor.getColumnIndex(cContactIdString) >= 0) {
                if (contactId == cursor.getString(cursor.getColumnIndex(cContactIdString))) {

                    phoneNum =  cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
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