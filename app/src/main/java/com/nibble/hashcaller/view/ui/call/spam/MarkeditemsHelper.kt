package com.nibble.hashcaller.view.ui.call.spam

import androidx.lifecycle.MutableLiveData

class MarkeditemsHelper {

    var markedAddres:MutableSet<String> = mutableSetOf()
    private var expandedLayoutId: Long? = null
    private var expandedLayoutPositin:Int? = null
    var contactAddress = ""
    var markedItems: MutableLiveData<MutableSet<Long>> = MutableLiveData(mutableSetOf())
    var markedItemsPositions: HashSet<Int> = hashSetOf()


    fun getExpanedLayoutPosition(): Int? {
        return expandedLayoutPositin
    }
    fun getExpandedLayoutId(): Long? {
        return expandedLayoutId
    }
    fun getPreviousExpandedLayout(): Long? {
        return expandedLayoutId

    }

    fun clearMarkeditems(){
        markedItems.value?.clear()
        markedAddres.clear()
    }
    fun addTomarkeditems(id: Long, position: Int, number: String){
        markedItems.value!!.add(id)
        markedItemsPositions.add(position)
        markedItems.value = markedItems.value
        contactAddress = number
        markedAddres.add(number)

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
    fun removeMarkeditemById(id: Long, position: Int, number: String){

        markedItems.value!!.remove(id)
        markedAddres.remove(number)
        markedItemsPositions.remove(position)
        markedItems.value = markedItems.value
    }

    fun getPrevExpandedPosition(): Int? {
        return expandedLayoutPositin
    }
}