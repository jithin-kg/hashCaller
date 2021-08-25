package com.hashcaller.app.work

import java.util.*
import java.util.concurrent.TimeUnit

/**
 * compares todays date and given given date and returns a-b
 */
class DateCompareHelper {
    /**
     * @param informationReceivedDate : date at which the data is inserted in db
     * @param limit : number of day in which a lookup for the current number should perform
     */

    fun isSyncDateLimitReached(
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
}