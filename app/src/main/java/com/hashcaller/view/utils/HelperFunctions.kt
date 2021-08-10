package com.hashcaller.view.utils

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.android.material.textfield.TextInputLayout
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


private const val TAG = "__HelperFunctions"

fun getDecodedBytes(photoUri:String): Bitmap? {
    val decodedString: ByteArray =
        android.util.Base64.decode(photoUri, android.util.Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
}

/**
 * function to validate user input in getinitialinfo activity and profile acticity forms
 */
@SuppressLint("LongLogTag")
 fun validateInput(firstName: String, lastName: String,
                          textFieldOne: TextInputLayout, outlineField2: TextInputLayout
): Boolean {
    var isValid = true;
    if( firstName.isEmpty() || firstName.length < 2 || firstName.length > 25){
        textFieldOne.error = "First name should contain at least 2 characters"
        isValid = false;
    }
    if(firstName.length> 25){
        textFieldOne.error = "First name can have maximum 25 characters"
        isValid = false
    }
    if( lastName.length > 25){
        outlineField2.error = "Last name can have maximum 25  characters"
        isValid = false;

    }
    if( lastName.isEmpty() || lastName.length < 1 ){
        outlineField2.error = "Last name should contain at least 1 characters"
        isValid = false;
    }

    return isValid
}
fun getDate(milliSeconds: Long, dateFormat: String?): String? {
    // Create a DateFormatter object for displaying date in specified format.
    val formatter = SimpleDateFormat(dateFormat)

    // Create a calendar object that will convert the date and time value in milliseconds to date.
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = milliSeconds
    return formatter.format(calendar.time)
}


 fun getRelativeTime(
     dateMilli: Long
 ): String {
    val days = getDaysDifference(dateMilli)
     var relativeTime = ""

    if(days == 0L ){

//                 val time = String.format("%02d" , c.get(Calendar.HOUR))+":"+
//                     String.format("%02d" , c.get(Calendar.MINUTE))
//                 val ftime = SimpleDateFormat("hh:mm:ss" ).format(time * 1000L)
        relativeTime =   setHourAndMinute(dateMilli)


    }else if(days == 1L){

        relativeTime = "Yesterday"
    }else{
//                 view.tvSMSTime.text = "prev days"

        val date = SimpleDateFormat("dd/MM/yyyy").format(Date(dateMilli))
        relativeTime = date
    }
    return relativeTime
}

/**
 * function to get call duration
 */
fun getRelativeDuration(durationInSeconds: Long): String {
    var duration = ""

      val seconds = durationInSeconds % 60
      var hour = durationInSeconds / (60 * 60)
      val min = (durationInSeconds / 60) % 60
//      hour /= 60
    if(hour > 0 && min>0 && seconds > 0){
        duration = "$hour h $min m $seconds s"
    }else if(hour >0 && min >0){
        duration = "$hour h $min m"
    }else if(hour >0){
        duration = "$hour h"
    }else if(min > 0 && seconds >0){
        duration = "$min m $seconds s"
    }else if(min >0){
        duration = "$min m"
    }else if(seconds >0){
        duration = "$seconds s"
    }


//    Log.d(TAG, "getRelativeDuration: hours $hours")
//    Log.d(TAG, "getRelativeDuration: hours $hours")
//    val minutes = TimeUnit.SECONDS.toMinutes(durationInSeconds)
//    if(hours>0){
//        duration = "$hours h"
//    }else if(minutes >0){
//        duration = "$minutes m"
//    }else if(durationInSeconds >0){
//        duration = "$durationInSeconds s"
//    }
//
//    val min =( durationInSeconds.toDouble() / 60).toDouble()
//    val hour = (min.toDouble()/60).toDouble()
//    val decimals =1
//    if(hour >=1){
//        duration = "${hour.round(decimals)} hr"
//    }else if(min >=1){
//        duration = "${min.round(decimals)} mn"
//    }else if(durationInSeconds>=1){
//        duration = "${durationInSeconds.toDouble().round(decimals)} sec"
//    }
    return duration






}

/**
 * function to return difference between today and passed date in milli secods
 */

private fun getDaysDifference(dateMilli: Long): Long {
//    val today = Date()
//    val miliSeconds: Long = today.time - dateMilli!!
//    val seconds = TimeUnit.MILLISECONDS.toSeconds(miliSeconds)
//    val minute = seconds / 60
//    val hour = minute / 60
//    val days = hour / 24
//    return days
    var differenceInDays  = 0L
//    try {
//        val oldDate = getDate(dateMilli, "dd/MM/yyyy hh:mm:ss.SSS")
//        val today = getDate(Date().time, "dd/MM/yyyy hh:mm:ss.SSS")

        val oldDate = getDate(dateMilli, "MM/dd/yyyy")
        val today = getDate(Date().time, "MM/dd/yyyy")

/////////////////
        val date1: Date?
        val date2: Date?
        val dates = SimpleDateFormat("MM/dd/yyyy")
        date1 = dates.parse(today)
        date2 = dates.parse(oldDate)
        val difference: Long = Math.abs(date1.getTime() - date2.getTime())
        val differenceDates = difference / (24 * 60 * 60 * 1000)
    differenceInDays = differenceDates as Long
         return differenceInDays



 ///////////////////

//        val  simpleDateFormat =  SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS")
//        val format = ""
//         diff = DurationUnit.DAYS.convert(
//             simpleDateFormat.parse(today).time - simpleDateFormat.parse(oldDate).time,
//             DurationUnit.MILLISECONDS
//         )
//    }catch (e: Exception){
//        Log.d(TAG, "getDaysDifference: exception $e")
//    }finally {
//        return diff
//
//    }

}


private fun setHourAndMinute(dateMilli: Long): String {
    val cc =  Calendar.getInstance()
    var relativeTime = ""
    cc.timeInMillis = dateMilli
    val formatter: DateFormat = SimpleDateFormat("hh:mm")
    val formattedIn24Hr: DateFormat = SimpleDateFormat("HH")



    relativeTime =  formatter.format(cc.time)
    val time24HrFormat = formattedIn24Hr.format(cc.time)
    if(time24HrFormat.toInt()>12){
        relativeTime = "$relativeTime pm"
    }else{
        relativeTime = "$relativeTime am"

    }
    return relativeTime
}