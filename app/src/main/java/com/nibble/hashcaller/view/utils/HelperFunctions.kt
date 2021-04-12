package com.nibble.hashcaller.view.utils

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit



 fun getRelativeTime(
    dateMilli: Long
): String {
    val days = getDaysDifference(dateMilli)
    var relativeTime = ""
    if(days == 0L ){

//                 val time = String.format("%02d" , c.get(Calendar.HOUR))+":"+
//                     String.format("%02d" , c.get(Calendar.MINUTE))
//                 val ftime = SimpleDateFormat("hh:mm:ss" ).format(time * 1000L)
        relativeTime =   setHourAndMinute( dateMilli)


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
 * function to return difference between today and passed date in milli secods
 */
private fun getDaysDifference(dateMilli: Long): Long {
    val today = Date()
    val miliSeconds: Long = today.time - dateMilli!!
    val seconds = TimeUnit.MILLISECONDS.toSeconds(miliSeconds)
    val minute = seconds / 60
    val hour = minute / 60
    val days = hour / 24
    return days
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