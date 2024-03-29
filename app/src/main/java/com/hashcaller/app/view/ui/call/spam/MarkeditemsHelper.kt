package com.hashcaller.app.view.ui.call.spam

import androidx.lifecycle.MutableLiveData
import com.hashcaller.app.network.search.model.CntctitemForView
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class MarkeditemsHelper {

    var markedAddres:MutableSet<String> = mutableSetOf()
    var markedAddressAndContactDetails: HashMap<String, CntctitemForView> = hashMapOf()
    private var expandedLayoutId: Long? = null
    private var expandedLayoutPositin:Int? = null
    var contactAddress = ""
    var markedItems: MutableLiveData<MutableSet<Long>> = MutableLiveData(mutableSetOf())
    var markedItemsPositions: HashSet<Int> = hashSetOf()

    fun getmarkedAddresAt(index:Int): String? {
         if(markedAddres.size>0){
             return markedAddres.toList()[0]
         }
        return null
    }
    fun getMarkedItems(): List<String> {
        return markedAddres.toList()
    }
    fun getContactDetailOfMarkedItem(number:String): CntctitemForView? {
        return markedAddressAndContactDetails[number]
    }
    fun getAllContactDetailOfMarkedItem(): HashMap<String, CntctitemForView> {
        return markedAddressAndContactDetails
    }

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
        markedItems.value = markedItems.value
        markedAddres.clear()
        markedAddressAndContactDetails.clear()
    }
    fun addTomarkeditems(id: Long, position: Int, number: String, nameStr: String?){
        markedItems.value!!.add(id)
        markedItemsPositions.add(position)
        markedItems.value = markedItems.value
        contactAddress = number
        markedAddres.add(number)
        nameStr?.let {
            markedAddressAndContactDetails[number] = CntctitemForView(informationReceivedDate =  Date(), nameForblockListPattern = it)
        }


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
        markedAddressAndContactDetails.remove(number)
    }

    fun getPrevExpandedPosition(): Int? {
        return expandedLayoutPositin
    }

}