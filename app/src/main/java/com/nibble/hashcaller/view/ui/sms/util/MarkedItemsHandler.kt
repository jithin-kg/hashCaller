package com.nibble.hashcaller.view.ui.sms.util

import android.view.View
import androidx.lifecycle.MutableLiveData

/**
 * to manage adding and deleting of sms thread id and contact address
 * for deleting and archiving sms
 */
object MarkedItemsHandler {
     var markedItems:MutableSet<Long> = mutableSetOf()
     var markedViews:MutableSet<View> = mutableSetOf()


//     fun pushNewItemId(id:Long){
//            markedItems.add(id)
//    }
//
//    fun getMakrdItemIdList(){
//
//        markedItems.
//    }


}