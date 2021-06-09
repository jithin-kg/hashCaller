package com.nibble.hashcaller.repository

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import com.nibble.hashcaller.local.db.blocklist.BlockTypes.Companion.BLOCK_TYPE_EXACT_NUMBER
import com.nibble.hashcaller.local.db.blocklist.BlockedListPattern
import com.nibble.hashcaller.local.db.blocklist.BlockedLIstDao
import com.nibble.hashcaller.local.db.blocklist.mutedCallers.IMutedCallersDAO
import com.nibble.hashcaller.local.db.blocklist.mutedCallers.MutedCallers
import com.nibble.hashcaller.view.ui.contacts.utils.ALREADY_EXISTS_IN_DB
import com.nibble.hashcaller.view.ui.contacts.utils.OPERATION_COMPLETED
import com.nibble.hashcaller.view.ui.sms.individual.util.EXACT_NUMBER
import com.nibble.hashcaller.view.utils.LibPhoneCodeHelper
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * Created by Jithin KG on 03,July,2020
 */
class BlockListPatternRepository(
    private val blockedLIstDao: BlockedLIstDao?,
    private val mutedCallersDAO: IMutedCallersDAO?,
    private val libCountryHelper: LibPhoneCodeHelper,
    private val countryCodeIso: String

) {

    //room executes all queries on a seperate thread
    val allBlockedList: LiveData<List<BlockedListPattern>>? = blockedLIstDao?.getAllBLockListPattern()

    @SuppressLint("LongLogTag")
    suspend fun insert(blockedListPattern: BlockedListPattern): Int  = withContext(Dispatchers.IO){
       val res =  blockedLIstDao?.find(libCountryHelper.getES164Formatednumber(formatPhoneNumber(blockedListPattern.numberPattern), countryCodeIso), blockedListPattern.type)
            if(res==null){
                blockedLIstDao?.insert(blockedListPattern)
                return@withContext OPERATION_COMPLETED

            }else{
                return@withContext ALREADY_EXISTS_IN_DB
            }

    }

    @SuppressLint("LongLogTag")
    suspend fun insertPattern(numberPattern: String, type:Int): Int  = withContext(Dispatchers.IO){
       val formatedNumPattern = libCountryHelper.getES164Formatednumber(formatPhoneNumber(numberPattern), countryCodeIso)
       val  blockedListPattern =  BlockedListPattern(
            null, formatedNumPattern,
            "", EXACT_NUMBER
        )

        val res =  blockedLIstDao?.find(formatedNumPattern, blockedListPattern.type)
        if(res==null){
            blockedLIstDao?.insert(blockedListPattern)
            return@withContext OPERATION_COMPLETED

        }else{
            return@withContext ALREADY_EXISTS_IN_DB
        }

    }
    @SuppressLint("LongLogTag")
    suspend fun delete(blockedListPattern: String, type: Int) = withContext(Dispatchers.IO){
        var blockPattern = blockedListPattern
        when(type){
            BLOCK_TYPE_EXACT_NUMBER -> {
                blockPattern = libCountryHelper.getES164Formatednumber(formatPhoneNumber(blockPattern), countryCodeIso)
            }
        }
        blockedLIstDao?.delete(blockPattern, type)
    }
    @SuppressLint("LongLogTag")
    suspend fun getListOfdata():List<BlockedListPattern>? = withContext(Dispatchers.IO){
        return@withContext blockedLIstDao?.getAllBLockListPatternList()


    }
     fun getListLiveData(): LiveData<List<BlockedListPattern>>? {
        return blockedLIstDao?.getAllBLockListPattern()


    }

    suspend fun isCallerMuted(phoneNumber: String):kotlinx.coroutines.flow.Flow<Boolean> = flow {
        var res : MutedCallers? = null
//        GlobalScope.launch{
            val num = formatPhoneNumber(phoneNumber)
//            res =  async { mutedCallersDAO.find(num) }.await()

//        }.join()
        res = mutedCallersDAO?.find(num)
        if(res==null) {
            emit(false)
        }else{
            emit(true)
        }
    }

    suspend fun clearAll() {
        blockedLIstDao?.deleteAll()

    }

    suspend fun fidOneLike(contactAddress:String){
        val formated = libCountryHelper.getES164Formatednumber(formatPhoneNumber(contactAddress), countryCodeIso)
        blockedLIstDao?.find(formated, BLOCK_TYPE_EXACT_NUMBER)
    }

    suspend fun getAll(): List<BlockedListPattern>? {
        return blockedLIstDao?.getAllBLockListPatternList()
    }

    fun markAsSpam(contactAddress: String) {

    }

    /**
     * Update chat threads with block list pattern
     * mark as reported by user if the number is present in blocklistpattern
     *
     */



    companion object{
        val TAG = "BlockListPatternRepository"
    }
}