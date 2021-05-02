package com.nibble.hashcaller.view.ui.sms.identifiedspam

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.Shimmer
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.contacts.utils.*
import com.nibble.hashcaller.view.ui.contacts.utils.pageOb.page
import com.nibble.hashcaller.view.ui.contacts.utils.pageOb.pageSpam
import com.nibble.hashcaller.view.ui.contacts.utils.pageOb.totalSMSCount
import com.nibble.hashcaller.view.ui.sms.SMSContainerFragment
import com.nibble.hashcaller.view.ui.sms.individual.IndividualSMSActivity
import com.nibble.hashcaller.view.ui.sms.list.SMSSpamListAdapter
import kotlinx.android.synthetic.main.fragment_message_container.*
import kotlinx.android.synthetic.main.fragment_spam_messages.*
import kotlinx.android.synthetic.main.fragment_spam_messages.view.*
import kotlinx.android.synthetic.main.sms_list_view.view.*


class SMSListSpamFragment : Fragment(), View.OnClickListener{
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var viewMesages:View
    private lateinit var smsListVIewModel:SMSSpamViewModel
    var smsRecyclerAdapter: SMSSpamListAdapter? = null
    private lateinit var searchV: SearchView
    private var searchQry:String? = null
    private lateinit var cntx:Context
    private lateinit var recyclerV:RecyclerView

    private lateinit var sView:EditText
    private lateinit var sharedPreferences: SharedPreferences
    var skeletonLayout: LinearLayout? = null
    var shimmer: Shimmer? = null
    var inflater: LayoutInflater? = null
    private var permissionGivenLiveData: MutableLiveData<Boolean> = MutableLiveData(false)
    private var layoutMngr:LinearLayoutManager? = null
    private lateinit var searchViewMessages: EditText
    private var isLoading = false
    var limit = 12

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cntx = this!!.requireContext()

    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewMesages = inflater.inflate(R.layout.fragment_spam_messages, container, false)

        initVieModel()
        if(checkContactPermission())
        {
            observeSMSList()
        }
        initListeners()
        val parent: Fragment? = (parentFragment as SMSContainerFragment).parentFragment

        observeSendersInfoFromServer()
        observePermissionLiveData()
        this.recyclerV = this.viewMesages.findViewById<RecyclerView>(R.id.rcrViewSMSSpamList)
        registerForContextMenu( this.recyclerV) // context menu registering

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
                    val visibleItemCount = layoutMngr!!.childCount
                    val pastVisibleItem = layoutMngr!!.findFirstCompletelyVisibleItemPosition()
                    val recyclerViewSize = smsRecyclerAdapter!!.itemCount
                    if(!isLoading){
                        if((visibleItemCount + pastVisibleItem) >= recyclerViewSize){
                            //we have reached the bottom
                            Log.d(TAG, "onScrolled:page: $page, totalsms count $totalSMSCount ")
//                                if(page+12 <= totalSMSCount ){
                            pageSpam+=12
                            smsListVIewModel.getNextSmsPage()
                            if(dy > 0){
                                if(!isSizeEqual){
//                                    viewMesages.shimmer_view_container.visibility = View.VISIBLE
//                                    viewMesages.rcrViewSMSList.visibility = View.INVISIBLE
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
            if(value == true){
                this.viewMesages.btnSmsReadPermission.visibility = View.GONE
                this.viewMesages.tvSMSPermissionInfo.visibility = View.GONE

                observeSMSList()
            }else{
                this.viewMesages.btnSmsReadPermission.visibility = View.VISIBLE
                this.viewMesages.tvSMSPermissionInfo.visibility = View.VISIBLE

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
        viewMesages.btnSmsReadPermission.setOnClickListener(this)
    }

    private fun initVieModel() {
        smsListVIewModel = ViewModelProvider(this, SMSListSpamInjectorUtil.provideDialerViewModelFactory(
            context,
            lifecycleScope
        )).get(
            SMSSpamViewModel::class.java)
    }


    private fun observeSendersInfoFromServer() {
        smsListVIewModel.getSmsSendersInfoFromServer().observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "observeSendersInfoFromServer: $it")

            smsListVIewModel.updateWithNewSenderInfo(it)

        })
    }

    private fun observeSMSList() {
        smsListVIewModel.SMS.observe(viewLifecycleOwner, Observer { sms->
            sms.let {

                Log.d(TAG, "observeSMSList: size is ${sms.size} ")
                this.smsListVIewModel.updateLiveData(sms)

            }

        })
    }
    private fun     observeMutabeLiveData() {
//        this.smsListVIewModel.smsLiveData.observe(viewLifecycleOwner, Observer {
//            smsListVIewModel.smsLIst = it as MutableList<SMS>?
//            Log.d(TAG, "observeMutabeLiveData: ")
//            var newList:MutableList<SMS> = mutableListOf()

//            it.forEach{sms-> newList.add(sms.deepCopy())}
//            smsRecyclerAdapter?.setList(it)

//            this.viewMesages.pgBarsmslist.visibility = View.GONE
//            this.viewMesages.shimmer_view_container.visibility = View.GONE
//            viewMesages.rcrViewSMSList.visibility = View.VISIBLE
//            SMSListAdapter.searchQry = searchQry
//        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        rcrViewSMSList.adapter  = null
    }

    @SuppressLint("WrongViewCast")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        this.searchViewMessages = viewMesages.rootView.findViewById(R.id.searchViewMessages)
        initRecyclerView()
        initListeners()

//        sView = viewMesages.rootView.findViewById(R.id.searchViewSms)

        Log.d(TAG, "onCreateView: $sView")

        observeMutabeLiveData()
        addScrollListener()




    }








