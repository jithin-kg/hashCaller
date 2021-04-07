package com.nibble.hashcaller.local.db.contacts

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Created by Jithin KG on 03,July,2020
 */
@Dao
interface IContactAddressesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(number: ContactAddresses)
    @Query("DELETE from contact_addresses_for_searching WHERE number=:number")
    suspend fun delete(number: String)
    
    @Query("SELECT * FROM contact_addresses_for_searching")
     fun getAll():List<ContactAddresses>

    @Query("SELECT * FROM contact_addresses_for_searching WHERE number =:number")
    suspend fun find(number: String):ContactAddresses?

}