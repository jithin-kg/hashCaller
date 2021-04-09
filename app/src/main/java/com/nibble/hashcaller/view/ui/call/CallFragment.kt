package com.nibble.hashcaller.view.ui.call

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.telephony.SubscriptionManager
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.FragmentCallBinding
import com.nibble.hashcaller.view.ui.MyUndoListener
import com.nibble.hashcaller.view.ui.blockConfig.blockList.BlockListActivity
import com.nibble.hashcaller.view.ui.call.dialer.DialerAdapter
import com.nibble.hashcaller.view.ui.call.dialer.DialerFragment
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogData
import com.nibble.hashcaller.view.ui.call.utils.CallContainerInjectorUtil
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.clearlists
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.getExpandedLayoutView
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.getMarkedContactAddress
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.getMarkedItemSize
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.isItemSizeEqualsOne
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.isMarkingStarted
import com.nibble.hashcaller.view.ui.call.work.CallContainerViewModel
import com.nibble.hashcaller.view.ui.contacts.individualContacts.IndividualCotactViewActivity
import com.nibble.hashcaller.view.ui.contacts.individualContacts.utils.PermissionUtil
import com.nibble.hashcaller.view.ui.contacts.makeCall
import com.nibble.hashcaller.view.ui.contacts.utils.*
import com.nibble.hashcaller.view.ui.extensions.getMyPopupMenu
import com.nibble.hashcaller.view.ui.extensions.getSpannableString
import com.nibble.hashcaller.view.ui.extensions.isScreeningRoleHeld
import com.nibble.hashcaller.view.ui.sms.individual.IndividualSMSActivity
import com.nibble.hashcaller.view.ui.sms.individual.util.*
import com.nibble.hashcaller.view.ui.sms.util.MarkedItemsHandler
import com.nibble.hashcaller.view.utils.ConfirmDialogFragment
import com.nibble.hashcaller.view.utils.ConfirmationClickListener
import com.nibble.hashcaller.view.utils.IDefaultFragmentSelection
import com.nibble.hashcaller.view.utils.spam.SpamLocalListManager
import kotlinx.android.synthetic.main.activity_individual_cotact_view.*
import kotlinx.android.synthetic.main.bottom_sheet_block.*
import kotlinx.android.synthetic.main.bottom_sheet_block_feedback.*
import kotlinx.android.synthetic.main.call_list.*
import kotlinx.android.synthetic.main.call_list.view.*
import kotlinx.android.synthetic.main.contact_list.*
import kotlinx.android.synthetic.main.fragment_call.*
import kotlinx.android.synthetic.main.fragment_call.view.*
import kotlinx.coroutines.flow.collect


/**
 * A simple [Fragment] subclass.
 * Use the [CallFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CallFragment : Fragment(),View.OnClickListener , IDefaultFragmentSelection,
    DialerAdapter.CallItemLongPressHandler, ConfirmationClickListener,
    MyUndoListener.SnackBarListner,android.widget.PopupMenu.OnMenuItemClickListener,
    PopupMenu.OnMenuItemClickListener {
    private  var _binding: FragmentCallBinding? = null
    private val binding get() = _binding!!
    private var isDflt = false


    private var isScreeningApp = false
//    private var toolbar: Toolbar? = null
//    var callFragment: View? = null
//    private lateinit var searchViewCall: EditText
    var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    var layoutBottomSheet: ConstraintLayout? = null
    private lateinit var dialerFragment: DialerFragment
    private lateinit var viewmodel: CallContainerViewModel
    private  var lastOperationPerformed: Int ? = null
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var bottomSheetDialogfeedback: BottomSheetDialog
    private  var selectedRadioButton: RadioButton? = null
    private  var spammerType:Int = -1
    private var SPAMMER_CATEGORY = SpamLocalListManager.SPAMMER_BUISINESS
    private lateinit var recyclerV : RecyclerView
    private lateinit var layoutMngr: LinearLayoutManager
    /************/
    var callLogAdapter: DialerAdapter? = null
    private var permissionGivenLiveData: MutableLiveData<Boolean> = MutableLiveData()


//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        Log.d(TAG, "onActivityCreated: ")
//        super.onActivityCreated(savedInstanceState)
//        if(savedInstanceState!= null){
//
//        }
//    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

    _binding = FragmentCallBinding.inflate(inflater, container, false)
