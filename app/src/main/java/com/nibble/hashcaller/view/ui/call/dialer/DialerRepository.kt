package com.nibble.hashcaller.view.ui.call.dialer

import android.content.Context
import android.telephony.PhoneNumberUtils
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.view.ui.call.db.CallLogTable
import com.nibble.hashcaller.view.ui.call.db.ICallLogDAO
import com.nibble.hashcaller.view.ui.sms.individual.util.normalizeString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DialerRepository(private val context: Context, private val callLogDAO: ICallLogDAO?) {
     suspend fun getFirst10Logs(): MutableList<CallLogTable>? = withContext(Dispatchers.IO)  {
         return@withContext callLogDAO?.getFirst10Logs()
    }

    suspend fun getFilteredlist(text: String, allContacts: MutableList<Contact>?) = withContext(Dispatchers.IO) {
        var filtered = allContacts?.filter {
            it.spanStartPosName = 0
            it.spanEndPosName = 0
            it.spanStartPosNum = 0
            it.spanEndPosNum = 0
            //converting name to digits, "1-800-GOOG-411" will return "1-800-4664-411"
            val convertedName = PhoneNumberUtils.convertKeypadLettersToDigits(it.name.normalizeString())
            //check if converted name contains search text
            if(it.doesContainPhoneNumber(text)){
                //phone number contains
                it.spanStartPosNum = it.phoneNumber.indexOf(text)
                it.spanEndPosNum = it.spanStartPosNum + text.length
            }
            if(convertedName.contains(text, true)){
                //
                it.spanStartPosName = convertedName.indexOf(text)
                it.spanEndPosName = it.spanStartPosName + text.length
            }
            it.doesContainPhoneNumber(text) || (convertedName.contains(text, true))

        }?.sortedWith(compareBy{
            !it.doesContainPhoneNumber(text)
        })?.toMutableList()
        val newList:MutableList<Contact> = mutableListOf()
        filtered?.let { newList.addAll(it) }

        return@withContext newList
    }


}