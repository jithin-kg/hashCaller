package com.nibble.hashcaller.repository.search

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nibble.hashcaller.repository.contacts.ContactUploadDTO
import com.nibble.hashcaller.view.ui.contacts.search.SearchContactSTub
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by Jithin KG on 31,July,2020
 */
class ContactSearchRepository(context: Context) {
    private val context: Context? = context

    var contactsLiveData = MutableLiveData<List<SearchContactSTub>>()
    var lastNumber = "0"

    fun fetchContacts(number:String): MutableLiveData<List<SearchContactSTub>> {
         var contacts = mutableListOf<SearchContactSTub>()
        if(number!=""){
            var cursor:Cursor?


//        if(number.length == 7){
//             cursor = context!!.contentResolver.query(
//                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                null,   ContactsContract.CommonDataKinds.Phone.NUMBER+" LIKE ?",  arrayOf("%$number"),
//                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
//            )
//        }else {
//             cursor = context!!.contentResolver.query(
//                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                null,   ContactsContract.CommonDataKinds.Phone.NUMBER+"=?",  arrayOf("$number"),
//                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
//            )
//
//
//        }
//        cursor = if (number.length === 7) {
            cursor = context?.contentResolver?.query(
//                ContactsContract.Data.CONTENT_URI,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE ?",
                arrayOf("%$number%"),
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )
//        }
//        else if (number?.length === 10) {
//            context?.contentResolver?.query(
////                ContactsContract.Data.CONTENT_URI,
//                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                null,
//                "(" + ContactsContract.CommonDataKinds.Phone.NUMBER + "=? AND LENGTH(" + ContactsContract.CommonDataKinds.Phone.NUMBER + ")=7) OR (" + ContactsContract.CommonDataKinds.Phone.NUMBER + "=? AND LENGTH(" + ContactsContract.CommonDataKinds.Phone.NUMBER + ")=10)",
//                arrayOf(number.substring(3), number),
//                null
//            )
//        }
//        else {
//            context?.contentResolver?.query(
//                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                null,
//                ContactsContract.CommonDataKinds.Phone.NUMBER + " = ? ",
//                arrayOf(number),
//                null
//            )
//        }


            if (cursor?.count ?: 0 > 0) {
                while (cursor!!.moveToNext()) {
                    var contact = SearchContactSTub()
                    val name =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    var phoneNo =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    phoneNo = phoneNo.trim { it <= ' ' }.replace(" ", "")
                    phoneNo = phoneNo.replace("-", "")
                    val photoUri =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
                    val duplicate =
                        AtomicBoolean(false)

                    if (lastNumber != phoneNo) {
                        contact.name = name
                        contact.phoneNumber = phoneNo
                        contacts.add(contact)
                        lastNumber = phoneNo
                    }
                }
                cursor.close()
                contactsLiveData.postValue(contacts)
            }
        }

        return contactsLiveData
    }

}