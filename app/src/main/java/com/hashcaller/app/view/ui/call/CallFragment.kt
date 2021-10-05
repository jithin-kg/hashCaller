package com.hashcaller.app.view.ui.call

import android.Manifest.permission.*
import android.app.ActivityOptions
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.google.firebase.auth.FirebaseAuth
import com.hashcaller.app.R
import com.hashcaller.app.databinding.FragmentCallBinding
import com.hashcaller.app.utils.Constants.Companion.SPAMMER_TYPE_BUSINESS
import com.hashcaller.app.utils.PermisssionRequestCodes.Companion.REQUEST_CODE_CALL_LOG
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.utils.constants.IntentKeys
import com.hashcaller.app.utils.extensions.requestCallPhonePermission
import com.hashcaller.app.utils.extensions.startSearchActivity
import com.hashcaller.app.utils.internet.ConnectionLiveData
import com.hashcaller.app.utils.notifications.HashCaller.Companion.CHANNEL_1_ID
import com.hashcaller.app.utils.notifications.HashCaller.Companion.CHANNEL_2_ID
import com.hashcaller.app.utils.notifications.HashCaller.Companion.CHANNEL_3_CALL_SERVICE_ID
import com.hashcaller.app.view.ui.MainActivity
import com.hashcaller.app.view.ui.MainActivityInjectorUtil
import com.hashcaller.app.view.ui.MyUndoListener
import com.hashcaller.app.view.ui.auth.getinitialInfos.UserInfoViewModel
import com.hashcaller.app.view.ui.blockConfig.blockList.BlockListActivity
import com.hashcaller.app.view.ui.call.RelativeTime.Companion.OLDER
import com.hashcaller.app.view.ui.call.RelativeTime.Companion.YESTERDAY
import com.hashcaller.app.view.ui.call.db.CallLogTable
import com.hashcaller.app.view.ui.call.dialer.CallLogAdapter
import com.hashcaller.app.view.ui.call.dialer.DialerFragment
import com.hashcaller.app.view.ui.call.dialer.util.CustomLinearLayoutManager
import com.hashcaller.app.view.ui.call.floating.NOTIFICATION_CHANNEL_GENERAL
import com.hashcaller.app.view.ui.call.individualCallLog.IndividualCallLogActivity
import com.hashcaller.app.view.ui.call.utils.CallContainerInjectorUtil
import com.hashcaller.app.view.ui.call.work.CallContainerViewModel
import com.hashcaller.app.view.ui.contacts.individualContacts.IndividualContactViewActivity
import com.hashcaller.app.view.ui.contacts.isDarkThemeOn
import com.hashcaller.app.view.ui.contacts.makeCall
import com.hashcaller.app.view.ui.contacts.utils.*
import com.hashcaller.app.view.ui.extensions.getSpannableString
import com.hashcaller.app.view.ui.extensions.isScreeningRoleHeld
import com.hashcaller.app.view.ui.sms.individual.util.*
import com.hashcaller.app.view.ui.sms.list.SMSListAdapter
import com.hashcaller.app.view.utils.ConfirmDialogFragment
import com.hashcaller.app.view.utils.ConfirmationClickListener
import com.hashcaller.app.view.utils.IDefaultFragmentSelection
import com.hashcaller.app.view.utils.getRelativeTime
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.models.PermissionRequest
import kotlinx.android.synthetic.main.activity_individual_cotact_view.*
import kotlinx.android.synthetic.main.bottom_sheet_block.*
import kotlinx.android.synthetic.main.bottom_sheet_block_feedback.*
import kotlinx.android.synthetic.main.call_list.*
import kotlinx.android.synthetic.main.call_list.view.*
import kotlinx.android.synthetic.main.contact_list.*
import kotlinx.android.synthetic.main.fragment_call.*
import kotlinx.android.synthetic.main.fragment_call.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * A simple [Fragment] subclass.
 * Use the [CallFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CallFragment : Fragment(), View.OnClickListener , IDefaultFragmentSelection,
    CallLogAdapter.ViewHandlerHelper, ConfirmationClickListener,
    MyUndoListener.SnackBarListner,android.widget.PopupMenu.OnMenuItemClickListener,
    PopupMenu.OnMenuItemClickListener, SMSListAdapter.NetworkHandler {

    private  lateinit var binding: FragmentCallBinding
    private var isDflt = false
    private var isInternetAvailable = false
    private var isScreeningApp = false

    var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    var layoutBottomSheet: ConstraintLayout? = null
    private lateinit var dialerFragment: DialerFragment

    private  var viewmodel :CallContainerViewModel? = null
    private lateinit var sharedUserInfoViewmodel: UserInfoViewModel
    private  var lastOperationPerformed: Int ? = null
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var bottomSheetDialogfeedback: BottomSheetDialog
    private  var spammerType:Int = SPAMMER_TYPE_SCAM
    private lateinit var recyclerV : RecyclerView
    private lateinit var layoutMngr: LinearLayoutManager

    private  var radioSales:RadioButton?= null
    private  var radioScam:RadioButton?= null
    private  var radioBusiness:RadioButton?= null
    private  var radioPerson:RadioButton?= null
    private  var selectedRadioButton: RadioButton? = null
    private var radioGroupOne: RadioGroup? = null
    private var radioGroupTwo: RadioGroup? = null
    private var isCallLogsCpEmpty = false
    var callLogAdapter: CallLogAdapter? = null
    private var permissionGivenLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private var tokenHelper: TokenHelper? = null
    private var btnBlock:Button? = null


    
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
    binding = FragmentCallBinding.inflate(inflater, container, false)
       tokenHelper =  TokenHelper(FirebaseAuth.getInstance().currentUser)
    registerForContextMenu(binding.rcrViewCallHistoryLogs) //in oncreatView
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        setupBottomSheet()
        initListeners()
        deleteNotificationChannels()

        if(checkRequiredPermission()){
            showRecyclerView()
            getDataDelayed()
        }else{
            binding.btnCallFragmentPermission.beVisible()
            hideRecyclerView()

        }

    }

    private fun deleteNotificationChannels() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationManager =requireContext().getSystemService(NotificationManager::class.java)
            notificationManager.deleteNotificationChannel(CHANNEL_3_CALL_SERVICE_ID)
            notificationManager.deleteNotificationChannel(CHANNEL_2_ID)
            notificationManager.deleteNotificationChannel(CHANNEL_1_ID)
            notificationManager.deleteNotificationChannel(NOTIFICATION_CHANNEL_GENERAL)
        }
    }

    private fun checkScreeningRole(): Boolean {
        var isDefault = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if((activity as AppCompatActivity).isScreeningRoleHeld()){
               isDefault = true
            }
        }else {
            isDefault = true
        }

        return isDefault
    }

    private fun hideRecyclerView() {
        binding.rcrViewCallHistoryLogs.beInvisible()
    }

    private fun showRecyclerView() {

        binding.btnCallFragmentPermission.beInvisible()
        binding.rcrViewCallHistoryLogs.beVisible()
        binding.pgBarCall.beVisible()
    }

     fun getDataDelayed() {

             lifecycleScope.launchWhenStarted {
                 initViewModel()
                 getFirst10items()
                 observeCallLog()
                 setupSimCardCount()
                 observeMarkedItems()
                 observeCallLogFromDb()
                 observeCallLogInfoFromServer()
                 observeInternetLivedata()
                 updateSpamThreshold()
             }
    }

    private fun updateSpamThreshold() {
        viewmodel?.updateSpamThreshold(applicationContext = requireContext().applicationContext)
    }


    private  fun observeInternetLivedata() {
        val cl = context?.let { ConnectionLiveData(it) }
        cl?.observe(viewLifecycleOwner, Observer {
            isInternetAvailable = it
        })
    }



    private suspend fun getFirst10items() {
        viewmodel?.getFirst10Logs()?.observe(viewLifecycleOwner, Observer {
            callLogAdapter?.itemCount.let { count ->
                if(count!=null && count < it.size ){
                        if(isCallLogsCpEmpty && it.isEmpty())
                            binding.pgBarCall.beGone()
                        else if(!isCallLogsCpEmpty && it.isNotEmpty())
                            binding.pgBarCall.beGone()
                    submitListToAdapter(it, true)
                }
            }

        })
    }

    private fun submitListToAdapter(it: MutableList<CallLogTable>, isFromFirst10Items: Boolean) {
        lifecycleScope.launchWhenStarted {
            if(!checkScreeningRole()){
                it.add(0, CallLogTable(id=ID_SHOW_SCREENING_ROLE, hUid = ""))
            }else {
                callLogAdapter?.removeCallerIdRoleItem()
            }
            callLogAdapter?.submitCallLogs(it, isFromFirst10Items)
        }
    }

    private suspend fun observeMarkedItems() {
        viewmodel?.markeditemsHelper?.markedItems?.observe(viewLifecycleOwner, Observer {
            when(it.size){
                0 ->{
//                    showSearchView()
                }
                else ->{
                    showBlockBtnInToolbar(it.size)
                }

            }
        })
    }


    private suspend fun observeCallLogFromDb() {
        this.viewmodel?.callLogTableData?.observe(viewLifecycleOwner, Observer {
            if(it.isEmpty() && isCallLogsCpEmpty) binding.pgBarCall.beGone()
            !isCallLogsCpEmpty && it.isNotEmpty()
            binding.pgBarCall.beGone()

            lifecycleScope.launchWhenCreated {
                withContext(Dispatchers.IO){
                    if(it.isNotEmpty()){
                        var todayDayNumber:String? = null
                        var yesterDayNumber:String? = null
                         var olderDayNumber:String? = null

                        for (log in it){
//                            if(olderDayNumber != null){
//                                break
//                            }
                            val relativeObj =  getRelativeTime(log.dateInMilliseconds)
                            if(relativeObj.relativeDay == RelativeTime.TODAY && todayDayNumber == null) {
                                log.relativeDay = "Today"
                                todayDayNumber ="Today"
                            }else if(relativeObj.relativeDay == YESTERDAY && yesterDayNumber == null ){
                                log.relativeDay = "Yesterday"
                                yesterDayNumber ="Yesterday"
                            }else if(relativeObj.relativeDay == OLDER && olderDayNumber == null ){
                                log.relativeDay = "Older"
                                olderDayNumber ="Older"
                            }else {
                                log.relativeDay = ""
                            }


                        }
                    }
                    withContext(Dispatchers.Main){
                        submitListToAdapter(it, false)
                    }

                }
            }

        })
    }




    private fun initListeners() {

//        binding.imgBtnCallTbrBlock.setOnClickListener(this)
//        binding.imgBtnCallUnMuteCaller.setOnClickListener(this)
//        binding.imgBtnCallSearch.setOnClickListener(this)
//        binding.imgBtnHamBrgerCalls.setOnClickListener(this)
//        binding.fabBtnShowDialpad.setOnClickListener(this)


        radioSales?.setOnClickListener(this)
        radioScam?.setOnClickListener(this)
        radioBusiness?.setOnClickListener(this)
        radioPerson?.setOnClickListener(this)
        btnBlock?.setOnClickListener(this)
        binding.btnCallFragmentPermission.setOnClickListener(this)
    }


    private  suspend fun observeCallLog() {
        viewmodel?.callLogs?.observe(viewLifecycleOwner, Observer { logs->
            logs.let {
                isCallLogsCpEmpty = it.isEmpty()
                viewmodel?.updateDatabase(logs, context?.applicationContext)

            }
        })
    }

    private fun checkRequiredPermission(): Boolean {
       return EasyPermissions.hasPermissions(context,
           READ_CALL_LOG
       )
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
                        if((visibleItemCount + pastVisibleItem) >= recyclerViewSize){
                            pageCall+=10
//                            viewmodel.getNextPage()
                            if(dy > 0){
                                if(!isSizeEqual){
                                }
                            }
                        }
                    }

                }
            }
        })
    }







    override fun onResume() {
        super.onResume()

        if(checkScreeningRole()){
            callLogAdapter?.removeCallerIdRoleItem()
        }else {
//            callLogAdapter?.addCallerIdRoleItem()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isScreeningApp = ( activity as AppCompatActivity).isScreeningRoleHeld()
        }
    }
    private suspend fun initViewModel() {
        withContext(Dispatchers.IO){

            viewmodel = ViewModelProvider(this@CallFragment, CallContainerInjectorUtil.provideViewModelFactory(context?.applicationContext, lifecycleScope, tokenHelper)).get(
                CallContainerViewModel::class.java)
            sharedUserInfoViewmodel = ViewModelProvider(this@CallFragment, MainActivityInjectorUtil.provideUserInjectorUtil(
                requireContext(),
                tokenHelper
            )).get(
                UserInfoViewModel::class.java
            )
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(savedInstanceState == null){

        }

    }

    private suspend fun setupSimCardCount() {
//        val subscriptionManager = requireContext(). getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
//        val availableSIMs  = subscriptionManager.activeSubscriptionInfoList
//        if(availableSIMs.size >0){
//
//        }

    }

    private suspend fun observeCallLogInfoFromServer() {
           viewmodel?.callersInfoFromDBLivedta?.observe(viewLifecycleOwner, Observer {
                viewmodel?.updateWithNewInfoFromServer(it)
            })

    }

    /**
     * important to prevent memory leak
     */
    override fun onDestroyView() {
        super.onDestroyView()
        viewmodel = null
    }


    private fun initRecyclerView() {
        binding.rcrViewCallHistoryLogs.apply {
            layoutManager = CustomLinearLayoutManager(context)
            layoutMngr = layoutManager as CustomLinearLayoutManager
                callLogAdapter = CallLogAdapter(

                    context= context.applicationContext,
                    viewMarkingHandlerHelper=this@CallFragment,
                    networkHandler=this@CallFragment,
                    isDarkThemOn=context.isDarkThemeOn(),

                    ) {
                        id:Long,
                        position:Int,
                        view:View,
                        btn:Int,
                        callLog: CallLogTable,
                        clickType:Int,
                        visibility:Int ->onCallItemClicked(id,
                    position,
                    view,
                    btn,
                    callLog,
                    clickType,
                    visibility)
                }
//            }

            adapter = callLogAdapter
            itemAnimator = null

        }
    }



    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }
    

    companion object {
            private const val TAG ="__CallFragment"
            var pageCall = 10
            var fullDataFromCproviderFetched = false
         const val INDIVIDUAL_CONTACT_ACTIVITY = 0
         const val INDIVIDUAL_CALL_LOG_ACTIVITY = 1
        const val ID_SHOW_SCREENING_ROLE = -1L

    
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnCallFragmentPermission ->{
                requestRequiredPermissions()
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
//            R.id.fabBtnShowDialpad ->{
//                (activity as MainActivity).showDialerFragment()
//            }
            R.id.imgBtnCallSearch ->{
                activity?.startSearchActivity()
            }
            R.id.imgBtnHamBrgerCalls ->{
                (activity as MainActivity).showDrawer()
            }

            R.id.btnBlock->{
                blockMarkedCaller()
            }
            else ->{
                setSpammerTypeBasedOnRadio(v)
            }
        }

    }

    private fun requestRequiredPermissions() {
        val request = PermissionRequest.Builder(this.context)
            .code(REQUEST_CODE_CALL_LOG)
            .perms(arrayOf(READ_CALL_LOG,
            ))
            .rationale("HashCaller needs access to call logs to identify unknown callers in call log.")
            .positiveButtonText("Continue")
            .negativeButtonText("Cancel")
            .build()
        EasyPermissions.requestPermissions(requireActivity(), request)
    }



    private fun setSpammerTypeBasedOnRadio(v: View?) {
        if(v is RadioButton){
            when(v?.id){
                R.id.radioSales-> {
                    radioGroupTwo?.clearCheck()
                    this.spammerType = SPAMMER_TYPE_SALES
                }
                R.id.radioScam ->{
                    radioGroupTwo?.clearCheck()
                    this.spammerType = SPAMMER_TYPE_SALES
                }
                R.id.radioBusiness ->{
                    radioGroupOne?.clearCheck()
                    spammerType = SPAMMER_TYPE_BUSINESS
                }
                R.id.radioPerson ->{
                    radioGroupOne?.clearCheck()
                    this.spammerType = SPAMMER_TYPE_PEERSON
                }

            }
        }

    }





    private fun unmuteUser() {
//        viewmodel?.unmuteByAddress()?.observe(viewLifecycleOwner, Observer {
//            when(it){
//                OPERATION_COMPLETED -> {
//                    requireActivity().toast("Enabled notification for ${viewmodel?.contactAddress} ", Toast.LENGTH_LONG)
//                    binding.imgBtnCallUnMuteCaller.beInvisible()
//                    showSearchView()
//                }
//            }
//        })
    }

    private fun setupBottomSheet() {
        bottomSheetDialog = BottomSheetDialog(this.requireActivity())
        bottomSheetDialogfeedback = BottomSheetDialog(this.requireActivity())
        val viewSheet = layoutInflater.inflate(R.layout.bottom_sheet_block, null)
        val viewSheetFeedback = layoutInflater.inflate(R.layout.bottom_sheet_block_feedback, null)

        bottomSheetDialog.setContentView(viewSheet)
        bottomSheetDialogfeedback.setContentView(viewSheetFeedback)

        radioBusiness = bottomSheetDialog.findViewById<RadioButton>(R.id.radioBusiness) as RadioButton
        radioPerson = bottomSheetDialog.findViewById<RadioButton>(R.id.radioPerson) as RadioButton
        radioSales = bottomSheetDialog.findViewById<RadioButton>(R.id.radioSales) as RadioButton
        radioScam = bottomSheetDialog.findViewById<RadioButton>(R.id.radioScam) as RadioButton

        radioGroupOne = bottomSheetDialog.findViewById<RadioGroup>(R.id.radioGroupOne) as RadioGroup
        radioGroupTwo = bottomSheetDialog.findViewById<RadioGroup>(R.id.radioPersonOrBusiness) as RadioGroup

        btnBlock = bottomSheetDialog.findViewById(R.id.btnBlock)
        selectedRadioButton = radioScam
    }

    override fun onMenuItemClick(menuItem: MenuItem?): Boolean {
        when(menuItem?.itemId){
            R.id.itemMyBlockList ->{

                val intent = Intent(activity, BlockListActivity::class.java)
                startActivity(intent)
            }
        }
        return true



    }
    private fun showBottomSheetDialog() {
        bottomSheetDialog.show()

    }


    override fun onPause() {
        super.onPause()
        clearMarkeditems()
    }

    private fun blockMarkedCaller() {
//        this.viewmodel?.blockThisAddress(
//            this.spammerType,
//            context?.applicationContext)?.observe(viewLifecycleOwner, Observer {
//                when(it){
//                    ON_COMPLETED -> {
//                        viewmodel?.clearMarkedItems()
//                        bottomSheetDialog.hide()
//                        bottomSheetDialog.dismiss()
//                        bottomSheetDialogfeedback.show()
//                        showSearchView()
//                    }
//                }
//        })
    }
    private fun muteMarkedCaller() {
    }

    private fun deletemarkedLogs() {
        val dialog = ConfirmDialogFragment(this,
            getSpannableString("This can't be undone"),
            getSpannableString("Delete call history ? "), TYPE_DELETE)
        dialog.show(childFragmentManager, "sample")
    }



    override var isDefaultFgmnt: Boolean
        get() = isDflt
        set(value) {isDflt = value}




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
            TYPE_CLICK_SCREENING_ROLE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    (activity as MainActivity).reqScreeningRole()
                }
                return UNMARK_ITEM
            }

            TYPE_CLICK_DISMISS_SCREENING_ROLE -> {
                callLogAdapter?.removeCallerIdRoleItem()
            }
            TYPE_LONG_PRESS ->{
                val prevExpandedLyoutId = viewmodel?.getPreviousExpandedLayout()
                if(prevExpandedLyoutId!=null){
                    //already a lyout is expanded
                    val oldPos = viewmodel?.getPrevExpandedPosition()
                    viewmodel?.setExpandedLayout(null, null)
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
            TYPE_MAKE_CALL ->{
                if(EasyPermissions.hasPermissions(context, CALL_PHONE)){
                    context?.makeCall(callLog.numberFormated)
                }else {
                   requireActivity().requestCallPhonePermission()
                }

                return UNMARK_ITEM
            }


            else ->{
                if(getMarkedItemsSize() == 0){
                    val prevExpandedLyoutId = viewmodel?.getPreviousExpandedLayout()
                    if(prevExpandedLyoutId==null){
                        viewmodel?.setExpandedLayout(id, position)
                        return EXPAND_LAYOUT
                    }else if(prevExpandedLyoutId != id){
                        val oldPos = viewmodel?.getPrevExpandedPosition()
                        viewmodel?.setExpandedLayout(id, position)
                        if(oldPos!=null){
                            callLogAdapter?.notifyItemChanged(oldPos)
                        }
                        return EXPAND_LAYOUT
                    }else{

                        viewmodel?.setExpandedLayout(null, null)
                        return COMPRESS_LAYOUT

                    }
                }else{
                    return markItem(id, clickType, position, callLog.number)
                }
            }
        }
        return UNMARK_ITEM

    }

    private fun startCallHistoryActivity(callLog: CallLogTable, view: View) {
        viewmodel?.setExpandedLayout(null, null)

        val intent = getContactIntent(callLog, INDIVIDUAL_CALL_LOG_ACTIVITY )

        startActivity(intent)
    }

    fun getMarkedItemsSize(): Int {
       return  viewmodel?.markeditemsHelper?.getmarkedItemSize()?:0
    }
    private fun startIndividualContactActivity(log: CallLogTable, view: View) {

        var name = log.nameInPhoneBook
        if(name.isNullOrEmpty()){
            name = log?.nameFromServer
        }
        if(name.isNullOrEmpty()){
            name = log.numberFormated
        }

        val intent = Intent(context, IndividualContactViewActivity::class.java )
        intent.putExtra(com.hashcaller.app.view.ui.contacts.utils.CONTACT_ID, log.number)
        intent.putExtra("name", name )
        intent.putExtra("photo", log.thumbnailFromCp)
        intent.putExtra("color", log.color)

        val pairList = java.util.ArrayList<Pair<View, String>>()
        var pair:android.util.Pair<View, String>? = null
        if(log.imageFromDb.isNotEmpty() || log.avatarGoogle.isNotEmpty() || log.thumbnailFromCp.isNotEmpty()){
            pair = android.util.Pair(view.findViewById(R.id.imgVThumbnail) as View,"contactImageTransition")
//           pair = android.util.Pair(binding.textViewcontactCrclr as View, "firstLetterTransition")
        }else {
            pair = android.util.Pair(view.findViewById(R.id.textViewCrclr) as View, "firstLetterTransition")
        }
        pairList.add(pair)
        val options = ActivityOptions.makeSceneTransitionAnimation(activity,pairList[0])
        startActivity(intent, options.toBundle())

//            startActivity(intent, options.toBundle())
    }


    private fun getOptions(view: View, log: CallLogTable): ActivityOptions {

        val pairList = ArrayList<android.util.Pair<View, String>>()
        val imgViewUserPhoto =
            view.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.imgVThumbnail)
        val textViewCrclr = view.findViewById<TextView>(R.id.textViewCrclr)
        var pair: android.util.Pair<View, String>? = null
        if (log.thumbnailFromCp.isNotEmpty()) {
            pair = android.util.Pair(imgViewUserPhoto as View, "contactImageTransition")
        } else if (log.imageFromDb.isNotEmpty()) {
            pair = android.util.Pair(imgViewUserPhoto as View, "contactImageTransition")

        }
        else if (log.avatarGoogle.isNotEmpty()) {
            pair = android.util.Pair(imgViewUserPhoto as View, "contactImageTransition")
        }
        else {
            pair = android.util.Pair(textViewCrclr as View, "firstLetterTransition")
        }
        pairList.add(pair)
        return ActivityOptions.makeSceneTransitionAnimation(activity, pairList[0])
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
            name = log.numberFormated
        }
        var intent:Intent? = null
        when(destinationActivity){
            INDIVIDUAL_CALL_LOG_ACTIVITY ->{
                 intent = Intent(context, IndividualCallLogActivity::class.java )
                intent.putExtra(CONTACT_ADDRES, log.numberFormated)
                intent.putExtra(IntentKeys.FULL_NAME_IN_C_PROVIDER, log.nameInPhoneBook)
                intent.putExtra(IntentKeys.FULL_NAME_FROM_SERVER, log.nameFromServer)
                intent.putExtra(IntentKeys.THUMBNAIL_FROM_CPROVIDER, log.thumbnailFromCp)
                intent.putExtra(IntentKeys.H_UID, log.hUid)
                intent.putExtra(IntentKeys.THUMBNAIL_FROM_DB, log.imageFromDb)
                intent.putExtra(IntentKeys.SPAM_COUNT, log.spamCount)
                intent.putExtra(IntentKeys.IS_REPORTED_BY_USER, log.isReportedByUser)
                intent.putExtra(IntentKeys.AVATAR_COLOR, log.color)

            }else ->{
             intent = Intent(context, IndividualContactViewActivity::class.java )
        }
        }
        intent.putExtra(com.hashcaller.app.view.ui.contacts.utils.CONTACT_ID, log.number)
        intent.putExtra("name", name )
        intent.putExtra("photo", log.thumbnailFromCp)
        intent.putExtra("color", log.color)
        return intent
    }


    private fun markItem(id: Long, clickType: Int, position: Int, number: String): Int {
        if(viewmodel?.markeditemsHelper?.markedItems?.value !=null){
            if(viewmodel?.markeditemsHelper?.markedItems?.value!!.isEmpty() && clickType == TYPE_LONG_PRESS){
                //if is empty and click type is long then start marking
                viewmodel?.addTomarkeditems(id, position, number)
                return MARK_ITEM
            }else if(clickType == TYPE_LONG_PRESS && viewmodel?.markeditemsHelper?.markedItems?.value!!.isNotEmpty()){
                //already some items are marked
                if(viewmodel?.markeditemsHelper?.markedItems?.value!!.contains(id)){
                    viewmodel?.removeMarkeditemById(id, position, number)
                    return UNMARK_ITEM
                }else{

                    viewmodel?.addTomarkeditems(id, position, number)
                    return MARK_ITEM
                }
            }else if(clickType == TYPE_CLICK && viewmodel?.markeditemsHelper?.markedItems?.value!!.isNotEmpty()){
                //already markig started , mark on unamrk new item
                if(viewmodel?.markeditemsHelper?.markedItems?.value!!.contains(id)){
                    viewmodel?.removeMarkeditemById(id, position, number)
                    return UNMARK_ITEM
                }else{
                    viewmodel?.addTomarkeditems(id, position, number)
                    return MARK_ITEM
                }
            }else {
                // normal click
                return UNMARK_ITEM
            }
        }
        return UNMARK_ITEM


    }




    private fun showBlockBtnInToolbar(count: Int) {
//        updateSelectedItemCount(count)
//        binding.imgBtnCallSearch.beInvisible()
//            binding.imgBtnCallTbrBlock.beVisible()
//
//        if(isScreeningApp){
////            binding.imgBtnCallTbrMuteCaller.beVisible()
//        }
//        binding.imgBtnCallSearch.beInvisible()
//        binding.tvVHashcaller.beInvisible()
//        binding.imgBtnHamBrgerCalls.beInvisible()

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        if(hidden){ // if fragment is hidden
            clearMarkeditems()
        }
    }

