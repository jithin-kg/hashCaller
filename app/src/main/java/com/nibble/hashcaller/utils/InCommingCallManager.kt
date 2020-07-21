package com.nibble.hashcaller.utils

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.android.internal.telephony.ITelephony
import com.nibble.hashcaller.data.local.db.dao.BlockedLIstDao
import com.nibble.hashcaller.data.repository.BlockListPatternRepository
import kotlinx.coroutines.*
import java.util.regex.Pattern

/**
 * Created by Jithin KG on 20,July,2020
 */
class InCommingCallManager (blockListPatternRepository: BlockListPatternRepository) :ITelephony {

    val repository = blockListPatternRepository



    @RequiresApi(Build.VERSION_CODES.N)
    fun getBLockedLists()  = GlobalScope.launch(Dispatchers.IO) {

          val job =  async { repository.getListOfdata() }
        runBlocking {
            val list = job.await()
            val phoneNumber:String="34234"
            Log.d(TAG, "getBLockedLists")
            list.stream()
                .anyMatch {
                  blockedListPattern->
//                    phoneNumber.matches(blockedListPattern.numberPattern)
                    Pattern.matches(blockedListPattern.numberPattern
                        ,"09304")
                }

        }


    }

    companion object{
        private const val  TAG = "__IncommingCAllManager"
    }
    override fun endCall(): Boolean {
          return false
    }

    override fun answerRingingCall() {

    }

    override fun silenceRinger() {

    }
}