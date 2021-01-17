package com.nibble.hashcaller.local.db.blocklist

import androidx.room.*
import java.time.OffsetDateTime
import java.util.*

//Todo unique contact address
/**
 * table that stores all the spammers details recieved from server
 * include sms spammers and call spammers
 * title -> eg vodafone
 * stage_one_address -> phone number hashed result directly from client
 *
 */
/**
 * @return all sms senders numbers list in the localDB which contains
 * ________________________________________________________________________________________________________
 * contact_address | spamReportCount | informationReceivedDate | name | type (business or general user) |
 * --------------------------------------------------------------------------------------------------------
 * when a new sms arrives that phone number is saved to this table with spamReportCount as -1
 * the value in spamReportCount is != -1 then that detail is updated with server, if that user
 * is not a spammer the value will be -1
 *
 * this is the table schema
 *
 */

//TODO save the hashof the five digit, and also in the carier info api use the five digits information

@Entity(tableName = "sms_senders_info_from_server",indices = [Index(value =["contact_address"], unique = true)])
data class SMSSendersInfoFromServer (
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name = "contact_address") val contactAddress: String?,
    @ColumnInfo(name = "type") val spammeerType: Int?,
    @ColumnInfo(name = "name") val title:String,
    @ColumnInfo(name = "informationReceivedDate") val informationReceivedDate:Date,
    @ColumnInfo(name = "spamReportCount") var spamReportCount: Long = -1,
    @ColumnInfo(name = "firstFiveDigitsOfNum") var firstFiveDigits: String // we need to know the first five digits
                                                            //to get the carrier info of a number


) {
}