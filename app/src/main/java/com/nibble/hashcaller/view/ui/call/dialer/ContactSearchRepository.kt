package com.nibble.hashcaller.view.ui.call.dialer

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.core.content.ContextCompat
import com.nibble.hashcaller.R
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.view.ui.sms.util.SMS
import com.nibble.hashcaller.work.formatPhoneNumber

class ContactSearchRepository(private val context: Context) {
    @SuppressLint("LongLogTag")
    suspend fun getContactsLike(queryString: String): MutableList<Contact>  {

        val listOfContacts = mutableListOf<Contact>()
        var lastNumber = ""
        var prevName = ""
        var count = 0
            val cursor: Cursor? = CursorCreator.createContactsSearchCursor(queryString, context)
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
                                setSpannableStringBuilder(contact, queryString, contact.name,  contact.phoneNumber)
                                listOfContacts.add(contact)
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


    companion object {
        const val TAG = "__ContactSearchRepository"
    }
}