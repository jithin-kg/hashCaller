package com.nibble.hashcaller.view.ui.call.utils

import android.view.View
import android.widget.ImageView
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.contacts.utils.markingStarted
import com.nibble.hashcaller.view.ui.sms.SMSContainerFragment
import com.nibble.hashcaller.view.ui.sms.individual.util.beInvisible
import com.nibble.hashcaller.view.ui.sms.util.MarkedItemsHandler

/**
 * call
 */
object IndividualMarkedItemHandlerCall {
    private var markedItems:MutableSet<Long> = mutableSetOf()
    private var markedViews:MutableSet<View> = mutableSetOf()

    fun getMarkedItemSize(): Int {
        return markedItems.size
    }
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
        unMarkAllItems()
        markedItems.clear()
        markedViews.clear()

    }

    fun containsItem(id: Long): Boolean {
        return markedItems.contains(id)
    }

    /**
     * unmark all recylcelerview list item
     */
    private fun unMarkAllItems(){
        for(view in markedViews){
            view.findViewById<ImageView>(R.id.imgViewCallMarked).beInvisible()
        }

//        SMSContainerFragment.updateSelectedItemCount(MarkedItemsHandler.markedItems.size)

    }


}