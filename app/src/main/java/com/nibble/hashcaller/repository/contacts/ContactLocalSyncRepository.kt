package com.nibble.hashcaller.repository.contacts

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.provider.ContactsContract
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import com.nibble.hashcaller.R
import com.nibble.hashcaller.local.db.contactInformation.ContactTable
import com.nibble.hashcaller.local.db.contactInformation.IContactIformationDAO
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.view.ui.contacts.generateCircleView
import com.nibble.hashcaller.view.ui.contacts.search.ActivitySearchPhone
import com.nibble.hashcaller.view.ui.contacts.utils.ContactLiveData
import com.nibble.hashcaller.work.formatPhoneNumber
import java.util.*

/**
 * Created by Jithin KG on 01,August,2020
 */
class ContactLocalSyncRepository(
    private val contactLisDAO: IContactIformationDAO?,
    private val context: Context
) {
     fun getCount(): LiveData<Int>? {
       return contactLisDAO?.getCount()
    }

    var  contactsFomLocalDB = contactLisDAO?.getContacts()

    suspend fun getContact(phonNumber:String): ContactTable? {
        val res = contactLisDAO?.search(phonNumber)
//        val res = contactLisDAO?.search()
//        contactsFomLocalDB = res
       return  res
    }

    @SuppressLint("LongLogTag")
    suspend fun insertContacts(preparedContacts: List<ContactTable>) {
        Log.d(TAG, "insertContacts: ")
        val insert = contactLisDAO?.insert(preparedContacts)
        Log.d(TAG, "insertContacts:$insert ")

    }

    suspend fun insertSingleContactItem(c: ContactTable) {
    contactLisDAO!!.insertSingleItem(c)
    }

    /**
     * functon to get contacts like the passed in contact address while searching for contacts
     * in content provider
     */
    @SuppressLint("LongLogTag")
    fun getContactsLike(queryString: String): MutableList<Contact> {
        val listOfContacts = mutableListOf<Contact>()
         var lastNumber = ""
         var prevName = ""
        var count = 0
        val cursor:Cursor? = createCursor(queryString)
        try {
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
//                    var drawable = context.generateCircleView()
                    //we are doing this is ensure that the first 3items and
                    //recycler view 3 items have same color for circle
                    if(count < 2){
//                       if(count==0){
//                           drawable = context.generateCircleView(ActivitySearchPhone.num1)
//                       }else if(count==1){
//                           drawable = context.generateCircleView(ActivitySearchPhone.num2)
//
//                       }
                        count++

                    }
                    var id = cursor.getString(0).toLong()
                    var name = cursor.getString(1)
                    var phoneNo = cursor.getString(2)

                    val photoThumnail = cursor.getString(3)

                    var photoURI = if(cursor.getString(4) == null) "" else cursor.getString(4)
                    if(name!=null){
                        if(prevName != name && lastNumber != phoneNo){
                            val nameSpann = getSpannableStringBuilderName(name,  queryString)
                            val phoneSpann = getSpnabelStringPhone(phoneNo, queryString)
                            var firstLetter = ""
                            if(name.isNotEmpty()){
                                firstLetter = name[0].toString()
                            }else{
                                val formatedNum = formatPhoneNumber(phoneNo)
                                firstLetter = formatedNum[0].toString()
                            }
                            val add = listOfContacts.add(
                                Contact(
                                    id,
                                    name,
                                    phoneNo,
                                    photoThumnail,
                                    photoURI,
                                    1,
                                    nameSpann,
                                    phoneSpann,
                                    firstLetter
                                )
                            )
                            lastNumber = phoneNo
                            prevName = name
                        }

                    }


                }while (cursor.moveToNext())
                cursor.close()
            }
        }catch (e:java.lang.Exception){
            Log.d(TAG, "getContactsLike: exception $e")
        }
        return listOfContacts

    }



    private fun getSpnabelStringPhone(phoneNo: String?, queryString: String): SpannableStringBuilder? {
        return getSpannedString(phoneNo!!, queryString)
    }

    private fun getSpannableStringBuilderName(
        nameStr: String,
        queryString: String
    ): SpannableStringBuilder? {

       return getSpannedString(nameStr, queryString)

    }

    private fun getSpannedString(str: String, queryString: String): SpannableStringBuilder? {
        var name = str
        var spannableStr: SpannableStringBuilder?

        if (queryString != null) {
            val lowercaseMsg = name.toLowerCase()
            val lowerSearchQuery = queryString.toLowerCase()

            if (lowercaseMsg.contains(lowerSearchQuery) && queryString.isNotEmpty()) {
                //search query pressent in sms body
                var startPos =
                    lowercaseMsg.indexOf(lowerSearchQuery) //getting the index of search query in msg body
                var endPos = 0
                if(startPos > 50){
                    name = "... " + name.substring(startPos)
                    startPos = 4
                }
                endPos = startPos + lowerSearchQuery.length
                val yellow =
                    BackgroundColorSpan(Color.YELLOW)
                spannableStr =
                    SpannableStringBuilder(name)
                spannableStr.setSpan(
                    yellow,
                    startPos,
                    endPos,
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE
                )

            }

            else {
                spannableStr =
                    SpannableStringBuilder(name)
            }
        } else {
            spannableStr =
                SpannableStringBuilder(name)
        }
        return spannableStr
    }



    @SuppressLint("LongLogTag")
    private fun createCursor(queryString: String): Cursor? {
        var cursor:Cursor? = null
        val selectionArgs = arrayOf("%$queryString%", "%$queryString%")
        //phone number like queryString or  display name like query string.

        var selection = "${ContactsContract.CommonDataKinds.Phone.NUMBER} " +
                "LIKE ? OR ${ContactsContract.Contacts.DISPLAY_NAME}  LIKE ? "
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
                selection,
                 selectionArgs,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " COLLATE NOCASE ASC"
            )
        }catch (e:Exception){
            Log.d(TAG, "createCursor: $e")
        }

        return cursor

    }


    companion object{
        private const val TAG = "__ContactLocalSyncRepository"
    }

}