package com.hashcaller.repository.contacts

import androidx.annotation.Keep


/**
 * Created by Jithin KG on 25,July,2020
 */

/**
 * By adding @JsonClass(generateAdapter = true)
 * which whill generate jsonadapter which handles the
 * serialization and deserialization to and fro Json of ContactUploadDTO
 *
 */
@Keep
data class PhoneNumWithHashedNumDTO(
  var name:String="",
   var phoneNumber:String="",
    var hashedPhoneNumber:String = ""
  ) {


}