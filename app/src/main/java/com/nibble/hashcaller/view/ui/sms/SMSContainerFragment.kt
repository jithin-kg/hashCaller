package com.nibble.hashcaller.view.ui.sms

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
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
import com.nibble.hashcaller.view.ui.sms.identifiedspam.SMSIdentifiedAsSpamFragment
import com.nibble.hashcaller.view.ui.sms.list.SMSListFragment
import com.nibble.hashcaller.view.ui.sms.schedule.ScheduleActivity
import com.nibble.hashcaller.view.ui.sms.util.MarkedItemsHandler
import com.nibble.hashcaller.view.ui.sms.util.MarkedItemsHandler.markedItems
import com.nibble.hashcaller.view.utils.IDefaultFragmentSelection
import com.nibble.hashcaller.work.DESTINATION_ACTIVITY
import com.nibble.hashcaller.work.INDIVIDUAL_SMS_ACTIVITY
import kotlinx.android.synthetic.main.fragment_message_container.*
import kotlinx.android.synthetic.main.fragment_message_container.view.*


class SMSContainerFragment : Fragment(), IDefaultFragmentSelection,
    TabLayout.OnTabSelectedListener, View.OnClickListener,
    androidx.appcompat.widget.Toolbar.OnMenuItemClickListener {
    private var isDflt = false

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var messagesView:View
    private lateinit var viewmodel: SmsContainerViewModel
    private var smsListFragment:SMSListFragment? = null
    private var smsIdentifiedAsSpamFragment:SMSIdentifiedAsSpamFragment? = null
    private var permissionGivenLiveData: MutableLiveData<Boolean> = MutableLiveData(false)

    private lateinit var toolbarSms:androidx.appcompat.widget.Toolbar

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
                val i = Intent(context, ContactSelectorActivity::class.java )
                i.putExtra(DESTINATION_ACTIVITY, INDIVIDUAL_SMS_ACTIVITY)
                startActivity(i)
            }
            R.id.imgBtnTbrDelete ->{
                deleteMarkedSMSThreads()
                deleteList()
            }
        }
    }

    private fun deleteList() {
        markedItems.clear()
    }

    private fun deleteMarkedSMSThreads() {
        for(id in markedItems){
            this.viewmodel.deleteThread(id)
        }

    }

    companion object {
        private const val TAG = "__SMSContainerFragment"
        var recyclerViewSpamSms:RecyclerView? = null
        var viewSms:View? = null
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

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        Log.d(TAG, "onMenuItemClick: ")
        return true
    }

    fun hideSearchView() {
        searchViewMessages.visibility = View.INVISIBLE
    }

    fun showToolbarButtons() {
        imgBtnTbrDelete.visibility = View.VISIBLE
        imgBtnTbrArchive.visibility = View.VISIBLE
        imgBtnTbrBlock.visibility = View.VISIBLE

    }

    fun showSearchView() {
        searchViewMessages.visibility = View.VISIBLE
        imgBtnTbrDelete.visibility = View.INVISIBLE
        imgBtnTbrArchive.visibility = View.INVISIBLE
        imgBtnTbrBlock.visibility = View.INVISIBLE

    }

    fun isSearchViewVisible(): Boolean {
        if(searchViewMessages.visibility== View.VISIBLE)
            return true
        return false
    }
}