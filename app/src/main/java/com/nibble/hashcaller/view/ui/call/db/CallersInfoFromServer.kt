package com.nibble.hashcaller.view.ui.call.db

import androidx.room.*
import java.time.OffsetDateTime
import java.util.*

//Todo unique contact address
/**
 * table that stores all the spammers details recieved from server
 * include all callers information available in server
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

//TODO save the

@Entity(tableName = "callers_info_from_server",indices = [Index(value =["contact_address"], unique = true)])
data class CallersInfoFromServer (
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name = "contact_address") var contactAddress: String= "",
    @ColumnInfo(name = "type") var  spammerType: Int = 0,
    @ColumnInfo(name = "name") var title:String = "",
    @ColumnInfo(name = "informationReceivedDate") val informationReceivedDate:Date,
    @ColumnInfo(name = "spamReportCount") var spamReportCount: Long = 0,
    @ColumnInfo(name = "city") var city: String  = "",
    @ColumnInfo(name = "country") var country: String = "",
    @ColumnInfo(name = "carrier") var carrier: String = ""

) {
}