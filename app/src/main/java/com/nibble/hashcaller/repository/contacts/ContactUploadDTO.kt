package com.nibble.hashcaller.repository.contacts



/**
 * Created by Jithin KG on 25,July,2020
 */

/**
 * By adding @JsonClass(generateAdapter = true)
 * which whill generate jsonadapter which handles the
 * serialization and deserialization to and fro Json of ContactUploadDTO
 *
 */
data class ContactUploadDTO(
  var name:String="",
   var phoneNumber:String="") {


}