package com.hashcaller.app.view.ui.auth.getinitialInfos.db

import androidx.annotation.Keep
import androidx.room.*

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
@Keep
@Entity(tableName = "user_info",indices = [Index(value =["phone_no"], unique = true)])
data class UserInfo (
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name = "first_name") var  firstname: String = "",
    @ColumnInfo(name = "last_name") var lastName:String = "",
    @ColumnInfo(name = "hashed_phone_no") var hashedPhoneNumber:String = "",
    @ColumnInfo(name = "phone_no") var phoneNumber: String = "",
    @ColumnInfo(name = "email") var email: String = "",
    @ColumnInfo(name = "photo_uri") var photoURI: String = "", // base 64 encoded string of image
    @ColumnInfo(name = "bio") var bio: String = "",
    @ColumnInfo(name = "googleProfileImgUrl") var googleProfileImgUrl:String = ""

) {
}