package com.nibble.hashcaller.repository

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.nibble.hashcaller.local.db.blocklist.BlockedListPattern
import com.nibble.hashcaller.local.db.blocklist.BlockedLIstDao
import com.nibble.hashcaller.local.db.blocklist.mutedCallers.IMutedCallersDAO
import com.nibble.hashcaller.local.db.blocklist.mutedCallers.MutedCallers
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.Flow

/**
 * Created by Jithin KG on 03,July,2020
 */
class BlockListPatternRepository(private val blockedLIstDao: BlockedLIstDao,
                                private val mutedCallersDAO : IMutedCallersDAO
                                 ) {

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

    suspend fun isCallerMuted(phoneNumber: String):kotlinx.coroutines.flow.Flow<Boolean> = flow {
        var res : MutedCallers? = null
//        GlobalScope.launch{
            val num = formatPhoneNumber(phoneNumber)
//            res =  async { mutedCallersDAO.find(num) }.await()

//        }.join()
        res = mutedCallersDAO.find(num)
        if(res==null) {
            emit(false)
        }else{
            emit(true)
        }



    }


    companion object{
        val TAG = "BlockListPatternRepository"
    }
}