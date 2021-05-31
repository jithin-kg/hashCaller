package com.nibble.hashcaller.view.ui.search

import android.database.Cursor
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.view.ui.contacts.ContactsQueryHelper
import com.nibble.hashcaller.view.ui.sms.util.SMS
import com.nibble.hashcaller.view.ui.sms.util.SmsRepositoryHelper
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AllSearchRepository(
    private val smsCursor: Cursor?,
    private val contactsCursor: Cursor?,
    private val allCallLogsCursor: Cursor?,
    private val contactQueryHelper: ContactsQueryHelper,
    private val smsRepositoryHelper: SmsRepositoryHelper ) {

    private var listofContacts:List<Contact> = emptyList()
    private var mapofContacts:HashMap<String, Contact> = hashMapOf()
    private var listOfSMS:List<SMS> = emptyList()

    suspend fun setListOfContacts() = withContext(Dispatchers.IO) {
        listofContacts =  contactQueryHelper.getAllContacts()
        for (contact in listofContacts){
            val formatedNumber = formatPhoneNumber(contact.phoneNumber)
            mapofContacts[formatedNumber] = contact
        }
    }

    suspend fun searchInContacts(searchTerm: String): MutableList<Contact>  = withContext(Dispatchers.IO){
        var contactsListOfSize3:MutableList<Contact> = mutableListOf()
        for (contact in listofContacts){

            contact.spanStartPosNum = 0
            contact.spanEndPosNum = 0
            contact.spanStartPosName = 0
            contact.spanEndPosName = 0
            val lowercaseName = contact.name.toLowerCase()
            var isTobeAddedToList = false
            if( lowercaseName.contains(searchTerm, true)

            ){
                if(contactsListOfSize3.size >=3){
                    break
                }
                isTobeAddedToList = true
                contact.spanStartPosName = lowercaseName.indexOf(searchTerm)
                contact.spanEndPosName = contact.spanStartPosName + searchTerm.length
            }

            if(contact.phoneNumber.contains(searchTerm, true)){
                if(contactsListOfSize3.size >=3){
                    break
                }
                isTobeAddedToList = true

                contact.spanStartPosNum = contact.phoneNumber.indexOf(searchTerm)
                contact.spanEndPosNum = contact.spanStartPosNum + searchTerm.length
            }
            if(isTobeAddedToList){
                contactsListOfSize3.add(contact)
            }
        }
        return@withContext contactsListOfSize3

    }

    suspend fun searchInSMS(searchTerm: String): MutableList<SMS>  = withContext(Dispatchers.IO){
        var smsListOfSize3:MutableList<SMS> = mutableListOf()
        for(sms in listOfSMS){
            smsRepositoryHelper.emptySpanPositions(sms)
            var isTobeAdded = false

            if(smsListOfSize3.size >=3 ){
                break
            }
            val formatedNum = sms.addressString?.let { formatPhoneNumber(it) }
            if(!formatedNum.isNullOrEmpty()){
               val contact =  mapofContacts[formatedNum]
                if(contact !=null){
                    sms.firstName = contact.name
                    sms.photoURI = contact.photoURI
                }
                if(sms.firstName != null) {
                  if(sms.firstName!!.toLowerCase().contains(searchTerm, true) ){
                    if(formatedNum.contains(searchTerm,true)){
//                        setSpannableStringBuilder(sms, searchTerm, sms.msgString, sms.addressString!!)
                       isTobeAdded = true
                    } else{
//                        setSpannableStringBuilder(sms, searchTerm, sms.msgString, sms.addressString!!)
//                        smsListOfSize3.add(sms)
                        isTobeAdded = true
                    }
                  }
                } else {
                    if(sms.addressString!!.contains(searchTerm, true)){
//                        setSpannableStringBuilder(sms, searchTerm, sms.msgString, sms.addressString!!)
                        isTobeAdded = true
                    }
                    if(sms.msgString!=null){
                        if (sms.msgString!!.contains(searchTerm, true)){
//                            setSpannableStringBuilder(sms, searchTerm, sms.msgString, sms.addressString!!)
                            isTobeAdded = true
                        }
                    }
                }
                if(isTobeAdded){
                    setSpannableStringBuilder(sms, searchTerm, sms.msgString, sms.addressString!!)
                    smsListOfSize3.add(sms)
                }
            }

        }
        return@withContext smsListOfSize3
    }

    suspend fun setListOfSMS() = withContext(Dispatchers.IO) {

        listOfSMS = smsRepositoryHelper.fetchWithRawData()

    }


    private fun setSpannableStringBuilder(
        objSMS: SMS,
        searchQuery: String?,
        mssg: String?,
        num: String
    ) {
        val lowercaseNum = num.toLowerCase()
        var msg = mssg
        var spannableStringBuilder: SpannableStringBuilder?

        if (searchQuery != null) {
            val lowercaseMsg = (msg?.toLowerCase())?:""
            val lowerSearchQuery = searchQuery.toLowerCase()
            objSMS.address = SpannableStringBuilder(num)
            objSMS.msg = SpannableStringBuilder(msg)

            if (lowercaseMsg.contains(lowerSearchQuery, true) && searchQuery.isNotEmpty()) {
                //search query pressent in sms body
                var startPos =
                    lowercaseMsg.indexOf(lowerSearchQuery) //getting the index of search query in msg body
                var endPos = 0
                if(startPos > 50){
                    msg = "... " + msg?.substring(startPos)
                    startPos = 4 // after ... three dots and one white space
                }
                if (msg != null) {
                    objSMS.body = msg
                }
                endPos = startPos + lowerSearchQuery.length
                objSMS.spanStartPosMsgPeek = startPos
                objSMS.spanEndPosMsgPeek = endPos
            }
            if (lowercaseNum.contains(searchQuery, true) && searchQuery.isNotEmpty()) {
                val startPos = lowercaseNum.indexOf(searchQuery)
                val endPos = startPos + searchQuery.length
                val yellow = BackgroundColorSpan(Color.YELLOW)
                objSMS.spanStartPos = startPos
                objSMS.spanEndPos = endPos

            }
        }
//        else {
//            spannableStringBuilder =
//                SpannableStringBuilder(msg)
//            objSMS.msg = spannableStringBuilder
//            objSMS.address = SpannableStringBuilder(num)
//        }

    }
}