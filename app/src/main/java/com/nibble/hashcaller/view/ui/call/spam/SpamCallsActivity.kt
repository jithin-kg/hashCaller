package com.nibble.hashcaller.view.ui.call.spam

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivitySpamCallsBinding
import com.nibble.hashcaller.utils.internet.ConnectionLiveData
import com.nibble.hashcaller.view.ui.call.CallFragment
import com.nibble.hashcaller.view.ui.call.db.CallLogTable
import com.nibble.hashcaller.view.ui.call.dialer.DialerAdapter
import com.nibble.hashcaller.view.ui.call.dialer.util.CustomLinearLayoutManager
import com.nibble.hashcaller.view.ui.call.individualCallLog.IndividualCallLogActivity
import com.nibble.hashcaller.view.ui.contacts.individualContacts.IndividualCotactViewActivity
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ADDRES
import com.nibble.hashcaller.view.ui.sms.individual.util.*
import com.nibble.hashcaller.view.ui.sms.list.SMSListAdapter

class SpamCallsActivity : AppCompatActivity(), DialerAdapter.ViewHandlerHelper, SMSListAdapter.NetworkHandler {
    private lateinit var binding: ActivitySpamCallsBinding
    private lateinit var layoutMngr: LinearLayoutManager
    var callLogAdapter: DialerAdapter? = null
    private var isInternetAvailable = false
    private lateinit var viewmodel: SpamCallViewModel



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpamCallsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observeInternetLivedata()
        initRecyclerView()
        initViewmodel()
        observeSpamCallLogs()
    }

    private fun observeSpamCallLogs() {
        viewmodel.livedata.observe(this, Observer {
            callLogAdapter?.submitCallLogs(it)
        })
    }

    private fun initViewmodel() {
        viewmodel = ViewModelProvider(this, SpamCallInjectorUtil.provideViewmodelFactory(this)).get(
            SpamCallViewModel::class.java)
    }

    private fun observeInternetLivedata() {
        val cl = ConnectionLiveData(this)
        cl?.observe(this, Observer {
            isInternetAvailable = it
        })
    }
    private fun initRecyclerView() {

        binding.recyvlerV.apply {
            layoutManager = CustomLinearLayoutManager(context)
            layoutMngr = layoutManager as CustomLinearLayoutManager
//            val topSpacingDecorator =
//                TopSpacingItemDecoration(
//                    30
//                )
//            addItemDecoration(topSpacingDecorator)
            callLogAdapter = DialerAdapter(context,this@SpamCallsActivity, this@SpamCallsActivity) {

                    id:Long, position:Int, view: View, btn:Int, callLog: CallLogTable, clickType:Int, visibility:Int ->onCallItemClicked(id, position, view, btn, callLog,clickType,visibility)}
            adapter = callLogAdapter
            itemAnimator = null

        }
    }

    private fun onCallItemClicked(
        id: Long,
        position: Int,
        view: View,
        btn: Int,
        callLog: CallLogTable,
        clickType: Int,
        visibility: Int
    ): Int {
        when(clickType){
            TYPE_LONG_PRESS ->{
                val prevExpandedLyoutId = viewmodel.markeditemsHelper .getPreviousExpandedLayout()
                if(prevExpandedLyoutId!=null){
                    //already a lyout is expanded
                    val oldPos = viewmodel.markeditemsHelper. getPrevExpandedPosition()
                    viewmodel.markeditemsHelper.setExpandedLayout(null, null)
                    if(oldPos!=null){
                        callLogAdapter?.notifyItemChanged(oldPos)
                    }
                }
                return  markItem(id, clickType, position,callLog.number)

            }

            TYPE_CLICK_VIEW_CALL_HISTORY ->{
                startCallHistoryActivity(callLog, view)

                return COMPRESS_LAYOUT
            }

            TYPE_CLICK_VIWE_INDIVIDUAL_CONTACT ->{
                if(getMarkedItemsSize() == 0){
                    startIndividualContactActivity(callLog, view)
                    return UNMARK_ITEM
                }else{
                    return  markItem(id, TYPE_CLICK, position,callLog.number) // mark item
                }
            }

            else ->{
                if(getMarkedItemsSize() == 0){
//                   startIndividualContactActivity(callLog, view)
                    val prevExpandedLyoutId = viewmodel.markeditemsHelper.getPreviousExpandedLayout()
                    if(prevExpandedLyoutId==null){
                        viewmodel.markeditemsHelper.setExpandedLayout(id, position)
                        return EXPAND_LAYOUT
                    }else if(prevExpandedLyoutId != id){
                        val oldPos = viewmodel.markeditemsHelper.getPrevExpandedPosition()
                        viewmodel.markeditemsHelper.setExpandedLayout(id, position)
                        if(oldPos!=null){
                            callLogAdapter?.notifyItemChanged(oldPos)
                        }
                        return EXPAND_LAYOUT
                    }else{

                        viewmodel.markeditemsHelper.setExpandedLayout(null, null)
                        return COMPRESS_LAYOUT

                    }
                }else{
                    return markItem(id, clickType, position, callLog.number)
                }
            }
        }
        return UNMARK_ITEM

    }

    private fun startIndividualContactActivity(log: CallLogTable, view: View) {

        val intent = getContactIntent(log, CallFragment.INDIVIDUAL_CONTACT_ACTIVITY)
        val options = getOptions(view, log)
        startActivity(intent, options.toBundle())
    }

    private fun getOptions(view: View, log: CallLogTable): ActivityOptions {
        val pairList = ArrayList<android.util.Pair<View, String>>()
        val imgViewUserPhoto = view.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.imgVThumbnail)
        val textViewCrclr = view.findViewById<TextView>(R.id.textViewCrclr)
        var pair:android.util.Pair<View, String>? = null
        if(log.thumbnailFromCp.isNotEmpty()){
            pair = android.util.Pair(imgViewUserPhoto as View,"contactImageTransition")
        }else if(log.imageFromDb.isNotEmpty()){
            pair = android.util.Pair(imgViewUserPhoto as View,"contactImageTransition")

        }else{
            pair = android.util.Pair(textViewCrclr as View, "firstLetterTransition")
        }
        pairList.add(pair)
        val options = ActivityOptions.makeSceneTransitionAnimation(this,pairList[0] )
        return options
    }

    fun getMarkedItemsSize(): Int {
        return  viewmodel.markeditemsHelper.getmarkedItemSize()
    }

    private fun markItem(id: Long, clickType: Int, position: Int, number: String): Int {
        if(viewmodel.markeditemsHelper.markedItems.value!!.isEmpty() && clickType == TYPE_LONG_PRESS){
            //if is empty and click type is long then start marking
            viewmodel.markeditemsHelper.addTomarkeditems(id, position, number)
            return MARK_ITEM
        }else if(clickType == TYPE_LONG_PRESS && viewmodel.markeditemsHelper.markedItems.value!!.isNotEmpty()){
            //already some items are marked
            if(viewmodel.markeditemsHelper.markedItems.value!!.contains(id)){
                viewmodel.markeditemsHelper.removeMarkeditemById(id, position)
                return UNMARK_ITEM
            }else{

                viewmodel.markeditemsHelper.addTomarkeditems(id, position, number)
                return MARK_ITEM
            }
        }else if(clickType == TYPE_CLICK && viewmodel.markeditemsHelper.markedItems.value!!.isNotEmpty()){
            //already markig started , mark on unamrk new item
            if(viewmodel.markeditemsHelper.markedItems.value!!.contains(id)){
                viewmodel.markeditemsHelper.removeMarkeditemById(id, position)
                return UNMARK_ITEM
            }else{
                viewmodel.markeditemsHelper.addTomarkeditems(id, position, number)
                return MARK_ITEM
            }
        }else {
            // normal click
            return UNMARK_ITEM
        }

    }

    private fun startCallHistoryActivity(callLog: CallLogTable, view: View) {
        viewmodel.markeditemsHelper.setExpandedLayout(null, null)

        val intent = getContactIntent(callLog, CallFragment.INDIVIDUAL_CALL_LOG_ACTIVITY)

        startActivity(intent)
    }
    private fun getContactIntent(
        log: CallLogTable,
        destinationActivity: Int
    ): Intent {
        var name = log.name
        if(name.isNullOrEmpty()){
            name = log?.nameFromServer
        }
        if(name.isNullOrEmpty()){
            name = log.number
        }
        var intent: Intent? = null
        when(destinationActivity){
            CallFragment.INDIVIDUAL_CALL_LOG_ACTIVITY ->{
                intent = Intent(this, IndividualCallLogActivity::class.java )
                intent.putExtra(CONTACT_ADDRES, log.number)
            }else ->{
            intent = Intent(this, IndividualCotactViewActivity::class.java )
        }
        }
        intent.putExtra(com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ID, log.number)
        intent.putExtra("name", name )
        intent.putExtra("photo", log.thumbnailFromCp)
        intent.putExtra("color", log.color)
        return intent
    }
    override fun isMarked(id: Long?): Boolean {
        return false
    }

    override fun isViewExpanded(id: Long): Boolean {
        return false
    }

    override fun isInternetAvailable(): Boolean {
        return isInternetAvailable
    }
}