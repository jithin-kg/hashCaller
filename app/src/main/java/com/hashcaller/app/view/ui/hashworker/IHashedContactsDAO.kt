package com.hashcaller.app.view.ui.hashworker

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface IHashedContactsDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(sms: List<MyContacts>)

    @Query("DELETE from my_synced_contacts")
    suspend fun deleteAll()
    @Query("SELECT * FROM my_synced_contacts")
    suspend fun getAll():List<MyContacts>

//    @Query("SELECT * FROM my_synced_contacts")
//    fun getLivedata():LiveData<List<MyContacts>?>
}