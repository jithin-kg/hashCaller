package com.hashcaller.app.repository.search

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import androidx.lifecycle.MutableLiveData
import com.hashcaller.app.repository.contacts.PhoneNumWithHashedNumDTO

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by Jithin KG on 31,July,2020
 */
class ContactSearchRepository(context: Context) {
    private val context: Context? = context

    var contactsLiveData = MutableLiveData<List<PhoneNumWithHashedNumDTO>>()
    var lastNumber = "0"

    fun fetchContactsLiveData(number:String): MutableLiveData<List<PhoneNumWithHashedNumDTO>> {

//        if(number!=""){
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
//                ContactsContract.com.hashcaller.app.network.user.Data.CONTENT_URI,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE ?",
                arrayOf("%$number%"),
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )
//        }
//        else if (number?.length === 10) {
//            context?.contentResolver?.query(
////                ContactsContract.com.hashcaller.app.network.user.Data.CONTENT_URI,
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


//            if (cursor?.count ?: 0 > 0) {
//                while (cursor!!.moveToNext()) {
//                    var contact = SearchContactSTub()
//                    val name =
//                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
//                    var phoneNo =
//                        cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
//                    phoneNo = phoneNo.trim { it <= ' ' }.replace(" ", "")
//                    phoneNo = phoneNo.replace("-", "")
//                    val photoUri =
//                        cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
//                    val duplicate =
//                        AtomicBoolean(false)
//
//                    if (lastNumber != phoneNo) {
//                        contact.name = name
//                        contact.phoneNumber = phoneNo
//                        contacts.add(contact)
//                        lastNumber = phoneNo
//                    }
//                }
//                cursor.close()
//                contactsLiveData.postValue(contacts)
//            }
//        }

        contactsLiveData.postValue(getcontacts(cursor))

        return contactsLiveData
    }

    private fun getcontacts(cursor: Cursor?): MutableList<PhoneNumWithHashedNumDTO> {
        var contacts = mutableListOf<PhoneNumWithHashedNumDTO>()
        if (cursor?.count ?: 0 > 0) {
            while (cursor!!.moveToNext()) {
                var contact = PhoneNumWithHashedNumDTO()
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

        }
        return contacts
    }

    fun fetchContacts(): List<PhoneNumWithHashedNumDTO> {

        var cursor:Cursor?


        cursor = context?.contentResolver?.query(
//                ContactsContract.com.hashcaller.app.network.user.Data.CONTENT_URI,
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null ,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
    return getcontacts(cursor)

    }

}