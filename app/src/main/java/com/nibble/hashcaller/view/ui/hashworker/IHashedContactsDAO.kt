package com.nibble.hashcaller.view.ui.hashworker

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface IHashedContactsDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(sms: List<HashedContacts>)

    @Query("DELETE from hashed_number_contacts")
    suspend fun delete()
    @Query("SELECT * FROM hashed_number_contacts")
    suspend fun getAll():List<HashedContacts>

    @Query("SELECT * FROM hashed_number_contacts")
    fun getLivedata():LiveData<List<HashedContacts>?>
}