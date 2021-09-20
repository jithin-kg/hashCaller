package com.hashcaller.app.view.ui.contacts

import android.content.Context
import android.util.Log
import com.hashcaller.app.stubs.Contact
import com.hashcaller.app.work.formatPhoneNumber

class ContactsQueryHelper(private val context: Context?) {

    suspend fun getAllContacts(isLimitedContactsNeeded:Boolean = false): MutableList<Contact> {
        val listOfContacts = mutableListOf<Contact>()
        val setOfContacts = mutableSetOf<String>()

        val cursor =  context?.getAllContactsCursor(isLimitedContactsNeeded)
        try {
            if(cursor != null && cursor.moveToFirst()){
                do{
                    var id = cursor.getString(0).toLong()
                    var name = cursor.getString(1)
                    var phoneNo = cursor.getString(2)
                    phoneNo  = formatPhoneNumber(phoneNo)
                    val photoThumnail = cursor.getString(3)

                    var photoURI = if(cursor.getString(4) == null) "" else cursor.getString(4)
                    if(name!=null){
                       val formatedNum =  formatPhoneNumber(phoneNo)
                       if(!setOfContacts.contains(formatedNum)){
                           listOfContacts.add(
                               Contact(
                                   id,
                                   name,
                                   phoneNumber = formatedNum,
                                   photoThumnail,
                                   photoURI,

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

    suspend fun getAllContactsForSearchActivity(isLimitedContactsNeeded:Boolean = false): MutableList<Contact> {
        val listOfContacts = mutableListOf<Contact>()
        val setOfContacts = mutableSetOf<String>()

        val cursor =  context?.getAllContactsCursor(isLimitedContactsNeeded)
        try {
            if(cursor != null && cursor.moveToFirst()){
                do{
                    var id = cursor.getString(0).toLong()
                    var name = cursor.getString(1)
                    var phoneNo = formatPhoneNumber(cursor.getString(2))

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