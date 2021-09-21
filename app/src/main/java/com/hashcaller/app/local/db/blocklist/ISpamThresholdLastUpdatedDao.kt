package com.hashcaller.app.local.db.blocklist

import androidx.room.*
import java.util.*


//SpamThresholdUpdatedDate
@Dao
interface  ISpamThresholdLastUpdatedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: SpamThresholdUpdatedDate)

    @Query("SELECT * FROM spam_threshold_update_date")
    suspend fun find(): List<SpamThresholdUpdatedDate>

    @Query("DELETE FROM spam_threshold_update_date")
    suspend fun deleteAll()
}