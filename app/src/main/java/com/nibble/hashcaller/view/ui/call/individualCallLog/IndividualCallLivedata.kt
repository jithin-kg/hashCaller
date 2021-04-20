package com.nibble.hashcaller.view.ui.call.individualCallLog

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CallLog
import android.telecom.Call
import android.util.Log
import com.nibble.hashcaller.view.ui.call.db.CallLogTable
import com.nibble.hashcaller.view.ui.contacts.utils.ContentProviderLiveData

class IndividualCallLivedata(private val context: Context): ContentProviderLiveData<MutableList<IndividualCallLogObj>>(
    context,
    URI
)  {

    @SuppressLint("LongLogTag")
    override suspend fun getContentProviderValue(text: String?): MutableList<IndividualCallLogObj> {
        /**
         * The ISO 3166-1 two letters country code of the country where the
         * user received or made the call.
         * <P>
         * Type: TEXT
        </P> *
         */
//        val COUNTRY_ISO = "countryiso"
        /**
         * A geocoded location for the number associated with this call.
         *
         *
         * The string represents a city, state, or country associated with the number.
         * <P>Type: TEXT</P>
         */

//        val GEOCODED_LOCATION = "geocoded_location"
        /**
         * The identifier for the account used to place or receive the call.
         * <P>Type: TEXT</P>
         */

        val PHONE_ACCOUNT_ID = "subscription_id"

        var list: MutableList<IndividualCallLogObj> = mutableListOf()
        val projection = arrayOf(
            CallLog.Calls._ID,                  //0
            CallLog.Calls.NUMBER,               //1
            CallLog.Calls.CACHED_NAME,          //2
            CallLog.Calls.TYPE,                 //3
            CallLog.Calls.DURATION,             //4
            CallLog.Calls.COUNTRY_ISO,          //5
            CallLog.Calls.GEOCODED_LOCATION,    //6
            CallLog.Calls.PHONE_ACCOUNT_ID,     //7
            CallLog.Calls.CACHED_PHOTO_URI,     //8
            CallLog.Calls.DATE                  //9
        )
        var cursor: Cursor? = null

        try {

            cursor = context.contentResolver.query(
                URI,
                projection,
                null,
                null,
                CallLog.Calls.DATE + " DESC"
            )
            if(cursor != null && cursor.moveToFirst()) {
                do {

                    val id  = cursor.getInt(0)
                    val number = cursor.getString(1)
                    val name = cursor.getString(2)
                    val type = cursor.getInt(3)
                    val duration = cursor.getLong(4)
                    val countryiso = cursor.getString(5)
                    val geocodedLocation = cursor.getString(6)
                    val subscriptionId = cursor.getString(7)
                    val photoUri = cursor.getString(8)
                    val date = cursor.getLong(9)

                    val obj = IndividualCallLogObj(id,
                        number,
                        name,
                        type,
                        duration,
                        countryiso,
                        geocodedLocation,
                        subscriptionId,
                        photoUri,
                        date
                    )
                    list.add(obj)
                }while (cursor.moveToNext())
            }

        }catch (e: Exception){
            Log.d(TAG, "getContentProviderValue: exception $e")
        }
        finally {
            cursor?.close()
        }
        return list
    }


    companion object{
        val URI =  Uri.withAppendedPath(
            CallLog.Calls.CONTENT_FILTER_URI,
            Uri.encode("+919495617494")
        );

        const val TAG = "__IndividualCallLivedata"
    }
}


//while (i<columCount){
//                      Log.d(
//                          TAG, "${cursor.getColumnName(i)} : ${
//                              cursor.getString(
//                                  cursor.getColumnIndexOrThrow(
//                                      cursor.getColumnName(
//                                          i
//                                      )
//                                  )
//                              )
//                          }"
//                      )
//                    i++
//                  }