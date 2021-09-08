package com.hashcaller.app.repository

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import com.hashcaller.app.local.db.blocklist.BlockTypes.Companion.BLOCK_TYPE_EXACT_NUMBER
import com.hashcaller.app.local.db.blocklist.BlockedListPattern
import com.hashcaller.app.local.db.blocklist.BlockedLIstDao
import com.hashcaller.app.local.db.blocklist.mutedCallers.IMutedCallersDAO
import com.hashcaller.app.local.db.blocklist.mutedCallers.MutedCallers
import com.hashcaller.app.view.ui.contacts.utils.ALREADY_EXISTS_IN_DB
import com.hashcaller.app.view.ui.contacts.utils.OPERATION_COMPLETED
import com.hashcaller.app.view.utils.LibPhoneCodeHelper
import com.hashcaller.app.work.formatPhoneNumber
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
    val allBlockedList: LiveData<MutableList<BlockedListPattern>>? = blockedLIstDao?.getAllBLockListPattern()

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
            "", type
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
     fun getListLiveData(): LiveData<MutableList<BlockedListPattern>>? {
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