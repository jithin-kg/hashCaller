package com.hashcaller.app.local.db.contactInformation

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Created by Jithin KG on 01,August,2020
 */
@Dao
interface IContactLastSycnedDateDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(date: ContactLastSyncedDate)

    /**
     * when returning data using live data we don't need to use suspend function
     */
    @Query("SELECT * FROM contact_last_synced_date")
     fun getLastSyncedDate(): ContactLastSyncedDate

    @Query("DELETE FROM contact_last_synced_date")
     fun delteAll()
//WHERE number LIKE '%'|| :phonNumber || '%'
    /**
     * Do not return Live data while searching, because live data observe to change,
     * here change in data in database only occur when new data is inserted
     */
    @Query("SELECT * FROM contacts_information WHERE number LIKE '%'|| :phonNumber || '%' LIMIT 3")
     suspend fun    search(phonNumber: String):List<ContactTable>


//    suspend fun getInfoForNumber(phoneNum: String?) :ContactTable


//    @Query("SELECT * FROM contacts_information")
//    suspend fun getAllBLockListPatternList():List<BlockedListPattern>
}