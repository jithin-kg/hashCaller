package com.hashcaller.app.view.ui.sms.util

import android.view.View

/**
 * to manage adding and deleting of sms thread id and contact address
 * for deleting and archiving sms
 */
object MarkedItemsHandler {
     var markedItems:MutableSet<Long> = mutableSetOf()
     var markedViews:MutableSet<View> = mutableSetOf()
     var markedContactAddress:MutableSet<String> = mutableSetOf()

     var markedTheadIdForBlocking:Long = 0L
     var markedContactAddressForBlocking:String = ""



}