//        callFragment =  inflater.inflate(R.layout.fragment_call, container, false)
//    recyclerV = callFragment!!.findViewById(R.id.rcrViewCallHistoryLogs)

        initViewModel()
    registerForContextMenu( binding.rcrViewCallHistoryLogs) //in oncreatView
    // Inflate the layout for this fragment
    if(checkContactPermission()){

        getFirst10items()
        observeCallLogMutabeLivedata()
        initRecyclerView()
        addScrollListener()
        observePermissionLiveData()
        observeCallLog()
        observeCallLogInfoFromServer()
        setupSimCardCount()

    }

    setupBottomSheet()
    initListeners()



//        addFragmentDialer()
        return binding.root

    }

    private fun getFirst10items() {
        viewmodel.fetchCallLogFlow(requireActivity())
//        lifecycleScope.launchWhenStarted {
//            CallLogFlowHelper.fetchCallLogFlow(this@CallFragment.requireActivity()).collect {
//               viewmodel. updateLiveDataWithFlow(it)
//            }
//        }

    }

    private fun observePermissionLiveData() {
        this.permissionGivenLiveData.observe(viewLifecycleOwner, Observer {
            if(it == true){
                Log.d(TAG, "observePermissionLiveData: permission given")
                if(this.viewmodel?.callLogs != null){
                    if(!this.viewmodel.callLogs!!.hasObservers()){
                        observeCallLog()
                        observeCallLogMutabeLivedata()
                        binding.btnCallhistoryPermission.visibility = View.GONE
                    }
                }

            }else{
                binding.btnCallhistoryPermission.visibility =View.VISIBLE
                Log.d(TAG, "observePermissionLiveData: permission not given")
                if (this.viewmodel!! != null  ) {

                    if(this.viewmodel?.callLogs != null)
                        if(this.viewmodel.callLogs!!.hasObservers())
                            this.viewmodel?.callLogs?.removeObservers(this);
                }
            }
        })
    }
    private fun initListeners() {

        binding.btnCallhistoryPermission.setOnClickListener(this)
        binding.imgBtnCallTbrBlock.setOnClickListener(this)
        binding.imgBtnCallTbrMuteCaller.setOnClickListener(this)
        binding.imgBtnCallTbrDelete.setOnClickListener(this)
        binding.fabBtnShowDialpad.setOnClickListener(this)
        binding.imgBtnCallUnMuteCaller.setOnClickListener(this)
        binding.imgBtnCallTbrMore.setOnClickListener(this)

        bottomSheetDialog.radioS.setOnClickListener(this)
        bottomSheetDialog.radioScam.setOnClickListener(this)
        bottomSheetDialog.imgExpand.setOnClickListener(this)
        bottomSheetDialog.btnBlock.setOnClickListener(this)
    }

    private fun observeCallLogMutabeLivedata(){
        viewmodel.callLogsMutableLiveData.observe(viewLifecycleOwner, Observer {
            binding.shimmerViewContainerCall.beInvisible()
            callLogAdapter?.setCallLogs(it)
        })
    }
    private fun observeCallLog() {
        viewmodel.callLogs.observe(viewLifecycleOwner, Observer { logs->
            logs.let {
                viewmodel.updateCAllLogLivedata(logs)
//                viewmodel.setAdditionalInfo(logs)
            }
        })
    }


    private fun checkContactPermission(): Boolean {
        val permissionSms =
            ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_CALL_LOG)
        if(permissionSms!= PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "checkContactPermission: false")
