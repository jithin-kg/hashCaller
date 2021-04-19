package com.nibble.hashcaller.repository

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import com.nibble.hashcaller.local.db.blocklist.BlockedListPattern
import com.nibble.hashcaller.local.db.blocklist.BlockedLIstDao
import com.nibble.hashcaller.local.db.blocklist.mutedCallers.IMutedCallersDAO
import com.nibble.hashcaller.local.db.blocklist.mutedCallers.MutedCallers
import com.nibble.hashcaller.view.ui.contacts.utils.ALREADY_EXISTS_IN_DB
import com.nibble.hashcaller.view.ui.contacts.utils.OPERATION_COMPLETED
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.flow.flow

/**
 * Created by Jithin KG on 03,July,2020
 */
class BlockListPatternRepository(private val blockedLIstDao: BlockedLIstDao,
                                private val mutedCallersDAO : IMutedCallersDAO
                                 ) {

    //room executes all queries on a seperate thread
    val allBlockedList:LiveData<List<BlockedListPattern>> = blockedLIstDao.getAllBLockListPattern()

    @SuppressLint("LongLogTag")
    suspend fun insert(blockedListPattern: BlockedListPattern): Int {
        blockedLIstDao.find(formatPhoneNumber(blockedListPattern.numberPattern), blockedListPattern.type).apply {
            if(this==null){
                blockedLIstDao.insert(blockedListPattern).apply {
                    return OPERATION_COMPLETED
                }
            }else{
                return ALREADY_EXISTS_IN_DB
            }
        }
    }
    @SuppressLint("LongLogTag")
    suspend fun delete(blockedListPattern: String, type: Int){
        val insert = blockedLIstDao.delete(blockedListPattern, type)
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