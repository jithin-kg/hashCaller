package com.nibble.hashcaller.view.ui.sms

import android.Manifest
import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.Shimmer
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.contacts.individualContacts.utils.PermissionUtil
import com.nibble.hashcaller.view.ui.contacts.individualContacts.utils.PermissionUtil.requesetPermission
import com.nibble.hashcaller.view.ui.contacts.startSettingsActivity
import com.nibble.hashcaller.view.ui.contacts.utils.*
import com.nibble.hashcaller.view.ui.extensions.getSpannableString
import com.nibble.hashcaller.view.ui.sms.individual.IndividualSMSActivity
import com.nibble.hashcaller.view.ui.sms.individual.util.beInvisible
import com.nibble.hashcaller.view.ui.sms.individual.util.beVisible
import com.nibble.hashcaller.view.ui.sms.list.SMSHelperFlow
import com.nibble.hashcaller.view.ui.sms.list.SMSListAdapter
import com.nibble.hashcaller.view.ui.sms.list.SMSListInjectorUtil
import com.nibble.hashcaller.view.ui.sms.search.SearchSMSActivity
import com.nibble.hashcaller.view.ui.sms.util.*
import com.nibble.hashcaller.view.ui.sms.util.MarkedItemsHandler.markedItems
import com.nibble.hashcaller.view.utils.ConfirmDialogFragment
import com.nibble.hashcaller.view.utils.ConfirmationClickListener
import com.nibble.hashcaller.view.utils.IDefaultFragmentSelection
import com.nibble.hashcaller.view.utils.spam.SpamLocalListManager
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.android.synthetic.main.bottom_sheet_block.*
import kotlinx.android.synthetic.main.bottom_sheet_block_feedback.*
import kotlinx.android.synthetic.main.fragment_message_container.*
import kotlinx.android.synthetic.main.fragment_message_container.MessagesFragment
import kotlinx.android.synthetic.main.fragment_message_container.imgBtnTbrBlock
import kotlinx.android.synthetic.main.fragment_message_container.imgBtnTbrDelete
import kotlinx.android.synthetic.main.fragment_message_container.imgBtnTbrMuteSender
import kotlinx.android.synthetic.main.fragment_message_container.tvSelectedCount
import kotlinx.android.synthetic.main.fragment_message_container.view.*
import kotlinx.android.synthetic.main.fragment_messages_list.view.*
import kotlinx.android.synthetic.main.fragment_test.*
import kotlinx.android.synthetic.main.sms_list_view.view.*
import kotlinx.coroutines.InternalCoroutinesApi


