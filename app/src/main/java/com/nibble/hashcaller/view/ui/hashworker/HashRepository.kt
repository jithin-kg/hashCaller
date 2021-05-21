package com.nibble.hashcaller.view.ui.hashworker

import android.database.Cursor
import android.provider.ContactsContract
import android.util.Log
import com.nibble.hashcaller.repository.contacts.ContactUploadDTO
import com.nibble.hashcaller.view.ui.call.db.CallersInfoFromServerDAO
import com.nibble.hashcaller.view.ui.contacts.utils.DATE_THREASHOLD
import com.nibble.hashcaller.view.ui.contacts.utils.isCurrentDateAndPrevDateisGreaterThanLimit
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.*
import java.util.stream.Collector
import java.util.stream.Collectors
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class HashRepository(
    private val callLogCursor: Cursor?,
    private val contactsCursor: Cursor?,
    private val hashedNumDao: IHashedNumbersDAO,
    private val callerInfoFromServerDAO: CallersInfoFromServerDAO
) {

    suspend fun getListOfUnkownCallers(): MutableList<String> = withContext(Dispatchers.IO) {
        val setOfContacts = getSetOfConatcts()
        var listOfUnknwonCallers: MutableList<String> = mutableListOf()
         val callerInfoFromServerList = getCallerInfofromServer()
        try {
            if(callLogCursor != null && callLogCursor.moveToFirst()){
                do{
                    var isTobeAddedToFinalList = false
                    var i = 0
                    var number = callLogCursor.getString(0)
                    val formatedNum = formatPhoneNumber(number)
                    if(!setOfContacts.contains(formatedNum)){

                        val informationReceivedDate:Date?= callerInfoFromServerList.get(formatedNum)
                         if(informationReceivedDate!=null) {
                             //this number is already searched in server
                             //check if that number info is outdated
                             if(isCurrentDateAndPrevDateisGreaterThanLimit(informationReceivedDate, DATE_THREASHOLD)){
                                 //this data is outdated
                                 isTobeAddedToFinalList = true
                             }
                         }else{
                             isTobeAddedToFinalList = true
                         }
                        if(isTobeAddedToFinalList){
                            listOfUnknwonCallers.add(formatedNum)
                        }
                    }else{
                        continue
                    }

                }while (callLogCursor.moveToNext())
            }
        }catch (e: java.lang.Exception){

            Log.d(TAG, "getRawCallLogs: exception $e")
        }finally {
            callLogCursor?.close()
        }

        return@withContext listOfUnknwonCallers
    }

    private suspend fun getCallerInfofromServer(): HashMap<String, Date> {

        val callersInfoFromServerList =  callerInfoFromServerDAO?.getAll()

        val mapOfNumbersAlreadyAvialableInDatabase:HashMap<String, Date> = hashMapOf()
        for(item in callersInfoFromServerList){
//            if(isCurrentDateAndPrevDateisGreaterThanLimit(item.informationReceivedDate, DATE_THREASHOLD)){
                mapOfNumbersAlreadyAvialableInDatabase.put(formatPhoneNumber(item.contactAddress), item.informationReceivedDate )

//            }
        }
        return mapOfNumbersAlreadyAvialableInDatabase

    }

    private suspend fun getSetOfConatcts(): HashSet<String> {
        var hashSetOfAddress : HashSet<String> = HashSet()
       try {
           if (contactsCursor?.count ?: 0 > 0) {
               while (contactsCursor!!.moveToNext()) {
                   var contact = ContactUploadDTO()
                   val name =
                       contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                   var phoneNo =
                       contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                   phoneNo = formatPhoneNumber(phoneNo)
                   if(!hashSetOfAddress.contains(phoneNo)){
                       hashSetOfAddress.add(phoneNo)
                   }else{
                       continue
                   }
               }
               contactsCursor.close()
           }
       }catch (e:Exception){
           Log.d(TAG, "getListOfConatcts: $e")
       }
        return hashSetOfAddress
    }

    companion object {
        const val TAG = "__Repository"
    }
}