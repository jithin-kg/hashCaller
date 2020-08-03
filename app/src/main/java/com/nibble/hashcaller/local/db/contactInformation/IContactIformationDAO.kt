package com.nibble.hashcaller.local.db.contactInformation

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nibble.hashcaller.local.db.blocklist.BlockedListPattern
import com.nibble.hashcaller.stubs.Contact

/**
 * Created by Jithin KG on 01,August,2020
 */
@Dao
interface IContactIformationDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(contact: ContactTable):Long

    @Query("SELECT * FROM contacts_information")
    suspend fun getContacts(): List<ContactTable>

    @Query("SELECT COUNT(number) FROM contacts_information")
     fun getCount(): LiveData<Int>


//    @Query("SELECT * FROM contacts_information")
//    suspend fun getAllBLockListPatternList():List<BlockedListPattern>
}