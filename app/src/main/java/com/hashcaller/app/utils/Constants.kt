package com.hashcaller.app.utils

import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Keep
import com.bumptech.glide.Glide
import com.hashcaller.app.stubs.Contact
import java.util.*
import java.util.concurrent.TimeUnit

@Keep
class Constants {
   companion object{
       const val TAG = "__Constants"
       //spam constatnts
      const val SPAMMER_TYPE = "spammer_typ"
      const val SPAMMER_TYPE_BUSINESS = 1

       //sim
       const val SIM_ONE = 1
       const val SIM_TWO = 2
       const val NO_SIM_DETECTED = -1

       const val DEFAULT_SPAM_THRESHOLD = 15

       fun setNameInView(textVFullName: TextView, contact: Contact): String {
           var name = ""
           if(!contact.firstName.isNullOrEmpty()){
               name = contact.firstName + " " + contact.lastName
           }else if(!contact.nameInPhoneBook.isNullOrEmpty()){
               name = contact.nameInPhoneBook
           }else {
               name = contact.phoneNumber
           }
           textVFullName.text = name
           return name
       }

       /**
        * returns true if outdated
        * @param informationReceivedDate : date at which the data is inserted in db
        * @param limit : update threshold
        */

       fun isDataOutdated(
           informationReceivedDate: Date,
           limit: Int
       ): Boolean {
           val today = Date()
           val miliSeconds: Long = today.getTime() - informationReceivedDate.getTime()
           val seconds = TimeUnit.MILLISECONDS.toSeconds(miliSeconds)
           val minute = seconds / 60
           val hour = minute / 60
           val days = hour / 24
           if(days > limit)
               return true
           return false
       }

       /**
        * function to get relative name from different name available for a number from different sources
        *
        */
       fun getCorrectName(){

      }
   }



}