package com.nibble.hashcaller.view.ui.sms

import android.Manifest
import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
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
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.adapter.ViewPagerAdapter
import com.nibble.hashcaller.view.ui.MainActivity
import com.nibble.hashcaller.view.ui.contactSelector.ContactSelectorActivity
import com.nibble.hashcaller.view.ui.contacts.IndividualContacts.utils.PermissionUtil.requesetPermission
import com.nibble.hashcaller.view.ui.contacts.utils.markingStarted
import com.nibble.hashcaller.view.ui.contacts.utils.unMarkItems
import com.nibble.hashcaller.view.ui.sms.identifiedspam.SMSIdentifiedAsSpamFragment
import com.nibble.hashcaller.view.ui.sms.individual.IndividualSMSActivity
import com.nibble.hashcaller.view.ui.sms.list.SMSListFragment
import com.nibble.hashcaller.view.ui.sms.schedule.ScheduleActivity
import com.nibble.hashcaller.view.ui.sms.util.MarkedItemsHandler
import com.nibble.hashcaller.view.ui.sms.util.MarkedItemsHandler.markedContactAddress
import com.nibble.hashcaller.view.ui.sms.util.MarkedItemsHandler.markedItems
import com.nibble.hashcaller.view.utils.ConfirmDialogFragment
import com.nibble.hashcaller.view.utils.ConfirmationClickListener
import com.nibble.hashcaller.view.utils.IDefaultFragmentSelection
import com.nibble.hashcaller.view.utils.spam.SpamLocalListManager
import com.nibble.hashcaller.work.DESTINATION_ACTIVITY
import com.nibble.hashcaller.work.INDIVIDUAL_SMS_ACTIVITY
import kotlinx.android.synthetic.main.bottom_sheet_block.*
import kotlinx.android.synthetic.main.bottom_sheet_block_feedback.*
import kotlinx.android.synthetic.main.fragment_message_container.*
import kotlinx.android.synthetic.main.fragment_message_container.view.*