//    fun showSearchView(){
//
//        binding.imgBtnCallTbrBlock.beInvisible()
//        binding. imgBtnCallTbrMuteCaller.beInvisible()
//        binding.tvCallSelectedCount.beInvisible()
//        binding.imgBtnCallUnMuteCaller.beInvisible()
//        binding.pgBarDeleting.beInvisible()
//        binding.tvVHashcaller.beVisible()
//        binding.imgBtnHamBrgerCalls.beVisible()
//    }

    fun updateSelectedItemCount(count: Int) {
//        binding.tvCallSelectedCount.text = "${count.toString()} Selected"
//        binding.tvCallSelectedCount.beVisible()
    }

    override fun onYesConfirmationDelete() {
        this.activity?.runOnUiThread{
//            binding.imgBtnCallTbrDelete.beInvisible()
        }
        this.viewmodel?.deleteThread()?.observe(viewLifecycleOwner, Observer {
            when (it) {
                ON_PROGRESS -> {
//                    binding.imgBtnCallTbrDelete.beInvisible()

//                    binding.pgBarDeleting.beVisible()
                }
                ON_COMPLETED -> {
//                    showSearchView()
                }
            }
        })
//        viewmodel.clearMarkedItems()
    }

    override fun onYesConfirmationMute() {
        viewmodel?.muteMarkedCaller()?.observe(viewLifecycleOwner, Observer {
            when(it){
                OPERATION_COMPLETED ->{

                    val sbar = Snackbar.make(binding.cordinateLyoutCall,
                        "You no longer notified on from ${viewmodel?.contactAddress}",
                        Snackbar.LENGTH_SHORT)
                    lastOperationPerformed = OPERTION_MUTE
                    sbar.setAction("Undo", MyUndoListener(this))
                    sbar.show()
//                    showSearchView()
                }
            }
        })
    }

    override fun onUndoClicked() {
        when(lastOperationPerformed){
            OPERTION_MUTE ->{
                viewmodel?.unmute()
            }
            OPERTION_DELETE ->{
            }
        }
    }

    /**
     * called from mainactivity on back button pressed
     */
    fun clearMarkeditems() {
        if(checkRequiredPermission()){
            if(viewmodel!=null){
                lifecycleScope.launchWhenStarted {
                    for(position in viewmodel?.getmarkeditemPositions()!!){
                        callLogAdapter?.notifyItemChanged(position)
                    }
                    viewmodel?.clearMarkedItems()
                    viewmodel?.clearMarkedItemPositions()
                }
            }

        }
    }

    /**
     * called from adapter to toggle marked view
     */
    override fun isMarked(id: Long?): Boolean {
        var isMrked = false
        if(viewmodel?.markeditemsHelper?.markedItems?.value !=null){
            if(viewmodel?.markeditemsHelper?.markedItems?.value!!.contains(id)){
                isMrked = true
            }
        }
    return isMrked
    }

    override fun isViewExpanded(id: Long): Boolean {
        return viewmodel?.isThisViewExpanded(id)?:false
    }

    override fun isInternetAvailable(): Boolean {
        return isInternetAvailable
    }

    fun activtyResultisDefaultScreening() {
        callLogAdapter?.removeCallerIdRoleItem()
    }

}