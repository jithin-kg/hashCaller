package com.hashcaller.view.utils

import android.content.Context
import android.provider.ContactsContract
import com.hashcaller.repository.contacts.PhoneNumWithHashedNumDTO
import com.hashcaller.view.ui.contacts.utils.contactWithMetaDataForSms
import com.hashcaller.work.formatPhoneNumber
import java.util.LinkedHashSet
import java.util.concurrent.atomic.AtomicBoolean

class ContactRepositoryTwo(context: Context) {

    private val context: Context? = context
    private var contacts: MutableList<PhoneNumWithHashedNumDTO> = ArrayList()
    var uniqueMobilePhones: List<PhoneNumWithHashedNumDTO> = ArrayList()
    var lastNumber = "0"

    fun fetchContacts(): MutableList<PhoneNumWithHashedNumDTO> {
        val cursor = context!!.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )


        if (cursor?.count ?: 0 > 0) {
            while (cursor!!.moveToNext()) {
                var contact = PhoneNumWithHashedNumDTO()
                val name =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                var phoneNo =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                phoneNo = formatPhoneNumber(phoneNo)

                val photoUri =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
                val duplicate =
                    AtomicBoolean(false)

                if (lastNumber != phoneNo) {
//                    contact.name = name
//                    contact.phoneNumber = phoneNo
                    //add first 8 number digits for getting  geographical information about a number in api
                    if(phoneNo.length>7){
                        contact.hashedPhoneNumber = phoneNo
                    }else{
                        contact.hashedPhoneNumber = phoneNo
                    }

                    contact.name = name

                    //encode and hash phone number
//                    phoneNo = Secrets().managecipher(context.packageName, phoneNo)
                    contact.phoneNumber = phoneNo

                    contacts.add(contact)
                    lastNumber = phoneNo
                }
            }
            cursor.close()
        }

        return sortAndSet(contacts)
    }

    private fun sortAndSet(contacts:  MutableList<PhoneNumWithHashedNumDTO>): ArrayList<PhoneNumWithHashedNumDTO> {
        val s: LinkedHashSet<PhoneNumWithHashedNumDTO> = LinkedHashSet(contacts)
        val data = ArrayList(s)

        return data
    }

    suspend fun setContactsMetaInfoHashMap() {
        val contacts = fetchContacts()
        for(contact in contacts){
            val obj = ContactGlobal(contact.phoneNumber, contact.name)
            contactWithMetaDataForSms.put(contact.phoneNumber, obj )
        }
    }


}