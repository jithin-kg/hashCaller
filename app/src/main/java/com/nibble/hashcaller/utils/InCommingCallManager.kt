package com.nibble.hashcaller.utils

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.nibble.hashcaller.local.db.HashCallerDatabase
import com.nibble.hashcaller.local.db.blocklist.BlockedLIstDao
import com.nibble.hashcaller.repository.BlockListPatternRepository
import com.nibble.hashcaller.repository.contacts.ContactLocalSyncRepository
import com.nibble.hashcaller.repository.search.SearchNetworkRepository
import com.nibble.hashcaller.view.ui.contacts.search.utils.SearchViewModel
import com.nibble.hashcaller.view.ui.sms.individual.util.NUMBER_CONTAINING
import com.nibble.hashcaller.view.ui.sms.individual.util.NUMBER_STARTS_WITH
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Created by Jithin KG on 20,July,2020
 */
class InCommingCallManager(
    blockListPatternRepository: BlockListPatternRepository,
    context: Context,
    phoneNumber: String,
    private val blockedListpatternDAO: BlockedLIstDao
)  {


    private val repository = blockListPatternRepository
    val context = context
    private val phoneNumber = formatPhoneNumber(phoneNumber)

    val serchNetworkRepo = SearchNetworkRepository(context)
    //        var searchResult = serchNetworkRepo.search(phoneNumber)
    val contactsListDAO = HashCallerDatabase.getDatabaseInstance(context).contactInformationDAO()
    val contactLocalSyncRepository = ContactLocalSyncRepository(contactsListDAO, context)

    val viewModel = SearchViewModel(serchNetworkRepo, contactLocalSyncRepository)


    @RequiresApi(Build.VERSION_CODES.N)
    fun manageCall()  = GlobalScope.launch(Dispatchers.IO) {




            repository.isCallerMuted(phoneNumber).collect {
                if(it){
                    silenceIncomingCall(context)
                }
            }
        //todo I can change this to flow for faster operation
        Log.d(TAG, "phoneNum: $phoneNumber")
        Log.d(TAG, "phoneNum: $phoneNumber")
        var match = false


        async {
              blockedListpatternDAO.getAllBLockListPatternByFlow().collect {
                  for (item in it){
                      if(item.type == NUMBER_STARTS_WITH){
                          match =   phoneNumber.startsWith(item.numberPattern)
                      }else if(item.type == NUMBER_CONTAINING ){
                          match =  phoneNumber.contains(item.numberPattern)
                      }else{
                          match = phoneNumber.endsWith(item.numberPattern)
                      }
                      if(match){
                          endIncommingCall(context)
                          break
                      }
                  }
              }

          }.await()
        if(match){
            endIncommingCall(context)

        }




    }

    companion object{
        private const val  TAG = "__IncomingCallManager"
    }


     fun endIncommingCall(context: Context) {
        val c =  CallEnder(context)
        c.endIncomingCall()
    }

    fun silenceIncomingCall(context: Context){
        val c = CallEnder(context)
        c.silenceIncomingCall()
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