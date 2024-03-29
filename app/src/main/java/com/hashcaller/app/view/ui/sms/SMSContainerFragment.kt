package com.hashcaller.app.view.ui.sms

import android.Manifest.permission.READ_SMS
import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.hashcaller.app.R
import com.hashcaller.app.databinding.FragmentMessageContainerBinding
import com.hashcaller.app.utils.Constants
import com.hashcaller.app.utils.PermisssionRequestCodes
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.utils.extensions.requestDefaultSMSrole
import com.hashcaller.app.utils.extensions.startIndividualSMSActivityByAddress
import com.hashcaller.app.utils.extensions.startSearchActivity
import com.hashcaller.app.utils.internet.ConnectionLiveData
import com.hashcaller.app.view.ui.MainActivity
import com.hashcaller.app.view.ui.call.dialer.util.CustomLinearLayoutManager
import com.hashcaller.app.view.ui.contacts.hasSMSReadPermission
import com.hashcaller.app.view.ui.contacts.isDefaultSMSHandler
import com.hashcaller.app.view.ui.contacts.showSnackBar

import com.hashcaller.app.view.ui.contacts.utils.*
import com.hashcaller.app.view.ui.extensions.getSpannableString
import com.hashcaller.app.view.ui.sms.individual.IndividualSMSActivity
import com.hashcaller.app.view.ui.sms.individual.util.*
import com.hashcaller.app.view.ui.sms.list.SMSHelperFlow
import com.hashcaller.app.view.ui.sms.list.SMSListAdapter
import com.hashcaller.app.view.ui.sms.list.SMSListInjectorUtil
import com.hashcaller.app.view.ui.sms.search.SearchSMSActivity
import com.hashcaller.app.view.ui.sms.util.*
import com.hashcaller.app.view.utils.ConfirmDialogFragment
import com.hashcaller.app.view.utils.ConfirmationClickListener
import com.hashcaller.app.view.utils.IDefaultFragmentSelection
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.models.PermissionRequest
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet_block.*
import kotlinx.android.synthetic.main.bottom_sheet_block_feedback.*
import kotlinx.android.synthetic.main.fragment_message_container.*
import kotlinx.android.synthetic.main.fragment_message_container.MessagesFragment
import kotlinx.android.synthetic.main.fragment_message_container.view.*
import kotlinx.android.synthetic.main.fragment_messages_list.view.*
import kotlinx.android.synthetic.main.fragment_test.*
import kotlinx.android.synthetic.main.sms_list_view.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import kotlin.collections.HashMap


