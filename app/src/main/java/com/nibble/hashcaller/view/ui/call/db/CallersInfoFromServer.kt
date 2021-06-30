package com.nibble.hashcaller.view.ui.call.db

import androidx.room.*
import com.nibble.hashcaller.view.ui.sms.individual.util.INFO_NOT_FOUND_IN_SERVER
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
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "contact_address") var contactAddress: String= "",
    @ColumnInfo(name = "hashedNum") var hashedNum: String= "",

    @ColumnInfo(name = "type") var  spammerType: Int = 0,
    @ColumnInfo(name = "firstName") var firstName:String = "",
    @ColumnInfo(name = "lastName") var lastName:String = "",
    @ColumnInfo(name = "informationReceivedDate") var informationReceivedDate:Date,
    @ColumnInfo(name = "spamReportCount") var spamReportCount: Long = 0,
    @ColumnInfo(name = "city") var city: String  = "",
    @ColumnInfo(name = "country") var country: String = "",
    @ColumnInfo(name = "carrier") var carrier: String = "",
    @ColumnInfo(name = "isBlockedByUser") var isBlockedByUser: Boolean = false,
    @ColumnInfo(name = "isInfoFoundInServer") var isUserInfoFoundInServer : Int = INFO_NOT_FOUND_IN_SERVER,
    @ColumnInfo(name = "thumbnailImg") var thumbnailImg:String = ""
    ) {
}