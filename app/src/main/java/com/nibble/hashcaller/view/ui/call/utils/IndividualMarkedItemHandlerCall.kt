package com.nibble.hashcaller.view.ui.call.utils

import android.view.View
import com.nibble.hashcaller.view.ui.contacts.utils.markingStarted

/**
 * call
 */
object IndividualMarkedItemHandlerCall {
    private var markedItems:MutableSet<Long> = mutableSetOf()
    private var markedViews:MutableSet<View> = mutableSetOf()

    fun getMarkedItems(): MutableSet<Long> {
        return markedItems
    }
    fun addTomarkedItemsById(id:Long){
        markedItems.add(id)
    }

    fun isMarkingStarted(): Boolean {
        if(markedItems.isNotEmpty())
        return true

        return false
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

    fun clearlists() {
        markedItems.clear()
        markedViews.clear()
    }


}