class SMSContainerFragment : Fragment(), IDefaultFragmentSelection,
    TabLayout.OnTabSelectedListener, View.OnClickListener,
    androidx.appcompat.widget.Toolbar.OnMenuItemClickListener, ConfirmationClickListener,
    PopupMenu.OnMenuItemClickListener {

    private var isDflt = false

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var messagesView:View
    private lateinit var viewmodel: SmsContainerViewModel
    private var smsListFragment:SMSListFragment? = null
    private var smsIdentifiedAsSpamFragment:SMSIdentifiedAsSpamFragment? = null
    private var permissionGivenLiveData: MutableLiveData<Boolean> = MutableLiveData(false)

    private lateinit var toolbarSms:MaterialToolbar
    private var permissionGivenLiveDAta: MutableLiveData<Boolean> = MutableLiveData()
    private var defaultSmsHandlerLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var bottomSheetDialogfeedback: BottomSheetDialog
    private  var selectedRadioButton: RadioButton? = null
    private  var spammerType:Int = -1
    private var SPAMMER_CATEGORY = SpamLocalListManager.SPAMMER_BUISINESS


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
//        if(checkPermission()){
            messagesView =  inflater.inflate(R.layout.fragment_message_container, container, false)
            viewSms = messagesView
         toolbarSms = messagesView.findViewById(R.id.toolbarSmS)
        toolbarSms.setOnMenuItemClickListener(this)
//        toolbarSms.inflateMenu(R.menu.sms_container_menu)
        toolbarSms.setNavigationOnClickListener(View.OnClickListener {
            Log.d(TAG, "onCreateView:item clicked ")
            (activity as MainActivity).showDrawer(it)
        })
//        (activity as AppCompatActivity).setSupportActionBar(toolbarSmS)

            initViewModel()
        observerDefaulsSmshandlerPermission()
        observeNumOfRowsDeleted()
        setupBottomSheet()

        if(checkContactPermission())
        {
            observeSMSList()
        }
        observePermissionLiveData()

        return messagesView
//        }else{
//            return inflater.inflate(R.layout.request_permission, container, false)
//        }

    }




    private fun observerDefaulsSmshandlerPermission() {
        this.permissionGivenLiveDAta.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "observerDefaulsSmshandlerPermission: $it")
            if(it == true){
                deleteSms()
            }
        })
    }

    private fun observeSMSList() {

    }

    private fun observePermissionLiveData() {
        this.permissionGivenLiveData.observe(viewLifecycleOwner, Observer { value->
            if(!value){
                observeSMSList()

                if (this.viewmodel!! != null  ) {
                    if(this.viewmodel?.SMS != null)
                        if(this.viewmodel.SMS!!.hasObservers())
                            this.viewmodel?.SMS?.removeObservers(this);
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

    private fun initViewModel() {
        this.viewmodel = ViewModelProvider(this, SMSContainerInjectorUtil.provideViewModelFactory(context)).get(
            SmsContainerViewModel::class.java)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        if(checkPermission()){
            setupViewPager(viewPagerMessages)
            tabLayoutMessages?.setupWithViewPager(viewPagerMessages)
//            tabLayoutMessages.addOnTabSelectedListener(this)
        tabLayoutMessages.getTabAt(0)!!.setIcon(R.drawable.ic_baseline_message_24)
        tabLayoutMessages.getTabAt(1)!!.setIcon(R.drawable.ic_baseline_block_no_color)
            initListeners()
        observerSmsLiveDataFromViewmodel()


//        }

    }

    /**
     * gets the sms livedata and retrieve information from server
     * for the numbers
     */
    private fun observerSmsLiveDataFromViewmodel() {
        this.viewmodel.SMS.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "observerSmsLiveDataFromViewmodel: ")
            this.viewmodel.getInformationForTheseNumbers(it, activity?.packageName!!)
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if(savedInstanceState!= null){
            if(childFragmentManager.getFragment(savedInstanceState, "smsListFragment") != null){

                this.smsListFragment = childFragmentManager.getFragment(savedInstanceState, "smsListFragment") as SMSListFragment?

                this.smsIdentifiedAsSpamFragment = childFragmentManager.getFragment(savedInstanceState, "smsIdentifiedAsSpamFragment") as SMSIdentifiedAsSpamFragment?

            }

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(this.smsIdentifiedAsSpamFragment !=null){
            if(this.smsIdentifiedAsSpamFragment!!.isAdded){
                childFragmentManager.putFragment(outState,"smsIdentifiedAsSpamFragment", this.smsIdentifiedAsSpamFragment!!)
                childFragmentManager.putFragment(outState,"smsListFragment", this.smsListFragment!!)
            }
        }


    }
    private fun initListeners() {

        tabLayoutMessages.addOnTabSelectedListener(this)
        this.messagesView.fabBtnDeleteSMS.setOnClickListener(this)
        this.messagesView.fabBtnDeleteSMSExpanded.setOnClickListener(this)
        this.fabSendNewSMS.setOnClickListener(this)
        this.imgBtnTbrDelete.setOnClickListener(this)
        this.messagesView.imgBtnTbrMuteSender.setOnClickListener(this)
        this.messagesView.imgBtnTbrBlock.setOnClickListener(this)

        bottomSheetDialog.radioS.setOnClickListener(this)
        bottomSheetDialog.radioScam.setOnClickListener(this)
        bottomSheetDialog.imgExpand.setOnClickListener(this)
        bottomSheetDialog.btnBlock.setOnClickListener(this)

    }

    private fun setupViewPager(viewPagerMessages: ViewPager?) {
        if(this.smsIdentifiedAsSpamFragment == null){
            this.smsIdentifiedAsSpamFragment = SMSIdentifiedAsSpamFragment()
            this.smsListFragment = SMSListFragment()
        }
        val viewPagerAdapter = ViewPagerAdapter(childFragmentManager)
        viewPagerAdapter.addFragment(this.smsListFragment!!, "Messages")
        viewPagerAdapter.addFragment(this.smsIdentifiedAsSpamFragment!!, "Identified as spam")
//
        viewPagerMessages!!.adapter = viewPagerAdapter


    }

    private fun checkSmsWritePermission(): Boolean {
        var permissionGiven = false
        //persmission
        Dexter.withContext(this.activity)
            .withPermissions(
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_MMS,
                Manifest.permission.SEND_RESPOND_VIA_MESSAGE


                ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) { /* ... */
//
                    report.let {
                        if(report?.areAllPermissionsGranted()!!){
                            permissionGiven = true
//                            Toast.makeText(applicationContext, "thank you", Toast.LENGTH_SHORT).show()

                        }else{
                            Log.d(TAG, "onPermissionsChecked: not given------")
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken?
                ) { /* ... */
                    token?.continuePermissionRequest()
//                    Toast.makeText(applicationContext, "onPermissionRationaleShouldBeShown", Toast.LENGTH_SHORT).show()
                }
            }).check()
        return permissionGiven
    }
    private fun checkPermission(): Boolean {
        var permissionGiven = false
        //persmission
        Dexter.withContext(this.activity)
            .withPermissions(
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS

            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) { /* ... */
//
                    report.let {
                        if(report?.areAllPermissionsGranted()!!){
                            permissionGiven = true
//                            Toast.makeText(applicationContext, "thank you", Toast.LENGTH_SHORT).show()

                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken?
                ) { /* ... */
                    token?.continuePermissionRequest()
//                    Toast.makeText(applicationContext, "onPermissionRationaleShouldBeShown", Toast.LENGTH_SHORT).show()
                }
            }).check()
        return permissionGiven
    }



    fun toggleDeleteFab(){

        val visibility = this.messagesView.fabBtnDeleteSMS.visibility
        if(visibility == View.VISIBLE){
            this.messagesView.fabBtnDeleteSMS.visibility = View.INVISIBLE

        }else{
            this.messagesView.fabBtnDeleteSMS.visibility = View.VISIBLE

        }
    }

    override var isDefaultFgmnt: Boolean
        get() = isDflt
        set(value) {isDflt = value}



    override fun onTabReselected(tab: TabLayout.Tab?) {
        Log.d(TAG, "onTabReselected: ${tab?.position}")
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        Log.d(TAG, "onTabUnselected: ${tab?.position}")
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        Log.d(TAG, "onTabSelected: ${tab?.position} ")
        if (tab != null) {
            when(tab.position){
                    0->{
                        this.messagesView.fabBtnDeleteSMS.visibility = View.INVISIBLE
                        this.messagesView.fabBtnDeleteSMSExpanded.visibility = View.INVISIBLE
                        this.messagesView.fabSendNewSMS.visibility = View.VISIBLE
                    }
                1->{
                    this.messagesView.fabSendNewSMS.visibility = View.INVISIBLE
                    this.messagesView.fabBtnDeleteSMSExpanded.visibility = View.VISIBLE
                    this.messagesView.fabBtnDeleteSMS.visibility = View.INVISIBLE
                    unMarkItems()
                    showSearchView()

                }
            }
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.fabBtnDeleteSMS, R.id.fabBtnDeleteSMSExpanded ->{
                val i = Intent(activity, ScheduleActivity::class.java)
                startActivity(i)
            }
            R.id.fabSendNewSMS ->{
                this.viewmodel.deleteAllSmsindb() // JUST FOR TESTING PURPOSE
//                val i = Intent(context, ContactSelectorActivity::class.java )
//                i.putExtra(DESTINATION_ACTIVITY, INDIVIDUAL_SMS_ACTIVITY)
//                startActivity(i)
            }
            R.id.imgBtnTbrDelete ->{
                deleteMarkedSMSThreads()
            }R.id.imgBtnTbrMuteSender ->{
                muteSender()
            }
            R.id.imgBtnTbrBlock ->{
                blockUser()
            }
            R.id.imgExpand->{
                Log.d(IndividualSMSActivity.TAG, "onClick: img button")
                val popup = PopupMenu(this.requireActivity(), bottomSheetDialog.viewPopup)
                popup.inflate(R.menu.image_chooser_popup)
                popup.setOnMenuItemClickListener(this)
                popup.show()

            }
            R.id.btnBlock->{
                Log.d(TAG, "onClick: ")
                addToBlockList(MarkedItemsHandler.markedContactAddressForBlocking!!)
            }
        }
    }


    //todo i cannot let user mark and block,
    //then we will not get information about that spammer
    private fun blockUser() {
        //check if markedItems.size ==1, if > 1 then show alert that block one contactAdress at a time

        if(markedItems.size>1){
           val dialog = ConfirmDialogFragment(this, "Please block one contact address at a time", 1)
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

    /**
     * To block a contact address
     * @param contact contact address
     *
     */
    private fun addToBlockList(contact: String) {
        this.viewmodel.blockThisAddress(contact, MarkedItemsHandler.markedTheadIdForBlocking, this.spammerType, this.SPAMMER_CATEGORY )

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

    override fun onMenuItemClick(menuItem: MenuItem?): Boolean {
        this.spammerType = SpamLocalListManager.menuItemClickPerformed(menuItem, bottomSheetDialog)
        return true
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

        bottomSheetDialog.setOnDismissListener{
            Log.d(IndividualSMSActivity.TAG, "bottomSheetDialogDismissed")

        }
    }

    private fun muteSender() {
        this.viewmodel.muteMarkedSenders()
        resetMarkingOptions()
    }

//    private fun deleteList() {
//        markedItems.clear()
//        markedContactAddress.clear()
//    }
    private fun observeNumOfRowsDeleted() {
        this.viewmodel.numRowsDeletedLiveData.observe(viewLifecycleOwner, Observer {
            if(it == 0 ){
                Log.d(TAG, "observeNumOfRowsDeleted: $it")
                checkDefaultSMSHandlerPermission()
            }
        })
    }
    private fun deleteMarkedSMSThreads() {
          deleteSms()
       }



    private fun deleteSms() {
        val dialog = ConfirmDialogFragment(this, "Delete conversation?", 2)
        dialog.show(childFragmentManager, "sample")
    }

    /**
     * callback of ConfirmDialogfragment
     */
    override fun onYesConfirmation() {
        Log.d(TAG, "deleteSms: called")
//        for(id in markedItems){
            this.viewmodel.deleteThread()
//        }
//        deleteList()

        resetMarkingOptions()
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

    /**
     * change visibility of view items to as beginning, ie
     * remove all marking/checked items hide delete,mute etc buttons in toolbar
     */
    private fun resetMarkingOptions() {
        markingStarted = false
        unMarkItems()
        this.searchViewMessages.visibility = View.VISIBLE
        this.imgBtnTbrMuteSender.visibility = View.INVISIBLE
        this.imgBtnTbrBlock.visibility = View.INVISIBLE
        this.imgBtnTbrDelete.visibility = View.INVISIBLE
        this.tvSelectedCount.visibility = View.INVISIBLE
    }

    companion object {
        private const val TAG = "__SMSContainerFragment"
        var recyclerViewSpamSms:RecyclerView? = null
        var viewSms:View? = null

        /**
         * function to update marked item count in fragment
         */
        fun updateSelectedItemCount(count:Int){
            if(count>0){
                viewSms!!.tvSelectedCount.visibility = View.VISIBLE
                viewSms!!.tvSelectedCount.text = "$count Selected"
            }else{
                viewSms!!.tvSelectedCount.visibility = View.INVISIBLE
                viewSms!!.tvSelectedCount.text = ""
                markingStarted = false
//                unMarkItems()
                viewSms!!.searchViewMessages.visibility = View.VISIBLE
                viewSms!!.imgBtnTbrMuteSender.visibility = View.INVISIBLE
                viewSms!!.imgBtnTbrBlock.visibility = View.INVISIBLE
                viewSms!!.imgBtnTbrDelete.visibility = View.INVISIBLE
                viewSms!!.tvSelectedCount.visibility = View.INVISIBLE
            }
        }
        fun show(){


              viewSms?.fabBtnDeleteSMSExpanded?.extend()

        }
        fun hide(){
            viewSms?.fabBtnDeleteSMSExpanded?.shrink()

        }
    }

    override fun onResume() {
        super.onResume()
        this.permissionGivenLiveData.value  = checkContactPermission()
    }



    fun hideSearchView() {
        searchViewMessages.visibility = View.INVISIBLE
    }

    fun showToolbarButtons() {
        imgBtnTbrDelete.visibility = View.VISIBLE
        imgBtnTbrMuteSender.visibility = View.VISIBLE
        imgBtnTbrBlock.visibility = View.VISIBLE
        tvSelectedCount.visibility = View.VISIBLE

    }

    fun showSearchView() {
        searchViewMessages.visibility = View.VISIBLE
        imgBtnTbrDelete.visibility = View.INVISIBLE
        imgBtnTbrMuteSender.visibility = View.INVISIBLE
        imgBtnTbrBlock.visibility = View.INVISIBLE
        tvSelectedCount.visibility = View.INVISIBLE

        unMarkItems()

    }

    fun isSearchViewVisible(): Boolean {
        if(searchViewMessages.visibility== View.VISIBLE)
            return true
        return false
    }
    @SuppressLint("LongLogTag")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        /**
         * Set as default SMS app onActivityResult if user chosen as deafult SMS app
         * is -1
         * else the result is 0
         */
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== -1 && requestCode == 222){
            permissionGivenLiveDAta.value  = true

        }
        Log.d(TAG, "onActivityResult: requestCode :$requestCode")
        Log.d(TAG, "onActivityResult: resultCode :$resultCode")

    }




}