class SMSContainerFragment : Fragment(), View.OnClickListener, IDefaultFragmentSelection,
SMSListAdapter.LongPressHandler, PopupMenu.OnMenuItemClickListener, ConfirmationClickListener,
    android.widget.PopupMenu.OnMenuItemClickListener, SMSListAdapter.ViewMarkHandler, SMSListAdapter.NetworkHandler,
    SetAsDefaultSMSSnackbarListener.SnackBarListner{

    private var _binding: FragmentMessageContainerBinding? =null
    private val binding get() = _binding!!
    private  var viewmodel: SMSViewModel? = null
    var smsRecyclerAdapter: SMSListAdapter? = null
    private lateinit var searchV: SearchView
    private var searchQry:String? = null
    private lateinit var smsFlowHelper:SMSHelperFlow
    private lateinit var sView: EditText
    private lateinit var sharedPreferences: SharedPreferences
    private var isInternetAvailable = false
    var skeletonLayout: LinearLayout? = null
//    var shimmer: Shimmer? = null
    var inflater: LayoutInflater? = null
    private var layoutMngr: LinearLayoutManager? = null
    private lateinit var searchViewMessages: EditText
    private var isLoading = false
    var limit = 12
    private var isDflt = false
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private  var spammerType:Int = SPAMMER_TYPE_SCAM
    private lateinit var bottomSheetDialogfeedback: BottomSheetDialog
//    private  var selectedRadioButton: RadioButton? = null
    private var isPaused = false
    private lateinit var toolbar : Toolbar

    private  var radioSales:RadioButton?= null
    private  var radioScam:RadioButton?= null
    private  var radioBusiness:RadioButton?= null
    private  var radioPerson:RadioButton?= null
    private var btnBlock:Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        pageOb.page = 0

    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        _binding = FragmentMessageContainerBinding.inflate(inflater, container, false)
        return  binding.root
    }
    @InternalCoroutinesApi
    @SuppressLint("WrongViewCast", "LongLogTag")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerForContextMenu( binding.recyclreviewSMSContainer )
        setupBottomSheet()
        initListeners()
        if(MainActivity.fetchSMSOnCreate){
            getData()
        }
    }




    private  fun showRecyclerView() {
//        withContext(Dispatchers.Main){
            binding.recyclreviewSMSContainer.beVisible()
//            binding.shimmerViewContainer.beVisible()
            binding.btnSMSPermsions.beInvisible()
//        }
    }
    private fun hideRecyclerView(){
        binding.recyclreviewSMSContainer.beInvisible()
//        binding.shimmerViewContainer.beInvisible()
        binding.btnSMSPermsions.beVisible()
    }


    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if(!hidden){
            if(viewmodel==null){
                getData()
            }
        }
    }

    private fun getData() {
        lifecycleScope.launchWhenResumed {
            if(checkRequiredPermissions()){
               withContext(Dispatchers.Main){
                   binding.pgbarSMSContainer.beVisible()
                   binding.btnSMSPermsions.beGone()
               }
                initRecyclerView()
                initVieModel()
                showRecyclerView()
                observeSMSList()
                observeSMSThreadsLivedata()
                observeSendersInfoFromServer()
                observeMarkedItems()
                observeInternetLivedata()
            }else {
                binding.pgbarSMSContainer.beGone()
                binding.btnSMSPermsions.beVisible()
            }

        }
    }

    private suspend fun observeInternetLivedata() {
        val cl = context?.let { ConnectionLiveData(it) }
        cl?.observe(viewLifecycleOwner, Observer {
            isInternetAvailable = it
        })
    }


    private  suspend fun observeSMSThreadsLivedata() {
//        withContext(Dispatchers.Main){
            viewmodel?.smsThreadsLivedata?.observe(viewLifecycleOwner, Observer {
                it.let {
//                viewmodel.updateLiveData(it)
                    binding.pgbarSMSContainer.beGone()
                    smsRecyclerAdapter?.setList(it)

                }
            })
//        }
    }
    private suspend fun observeSMSList() {
//        withContext(Dispatchers.Main){
            viewmodel?.SMS?.observe(viewLifecycleOwner, Observer { sms ->

                sms.let {

                    viewmodel?.updateDatabase(it, context?.applicationContext)

                }
            })
//        }

    }

    fun getMarkedItemsSize(): Int {
        return  viewmodel?.markeditemsHelper?.getmarkedItemSize()?:0
    }







    private fun checkRequiredPermissions(): Boolean {
        return context?.hasSMSReadPermission()?:false
    }




    private fun initListeners() {
        binding.btnSMSPermsions.setOnClickListener(this)
//        binding.searchViewSms.setOnClickListener(this)
//        binding.imgBtnTbrMuteSender.setOnClickListener(this)
        binding.imgBtnTbrBlock.setOnClickListener(this)
        binding.imgBtnTbrMore.setOnClickListener(this)
        binding.imgBtnTbrDelete.setOnClickListener(this)
        binding.imgBtnHamBrgr.setOnClickListener(this)
        binding.fabSendNewSMS.setOnClickListener(this)
        binding.imgBtnSearch.setOnClickListener(this)

        radioSales?.setOnClickListener(this)
        radioScam?.setOnClickListener(this)
        radioBusiness?.setOnClickListener(this)
        radioPerson?.setOnClickListener(this)
        btnBlock?.setOnClickListener(this)

    }

    /**
     * change visibility of view items to as beginning, ie
     * remove all marking/checked items hide delete,mute etc buttons in toolbar
     */
    private fun resetMarkingOptions() {
        markingStarted = false
//        unMarkItems()
//        binding.searchViewSms.visibility = View.VISIBLE
//        binding.imgBtnTbrMuteSender.visibility = View.INVISIBLE
        binding.imgBtnTbrBlock.beInvisible()
        binding.imgBtnTbrDelete.beInvisible()
        binding.tvSelectedCount.beInvisible()
    }


    /**
     * To block a contact address
     * @param contact contact address
     *
     */
    private fun addToBlockList() {
        this.viewmodel?.blockThisAddress( this.spammerType, context?.applicationContext )?.observe(viewLifecycleOwner, Observer {
            when(it){
                ON_COMPLETED ->{
                    Toast.makeText(this.requireActivity(), "Number added to spamlist", Toast.LENGTH_LONG)
                    bottomSheetDialog.hide()
                    bottomSheetDialog.dismiss()
                    bottomSheetDialogfeedback.show()
                    var txt = "$ can no longer send SMS or call you."
                    val  sb =  SpannableStringBuilder(txt);
                    val bss =  StyleSpan(Typeface.BOLD); // Span to make text bold
//        sb.setSpan(bss, 0, .length, Spannable.SPAN_INCLUSIVE_INCLUSIVE); // make first 4 characters Bold
//        bottomSheetDialogfeedback.tvSpamfeedbackMsg.text = sb
//                    resetMarkingOptions()
                    clearMarkeditems()
                }
            }
        })

    }

    fun clearMarkeditems(){
        if(viewmodel!=null){
            lifecycleScope.launchWhenStarted {
                for(position in viewmodel?.getmarkeditemPositions()!!){
                    smsRecyclerAdapter?.notifyItemChanged(position)
                }
                viewmodel?.clearMarkedItems()
                viewmodel?.clearMarkedItemPositions()
            }
        }

    }
    private fun setupBottomSheet() {
        bottomSheetDialog = BottomSheetDialog(this.requireActivity())
        bottomSheetDialogfeedback = BottomSheetDialog(this.requireActivity())
        val viewSheet = layoutInflater.inflate(R.layout.bottom_sheet_block, null)
        val viewSheetFeedback = layoutInflater.inflate(R.layout.bottom_sheet_block_feedback, null)

        bottomSheetDialog.setContentView(viewSheet)
        bottomSheetDialogfeedback.setContentView(viewSheetFeedback)

//        selectedRadioButton = bottomSheetDialog.radioScam
        radioBusiness = bottomSheetDialog.findViewById<RadioButton>(R.id.radioBusiness)
        radioPerson = bottomSheetDialog.findViewById<RadioButton>(R.id.radioPerson)
        radioSales = bottomSheetDialog.findViewById<RadioButton>(R.id.radioSales)
        radioScam = bottomSheetDialog.findViewById<RadioButton>(R.id.radioScam)
        btnBlock = bottomSheetDialog.findViewById(R.id.btnBlock)

//        bottomSheetDialog.imgExpand.setOnClickListener(this)


//        if(this.view?.visibility == View.VISIBLE){
//            bottomSheetDialog.hide()

//        }

        bottomSheetDialog.setOnDismissListener {
            Log.d(TAG, "bottomSheetDialogDismissed")

        }
    }
    private  suspend fun initVieModel() {
            val factory =  SMSListInjectorUtil.provideDialerViewModelFactory(
                context?.applicationContext,
                lifecycleScope,
                TokenHelper(FirebaseAuth.getInstance().currentUser)
            )

//        lifecycleScope.launchWhenStarted {
            viewmodel = ViewModelProvider(
                this@SMSContainerFragment,
                factory

            ).get(
                SMSViewModel::class.java
            )

//        }



}


    private suspend fun observeSendersInfoFromServer() {
        //todo convert this to livedata, because this calling is does not observe livedata properly
        viewmodel?.getSmsSendersInfoFromServer()?.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "observeSendersInfoFromServer: $it")
            viewmodel?.updateWithNewSenderInfo(it)

        })
    }

    private fun observeMutabeLiveData() {
        this.viewmodel?.smsLiveData?.observe(viewLifecycleOwner, Observer {
//            smsListVIewModel.smsLIst = it as MutableList<SMS>?
//            Log.d(TAG, "observeMutabeLiveData: spamcount${it[0].spamCount} ")
//            var newList:MutableList<SMS> = mutableListOf()

//            it.forEach{sms-> newList.add(sms.deepCopy())}
            Log.d(TAG, "observeMutabeLiveData: ")
//            smsRecyclerAdapter?.setList(it)
//            binding.shimmerViewContainer.beGone()
//            this.viewMesages.pgBarsmslist.visibility = View.GONE
//            binding.shimmer_view_container.visibility = View.GONE
            binding.recyclreviewSMSContainer.visibility = View.VISIBLE
            SMSListAdapter.searchQry = searchQry
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: ")
        binding.recyclreviewSMSContainer.adapter  = null
    }



    private fun getFirstPageOfSMS() {
        viewmodel?.getFirstPageOfSMS()
    }


    private fun initRecyclerView() {
       binding.recyclreviewSMSContainer?.apply {
            layoutManager = CustomLinearLayoutManager(context)
            layoutMngr = layoutManager as LinearLayoutManager
            smsRecyclerAdapter = SMSListAdapter(context.applicationContext, this@SMSContainerFragment, this@SMSContainerFragment, this@SMSContainerFragment){ view: View, threadId:Long, pos:Int,
                                                                                pno:String, clickType:Int->onContactItemClicked(view,threadId, pos, pno, clickType)  }
//            smsRecyclerAdapter = SMSListAdapter(context, onContactItemClickListener =){view:View, pos:Int ->onLongpressClickLister(view,pos)}
            adapter = smsRecyclerAdapter

//                setContacts()

//                adapter.onItemClick =
        }
    }
    private fun onDeleteItemClicked(){

    }


    private fun onContactItemClicked(view: View, threadId: Long, position: Int, address: String, clickType:Int): Int {
        when(clickType){
            TYPE_LONG_PRESS ->{
                return  markItem(threadId, clickType, position, address)

            }else ->{
            if(viewmodel?.getmarkedItemSize() == 0){
               requireActivity().startIndividualSMSActivityByAddress(address, view)
//                startIndividualSMSActivity(address, view)

            }else{
                return markItem(threadId, clickType, position, address)
            }
        }
        }


        return UNMARK_ITEM
    }

    private fun startIndividualSMSActivity(address: String, view: View) {
        val intent = Intent(context, IndividualSMSActivity::class.java )
        val bundle = Bundle()
        bundle.putString(CONTACT_ADDRES, address)
        bundle.putString(SMS_CHAT_ID, "")

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    @SuppressLint("LongLogTag")
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
//        viewmodel.updateWithNewSenderInfo()
        isPaused = false


    }








    private fun deleteMarkedSMSThreads() {

        if(context?.isDefaultSMSHandler() == true){
            deleteSms()
        }else {
            context?.showSnackBar(
                binding.MessagesFragment,
                        getString(R.string.enable_hash_caller_for_sms),
                       getString(R.string.enable_hash_caller_sms_action),
                         SetAsDefaultSMSSnackbarListener(this)
                      )

//            val sbar = Snackbar.make((activity as MainActivity).getCorinateLayout(), getString(R.string.enable_hash_caller_for_sms), Snackbar.LENGTH_SHORT)
//            sbar.setAction(getString(R.string.enable_hash_caller_sms_action), SetAsDefaultSMSSnackbarListener(this))
//            sbar.anchorView = (activity as MainActivity).getBottomNavView()
//            sbar.show()
//            context?.toast("Not defaul sms handler")
        }

    }
    private fun deleteSms() {

        val dialog = ConfirmDialogFragment(this,
            getSpannableString("This is permenant and cant be undone"),

            getSpannableString("Delete conversation?"), TYPE_DELETE)
        dialog.show(childFragmentManager, "sample")

    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: ")
    }

    companion object {

        private const val TAG = "__SMSContainerFragment"
        //todo remove this, this is a memory leak
//        private  var viewMesagesRef: View?=null


        var mapofAddressAndSMS: HashMap<String, SMS> = hashMapOf() // for findin duplicate sms in list

        var recyclerViewSpamSms:RecyclerView? = null

        /**
         * function to update marked item count in fragment
         */
        fun updateSelectedItemCount(count:Int){
//            if(count>0){
//                binding.tvSelectedCount.visibility = View.VISIBLE
//                this.viewMesagesRef!!.tvSelectedCount.text = "$count Selected"
//            }else{
//                this.viewMesagesRef!!.tvSelectedCount.visibility = View.INVISIBLE
//                this.viewMesagesRef!!.tvSelectedCount.text = ""
//                markingStarted = false
//
//                this.viewMesagesRef!!.searchViewSms.visibility = View.VISIBLE
//                this.viewMesagesRef!!.imgBtnTbrMuteSender.visibility = View.INVISIBLE
//                this.viewMesagesRef!!.imgBtnTbrBlock.visibility = View.INVISIBLE
//                this.viewMesagesRef!!.imgBtnTbrDelete.visibility = View.INVISIBLE
//                this.viewMesagesRef!!.tvSelectedCount.visibility = View.INVISIBLE
//            }
        }
        fun showHideBlockButton() {
//            if(markedItems.size == 1){
//                viewMesagesRef!!.imgBtnTbrBlock.visibility = View.VISIBLE
//            }else{
//                viewMesagesRef!!.imgBtnTbrBlock.visibility = View.INVISIBLE
//            }
        }




    }

    override fun onClick(v: View?) {
        Log.d(TAG, "onClick: ")
        when(v?.id){
            R.id.imgBtnSearch -> {
                activity?.startSearchActivity()
            }
            R.id.btnSMSPermsions ->{
                requestSMSPermissions()
            }
            R.id.searchViewSms->{
                startSearchActivity()
            }
            R.id.imgBtnTbrBlock->{
//                blockUser()
                bottomSheetDialog.show()

            }
//            R.id.imgExpand->{
//                showPopupMenu(R.menu.image_chooser_popup, bottomSheetDialog.viewPopup)
//
//            }
            R.id.imgBtnTbrDelete ->{
                    deleteMarkedSMSThreads()

            }
//            R.id.imgBtnTbrMuteSender ->{
//                muteSender()
//            }
//            R.id.btnBlock->{
//                Log.d(TAG, "onClick: ")
//                addToBlockList()
//            }
            R.id.fabSendNewSMS -> {
                (activity as MainActivity).showSMSSearchFragment()
//                startSMSSearchActivity()
                Log.d(TAG, "onClick: fabSendNewSMS")
//                this.viewmodel?.deleteAllSmsindb() // JUST FOR TESTING PURPOSE
//                val i = Intent(context, ContactSelectorActivity::class.java )
//                i.putExtra(DESTINATION_ACTIVITY, INDIVIDUAL_SMS_ACTIVITY)
//                startActivity(i)
            }
            R.id.imgBtnTbrMore->{
                showPopupMenu(R.menu.sms_list_more_popup,imgBtnTbrMore)
            }
            R.id.imgBtnHamBrgr ->{
                (activity as MainActivity).showDrawer()

//               requireContext().startSettingsActivity(activity)
            }
            R.id.radioSales, R.id.radioBusiness, R.id.radioScam,R.id.radioSales ->{
                setSpammerTypeBasedOnRadio(v)
            }
            R.id.btnBlock->{

                addToBlockList()
            }
            else ->{
                viewmodel?.getUnrealMsgCount()

            }
        }
    }

    private fun startSMSSearchActivity() {
        val intent = Intent(context, SearchSMSActivity::class.java)
        startActivity(intent)
    }

    private fun setSpammerTypeBasedOnRadio(v: View) {
        when(v?.id){
            R.id.radioSales-> {
                this.spammerType = SPAMMER_TYPE_SALES
            }
            R.id.radioScam ->{
                this.spammerType = SPAMMER_TYPE_SCAM
            }
            R.id.radioBusiness ->{
                spammerType = Constants.SPAMMER_TYPE_BUSINESS
            }
            R.id.radioPerson ->{
                spammerType  = SPAMMER_TYPE_PEERSON
            }

        }
    }
    private fun requestSMSPermissions() {
        Timber.d("requestSMSPermissions: ")
        val request = PermissionRequest.Builder(this.context)
            .code(PermisssionRequestCodes.REQUEST_CODE_READ_SMS)

            .perms(
                arrayOf(
                    READ_SMS,
                )
            )
            .rationale("HashCaller needs access to sms  to identify unknown senders in sms.")
            .positiveButtonText("Continue")
            .negativeButtonText("Cancel")
            .build()
        //important to give host as require activity, giving this fragment as host not helping to detach and attach framment
        EasyPermissions.requestPermissions(requireActivity(), request)
    }

    private fun showPopupMenu(menu: Int, anchorView: View) {
        Log.d(TAG, "onClick: img button")

        val popup = PopupMenu(this.requireActivity(), anchorView )
        popup.inflate(menu)
        popup.setOnMenuItemClickListener(this)
        popup.show()
    }
    override fun onMenuItemClick(menuItem: MenuItem?): Boolean {
        Log.d(TAG, "onMenuItemClick: ")
        when (menuItem!!.itemId){
         R.id.popupMarkAllAsRead->{
             viewmodel?.markSMSAsRead(null)
             return true
         }
//            else ->{
//            this.spammerType = SpamLocalListManager.menuItemClickPerformed(menuItem, bottomSheetDialog)
//            return true
//        }
        }
       return true
    }

    private fun blockUser() {

            //set threadId and contact Address
            var num = ""
            var tId = 0L
//            for (item in MarkedItemsHandler.markedContactAddress){
//                num = item
//            }
//            for (item in MarkedItemsHandler.markedItems){
//                tId = item
//            }

//            MarkedItemsHandler.markedContactAddressForBlocking = num
//            MarkedItemsHandler.markedTheadIdForBlocking = tId
//            bottomSheetDialog.show()

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


    }

    private fun muteSender() {
        this.viewmodel?.muteMarkedSenders()
        resetMarkingOptions()
        val sbar = Snackbar.make(MessagesFragment, "hi", Snackbar.LENGTH_SHORT)
        sbar.setAction("Action", null)
//        sbar.anchorView = bottomNavigationView

        sbar.show()

//        (activity as MainActivity).showSnackBar("hi")
    }

    fun hideSearchView() {
        Log.d(TAG, "hideSearchView: ")
//        binding.searchViewSms.visibility = View.INVISIBLE
    }





    override fun onPause() {
        Log.d(TAG, "onPause: ")
        super.onPause()
        isPaused = true
        if (this.viewmodel != null  ) {
            if(this.viewmodel?.SMS != null)
                if(this.viewmodel!!.SMS!!.hasObservers()){
                    this.viewmodel?.SMS?.removeObservers(this);
                    this.viewmodel!!.smsLiveData.removeObservers(this)
                    viewmodel?.getSmsSendersInfoFromServer()?.removeObservers(this)
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
//        binding.searchViewSms.beVisible()
        binding.imgBtnTbrDelete.beInvisible()
//        binding.imgBtnTbrMuteSender.beInvisible()
        binding.imgBtnTbrBlock.beInvisible()
        binding.tvSelectedCount.beInvisible()
        binding.pgBarSMSDeleting.beInvisible()
        binding.tvSelectedCount.beInvisible()
        binding.imgBtnSearch.beVisible()
        binding.imgBtnHamBrgr.beVisible()
        binding.tvMessages.beVisible()


//        unMarkItems()

    }
    fun showToolbarButtons(size: Int) {
        Log.d(TAG, "showToolbarButtons: ")
//        binding.searchViewSms.beInvisible()
        binding.imgBtnSearch.beInvisible()
        binding.imgBtnHamBrgr.beInvisible()
        binding.tvMessages.beInvisible()
        binding.imgBtnTbrDelete.beVisible()
//        binding.imgBtnTbrMuteSender.beVisible()

        binding.tvSelectedCount.beVisible()
        if(size>1){
            binding.imgBtnTbrBlock.beInvisible()
        }else{
            binding.imgBtnTbrBlock.beVisible()
        }
        binding.tvSelectedCount.text = "${size.toString()} Selected"
        binding.tvSelectedCount.beVisible()
    }
//    fun isSearchViewVisible(): Boolean {
//        if(binding.searchViewSms.visibility== View.VISIBLE)
//            return true
//        return false
//    }

    private  fun observeMarkedItems() {
//       withContext(Dispatchers.Main){
           viewmodel?.markeditemsHelper?.markedItems?.observe(viewLifecycleOwner, Observer {
               when(it.size){
                   0 ->{
                       showSearchView()
                   }

                   else ->{
                       showToolbarButtons(it.size)
                   }
               }
           })
//       }
    }

    /**
     * mark for deletion or archival or block of sms list
     */
    private fun markItem(id: Long, clickType: Int, position: Int, address: String): Int {
        if(viewmodel?.markeditemsHelper?.markedItems?.value!!.isEmpty() && clickType == TYPE_LONG_PRESS){
            //if is empty and click type is long then start marking
            viewmodel?.addTomarkeditems(id, position, address)
            return MARK_ITEM
        }else if(clickType == TYPE_LONG_PRESS && viewmodel?.markeditemsHelper?.markedItems?.value!!.isNotEmpty()){
            //already some items are marked
            if(viewmodel?.markeditemsHelper?.markedItems?.value!!.contains(id)){
                viewmodel?.removeMarkeditemById(id, position, address)
                return UNMARK_ITEM
            }else{
                viewmodel?.addTomarkeditems(id, position, address)
                return MARK_ITEM
            }
        }else if(clickType == TYPE_CLICK && viewmodel?.markeditemsHelper?.markedItems?.value!!.isNotEmpty()){
            //already markig started , mark on unamrk new item
            if(viewmodel?.markeditemsHelper?.markedItems?.value!!.contains(id)){
                viewmodel?.removeMarkeditemById(id, position, address)
                return UNMARK_ITEM
            }else{
                viewmodel?.addTomarkeditems(id, position, address)
                return MARK_ITEM
            }
        }else {
            // normal click
            return UNMARK_ITEM
        }

    }

    /**
     * callback of ConfirmDialogfragment for deleting sms
     */

    override fun onYesConfirmationDelete() {

        Log.d(TAG, "deleteSms: called")
//        for(id in markedItems){
        this.viewmodel?.deleteMarkedSMSThreads()?.observe(viewLifecycleOwner, Observer {
            when (it) {
                ON_PROGRESS -> {
                    binding.imgBtnTbrDelete.beInvisible()
                    binding.imgBtnTbrBlock.beInvisible()
                    binding.imgBtnTbrMore.beInvisible()
//                    binding.imgBtnTbrMuteSender.beInvisible()
                    binding.pgBarSMSDeleting.beVisible()
//                    binding.pgBarDeleting.beVisible()
                }
                ON_COMPLETED -> {
                    showSearchView()
                    clearMarkeditems()
                }
            }
        })
//        }
//        deleteList()

//        resetMarkingOptions()
    }

    override fun onYesConfirmationMute() {

    }

    private fun observeNumOfRowsDeleted() {
        this.viewmodel?.numRowsDeletedLiveData?.observe(viewLifecycleOwner, Observer {
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
//                        requesetPermission(requireContext())
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
    /**
     * called from adapter to toggle marked view
     */
    override fun isMarked(id: Long): Boolean {
        var isMrked = false
        if(viewmodel?.markeditemsHelper?.markedItems?.value !=null){
            if(viewmodel?.markeditemsHelper?.markedItems?.value!!.contains(id)){
                isMrked = true
            }
        }
        return isMrked
    }

    override fun isInternetAvailable(): Boolean {

        return isInternetAvailable
    }

    private fun checkDefaultSMSHandlerSettings(): Boolean {
        var requestCode=  222
        var resultCode = 232
        var isDefaultSMSHandler = false
        try{

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager: RoleManager? = context?.getSystemService(RoleManager::class.java)
                // check if the app is having permission to be as default SMS app
                val isRoleAvailable =
                    roleManager?.isRoleAvailable(RoleManager.ROLE_SMS)
                if (isRoleAvailable == true) {
                    // check whether your app is already holding the default SMS app role.
                    val isRoleHeld = roleManager.isRoleHeld(RoleManager.ROLE_SMS)
                    isDefaultSMSHandler = isRoleHeld
                }
            } else {

               isDefaultSMSHandler= context?.getPackageName() == Telephony.Sms.getDefaultSmsPackage(context)
            }

        }catch (e: Exception){
            Log.d(TAG, "checkDefaultSettings: exception $e")
        }

        return isDefaultSMSHandler
    }

    override fun onSetAsDefaultSMSHandlerClicked() {

        Log.d(TAG, "onSetAsDefaultSMSHandlerClicked: ")
       activity?.requestDefaultSMSrole()
        
    }





}