    private fun initRecyclerView() {
        rcrViewSMSSpamList?.apply {
            layoutManager = LinearLayoutManager(activity)
            layoutMngr = layoutManager as LinearLayoutManager
//            smsRecyclerAdapter = SMSSpamListAdapter(context, this@SMSListSpamFragment){ view:View, threadId:Long, pos:Int,
//                                                                                    pno:String->onContactItemClicked(view,threadId, pos, pno)  }
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
        if(markingStarted){
            //if the view is already marked, then uncheck it
            val imgVSmsMarked = view.findViewById<ImageView>(R.id.smsMarked)
            if(imgVSmsMarked.visibility == View.VISIBLE){
                unMarkItem(imgVSmsMarked, threadId, address)

            }else{
                markItem(view, threadId, address)
            }

        }else{
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
    override fun onResume() {
        super.onResume()
        this.permissionGivenLiveData.value  = checkContactPermission()
    }

    companion object {
        private const val TAG = "__SMSListFragment"
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnSmsReadPermission ->{
//                this.permissionGivenLiveData.value = PermissionUtil.requesetPermission(this.requireActivity())
            }
            else ->{
                smsListVIewModel.getUnrealMsgCount()

            }
        }
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

//    override fun onLongPressed(v:View, pos:Int, id: Long, address:String) {


//        (parentFragment as SMSContainerFragment?)!!.hideSearchView()
//        (parentFragment as SMSContainerFragment?)!!.showToolbarButtons()
//        markingStarted = true

//        markItem(v, id, address)
//    }

    /**
     * mark for deletion or archival or block of sms list
     */
    private fun markItem(v: View, id: Long, address: String) {

        v.smsMarked.visibility = View.VISIBLE
//        MarkedItemsHandler.markedItemsSpam.add(id)
//        MarkedItemsHandler.markedViewsSpam.add(v)
//        markedContactAddressSpam.add(address)
//        SMSContainerFragment.showHideBlockButton()
//        SMSContainerFragment.updateSelectedItemCountOfspam(MarkedItemsHandler.markedItemsSpam.size)
    }

    override fun onPause() {
        Log.d(TAG, "onPause: ")
        super.onPause()
    }

    override fun onStop() {
        Log.d(TAG, "onStop: ")
        super.onStop()
    }




}