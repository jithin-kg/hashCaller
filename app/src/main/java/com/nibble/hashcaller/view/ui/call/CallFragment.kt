package com.nibble.hashcaller.view.ui.call

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.call.dialer.DialerAdapter
import com.nibble.hashcaller.view.ui.call.dialer.DialerFragment
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogData
import com.nibble.hashcaller.view.ui.call.utils.CallContainerInjectorUtil
import com.nibble.hashcaller.view.ui.call.utils.CallLogFlowHelper
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.clearlists
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.getExpandedLayoutView
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.getMarkedItemSize
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.isItemSizeEqualsOne
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.isMarkingStarted
import com.nibble.hashcaller.view.ui.call.work.CallContainerViewModel
import com.nibble.hashcaller.view.ui.contacts.IndividualContacts.utils.PermissionUtil
import com.nibble.hashcaller.view.ui.contacts.utils.OPERATION_COMPLETED
import com.nibble.hashcaller.view.ui.sms.individual.util.*
import com.nibble.hashcaller.view.utils.ConfirmDialogFragment
import com.nibble.hashcaller.view.utils.ConfirmationClickListener
import com.nibble.hashcaller.view.utils.IDefaultFragmentSelection
import kotlinx.android.synthetic.main.call_list.view.*
import kotlinx.android.synthetic.main.fragment_call.*
import kotlinx.android.synthetic.main.fragment_call.view.*
import kotlinx.android.synthetic.main.fragment_call_history.rcrViewCallHistoryLogs
import kotlinx.android.synthetic.main.fragment_call_history.view.btnCallhistoryPermission
import kotlinx.coroutines.flow.collect


