package com.hashcaller.app.view.ui.call

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hashcaller.app.network.search.model.CntctitemForView
import com.hashcaller.app.view.ui.call.spam.MarkeditemsHelper
import com.hashcaller.app.view.ui.sms.individual.util.SPAMMER_TYPE_SCAM
import kotlinx.coroutines.launch

class MarkhelperViewmodel() : ViewModel()  {

    private var spammerType:Int = SPAMMER_TYPE_SCAM


    var markeditemsHelper = MarkeditemsHelper()
    var hiddenSatte = MutableLiveData(HiddenStates.Visible)
    var markedItems: MutableLiveData<MutableSet<Long>> = MutableLiveData(mutableSetOf())
    val toggleBottomSheet = MutableLiveData(false)

    fun addTomarkeditems(id: Long, position: Int, number: String, nameStr: String?){
        markeditemsHelper.addTomarkeditems(id, position, number, nameStr)
    }
    fun removeMarkeditemById(id: Long, position: Int, number: String){
        markeditemsHelper.removeMarkeditemById(id, position, number)
    }
    fun getmarkeditemPositions(): Iterable<Int> {
        return markeditemsHelper.markedItemsPositions
    }
    fun clearMarkedItems() = viewModelScope.launch{
        markeditemsHelper.clearMarkeditems()
        markeditemsHelper.clearMarkedItemPositions()
    }

    fun clearMarkedItemPositions() = viewModelScope.launch{
        markeditemsHelper.clearMarkedItemPositions()
    }
    fun isThisViewExpanded(id: Long): Boolean {
        return id == markeditemsHelper.getExpandedLayoutId()
    }
    fun setExpandedLayout(id: Long?, position: Int?) {
        markeditemsHelper.setExpandedLayout(id, position)
//        expandedLayoutId = id
//        expandedLayoutPositin = position
    }

    fun getPreviousExpandedLayout(): Long? {
        return markeditemsHelper.getExpandedLayoutId()
    }
    fun getPrevExpandedPosition(): Int? {
        return markeditemsHelper.getExpanedLayoutPosition()

    }
    fun getMakedItems(): List<String> {
        return markeditemsHelper.getMarkedItems()
    }
    fun getMarkedItemContactDetails(): HashMap<String, CntctitemForView> {
        return markeditemsHelper.getAllContactDetailOfMarkedItem()
    }
    fun getMakedItemsSize(): Int {
        return markeditemsHelper.getmarkedItemSize()
    }

    fun toggleBottomSheet(flag: Boolean) = viewModelScope.launch  {
        toggleBottomSheet.value = flag
//        toggleBottomSheet.value = toggleBottomSheet.value
    }

    fun onCallFragmentHidden() {

    }

    fun onHddenStateChange(hidden: Boolean) = viewModelScope.launch {
        if(hidden){
            hiddenSatte.value = HiddenStates.Hidden
        }else {
            hiddenSatte.value = HiddenStates.Visible

        }
    }
}

enum class HiddenStates {
    Hidden,
    Visible
}
