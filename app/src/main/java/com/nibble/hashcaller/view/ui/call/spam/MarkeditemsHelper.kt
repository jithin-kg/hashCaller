package com.nibble.hashcaller.view.ui.call.spam

import androidx.lifecycle.MutableLiveData


class MarkeditemsHelper {

    var expandedLayoutId: Long? = null
    var expandedLayoutPositin:Int? = null
    var contactAddress = ""
    var markedItems: MutableLiveData<MutableSet<Long>> = MutableLiveData(mutableSetOf())
    var markedItemsPositions: HashSet<Int> = hashSetOf()

    fun getPreviousExpandedLayout(): Long? {
        return expandedLayoutId
    }

    fun clearMarkeditems(){
        markedItems.value?.clear()
    }
    fun addTomarkeditems(id: Long, position: Int, number: String){
        markedItems.value!!.add(id)
        markedItemsPositions.add(position)
        markedItems.value = markedItems.value
        contactAddress = number
    }
    fun isThisViewExpanded(id: Long): Boolean {
        return id == expandedLayoutId
    }

    fun getmarkedItemSize(): Int {

        var size = markedItems.value?.size
        return size ?: 0
    }

    fun clearMarkedItemPositions(){
        markedItemsPositions.clear()
    }
    fun setExpandedLayout(id: Long?, position: Int?) {
        expandedLayoutId = id
        expandedLayoutPositin = position
    }
    fun removeMarkeditemById(id: Long, position: Int){
        markedItems.value!!.remove(id)
        markedItemsPositions.remove(position)
        markedItems.value = markedItems.value
    }

    fun getPrevExpandedPosition(): Int? {
        return expandedLayoutPositin
    }
}