//            this.callFragment!!.pgbarCallHistory.visibility = View.GONE
            return false
        }
        Log.d(TAG, "checkContactPermission: true")

        return true
    }

    private fun addScrollListener() {
        binding.rcrViewCallHistoryLogs.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
//                if(dy>0){
                //scrollview scrolled vertically
                //get the visible item count
                if(layoutMngr!=null){
                    val visibleItemCount = layoutMngr!!.childCount
                    val pastVisibleItem = layoutMngr!!.findFirstCompletelyVisibleItemPosition()
                    val recyclerViewSize = callLogAdapter!!.itemCount
                    var isLoading = false
                    if(!fullDataFromCproviderFetched){
                        Log.d(TAG, "onScrolled: getting next page")
                        if((visibleItemCount + pastVisibleItem) >= recyclerViewSize){
                            //we have reached the bottom
                            pageCall+=10
                            viewmodel.getNextPage()
                            if(dy > 0){
                                if(!isSizeEqual){
//                                    viewMesages.shimmer_view_container.visibility = View.VISIBLE
//                                    viewMesages.rcrViewSMSList.visibility = View.INVISIBLE
                                }
                            }
                        }
                    }

                }
//                }
            }
        })
    }







    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isScreeningApp = ( activity as AppCompatActivity).isScreeningRoleHeld()
        }

        this.permissionGivenLiveData.value  = checkContactPermission()
    }

    private fun initViewModel() {
        viewmodel = ViewModelProvider(this, CallContainerInjectorUtil.provideViewModelFactory(context)).get(
            CallContainerViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: ")
        super.onCreate(savedInstanceState)
        if(savedInstanceState == null){

        }

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: ")
        super.onViewCreated(view, savedInstanceState)


//        intialize()






    }
    @SuppressLint("MissingPermission  Manifest.permission.READ_PHONE_STATE", "MissingPermission")
    private fun setupSimCardCount() {
        val subscriptionManager = requireContext(). getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val availableSIMs  = subscriptionManager.activeSubscriptionInfoList
        if(availableSIMs.size >0){

        }

    }

    private fun observeCallLogInfoFromServer() {
        this.viewmodel.getCallLogFromServer().observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "observeCallLogInfoFromServer: ")

            viewmodel.updateWithNewInfoFromServer()
        })
    }

    /**
     * important to prevent memory leak
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun initRecyclerView() {

        binding.rcrViewCallHistoryLogs.apply {
            layoutManager = LinearLayoutManager(activity)
            layoutMngr = layoutManager as LinearLayoutManager
//            val topSpacingDecorator =
//                TopSpacingItemDecoration(
//                    30
//                )
//            addItemDecoration(topSpacingDecorator)
            callLogAdapter = DialerAdapter(context,this@CallFragment) {

                    id:Long, position:Int, view:View, btn:Int, callLog: CallLogData ->onCallItemClicked(id, position, view, btn, callLog)}
            adapter = callLogAdapter

        }
    }



    private fun toggleExpandableView(v: View, pos: Int) {

    }

//    private fun makeCall(callLog: CallLogData) {
//
//        val num = callLog.number
//
//        if(num.isNotEmpty())  {
//
//
//            val intent =
//                Intent(Intent.ACTION_CALL, Uri.parse("tel:${callLog.number}"))
//            val simSlotIndex = 1 //Second sim slot
//
//
////            try {
////                val getSubIdMethod =
////                    SubscriptionManager::class.java.getDeclaredMethod(
////                        "getSubId",
////                        Int::class.javaPrimitiveType
////                    )
////                getSubIdMethod.isAccessible = true
////                val subIdForSlot = (getSubIdMethod.invoke(
////                    SubscriptionManager::class.java,
////                    simSlotIndex
////                ) as LongArray)[0]
////                val componentName = ComponentName(
////                    "com.android.phone",
////                    "com.android.services.telephony.TelephonyConnectionService"
////                )
////                val phoneAccountHandle =
////                    PhoneAccountHandle(componentName, 0.toString())
////                intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", phoneAccountHandle)
////            } catch (e: Exception) {
////                e.printStackTrace()
////            }
////
////            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
////            requireActivity()!!.startActivity(intent)
//
//
//
//            val callIntent = Intent(Intent.ACTION_CALL)
//            callIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            callIntent.data = Uri.parse("tel:$num")
//            requireActivity().startActivity(callIntent)
//
////            callIntent.putExtra("com.android.phone.extra.slot", 0); //For sim 1
////           callIntent.putExtra("simSlot", 0)
////
//        }
//    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)



    }


    private fun addFragmentDialer() {

       val  ft = childFragmentManager.beginTransaction()
        ft?.add(R.id.frameFragmentDialer, dialerFragment)
        ft.replace(R.id.frameFragmentDialer, dialerFragment).commit();

    }



    private fun intialize() {
//        fabBtnShowDialpad?.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_baseline_add_24))
//        fabBtnShowDialpad?.setOnClickListener(this as View.OnClickListener)
//        layoutBottomSheet = callFragment!!.findViewById(R.id.bottom_sheet)
    }

    private fun toggleBottomNavView() {
        val bottomNavigation =
            requireActivity().findViewById<View>(R.id.bottomNavigationView)

        if (bottomNavigation.visibility == View.VISIBLE) {
            bottomNavigation.visibility = View.INVISIBLE
//            fabBtnShowDialpad.hide()
            //            bottomSheetBehavior.setPeekHeight((Resources.getSystem().getDisplayMetrics().heightPixels)/2);
            return
        }
        bottomNavigation.visibility = View.VISIBLE
//        fabBtnShowDialpad.show()
    }



    private fun initialize() {
//        toolbar = callFragment?.findViewById(R.id.toolbarCall)
//        searchViewCall = callFragment?.findViewById(R.id.searchViewCall)!!
    }

    companion object {
            private const val TAG ="__CallFragment"
            var pageCall = 10
            var fullDataFromCproviderFetched = false
    
    }


    override fun onClick(v: View?) {
        Log.d(TAG, "onClick: ")
        when (v?.id) {
//            R.id.fabBtnShowDialpad-> {
//                Log.d(TAG, "onClick: show dialpad button clicked")
////                if (bottomSheetBehavior?.state != BottomSheetBehavior.STATE_EXPANDED) {
//////                    toggleBottomNavView()
////                    bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
////                    Log.d(TAG, "onClick:  hiding")
////                }
//                showDialerFragment()
////                else{
////                    Log.d(TAG, "onClick: expanding")
////                    bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
////                }
//
//            }else->{
//            Log.d(TAG, "onClick: clicked ")
//        }
            R.id.btnCallhistoryPermission->{
                val res  = PermissionUtil.requestCallLogPermission(this.requireActivity())
                Log.d(TAG, "onClick: res is $res")
                this.permissionGivenLiveData.value = res
            }
            R.id.imgBtnCallTbrDelete->{
                deletemarkedLogs()
            }
            R.id.imgBtnCallTbrMuteCaller ->{
                muteMarkedCaller()
            }
            R.id.imgBtnCallTbrBlock->{
                showBottomSheetDialog()
            }
            R.id.imgBtnCallUnMuteCaller ->{
                unmuteUser()
            }
            R.id.fabBtnShowDialpad ->{
                viewmodel.clearCallLogDB()
//                (activity as MainActivity).showDialerFragment()
            }
            R.id.imgBtnCallTbrMore ->{
                val popup = (requireActivity() as AppCompatActivity).getMyPopupMenu(R.menu.call_fragment_popup_menu, imgBtnCallTbrMore)
                popup.setOnMenuItemClickListener(this)
                popup.show()
            }
            R.id.btnBlock->{
                blockMarkedCaller()
            }

        }

    }




    private fun unmuteUser() {
        viewmodel.unmuteByAddress().observe(viewLifecycleOwner, Observer {
            when(it){
                OPERATION_COMPLETED -> {
                    requireActivity().toast("Enabled notification for ${viewmodel.contactAdders} ", Toast.LENGTH_LONG)
                    binding.imgBtnCallUnMuteCaller.beInvisible()
                    binding.imgBtnCallTbrMuteCaller.beVisible()
                    clearlists()
                    showSearchView()
                }
            }
        })
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
            Log.d(IndividualSMSActivity.TAG, "bottomSheetDialogDismissed")

        }
    }

    override fun onMenuItemClick(menuItem: MenuItem?): Boolean {
        when(menuItem?.itemId){
            R.id.itemMyBlockList ->{
                val intent = Intent(activity, BlockListActivity::class.java)
                startActivity(intent)
            }else ->{
            this.spammerType = SpamLocalListManager.menuItemClickPerformed(menuItem, bottomSheetDialog)
            }
        }
        return true



    }
    private fun showBottomSheetDialog() {
        bottomSheetDialog.show()

    }

    private fun blockMarkedCaller() {

        this.viewmodel.blockThisAddress(getMarkedContactAddress()!!, MarkedItemsHandler.markedTheadIdForBlocking,
            this.spammerType,
            this.SPAMMER_CATEGORY )

//        Toast.makeText(this.requireActivity(), "Number added to spamlist", Toast.LENGTH_LONG)
        bottomSheetDialog.hide()
        bottomSheetDialog.dismiss()
            bottomSheetDialogfeedback.show()
        var txt = "${getMarkedContactAddress()} can no longer send SMS or call you."
        val  sb =  SpannableStringBuilder(txt);
        val bss =  StyleSpan(Typeface.BOLD); // Span to make text bold
        sb.setSpan(bss, 0, getMarkedContactAddress()!!.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE); // make first 4 characters Bold
        bottomSheetDialogfeedback.tvSpamfeedbackMsg.text = sb
//        resetMarkingOptions()

    }
    private fun muteMarkedCaller() {
//        val dialog = ConfirmDialogFragment(this,  "Mut")
        val dialog = ConfirmDialogFragment(this,
            getSpannableString("You won't receive call notification from ${getMarkedContactAddress()}"),
            getSpannableString("Mute caller  "), TYPE_MUTE)
        dialog.show(childFragmentManager, "sample")


    }

    private fun deletemarkedLogs() {

        val dialog = ConfirmDialogFragment(this,
            getSpannableString("This can't be undone"),
            getSpannableString("Delete call history ? "), TYPE_DELETE)
        dialog.show(childFragmentManager, "sample")
    }

    private fun showDialerFragment() {
        val ft = childFragmentManager.beginTransaction()
        if (dialerFragment.isAdded) { // if the fragment is already in container
            ft?.show(dialerFragment)
        }else{
            Log.d(TAG, "showDialerFragment:  fragment is not added")
        }
    }

    override var isDefaultFgmnt: Boolean
        get() = isDflt
        set(value) {isDflt = value}

    override fun onLongPressed(view: View, pos: Int, id: Long, address: String) {
        val expandedView = getExpandedLayoutView()
        expandedView?.beGone()
        markItem(id, pos, view, address)
    }

    override fun onCallButtonClicked(view: View, type: Int, log: CallLogData) {

        when(type){
            INTENT_TYPE_MAKE_CALL->{
                requireContext().makeCall(log.number)
            }
            INTENT_TYPE_START_INDIVIDUAL_SMS ->{
                val intent = Intent(context, IndividualSMSActivity::class.java )
                val bundle = Bundle()
                bundle.putString(CONTACT_ADDRES, log.number)
                bundle.putString(SMS_CHAT_ID, "")

                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                intent.putExtras(bundle)
                startActivity(intent)
            }
            INTENT_TYPE_MORE_INFO -> {
                val intent = Intent(context, IndividualCotactViewActivity::class.java )
                intent.putExtra(com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ID, log.number)
                intent.putExtra("name", log.name )
                intent.putExtra("photo", "")
                intent.putExtra("color", log.color)

                val pairList = ArrayList<android.util.Pair<View, String>>()
                val p1 = android.util.Pair(imgViewUserPhoto as View,"contactImageTransition")
                val p2 = android.util.Pair(textViewCrclr as View, "firstLetterTransition")
//                pairList.add(p1)
                pairList.add(p2)
                val options = ActivityOptions.makeSceneTransitionAnimation(activity,pairList[0] )
                options.toBundle()

                startActivity(intent, options.toBundle())
            }



        }
    }



    private fun onCallItemClicked(
        id: Long,
        position: Int,
        view: View,
        btn: Int,
        callLog: CallLogData
    ): Int {
        Log.d(TAG, "onCallLog item clicked: $id")
        var viewExpanded = 0
        if(isMarkingStarted()) {

            markItem(id, position, view, callLog.number)
        }else{
            viewExpanded = 1
        }
//        else{
//            viewExpanded = 1
//            //no marking started, then expand the layout
//            val id = callLogAdapter!!.getItemId(position)
//            val v = view.findViewById<ConstraintLayout>(R.id.layoutExpandableCall)
//            when(btn){
//                DialerAdapter.BUTTON_SIM_1->{
//                    Log.d(TAG, "onCallLogItemClicked: buttonsim 1")
//                    makeCall(callLog)
//
//                }
//                DialerAdapter.BUTTON_SIM_2->{
//
//                }
//                DialerAdapter.BUTTON_SMS->{
//
//                }
//                DialerAdapter.BUTTON_INFO->{
//
//                }
//
//            }
//        }

        return viewExpanded
//        if(v.visibility == View.GONE){
//            v.visibility = View.VISIBLE
//        }else{
//            v.visibility = View.GONE
//        }

//        Log.d(TAG, "onCallLogItemClicked: ")
//        val intent = Intent(context, IndividualCotactViewActivity::class.java )
//        intent.putExtra(CONTACT_ID, id)
//        startActivity(intent)
    }

    private fun markItem(
        id: Long,
        position: Int,
        view: View,
        number: String
    ) {

        lifecycleScope.launchWhenStarted {
            viewmodel.markItem(id, view, position, number).collect{

                var viewMain = view.findViewById<ConstraintLayout>(R.id.layoutcallMain)


                when(it){
                    CALL_NEW_ITEM_MARKED ->{
                        viewMain.imgViewCallMarked.beVisible()

                    }
                    CALL_ITEM_UN_MARKED ->{
                        viewMain.imgViewCallMarked.beInvisible()
//                        updateSelectedItemCount()

                    }
                }

                    if(isMarkingStarted()){
                        showDeleteBtnInToolbar()
                    }

                    updateSelectedItemCount()
                if(isItemSizeEqualsOne()){
                    showBlockButon()
                }
                else{
                    hideBlockButton()
                }
//                    view.setBackgroundColor(ContextCompat.getColorColor(requireContext(), R.color.colorPrimaryLowOpacity))
            }
        }

    }

    private fun hideBlockButton() {
        this.requireActivity().runOnUiThread {
            binding.imgBtnCallTbrBlock.beInvisible()
            binding.imgBtnCallTbrMuteCaller.beInvisible()
            binding.imgBtnCallUnMuteCaller.beInvisible()
        }

    }

    private fun showBlockButon() {
       this.requireActivity().runOnUiThread {
           binding.imgBtnCallTbrBlock.beVisible()
          if(isScreeningApp){ // checking screening app rol is available
              //check if user already muted or blocked the contact
              viewmodel.checkWhetherMutedOrBlocked().observe(viewLifecycleOwner, Observer {
                  when(it){
                      IS_MUTED_ADDRESS -> {
                          if(isScreeningApp){
                              binding.imgBtnCallUnMuteCaller.beVisible()
                              binding.imgBtnCallTbrMuteCaller.beInvisible()
                          }

                      }
                      IS_NOT_MUTED_ADDRESS ->{

                          binding.imgBtnCallUnMuteCaller.beInvisible()
                          binding.imgBtnCallTbrMuteCaller.beVisible()
                      }
                  }
              })

          }
       }

    }

    private fun showDeleteBtnInToolbar() {
        Log.d(TAG, "showDeleteBtnInToolbar: ")
        binding.searchViewCall.beInvisible()
        binding.imgBtnCallTbrBlock.beVisible()
        if(isScreeningApp){
            binding.imgBtnCallTbrMuteCaller.beVisible()

        }
        binding.imgBtnCallTbrDelete.beVisible()
        binding.imgBtnCallTbrMore.beVisible()

    }
     fun showSearchView(){
        binding.searchViewCall.beVisible()
        binding.imgBtnCallTbrBlock.beInvisible()
        binding. imgBtnCallTbrMuteCaller.beInvisible()
        binding.imgBtnCallTbrDelete.beInvisible()
        binding.imgBtnCallTbrMore.beInvisible()
        binding.tvCallSelectedCount.beInvisible()
         binding.imgBtnCallUnMuteCaller.beInvisible()
    }

    fun updateSelectedItemCount(){
        val count = getMarkedItemSize()
        binding.tvCallSelectedCount.text = "${count.toString()} Selected"
        if(count>0){
            binding.tvCallSelectedCount.beVisible()
        }else{
            showSearchView()
        }
    }

    override fun onYesConfirmationDelete() {
        this.viewmodel.deleteThread().observe(viewLifecycleOwner, Observer {
           when(it){
               SMS_DELETE_ON_PROGRESS ->{

               }
               SMS_DELETE_ON_COMPLETED ->{
                   showSearchView()
               }
           }
        })
    }

    override fun onYesConfirmationMute() {
        viewmodel.muteMarkedCaller().observe(viewLifecycleOwner, Observer {
            when(it){
                OPERATION_COMPLETED ->{

                    val sbar = Snackbar.make(binding.cordinateLyoutCall,
                        "You no longer notified on from ${viewmodel.contactAdders}",
                        Snackbar.LENGTH_SHORT)
                    lastOperationPerformed = OPERTION_MUTE
                    sbar.setAction("Undo", MyUndoListener(this))
//        sbar.anchorView = bottomNavigationView

                    sbar.show()
//                   showSnackBar("You no longer notified on from 800")
//                val sbar = Snackbar.make(cordinateLyoutMainActivity, "You no longer notified on from 800", Snackbar.LENGTH_SHORT)
//                sbar.show()
                    clearlists()
                    showSearchView()
                }
            }
        })
    }

    override fun onUndoClicked() {
        when(lastOperationPerformed){
            OPERTION_MUTE ->{
                viewmodel.unmute()
            }
            OPERTION_DELETE ->{

            }
        }
    }

}