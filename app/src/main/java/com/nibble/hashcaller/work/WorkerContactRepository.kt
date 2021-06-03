package com.nibble.hashcaller.work

import android.database.Cursor
import android.provider.ContactsContract
import com.nibble.hashcaller.repository.contacts.ContactUploadDTO
import com.nibble.hashcaller.view.ui.contacts.utils.contactWithMetaDataForSms
import com.nibble.hashcaller.view.utils.ContactGlobal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.LinkedHashSet
import java.util.concurrent.atomic.AtomicBoolean

class WorkerContactRepository(private val cursor: Cursor?)  {

    private var contacts: MutableList<ContactUploadDTO> = ArrayList()
    var uniqueMobilePhones: List<ContactUploadDTO> = ArrayList()
    var lastNumber = "0"

    suspend fun fetchContacts(): MutableList<ContactUploadDTO> = withContext(Dispatchers.IO) {

        var hashSetOfAddress : HashSet<String> = HashSet()


        if (cursor?.count ?: 0 > 0) {
            while (cursor!!.moveToNext()) {
                var contact = ContactUploadDTO()
                val name =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                var phoneNo =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                phoneNo = formatPhoneNumber(phoneNo)
                if(!hashSetOfAddress.contains(phoneNo)){
                    hashSetOfAddress.add(phoneNo)
                }else{
                    continue
                }

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

        return@withContext sortAndSet(contacts)
    }

    private fun sortAndSet(contacts:  MutableList<ContactUploadDTO>): ArrayList<ContactUploadDTO> {
        val s: LinkedHashSet<ContactUploadDTO> = LinkedHashSet(contacts)
        val data = ArrayList(s)

        return data
    }

    suspend fun setContactsMetaInfoHashMap() {
//        val contacts = fetchContacts()
        for(contact in contacts){
            val obj = ContactGlobal(contact.phoneNumber, contact.name)
            contactWithMetaDataForSms.put(contact.phoneNumber, obj )
        }
    }




}