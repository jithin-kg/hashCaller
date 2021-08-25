package com.hashcaller.app.view.ui.call.dialer

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.provider.ContactsContract
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.util.Log
import com.hashcaller.app.stubs.Contact
import com.hashcaller.app.view.ui.contacts.utils.ContactLiveData
import com.hashcaller.app.work.formatPhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

class ContactSearchRepository(private val context: Context) {
    val threadPool = Executors.newCachedThreadPool().asCoroutineDispatcher()


    @SuppressLint("LongLogTag")
    suspend fun getContacts(cursor: Cursor, queryItem: String?): MutableList<Contact> = withContext(Dispatchers.IO) {
        val listOfContacts = mutableListOf<Contact>()
        var lastNumber = ""
        var prevName = ""
        var count = 0
        try {
            if(cursor != null && cursor.moveToFirst()){
                do{

                    if(count < 2){
                        count++
                    }
                    var id = cursor.getString(0).toLong()
                    var name = cursor.getString(1)
                    var phoneNo = cursor.getString(2)

                    val photoThumnail = cursor.getString(3)

                    var photoURI = if(cursor.getString(4) == null) "" else cursor.getString(4)
                    if(name!=null){
                        if(prevName != name && lastNumber != phoneNo){

                            var firstLetter = ""
                            if(name.isNotEmpty()){
                                firstLetter = name[0].toString()
                            }else{
                                val formatedNum = formatPhoneNumber(phoneNo)
                                firstLetter = formatedNum[0].toString()
                            }
                            var contact = Contact(id, name, phoneNo, photoThumnail,
                                photoURI, 1, firstletter =  firstLetter)
                            setSpannableStringBuilder(contact, queryItem, contact.firstName,  contact.phoneNumber)
                            listOfContacts.add(contact)
                            lastNumber = phoneNo
                            prevName = name
                        }

                    }


                }while (cursor.moveToNext())

            }else{
                Log.d(TAG, "getContacts: cursor has nothing to move to")
            }
        }catch (e:java.lang.Exception){
            Log.d(TAG, "getContactsLike: exception $e")
        }finally {
            cursor.close()
        }
        return@withContext listOfContacts
    }
    @SuppressLint("LongLogTag")
    suspend fun getContactsLike(queryItem: String): MutableList<Contact> = withContext(Dispatchers.IO)  {
            val cursor: Cursor? = CursorCreator.createContactsSearchCursor(queryItem, context)



        return@withContext getContacts(cursor!!, queryItem)

    }

    private fun setSpannableStringBuilder(
        contact: Contact,
        searchQuery: String?,
        name: String,
        num: String
    ) {
        val lowercaseNum = num.toLowerCase()
        var nameStr = name
        var spannableStringBuilder: SpannableStringBuilder?

        if (searchQuery != null) {
            val lowercaseName = nameStr.toLowerCase()
            val lowerSearchQuery = searchQuery.toLowerCase()
            contact.phoneSpann = SpannableStringBuilder(num)
            contact.nameSpann = SpannableStringBuilder(nameStr)

            if (lowercaseName.contains(lowerSearchQuery) && searchQuery.isNotEmpty()) {
                //search query pressent in sms body
                var startPos =
                    lowercaseName.indexOf(lowerSearchQuery) //getting the index of search query in msg body
                var endPos = 0
//                if(startPos > 50){
//                    nameStr = "... " + nameStr.substring(startPos)
//                    startPos = 4
//                }

                endPos = startPos + lowerSearchQuery.length
                contact.spanStartPosName = startPos
                contact.spanEndPosName = endPos
            }
            if (lowercaseNum.contains(searchQuery) && searchQuery.isNotEmpty()) {
                val startPos = lowercaseNum.indexOf(searchQuery)
                val endPos = startPos + searchQuery.length
                val yellow = BackgroundColorSpan(Color.YELLOW)
                contact.spanStartPosNum = startPos
                contact.spanEndPosNum = endPos
            }
        }
//        else {
//            spannableStringBuilder =
//                SpannableStringBuilder(msg)
//            objSMS.msg = spannableStringBuilder
//            objSMS.address = SpannableStringBuilder(num)
//        }

    }

    suspend fun getAllContacts(): MutableList<Contact> = withContext(Dispatchers.IO) {
        val listOfContacts = mutableListOf<Contact>()
        var hasSetOfNumbers: HashSet<String> = hashSetOf()
        var cursor:Cursor? = null

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
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " COLLATE NOCASE ASC"
            )
            if(cursor != null && cursor.moveToFirst()){
                do{

                    //                val id = cursor.getLong(0)
                    //                val name = cursor.getString(1)
//                    val id = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts.NAME_RAW_CONTACT_ID))
//                    Log.d(TAG, "id is $id ")
//                    val name =
//                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
//                     val photoURI =  cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI))
//                    var photoThumnail = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI))
//
//                    ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
//                    val phoneNo =
//                        cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

//                    Log.d(TAG, "phone num is $phoneNo")
//                    Log.d(TAG, "name is  $name")
                    var id = cursor.getString(0).toLong()
                    var name = cursor.getString(1)
                    var phoneNo = cursor.getString(2)
                    if(!hasSetOfNumbers.contains(phoneNo)){
                        hasSetOfNumbers.add(phoneNo)

                    }else{
                        continue
                    }
                    val photoThumnail = cursor.getString(3)

                    var photoURI = if(cursor.getString(4) == null) "" else cursor.getString(4)
                    if(name!=null){
                            listOfContacts.add(Contact(
                                id,
                                name,
                                phoneNo,
                                photoThumnail,
                                photoURI

                            ))


                    }



                }while (cursor.moveToNext())
            }
        }catch (e:Exception){
            Log.e(TAG, "getContacts: execption $e")
        }finally {
            cursor?.close()
        }
        return@withContext listOfContacts
    }





    companion object {
        const val TAG = "__ContactSearchRepository"
    }
}