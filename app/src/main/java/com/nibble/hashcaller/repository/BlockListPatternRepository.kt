package com.nibble.hashcaller.repository

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import com.nibble.hashcaller.local.db.blocklist.BlockedListPattern
import com.nibble.hashcaller.local.db.blocklist.BlockedLIstDao

/**
 * Created by Jithin KG on 03,July,2020
 */
class BlockListPatternRepository(private val blockedLIstDao: BlockedLIstDao) {

    //room executes all queries on a seperate thread
    val allBlockedList:LiveData<List<BlockedListPattern>> = blockedLIstDao.getAllBLockListPattern()

    @SuppressLint("LongLogTag")
    suspend fun insert(blockedListPattern: BlockedListPattern){
        val insert = blockedLIstDao.insert(blockedListPattern)
        Log.d(TAG, "insert: $insert")
    }
    @SuppressLint("LongLogTag")
    suspend fun delete(blockedListPattern: String){
        val insert = blockedLIstDao.delete(blockedListPattern)
        Log.d(TAG, "insert: $insert")
    }
    @SuppressLint("LongLogTag")
    suspend fun getListOfdata():List<BlockedListPattern>{
        return blockedLIstDao.getAllBLockListPatternList()


    }
     fun getListLiveData(): LiveData<List<BlockedListPattern>> {
        return blockedLIstDao.getAllBLockListPattern()


    }


    companion object{
        val TAG = "BlockListPatternRepository"
    }
}