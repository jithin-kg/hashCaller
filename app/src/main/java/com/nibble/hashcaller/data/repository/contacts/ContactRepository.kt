//package com.nibble.hashcaller.data.repository.contacts
//
//import android.content.Context
//import android.provider.ContactsContract
//import com.nibble.hashcaller.data.stubs.Contact
//import java.util.*
//import java.util.concurrent.atomic.AtomicBoolean
//
///**
// * Created by Jithin KG on 21,July,2020
// */
//class ContactRepository(context:Context) {
//
//    private val context: Context? = context
//    private var contacts: MutableList<Contact> = ArrayList()
//    var uniqueMobilePhones: List<Contact> = ArrayList()
//    var lastNumber = "0"
//
//    fun fetchContacts(): List<Contact?>? {
//        val cursor = context!!.contentResolver.query(
//            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//            null, null, null,
//            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
//        )
//        if (cursor?.count ?: 0 > 0) {
//            while (cursor!!.moveToNext()) {
//                val contact = Contact()
//                val name =
//                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
//                var phoneNo =
//                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
//                phoneNo = phoneNo.trim { it <= ' ' }.replace(" ", "")
//                phoneNo = phoneNo.replace("-", "")
//                val photoUri =
//                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
//                val duplicate =
//                    AtomicBoolean(false)
//
//                if (lastNumber != phoneNo) {
//                    contact.name = name
//                    contact.phoneNumber = phoneNo
//                    contact.photoUri = (photoUri)
//                    contacts.add(contact)
//                    lastNumber = phoneNo
//                }
//            }
//            cursor.close()
//        }
//        return contacts
//    }
//
//}