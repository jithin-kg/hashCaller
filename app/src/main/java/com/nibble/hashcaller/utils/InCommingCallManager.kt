package com.nibble.hashcaller.utils

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.nibble.hashcaller.data.repository.BlockListPatternRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.regex.Pattern

/**
 * Created by Jithin KG on 20,July,2020
 */
class InCommingCallManager(
    blockListPatternRepository: BlockListPatternRepository,
    context: Context,
    phoneNumber: String
)  {

    private val repository = blockListPatternRepository
    val context = context
    private val phoneNumber = phoneNumber.replace("+","")
        .replace("(", "")
        .replace(")", "")
        .replace("-","")
//    preparedPhoenNumber()



    @RequiresApi(Build.VERSION_CODES.N)
    fun getBLockedLists()  = GlobalScope.launch(Dispatchers.IO) {
        Log.d(TAG, "phoneNum: $phoneNumber")
          val job =  async { repository.getListOfdata() }
//        runBlocking {
            val list = job.await()
           val match:Boolean  =list.stream()
                .anyMatch {
                  blockedListPattern->
                    Log.d(TAG, blockedListPattern.numberPatterRegex)
//                    phoneNumber.matches(blockedListPattern.numberPattern)
                    Pattern.matches(blockedListPattern.numberPatterRegex
                        ,phoneNumber)
                }
        if(match){
            endIncommingCall(context)
            Log.d(TAG, "getBLockedLists: $match")
        }else{
            Log.d(TAG, "getBLockedLists: $match")
        }
//        }


    }

    companion object{
        private const val  TAG = "__IncomingCallManager"
    }


    private fun endIncommingCall(context: Context) {
        val c =  CallEnder(context)
        c.endIncomingCall()
    }
//    fun preparedPhoenNumber(num:String):Boolean{
//
//    }
}