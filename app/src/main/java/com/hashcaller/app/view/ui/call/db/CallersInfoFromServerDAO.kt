package com.hashcaller.app.view.ui.call.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.*

/**
 * table which holds user reported and other other spammers number
 * retrieved from database
 */
@Dao
interface CallersInfoFromServerDAO {
    //important to make OnConflictStrategy to REAPLACE , otherwise when new data are inserted for outdated number infos,
    //insert wont happen
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(callersList: List<CallersInfoFromServer>)

    @Query("DELETE from callers_info_from_server WHERE contact_address=:address")
    suspend fun delete(address: String)

    @Query("SELECT * FROM callers_info_from_server")
    fun getAllBLockListPattern(): LiveData<List<CallersInfoFromServer>>

    @Query("SELECT * FROM callers_info_from_server")
    suspend fun getAll(): List<CallersInfoFromServer>

    @Query("SELECT * FROM callers_info_from_server")
     fun getFlow(): kotlinx.coroutines.flow.Flow<List<CallersInfoFromServer>>

    @Query("SELECT * FROM callers_info_from_server WHERE contact_address=:contactAddress")
    suspend fun find(contactAddress: String) : CallersInfoFromServer?

    @Query("DELETE from callers_info_from_server ")
    suspend fun deleteAll()

    @Query("UPDATE  callers_info_from_server  SET spamReportCount =:spamCount,isBlockedByUser =:isBlockedByUser  WHERE contact_address =:contactAddress ")
    suspend fun update(spamCount: kotlin.Long, contactAddress: kotlin.String, isBlockedByUser:Boolean)

    @Query("UPDATE  callers_info_from_server  SET spamReportCount =:spamCount, firstName =:firstName,lastName=:lastName,nameInPhoneBook =:nameInPhoneBook,hUid=:hUid, informationReceivedDate=:date,isInfoFoundInServer=:isUserInfoFoundInServer,thumbnailImg=:thumbnailImg, city=:city,carrier=:carrier,bio=:bio,email=:email,avatarGoogle=:avatarGoogle, isVerifiedUser=:isVerifiedUser  WHERE hashedNum =:hashedNum ")
    suspend fun updateByHash(hashedNum:String,
                     spamCount: Long ,
                     firstName:String,
                     lastName:String,
                     nameInPhoneBook:String,
                     date:Date,
                     isUserInfoFoundInServer:Int,
                     thumbnailImg:String="",
                     city:String="",
                     carrier:String = "",
                     hUid:String = "",
                     bio:String = "",
                     email:String = "",
                     avatarGoogle:String = "",
                     isVerifiedUser:Boolean
                     )

    @Query("UPDATE  callers_info_from_server  SET spamReportCount =:spamReportCount,firstName =:firstName, lastName=:lastName,thumbnailImg=:thumbnailImg, informationReceivedDate=:informationReceivedDate, city=:city,country=:country, carrier=:carrier WHERE contact_address =:contactAddress ")
    suspend fun updateWithServerinfo(
                                     contactAddress: kotlin.String,
                                     firstName:String,
                                     lastName: String,
                                     informationReceivedDate:Date,
                                     spamReportCount:Long,
                                     city:String,
                                     country:String,
                                     carrier:String,
                                     thumbnailImg:String
                                     )

    @Query("UPDATE  callers_info_from_server  SET isBlockedByUser =:isBlockedByUser, spamReportCount =:spamCount WHERE contact_address =:contactAddress")
    suspend fun unBlock(isBlockedByUser:Boolean, contactAddress: kotlin.String, spamCount: Long)

    @Query("SELECT * FROM callers_info_from_server")
    fun getAllLiveData()  : LiveData<List<CallersInfoFromServer>>
}