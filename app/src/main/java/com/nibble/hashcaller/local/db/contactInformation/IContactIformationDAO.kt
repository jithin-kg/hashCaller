package com.nibble.hashcaller.local.db.contactInformation

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Created by Jithin KG on 01,August,2020
 */
@Dao
interface IContactIformationDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(contacts: List<ContactTable>)

    /**
     * when returning data using live data we don't need to use suspend function
     */
    @Query("SELECT * FROM contacts_information")
     fun getContacts(): LiveData<List<ContactTable>>

    @Query("SELECT COUNT(number) FROM contacts_information")
     fun getCount(): LiveData<Int>
//WHERE number LIKE '%'|| :phonNumber || '%'
    @Query("SELECT * FROM contacts_information WHERE number LIKE '%'|| :phonNumber || '%'")
     suspend fun search(phonNumber: String):List<ContactTable>



//    @Query("SELECT * FROM contacts_information")
//    suspend fun getAllBLockListPatternList():List<BlockedListPattern>
}