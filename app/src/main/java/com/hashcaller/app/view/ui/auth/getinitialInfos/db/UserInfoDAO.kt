package com.hashcaller.app.view.ui.auth.getinitialInfos.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * table which holds user reported and other other spammers number
 * retrieved from database
 */
@Dao
interface UserInfoDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: UserInfo)

    @Query("DELETE from user_info WHERE phone_no=:address")
    suspend fun delete(address: String)

    @Query("SELECT * FROM user_info LIMIT 1")
    fun getUserInfoLiveData(): LiveData<UserInfo>

    @Query("SELECT * FROM user_info LIMIT 1")
    suspend fun getUser(): UserInfo?

    @Query("UPDATE user_info SET first_name =:firstName, last_name =:lastName, photo_uri =:imageUri ")
    suspend fun updateUserInfo(firstName: String, lastName: String, imageUri: String)

}