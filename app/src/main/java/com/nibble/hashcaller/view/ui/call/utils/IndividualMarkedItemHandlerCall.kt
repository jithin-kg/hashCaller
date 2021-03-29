package com.nibble.hashcaller.view.ui.call.utils

import android.view.View
import android.widget.ImageView
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.sms.individual.util.beInvisible

/**
 * call
 */
object IndividualMarkedItemHandlerCall {
    private var markedItems:MutableSet<Long> = mutableSetOf()
    private var markedViews:MutableSet<View> = mutableSetOf()
    private var expandedLayoutId: Long? = null // to keep track what layout is now expanded
    private var expandedLayoutView: View? = null
    private var markedContactAddress: String? = null // to mute or block

    fun setMarkedContactAddress(address:String){
        markedContactAddress = address
    }
    fun getMarkedContactAddress(): String? {
        return markedContactAddress
    }
    fun setExpandedLayoutView(view: View?){
        expandedLayoutView = view
    }
    fun isItemSizeEqualsOne(): Boolean {
        if(markedItems.size == 1) return true
        return false
    }
    fun getExpandedLayoutView(): View? {
        return expandedLayoutView
    }
    fun setExpandedLayoutId(id: Long?){
        expandedLayoutId = id
    }
    fun getExpandedLayoutId(): Long? {
        return expandedLayoutId
    }
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
    fun idContainsInList(id:Long): Boolean {
        return markedItems.contains(id)

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