class SMSContainerFragment : Fragment(), View.OnClickListener, IDefaultFragmentSelection,
SMSListAdapter.LongPressHandler, PopupMenu.OnMenuItemClickListener, ConfirmationClickListener,
    android.widget.PopupMenu.OnMenuItemClickListener {


    private var param1: String? = null
    private var param2: String? = null
    private lateinit var viewMesages: View
    private lateinit var smsListVIewModel: SMSViewModel
    var smsRecyclerAdapter: SMSListAdapter? = null
    private lateinit var searchV: SearchView
    private var searchQry:String? = null
    private lateinit var cntx: Context
    private lateinit var recyclerV: RecyclerView
    private lateinit var smsFlowHelper:SMSHelperFlow
    private lateinit var sView: EditText
    private lateinit var sharedPreferences: SharedPreferences
    var skeletonLayout: LinearLayout? = null
    var shimmer: Shimmer? = null
    var inflater: LayoutInflater? = null
    private var permissionGivenLiveData: MutableLiveData<Boolean> = MutableLiveData(false)
    private var layoutMngr: LinearLayoutManager? = null
    private lateinit var searchViewMessages: EditText
    private var isLoading = false
    var limit = 12
    private var isDflt = false
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private  var spammerType:Int = -1
    private lateinit var bottomSheetDialogfeedback: BottomSheetDialog
    private  var selectedRadioButton: RadioButton? = null
    private var SPAMMER_CATEGORY = SpamLocalListManager.SPAMMER_BUISINESS
    private var isPaused = false
    private lateinit var toolbar : Toolbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cntx = this!!.requireContext()
        pageOb.page = 0

    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewMesages = inflater.inflate(R.layout.fragment_message_container, container, false)
        viewMesagesRef = viewMesages
        initVieModel()
        if(checkContactPermission())
        {
            getFirstPageOfSMS()


            observeSMSList()
        }
//        initListeners()
//        val parent: Fragment? = (parentFragment as SMSContainerFragment).parentFragment
//        toolbar = viewmo

        observeSendersInfoFromServer()
        observePermissionLiveData()
        observeNumOfRowsDeleted()

        this.recyclerV = this.viewMesages.findViewById<RecyclerView>(R.id.rcrViewSMSList)
        registerForContextMenu( this.recyclerV) // context menu registering
        setupBottomSheet()
        return  viewMesages
    }


    private fun addScrollListener() {
        this.recyclerV.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
//                if(dy>0){
                //scrollview scrolled vertically
                //get the visible item count
                if(layoutMngr!=null){

//                   val h =  (toolbarSmS.height!!).toFloat()
//                    if(dy>0){
//                        toolbarSmS.animate().translationY(-h)
//                            .setInterpolator(DecelerateInterpolator()).start()
//                    }
//                    else if(dy<0){
//                        toolbarSmS.animate().translationY(h)
//                            .setInterpolator(DecelerateInterpolator()).start()
//                    }

                    val visibleItemCount = layoutMngr!!.childCount
                    val pastVisibleItem = layoutMngr!!.findFirstCompletelyVisibleItemPosition()
                    val recyclerViewSize = smsRecyclerAdapter!!.itemCount
                    if(!isLoading){
                        if((visibleItemCount + pastVisibleItem) >= recyclerViewSize){
                            //we have reached the bottom
                            Log.d(TAG, "onScrolled:page: ${pageOb.page}, totalsms count ${pageOb.totalSMSCount} ")
//                                if(page+12 <= totalSMSCount ){
                            pageOb.page +=12
//                            smsListVIewModel.getNextSmsPage()
                            if(dy > 0){

                                if(!isSizeEqual){
                                    viewMesages.shimmer_view_container.visibility = View.VISIBLE
                                    viewMesages.rcrViewSMSList.visibility = View.INVISIBLE
                                }
//                                    }
                            }


                        }
                    }

                }
//                }
            }
        })
    }


