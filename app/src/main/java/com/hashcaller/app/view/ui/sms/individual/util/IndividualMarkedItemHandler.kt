package com.hashcaller.app.view.ui.sms.individual.util

import android.view.View

/**
 * mark item handler in sms
 */
object IndividualMarkedItemHandler {
    private var markedItems:MutableSet<Long> = mutableSetOf()
    private var markedViews:MutableSet<View> = mutableSetOf()

    fun addTomarkedItemsById(id:Long){
        markedItems.add(id)
    }
    fun removeFromMarkedItemsById(id: Long){
        markedItems.remove(id)
    }

    fun addToMarkedViews(view:View){
        markedViews.add(view)
    }
    fun removeFromMarkedViews(view: View){
        markedViews.remove(view)
    }
    fun isMarkedViewsEmpty(): Boolean {
        return markedViews.isEmpty()
    }
    fun getMarkedViews(): List<View> {
        return markedViews.toList()
    }


}