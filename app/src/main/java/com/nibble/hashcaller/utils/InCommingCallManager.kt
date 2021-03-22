package com.nibble.hashcaller.utils

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.repository.BlockListPatternRepository
import com.nibble.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import com.nibble.hashcaller.view.ui.contacts.search.utils.SearchViewModel
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
    private val phoneNumber = preparedPhoenNumber(phoneNumber)

    val serchNetworkRepo = SearchNetworkRepository(context)
    //        var searchResult = serchNetworkRepo.search(phoneNumber)
    val contactsListDAO = HashCallerDatabase.getDatabaseInstance(context).contactInformationDAO()
    val contactLocalSyncRepository = ContactLocalSyncRepository(contactsListDAO, context)

    val viewModel = SearchViewModel(serchNetworkRepo, contactLocalSyncRepository)


    @RequiresApi(Build.VERSION_CODES.N)
    fun getBLockedLists()  = GlobalScope.launch(Dispatchers.IO) {
        Log.d(TAG, "phoneNum: $phoneNumber")
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

        }else{

        }
//        }


    }

    companion object{
        private const val  TAG = "__IncomingCallManager"
    }


     fun endIncommingCall(context: Context) {
        val c =  CallEnder(context)
        c.endIncomingCall()
    }

    fun preparedPhoenNumber(num:String):String{
        return num.replace("+","")
            .replace("(", "")
            .replace(")", "")
            .replace("-","").trim()
    }

    fun getCallerInfo()  {

//        viewModel.search(phoneNumber)

//        viewModel.search(phoneNumber!!).observeForever( androidx.lifecycle.Observer {
//            it.let {
//                    resource ->
//                when(resource.status){
//                    Status.SUCCESS->{
//                        Log.d(TAG, " mhan: $it")
//                        resource.data?.let {
//                                searchResult->
//                            Log.d(TAG, "getCallerInfo: $searchResult")
//                        }
//                    }
//                    Status.LOADING->{
//                        //show loading
//
//                        Log.d(TAG, "onQueryTextChange: Loading....")
//                    }
//                    else ->{
//                        Log.d(TAG, "onQueryTextChange: Error ${resource}")
//
//                        Toast.makeText(context.applicationContext, it.message, Toast.LENGTH_LONG).show()
//                    }
//                }
//            }
//
//        })
    }
}