//    override fun onCreateContextMenu(
//        menu: ContextMenu,
//        v: View,
//        menuInfo: ContextMenu.ContextMenuInfo?
//    ) {
//        Log.d(TAG, "onCreateContextMenu: ")
//        super.onCreateContextMenu(menu, v, menuInfo)
//        requireActivity().menuInflater.inflate(R.menu.sms_container_menu, menu);
//    }

    private fun observePermissionLiveData() {

        this.permissionGivenLiveData.observe(viewLifecycleOwner, Observer { value->
            //this caused to view not updating afte returning from individual sms activity,
            //after user blockes a number and comes that the spam counts were not getting updated
            if(value == true){
//                this.viewMesages.btnSmsPermission.visibility = View.GONE
//                this.viewMesages.tvSMSPermission.visibility = View.GONE
                if(this.smsListVIewModel.SMS.hasObservers()){
                    Log.d(TAG, "observePermissionLiveData: already have observer")
                }else{
                    observeSMSList()
                    Log.d(TAG, "observePermissionLiveData: do not have observer")
                }

            }else{
//                this.viewMesages.btnSmsPermission.visibility = View.VISIBLE
//                this.viewMesages.tvSMSPermission.visibility = View.VISIBLE
                if (this.smsListVIewModel!! != null  ) {
                    if(this.smsListVIewModel?.SMS != null)
                        if(this.smsListVIewModel.SMS!!.hasObservers())
                            this.smsListVIewModel?.SMS?.removeObservers(this);
                }


            }


        })
    }

    private fun checkContactPermission(): Boolean {
        val permissionContact =
            ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_SMS)
        if(permissionContact!= PackageManager.PERMISSION_GRANTED){
            return false
        }
        return true
    }




    private fun initListeners() {
//        viewMesages.btnSmsPermission.setOnClickListener(this)
        viewMesages.searchViewSms.setOnClickListener(this)
        viewMesages.imgBtnTbrMuteSender.setOnClickListener(this)
        viewMesages.imgBtnTbrBlock.setOnClickListener(this)
        viewMesages.imgBtnTbrMore.setOnClickListener(this)
        viewMesages.imgBtnTbrDelete.setOnClickListener(this)
        viewMesages.imgBtnAvatarMain.setOnClickListener(this)
        this.fabSendNewSMS.setOnClickListener(this)

        bottomSheetDialog.radioS.setOnClickListener(this)
        bottomSheetDialog.radioScam.setOnClickListener(this)
        bottomSheetDialog.imgExpand.setOnClickListener(this)
        bottomSheetDialog.btnBlock.setOnClickListener(this)

    }

    /**
     * change visibility of view items to as beginning, ie
     * remove all marking/checked items hide delete,mute etc buttons in toolbar
     */
    private fun resetMarkingOptions() {
        markingStarted = false
        unMarkItems()
        this.searchViewSms.visibility = View.VISIBLE
        this.imgBtnTbrMuteSender.visibility = View.INVISIBLE
        this.imgBtnTbrBlock.visibility = View.INVISIBLE
        this.imgBtnTbrDelete.visibility = View.INVISIBLE
        this.tvSelectedCount.visibility = View.INVISIBLE
    }


    /**
     * To block a contact address
     * @param contact contact address
     *
     */
    private fun addToBlockList(contact: String) {
        this.smsListVIewModel.blockThisAddress(contact, MarkedItemsHandler.markedTheadIdForBlocking, this.spammerType, this.SPAMMER_CATEGORY )

        Toast.makeText(this.requireActivity(), "Number added to spamlist", Toast.LENGTH_LONG)
        bottomSheetDialog.hide()
        bottomSheetDialog.dismiss()
        bottomSheetDialogfeedback.show()
        var txt = "$contact can no longer send SMS or call you."
        val  sb =  SpannableStringBuilder(txt);
        val bss =  StyleSpan(Typeface.BOLD); // Span to make text bold
        sb.setSpan(bss, 0, contact.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE); // make first 4 characters Bold
        bottomSheetDialogfeedback.tvSpamfeedbackMsg.text = sb
        resetMarkingOptions()
    }
    private fun setupBottomSheet() {
        bottomSheetDialog = BottomSheetDialog(this.requireActivity())
        bottomSheetDialogfeedback = BottomSheetDialog(this.requireActivity())
        val viewSheet = layoutInflater.inflate(R.layout.bottom_sheet_block, null)
        val viewSheetFeedback = layoutInflater.inflate(R.layout.bottom_sheet_block_feedback, null)

        bottomSheetDialog.setContentView(viewSheet)
        bottomSheetDialogfeedback.setContentView(viewSheetFeedback)

        selectedRadioButton = bottomSheetDialog.radioScam
        bottomSheetDialog.imgExpand.setOnClickListener(this)


//        if(this.view?.visibility == View.VISIBLE){
//            bottomSheetDialog.hide()

//        }

        bottomSheetDialog.setOnDismissListener {
            Log.d(TAG, "bottomSheetDialogDismissed")

        }
    }
    private fun initVieModel() {
        smsListVIewModel = ViewModelProvider(this, SMSListInjectorUtil.provideDialerViewModelFactory(context)).get(
            SMSViewModel::class.java)
    }


    private fun observeSendersInfoFromServer() {
        smsListVIewModel.getSmsSendersInfoFromServer().observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "observeSendersInfoFromServer: $it")

            smsListVIewModel.updateWithNewSenderInfo()

        })
    }

    private fun observeSMSList() {
        
        smsListVIewModel.SMS.observe(viewLifecycleOwner, Observer { sms ->

            sms.let {

//                Log.d(TAG, "observeSMSList: spamcount ${sms[0].spamCount}")
//                smsRecyclerAdapter?.setSMSList(it, searchQry)
//                Log.d(TAG, "observeSMSList: data changed")
//                smsRecyclerAdapter?.submitList(it)
//                SMSListAdapter.searchQry = searchQry
//                this.smsLIst = it as MutableList<SMS>?
//                this.smsListVIewModel.updateFlowList(sms)
                Log.d(TAG, "observeSMSList: $sms")
                smsListVIewModel.updateLiveData(sms)
//                this.smsListVIewModel.updateLiveData(sms)
                this.smsListVIewModel.getInformationForTheseNumbers(
                    sms,
                    requireActivity().packageName
                )


            }
        })
    }
    private fun observeMutabeLiveData() {
        this.smsListVIewModel.smsLiveData.observe(viewLifecycleOwner, Observer {
//            smsListVIewModel.smsLIst = it as MutableList<SMS>?
//            Log.d(TAG, "observeMutabeLiveData: spamcount${it[0].spamCount} ")
//            var newList:MutableList<SMS> = mutableListOf()

//            it.forEach{sms-> newList.add(sms.deepCopy())}
            smsRecyclerAdapter?.setList(it)

//            this.viewMesages.pgBarsmslist.visibility = View.GONE
            this.viewMesages.shimmer_view_container.visibility = View.GONE
            viewMesages.rcrViewSMSList.visibility = View.VISIBLE
            SMSListAdapter.searchQry = searchQry
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: ")
        rcrViewSMSList.adapter  = null
    }

    @InternalCoroutinesApi
    @SuppressLint("WrongViewCast", "LongLogTag")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        initListeners()
        getFirstPageOfSMS()

//        sView = viewMesages.findViewById(R.id.searchViewSms)

//        Log.d(TAG, "onCreateView: $sView")






        observeMutabeLiveData()
//        addScrollListener()
        if(markedItems.size > 0){
            Log.d(TAG, "onViewCreated: greater than one")
            showToolbarButtons()
            hideSearchView()
            showToolbarButtons()
        }
        if(checkContactPermission()){
        }






    }

    private fun getFirstPageOfSMS() {
        smsListVIewModel.getFirstPageOfSMS()
    }


    private fun initRecyclerView() {
        rcrViewSMSList?.apply {
            layoutManager = LinearLayoutManager(activity)
            layoutMngr = layoutManager as LinearLayoutManager
            smsRecyclerAdapter = SMSListAdapter(context, this@SMSContainerFragment){ view: View, threadId:Long, pos:Int,
                                                                                pno:String->onContactItemClicked(view,threadId, pos, pno)  }
//            smsRecyclerAdapter = SMSListAdapter(context, onContactItemClickListener =){view:View, pos:Int ->onLongpressClickLister(view,pos)}
            adapter = smsRecyclerAdapter

//                setContacts()

//                adapter.onItemClick =
        }
    }
    private fun onDeleteItemClicked(){

    }


    private fun onContactItemClicked(view: View, threadId: Long, pos: Int, address: String) {
        Log.d(TAG, "onContactItemClicked address is : $address")
        val formatedNum = formatPhoneNumber(address)
        if(markingStarted){
            //if the view is already marked, then uncheck it
            val imgVSmsMarked = view.findViewById<ImageView>(R.id.smsMarked)
            if(imgVSmsMarked.visibility == View.VISIBLE){
                unMarkItem(imgVSmsMarked, threadId, formatedNum)

            }else{
                markItem(view, threadId, formatedNum)
            }

        }else{
//            smsListVIewModel.markSMSAsRead(address)
            val intent = Intent(context, IndividualSMSActivity::class.java )
            val bundle = Bundle()
            bundle.putString(CONTACT_ADDRES, address)
            bundle.putString(SMS_CHAT_ID, "")

            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            intent.putExtras(bundle)
            startActivity(intent)
        }

//            this.smsListVIewModel.changelist(this.smsLIst!!, this.requireActivity())

    }
    @SuppressLint("LongLogTag")
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
        smsListVIewModel.updateWithNewSenderInfo()
        isPaused = false
        if(this.smsListVIewModel.SMS.hasObservers()){
            Log.d("__SMSContainerFragmentob", "onResume: hasobservers")
        }else{
            Log.d("__SMSContainerFragmentob", "onResume: no observers")
        }
        if(this.smsListVIewModel.SMS.hasActiveObservers()){
            Log.d("__SMSContainerFragmentob", "onResume: has active observers ")
        }else{
            Log.d("__SMSContainerFragmentob", "onResume: has not active observer")
        }
        this.permissionGivenLiveData.value  = checkContactPermission()

    }

    fun show(){


        this.viewMesages?.fabBtnDeleteSMSExpanded?.extend()

    }

    fun hide(){
        this.viewMesages?.fabBtnDeleteSMSExpanded?.shrink()

    }




    private fun deleteMarkedSMSThreads() {
        deleteSms()
    }
    private fun deleteSms() {
        val dialog = ConfirmDialogFragment(this,
            getSpannableString("This is permenant and cant be undone"),

            getSpannableString("Delete conversation?"), TYPE_DELETE)
        dialog.show(childFragmentManager, "sample")

    }

    companion object {

        private const val TAG = "__SMSContainerFragment"
        private  var viewMesagesRef: View?=null


        var mapofAddressAndSMS: HashMap<String, SMS> = hashMapOf() // for findin duplicate sms in list

        var recyclerViewSpamSms:RecyclerView? = null

        /**
         * function to update marked item count in fragment
         */
        fun updateSelectedItemCount(count:Int){
            if(count>0){
                this.viewMesagesRef!!.tvSelectedCount.visibility = View.VISIBLE
                this.viewMesagesRef!!.tvSelectedCount.text = "$count Selected"
            }else{
                this.viewMesagesRef!!.tvSelectedCount.visibility = View.INVISIBLE
                this.viewMesagesRef!!.tvSelectedCount.text = ""
                markingStarted = false

                this.viewMesagesRef!!.searchViewSms.visibility = View.VISIBLE
                this.viewMesagesRef!!.imgBtnTbrMuteSender.visibility = View.INVISIBLE
                this.viewMesagesRef!!.imgBtnTbrBlock.visibility = View.INVISIBLE
                this.viewMesagesRef!!.imgBtnTbrDelete.visibility = View.INVISIBLE
                this.viewMesagesRef!!.tvSelectedCount.visibility = View.INVISIBLE
            }
        }
        fun showHideBlockButton() {
            if(markedItems.size == 1){
                viewMesagesRef!!.imgBtnTbrBlock.visibility = View.VISIBLE
            }else{
                viewMesagesRef!!.imgBtnTbrBlock.visibility = View.INVISIBLE
            }
        }




    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnSmsPermission ->{
                this.permissionGivenLiveData.value = PermissionUtil.requesetPermission(this.requireActivity())
            }
            R.id.searchViewSms->{
                startSearchActivity()
            }
            R.id.imgBtnTbrBlock->{
                blockUser()
            }
            R.id.imgExpand->{
                showPopupMenu(R.menu.image_chooser_popup, bottomSheetDialog.viewPopup)

            }
            R.id.imgBtnTbrDelete ->{
                deleteMarkedSMSThreads()
            }
            R.id.imgBtnTbrMuteSender ->{
                muteSender()
            }
            R.id.btnBlock->{
                Log.d(TAG, "onClick: ")
                addToBlockList(MarkedItemsHandler.markedContactAddressForBlocking!!)
            }
            R.id.fabSendNewSMS ->{
                this.smsListVIewModel.deleteAllSmsindb() // JUST FOR TESTING PURPOSE
//                val i = Intent(context, ContactSelectorActivity::class.java )
//                i.putExtra(DESTINATION_ACTIVITY, INDIVIDUAL_SMS_ACTIVITY)
//                startActivity(i)
            }
            R.id.imgBtnTbrMore->{
                showPopupMenu(R.menu.sms_list_more_popup,imgBtnTbrMore)
            }
            R.id.imgBtnAvatarMain ->{
               requireContext().startSettingsActivity(activity)
            }

            else ->{
                smsListVIewModel.getUnrealMsgCount()

            }
        }
    }

    private fun showPopupMenu(menu: Int, anchorView: View) {
        Log.d(IndividualSMSActivity.TAG, "onClick: img button")

        val popup = PopupMenu(this.requireActivity(), anchorView )
        popup.inflate(menu)
        popup.setOnMenuItemClickListener(this)
        popup.show()
    }
    override fun onMenuItemClick(menuItem: MenuItem?): Boolean {
        Log.d(TAG, "onMenuItemClick: ")
        when (menuItem!!.itemId){
         R.id.popupMarkAllAsRead->{
             smsListVIewModel.markSMSAsRead(null)
             return true
         }else ->{
            this.spammerType = SpamLocalListManager.menuItemClickPerformed(menuItem, bottomSheetDialog)
            return true
        }
        }
       
    }

    private fun blockUser() {
        if(markedItems.size>1){
            val dialog = ConfirmDialogFragment(this,
                getSpannableString("Please block one contact address at a time"),
                getSpannableString("whaterver"),
                1)
            dialog.show(childFragmentManager,"block")
        }else{
            //set threadId and contact Address
            var num = ""
            var tId = 0L
            for (item in MarkedItemsHandler.markedContactAddress){
                num = item
            }
            for (item in MarkedItemsHandler.markedItems){
                tId = item
            }

            MarkedItemsHandler.markedContactAddressForBlocking = num
            MarkedItemsHandler.markedTheadIdForBlocking = tId
            bottomSheetDialog.show()
        }
    }

    private fun startSearchActivity() {
        Log.d(TAG, "startSearchActivity: ")
        val intent = Intent(activity, SearchSMSActivity::class.java)
        intent.putExtra("animation", "explode")
//        Log.d(TAG, "startSearchActivity: $btnSampleTransition")
//        val p1 = android.util.Pair(searchViewContacts as View,"editTextTransition")

//        val options = ActivityOptions.makeSceneTransitionAnimation(activity )
//        val options  = ActivityOptionsCompat.makeSceneTransitionAnimation(
//            this!!.requireActivity(), btnSampleTransition,
//            ViewCompat.getTransitionName(btnSampleTransition)!!
//        )
        startActivity(intent)
    }

    fun getSkeletonRowCount(context: Context): Int {
        val pxHeight = getDeviceHeight(context)
        val skeletonRowHeight = resources
            .getDimension(R.dimen.row_layout_height).toInt() //converts to pixel
        return Math.ceil(pxHeight / skeletonRowHeight.toDouble()).toInt()
    }

    fun getDeviceHeight(context: Context): Int {
        val resources: Resources = context.resources
        val metrics: DisplayMetrics = resources.getDisplayMetrics()
        return metrics.heightPixels
    }

    override fun onLongPressed(v: View, pos:Int, id: Long, address:String) {


        hideSearchView()
        showToolbarButtons()
        markingStarted = true

        markItem(v, id, address)
    }
    fun showToolbarButtons() {
        Log.d(TAG, "showToolbarButtons: ")

        imgBtnTbrDelete.beVisible()
        imgBtnTbrMuteSender.beVisible()
        imgBtnTbrBlock.beVisible()
        tvSelectedCount.beVisible()

    }
    private fun muteSender() {
        this.smsListVIewModel.muteMarkedSenders()
        resetMarkingOptions()
        val sbar = Snackbar.make(MessagesFragment, "hi", Snackbar.LENGTH_SHORT)
        sbar.setAction("Action", null)
//        sbar.anchorView = bottomNavigationView

        sbar.show()

//        (activity as MainActivity).showSnackBar("hi")
    }

    fun hideSearchView() {
        Log.d(TAG, "hideSearchView: ")
        searchViewSms.visibility = View.INVISIBLE
    }

    /**
     * mark for deletion or archival or block of sms list
     */
    private fun markItem(v: View, id: Long, address: String) {

        v.smsMarked.visibility = View.VISIBLE
        MarkedItemsHandler.markedItems.add(id)
        MarkedItemsHandler.markedViews.add(v)
        MarkedItemsHandler.markedContactAddress.add(address)
        showHideBlockButton()
        updateSelectedItemCount(MarkedItemsHandler.markedItems.size)

    }



    override fun onPause() {
        Log.d(TAG, "onPause: ")
        super.onPause()
        isPaused = true
        if (this.smsListVIewModel!! != null  ) {
            if(this.smsListVIewModel?.SMS != null)
                if(this.smsListVIewModel.SMS!!.hasObservers()){
                    this.smsListVIewModel?.SMS?.removeObservers(this);
                    this.smsListVIewModel.smsLiveData.removeObservers(this)
                    smsListVIewModel.getSmsSendersInfoFromServer().removeObservers(this)
                }

        }
    }

    override fun onStop() {
        Log.d(TAG, "onStop: ")
        super.onStop()

    }

    override var isDefaultFgmnt: Boolean
        get() = isDflt
        set(value) {isDflt = value}

    fun showSearchView() {
        searchViewSms.beVisible()
        imgBtnTbrDelete.beInvisible()
        imgBtnTbrMuteSender.beInvisible()
        imgBtnTbrBlock.beInvisible()
        tvSelectedCount.beInvisible()

        unMarkItems()

    }
    fun isSearchViewVisible(): Boolean {
        if(searchViewSms.visibility== View.VISIBLE)
            return true
        return false
    }



    /**
     * callback of ConfirmDialogfragment for deleting sms
     */
    override fun onYesConfirmationDelete() {
        Log.d(TAG, "deleteSms: called")
//        for(id in markedItems){
        this.smsListVIewModel.deleteThread()
//        }
//        deleteList()

        resetMarkingOptions()
    }

    override fun onYesConfirmationMute() {

    }

    private fun observeNumOfRowsDeleted() {
        this.smsListVIewModel.numRowsDeletedLiveData.observe(viewLifecycleOwner, Observer {
            if(it == 0 ){
                Log.d(TAG, "observeNumOfRowsDeleted: $it")
                checkDefaultSMSHandlerPermission()
            }
        })
    }

    private fun checkDefaultSMSHandlerPermission(): Boolean {
        var requestCode=  222
        var resultCode = 232
        var isDefault = false
        try{

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager: RoleManager = requireContext().getSystemService(RoleManager::class.java)
                // check if the app is having permission to be as default SMS app
                val isRoleAvailable =
                    roleManager.isRoleAvailable(RoleManager.ROLE_SMS)
                if (isRoleAvailable) {
                    // check whether your app is already holding the default SMS app role.
                    val isRoleHeld = roleManager.isRoleHeld(RoleManager.ROLE_SMS)
                    if (!isRoleHeld) {
                        val roleRequestIntent =
                            roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                        startActivityForResult(roleRequestIntent, requestCode)
                    }else{
                        isDefault = true
                        requesetPermission(requireContext())
                    }
                }
            } else {

                val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, requireContext().packageName)
                startActivityForResult(intent, requestCode)
            }

        }catch (e:Exception){
            Log.d(TAG, "checkDefaultSettings: exception $e")
        }
        Log.d(TAG, "checkDefaultSMSHandlerPermission: isDefault $isDefault")
        if(!isDefault){
            resetMarkingOptions()

        }
        return isDefault

    }
}