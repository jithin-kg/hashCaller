package com.nibble.hashcaller.view.ui.hashworker

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface IHashedNumbersDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(sms: List<HashedNumber>)

    @Query("DELETE from unknown_hashed_number")
    suspend fun delete()
    @Query("SELECT * FROM unknown_hashed_number")
    suspend fun getAll():List<HashedNumber>

    @Query("SELECT * FROM unknown_hashed_number")
    fun getLivedata():LiveData<List<HashedNumber>?>
}