/**
 * A simple [Fragment] subclass.
 * Use the [CallFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CallFragment : Fragment(),View.OnClickListener , IDefaultFragmentSelection,
    DialerAdapter.CallItemLongPressHandler, ConfirmationClickListener {
    private var isDflt = false

    private var toolbar: Toolbar? = null
    var callFragment: View? = null
//    private lateinit var searchViewCall: EditText
    var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    var layoutBottomSheet: ConstraintLayout? = null
    private lateinit var dialerFragment: DialerFragment
    private lateinit var viewmodel: CallContainerViewModel
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
        Log.d(TAG, "onCreateView: ")
        callFragment =  inflater.inflate(R.layout.fragment_call, container, false)

        initViewModel()

        // Inflate the layout for this fragment
        if(checkContactPermission()){

            observeCallLog()
            observeCallLogMutabeLivedata()
            collectdata()

        }
        initListeners()
        observePermissionLiveData()

//        addFragmentDialer()
        return callFragment

    }

    private fun collectdata() {
        lifecycleScope.launchWhenStarted {
            CallLogFlowHelper.fetchCallLogFlow(this@CallFragment.requireActivity()).collect {
               viewmodel. updateLiveDataWithFlow(it)
            }
        }

    }

    private fun observePermissionLiveData() {
        this.permissionGivenLiveData.observe(viewLifecycleOwner, Observer {
            if(it == true){
                Log.d(TAG, "observePermissionLiveData: permission given")
                observeCallLog()
                observeCallLogMutabeLivedata()
                this.callFragment!!.btnCallhistoryPermission.visibility = View.GONE
            }else{
                this.callFragment!!.btnCallhistoryPermission.visibility =View.VISIBLE
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

        this.callFragment!!.btnCallhistoryPermission.setOnClickListener(this)
        this.callFragment!!.imgBtnCallTbrBlock.setOnClickListener(this)
        this.callFragment!!.imgBtnCallTbrMuteCaller.setOnClickListener(this)
        this.callFragment!!.imgBtnCallTbrDelete.setOnClickListener(this)
    }

    private fun observeCallLogMutabeLivedata(){
        viewmodel.callLogsMutableLiveData.observe(viewLifecycleOwner, Observer {
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
    override fun onResume() {
        super.onResume()
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
        initRecyclerView()
        initListeners()




    }
    private fun initRecyclerView() {
        rcrViewCallHistoryLogs?.apply {
            layoutManager = LinearLayoutManager(activity)
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

    private fun makeCall(callLog: CallLogData) {
        val num = callLog.number
        if(num.isNotEmpty())  {
            Log.d(TAG, "onClick: make call ")
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            callIntent.data = Uri.parse("tel:$num")
            requireActivity().startActivity(callIntent)
        }
    }

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
        toolbar = callFragment?.findViewById(R.id.toolbarCall)
//        searchViewCall = callFragment?.findViewById(R.id.searchViewCall)!!
    }

    companion object {
            private const val TAG ="__CallFragment"
    
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
            }

        }

    }

    private fun muteMarkedCaller() {
        lifecycleScope.launchWhenStarted {
            viewmodel.muteMarkedCaller().collect {
                when(it){
                    OPERATION_COMPLETED ->{
                        showSearchView()
                        clearlists()
                    }
                }
            }
        }
    }

    private fun deletemarkedLogs() {
        val dialog = ConfirmDialogFragment(this, "Delete callLogs?", 2)
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
//
//        lifecycleScope.launchWhenStarted {
//            viewmodel.markItem(id, view, pos).collect{
//                if(it!=null){
//                    if(isMarkingStarted()){
//                        showDeleteBtnInToolbar()
//                    }
//                    val view = it.findViewById<ConstraintLayout>(R.id.layoutcallMain)
//                        view.imgViewCallMarked.beVisible()
//                    updateSelectedItemCount()
////                    view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryLowOpacity))
//                }
//            }
//        }
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
        if(isMarkingStarted()){
            markItem(id, position, view, callLog.number)


        }else{
            viewExpanded = 1
            //no marking started, then expand the layout
            val id = callLogAdapter!!.getItemId(position)
            val v = view.findViewById<ConstraintLayout>(R.id.layoutExpandableCall)
            when(btn){
                DialerAdapter.BUTTON_SIM_1->{
                    Log.d(TAG, "onCallLogItemClicked: buttonsim 1")
//                    makeCall(callLog)
                }
                DialerAdapter.BUTTON_SIM_2->{

                }
                DialerAdapter.BUTTON_SMS->{

                }
                DialerAdapter.BUTTON_INFO->{

                }

            }
        }

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

                val viewMain = view.findViewById<ConstraintLayout>(R.id.layoutcallMain)

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
//                    view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimaryLowOpacity))
            }
        }

    }

    private fun hideBlockButton() {
        this.requireActivity().runOnUiThread {
            this.callFragment!!.imgBtnCallTbrBlock.beInvisible()
            this.callFragment!!.imgBtnCallTbrMuteCaller.beInvisible()
        }

    }

    private fun showBlockButon() {
       this.requireActivity().runOnUiThread {
           imgBtnCallTbrBlock.beVisible()
           imgBtnCallTbrMuteCaller.beVisible()
       }

    }

    private fun showDeleteBtnInToolbar() {
        Log.d(TAG, "showDeleteBtnInToolbar: ")
        searchViewCall.beInvisible()
        imgBtnCallTbrBlock.beVisible()
        imgBtnCallTbrMuteCaller.beVisible()
        imgBtnCallTbrDelete.beVisible()
        imgBtnCallTbrMore.beVisible()

    }
     fun showSearchView(){
        searchViewCall.beVisible()
        imgBtnCallTbrBlock.beInvisible()
         imgBtnCallTbrMuteCaller.beInvisible()
        imgBtnCallTbrDelete.beInvisible()
        imgBtnCallTbrMore.beInvisible()
        tvCallSelectedCount.beInvisible()
    }

    fun updateSelectedItemCount(){
        val count = getMarkedItemSize()
        tvCallSelectedCount.text = "${count.toString()} Selected"
        if(count>0){
            tvCallSelectedCount.beVisible()
        }else{
            showSearchView()
        }
    }

    override fun onYesConfirmation() {
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

}