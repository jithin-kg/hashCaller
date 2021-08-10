package com.hashcaller.repository.contacts

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.provider.ContactsContract
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.util.Log
import androidx.lifecycle.LiveData
import com.hashcaller.local.db.HashCallerDatabase
import com.hashcaller.local.db.contactInformation.ContactTable
import com.hashcaller.local.db.contactInformation.IContactIformationDAO
import com.hashcaller.stubs.Contact
import com.hashcaller.utils.getStringValue
import com.hashcaller.view.ui.contacts.utils.ContactLiveData
import com.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    suspend fun getContact(phonNumber:String): ContactTable?  = withContext(Dispatchers.IO){
        val res = contactLisDAO?.search(phonNumber)
//        val res = contactLisDAO?.search()
//        contactsFomLocalDB = res
        return@withContext res
    }

    @SuppressLint("LongLogTag")
    suspend fun insertContacts(preparedContacts: List<ContactTable>) = withContext(Dispatchers.IO) {
        Log.d(TAG, "insertContacts: ")
        val insert = contactLisDAO?.insert(preparedContacts)
        Log.d(TAG, "insertContacts:$insert ")

    }

    suspend fun insertSingleContactItem(c: ContactTable) = withContext(Dispatchers.IO) {
        contactLisDAO!!.insertSingleItem(c)
    }

    /**
     * functon to get contacts like the passed in contact address while searching for contacts
     * in content provider
     */
    @SuppressLint("LongLogTag")
    suspend fun getContactsLike(queryString: String): MutableList<Contact>  = withContext(Dispatchers.IO)  {

        val listOfContacts = mutableListOf<Contact>()
        var lastNumber = ""
        var prevName = ""
        var count = 0
        val cursor:Cursor? = createCursor(queryString)
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
            }
        }catch (e:java.lang.Exception){
            Log.d(TAG, "getContactsLike: exception $e")
        }finally {

            cursor?.close()

        }


        return@withContext listOfContacts

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

    suspend fun deleteAllitems()   = withContext(Dispatchers.IO){
        contactLisDAO?.delete()
        HashCallerDatabase.getDatabaseInstance(context).callersInfoFromServerDAO().deleteAll()
        HashCallerDatabase.getDatabaseInstance(context).callLogDAO().deleteAll()
        HashCallerDatabase.getDatabaseInstance(context).smsThreadsDAO().deleteAll()

    }

    @SuppressLint("LongLogTag")
    suspend fun getNameFromPhoneNumber(phoneNumber: String): String = withContext(Dispatchers.IO) {
        var cursor:Cursor? = null
        var name = ""

        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
        val projection = arrayOf(
            ContactsContract.PhoneLookup.DISPLAY_NAME
        )
        try {
             cursor = context.contentResolver.query(uri, projection, null, null, null)
            cursor.use {
                if (cursor?.moveToFirst() == true) {
                    name=  cursor.getStringValue(ContactsContract.PhoneLookup.DISPLAY_NAME)
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "getNameFromPhoneNumber: $e")
        }
        finally {
            cursor?.close()
        }

        return@withContext name
    }


    companion object{
        private const val TAG = "__ContactLocalSyncRepository"
    }

}