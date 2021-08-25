package com.hashcaller.app.view.ui.hashworker

import android.database.Cursor
import android.provider.ContactsContract
import android.util.Log
import com.hashcaller.app.repository.contacts.PhoneNumWithHashedNumDTO
import com.hashcaller.app.view.ui.call.db.CallersInfoFromServerDAO
import com.hashcaller.app.view.ui.contacts.utils.DATE_THREASHOLD
import com.hashcaller.app.view.ui.contacts.utils.isCurrentDateAndPrevDateisGreaterThanLimit
import com.hashcaller.app.work.formatPhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.Exception
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class HashRepository(
    private val callLogCursor: Cursor?,
    private val contactsCursor: Cursor?,
    private val hashedNumDao: IHashedNumbersDAO,
    private val callerInfoFromServerDAO: CallersInfoFromServerDAO,
    private val setOfContacts: java.util.HashSet<String>,
    private val smsCurosor: Cursor?,
    private val setofAddressInDb: HashSet<String>
) {

    suspend fun getListOfUnkownCallers(): MutableList<String> = withContext(Dispatchers.IO) {
        var listOfUnknwonCallers: MutableList<String> = mutableListOf()
         val callerInfoFromServerList = getCallerInfofromServer()
        try {
            if(callLogCursor != null && callLogCursor.moveToFirst()){
                do{
                    var isTobeAddedToFinalList = false
                    var i = 0
                    var number = callLogCursor.getString(0)
                    val formatedNum = formatPhoneNumber(number)
                    if(!setofAddressInDb.contains(formatedNum)){
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
                   var contact = PhoneNumWithHashedNumDTO()
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

    suspend fun getListOfAllUnkonSMSSenders(): MutableList<String> = withContext(Dispatchers.IO) {
        var listOfUnknownSMSSenders:MutableList<String> = mutableListOf()

        try {
            if (smsCurosor != null && smsCurosor.moveToFirst()) {
                do {
                    var num =
                        smsCurosor.getString(smsCurosor.getColumnIndexOrThrow("address"))
                    val formatedNum = formatPhoneNumber(num)

                    if(!setofAddressInDb.contains(formatedNum)){
                        if(!setOfContacts.contains(formatedNum)){
                            listOfUnknownSMSSenders.add(formatedNum)
                        }
                    }
                }while (smsCurosor.moveToNext())
            }
            }catch (e:Exception){
            Log.d(TAG, "getListOfAllUnkonSMSSenders: ")
        }
        return@withContext listOfUnknownSMSSenders
    }

    companion object {
        const val TAG = "__Repository"
    }
}