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
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contacts: List<ContactTable>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSingleItem(contacts: ContactTable)

    /**
     * when returning data using live data we don't need to use suspend function
     */
    @Query("SELECT * FROM contacts_information")
     fun getContacts(): LiveData<List<ContactTable>>

    @Query("SELECT COUNT(number) FROM contacts_information")
     fun getCount(): LiveData<Int>
//WHERE number LIKE '%'|| :phonNumber || '%'
    /**
     * Do not return Live data while searching, because live data observe to change,
     * here change in data in database only occur when new data is inserted
     */
    @Query("SELECT * FROM contacts_information WHERE number LIKE '%'|| :phonNumber || '%' LIMIT 3")
     suspend fun  search(phonNumber: String):List<ContactTable>


//    suspend fun getInfoForNumber(phoneNum: String?) :ContactTable


//    @Query("SELECT * FROM contacts_information")
//    suspend fun getAllBLockListPatternList():List<BlockedListPattern>
}