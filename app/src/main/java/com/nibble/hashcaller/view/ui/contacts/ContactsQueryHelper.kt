package com.nibble.hashcaller.view.ui.contacts

import android.database.Cursor
import android.util.Log
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.view.ui.contacts.utils.ContactLiveData
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactsQueryHelper(private val cursor: Cursor?) {

    suspend fun getAllContacts(): MutableList<Contact> {
        val listOfContacts = mutableListOf<Contact>()
        val setOfContacts = mutableSetOf<String>()
        try {

            if(cursor != null && cursor.moveToFirst()){

                do{
                    var id = cursor.getString(0).toLong()
                    var name = cursor.getString(1)
                    var phoneNo = cursor.getString(2)

                    val photoThumnail = cursor.getString(3)

                    var photoURI = if(cursor.getString(4) == null) "" else cursor.getString(4)
                    if(name!=null){
                       val formatedNum =  formatPhoneNumber(phoneNo)
                       if(!setOfContacts.contains(formatedNum)){
                           listOfContacts.add(
                               Contact(
                                   id,
                                   name,
                                   phoneNo,
                                   photoThumnail,
                                   photoURI
                               )
                           )
                           setOfContacts.add(formatedNum)
                       }
                    }
                }while (cursor.moveToNext())
            }
        }catch (e:Exception){
            Log.e(TAG, "getContacts: execption $e")
        }finally {
            cursor?.close()
        }

        return listOfContacts
    }

    companion object{
        const val TAG = "__ContactsQueryHelper"
    }

}