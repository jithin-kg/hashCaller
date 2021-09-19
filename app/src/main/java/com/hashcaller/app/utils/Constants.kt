package com.hashcaller.app.utils

import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Keep
import com.bumptech.glide.Glide
import com.hashcaller.app.stubs.Contact

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
        * function to get relative name from different name available for a number from different sources
        *
        */
       fun getCorrectName(){

      }
   }



}