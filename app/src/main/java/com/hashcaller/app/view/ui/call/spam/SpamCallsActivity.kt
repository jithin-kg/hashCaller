package com.hashcaller.app.view.ui.call.spam

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hashcaller.app.R
import com.hashcaller.app.databinding.ActivitySpamCallsBinding
import com.hashcaller.app.utils.internet.ConnectionLiveData
import com.hashcaller.app.view.ui.SwipeToDeleteCallback
import com.hashcaller.app.view.ui.call.CallFragment
import com.hashcaller.app.view.ui.call.db.CallLogTable
import com.hashcaller.app.view.ui.call.dialer.CallLogAdapter
import com.hashcaller.app.view.ui.call.dialer.util.CustomLinearLayoutManager
import com.hashcaller.app.view.ui.call.individualCallLog.IndividualCallLogActivity
import com.hashcaller.app.view.ui.contacts.individualContacts.IndividualContactViewActivity
import com.hashcaller.app.view.ui.contacts.isDarkThemeOn
import com.hashcaller.app.view.ui.contacts.utils.CONTACT_ADDRES
import com.hashcaller.app.view.ui.sms.individual.util.*
import com.hashcaller.app.view.ui.sms.list.SMSListAdapter
import kotlinx.android.synthetic.main.activity_block_list.*


class SpamCallsActivity : AppCompatActivity(), CallLogAdapter.ViewHandlerHelper, SMSListAdapter.NetworkHandler,
    View.OnClickListener {
    private lateinit var binding: ActivitySpamCallsBinding
    private lateinit var layoutMngr: LinearLayoutManager
    var callLogAdapter: CallLogAdapter? = null
    private var isInternetAvailable = false
    private lateinit var viewmodel: SpamCallViewModel
    private lateinit var swipeHandler: SwipeToDeleteCallback


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpamCallsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observeInternetLivedata()
//        initSwipeHandler()
        initRecyclerView()
        initViewmodel()
        observeSpamCallLogs()
        initListeners()
    }

    private fun initListeners() {
        binding.imgBtnBackBlock.setOnClickListener(this)
    }

    private fun initSwipeHandler() {
        swipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding.recyvlerV.adapter
                val log = callLogAdapter?.getLogAt(viewHolder.adapterPosition)
                viewmodel.delete(log)
                toast("All Call history of ${log?.number} is deleted", Toast.LENGTH_LONG)

            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.recyvlerV)
    }

    private fun observeSpamCallLogs() {
        viewmodel.spamCalllivedata.observe(this, Observer {
            callLogAdapter?.submitCallLogs(it, false)
            if(it.isEmpty()){
                binding.tvSpamInfoCall.beVisible()
            }else {
                binding.tvSpamInfoCall.beGone()
            }
        })
    }

    private fun initViewmodel() {
        viewmodel = ViewModelProvider(this, SpamCallInjectorUtil.provideViewmodelFactory(applicationContext)).get(
            SpamCallViewModel::class.java
        )
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
//            ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.recyvlerV);

            layoutMngr = layoutManager as CustomLinearLayoutManager

            callLogAdapter = CallLogAdapter(context, this@SpamCallsActivity, this@SpamCallsActivity, isDarkThemeOn()) {

                    id: Long, position: Int, view: View, btn: Int, callLog: CallLogTable, clickType: Int, visibility: Int ->onCallItemClicked(
                id,
                position,
                view,
                btn,
                callLog,
                clickType,
                visibility
            )}
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
            TYPE_LONG_PRESS -> {
//                toast("Slide left or right to delete")
            }

            TYPE_CLICK_VIEW_CALL_HISTORY -> {
                startCallHistoryActivity(callLog, view)
                return COMPRESS_LAYOUT
            }

            TYPE_CLICK_VIWE_INDIVIDUAL_CONTACT -> {
                if (getMarkedItemsSize() == 0) {
                    startIndividualContactActivity(callLog, view)
                    return UNMARK_ITEM
                }
//                else {
//                    return markItem(id, TYPE_CLICK, position, callLog.number) // mark item
//                }
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
//                            callLogAdapter?.notifyItemChanged(oldPos)
                        }
                        return EXPAND_LAYOUT
                    }else{

                        viewmodel.markeditemsHelper.setExpandedLayout(null, null)
                        return COMPRESS_LAYOUT

                    }
                }
//                else{
//                    return markItem(id, clickType, position, callLog.number)
//                }
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
            pair = android.util.Pair(imgViewUserPhoto as View, "contactImageTransition")
        }else if(log.imageFromDb.isNotEmpty()){
            pair = android.util.Pair(imgViewUserPhoto as View, "contactImageTransition")

        }else{
            pair = android.util.Pair(textViewCrclr as View, "firstLetterTransition")
        }
        pairList.add(pair)
        val options = ActivityOptions.makeSceneTransitionAnimation(this, pairList[0])
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
                viewmodel.markeditemsHelper.removeMarkeditemById(id, position,number)
                return UNMARK_ITEM
            }else{

                viewmodel.markeditemsHelper.addTomarkeditems(id, position, number)
                return MARK_ITEM
            }
        }else if(clickType == TYPE_CLICK && viewmodel.markeditemsHelper.markedItems.value!!.isNotEmpty()){
            //already markig started , mark on unamrk new item
            if(viewmodel.markeditemsHelper.markedItems.value!!.contains(id)){
                viewmodel.markeditemsHelper.removeMarkeditemById(id, position,number)
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
        var name = log.nameInPhoneBook
        if(name.isNullOrEmpty()){
            name = log?.nameFromServer
        }
        if(name.isNullOrEmpty()){
            name = log.number
        }
        var intent: Intent? = null
        when(destinationActivity){
            CallFragment.INDIVIDUAL_CALL_LOG_ACTIVITY -> {
                intent = Intent(this, IndividualCallLogActivity::class.java)
                intent.putExtra(CONTACT_ADDRES, log.number)
            }else ->{
            intent = Intent(this, IndividualContactViewActivity::class.java)
        }
        }
        intent.putExtra(com.hashcaller.app.view.ui.contacts.utils.CONTACT_ID, log.number)
        intent.putExtra("name", name)
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

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.imgBtnBackBlock -> {
                finishAfterTransition()
            }
        }
    }

    override fun onBackPressed() {
        finishAfterTransition()
    }
}