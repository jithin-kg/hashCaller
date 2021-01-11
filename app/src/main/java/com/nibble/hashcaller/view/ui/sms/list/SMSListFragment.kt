package com.nibble.hashcaller.view.ui.sms.list

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.facebook.shimmer.Shimmer
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.contacts.IndividualContacts.utils.PermissionUtil
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ADDRES
import com.nibble.hashcaller.view.ui.sms.SMSContainerFragment
import com.nibble.hashcaller.view.ui.sms.individual.IndividualSMSActivity
import com.nibble.hashcaller.view.ui.sms.send.SendTestActivity
import com.nibble.hashcaller.view.ui.sms.util.SMS
import com.nibble.hashcaller.view.ui.sms.util.SMSViewModel

import kotlinx.android.synthetic.main.fragment_messages_list.*
import kotlinx.android.synthetic.main.fragment_messages_list.view.*


class SMSListFragment : Fragment(), View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var viewMesages:View
    private lateinit var smsListVIewModel:SMSViewModel
    var smsRecyclerAdapter: SMSListAdapter? = null
    private lateinit var searchV: SearchView
    private var searchQry:String? = null
    private lateinit var cntx:Context
    private var smsLIst:MutableList<SMS>? = null
    private lateinit var sView:SearchView

    var skeletonLayout: LinearLayout? = null
    var shimmer: Shimmer? = null
    var inflater: LayoutInflater? = null
    private var permissionGivenLiveData: MutableLiveData<Boolean> = MutableLiveData(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cntx = this!!.requireContext()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewMesages = inflater.inflate(R.layout.fragment_messages_list, container, false)

       initVieModel()
        if(checkContactPermission())
        {
            observeSMSList()
        }
        initListeners()
        val parent: Fragment? = (parentFragment as SMSContainerFragment).parentFragment

       observeLoadinState()
        observePermissionLiveData()
        return  viewMesages
    }
    private fun observePermissionLiveData() {
        this.permissionGivenLiveData.observe(viewLifecycleOwner, Observer { value->
            if(value == true){
                this.viewMesages.btnSmsPermission.visibility = View.GONE
                this.viewMesages.tvSMSPermission.visibility = View.GONE
                this.viewMesages.pgBarSMSList.visibility = View.VISIBLE
                observeSMSList()
            }else{
                this.viewMesages.btnSmsPermission.visibility = View.VISIBLE
                this.viewMesages.tvSMSPermission.visibility = View.VISIBLE
                this.viewMesages.pgBarSMSList.visibility = View.GONE

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
    private fun observeLoadinState() {
        SMSViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading->
            if(isLoading){
                pgBarSMSList.visibility = View.VISIBLE
//                showSkeleton(true)

            }else{
//                showSkeleton(false)
                pgBarSMSList.visibility = View.GONE
            }
            
        })
    }



    private fun initListeners() {
        viewMesages.btnSmsPermission.setOnClickListener(this)
    }

    private fun initVieModel() {
        smsListVIewModel = ViewModelProvider(this, SMSListInjectorUtil.provideDialerViewModelFactory(context)).get(
            SMSViewModel::class.java)
    }

    private fun observeSMSList() {
//        smsListVIewModel.SMS.observe(viewLifecycleOwner, Observer { sms->
//            sms.let {
////                smsRecyclerAdapter?.setSMSList(it, searchQry)
//                Log.d(TAG, "observeSMSList: data changed")
//                smsRecyclerAdapter?.submitList(it)
//                SMSListAdapter.searchQry = searchQry
//                this.smsLIst = it as MutableList<SMS>?
//
//            }
//        })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        rcrViewSMSList.adapter  = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initListeners()

         sView = viewMesages.rootView.findViewById(R.id.searchViewMessages) as SearchView

        Log.d(TAG, "onCreateView: $sView")





        observeLive()
        searchViewListener()

    }

    private fun searchViewListener() {
        sView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{

            override fun onQueryTextSubmit(p0: String?): Boolean {
                Log.d(TAG, "onQueryTextSubmit: ")
                return true
            }

            override fun onQueryTextChange(searchQuery: String?): Boolean {
                Log.d(TAG, "onQueryTextChange: $searchQuery")
                searchQry = searchQuery

                smsListVIewModel.search(searchQuery)

                return true

            }
        })
    }

    private fun observeLive() {
        smsListVIewModel.smsLive.observe(viewLifecycleOwner, Observer { sms->
            sms.let {
                Log.d(TAG, "observeLive: size ${sms.size}")
//                smsRecyclerAdapter?.setSMSList(it, searchQry)
                Log.d(TAG, "observeLive: data changed")
                smsRecyclerAdapter?.setList(it)
                SMSListAdapter.searchQry = searchQry
                this.smsLIst = it as MutableList<SMS>?


            }
        })
    }


    private fun initRecyclerView() {
        rcrViewSMSList?.apply {
            layoutManager = LinearLayoutManager(activity)
            smsRecyclerAdapter = SMSListAdapter(context){id:String, pos:Int,
                                                         pno:String->onContactItemClicked(id, pos, pno) }
            adapter = smsRecyclerAdapter

//                setContacts()

//                adapter.onItemClick =
        }
    }
    private fun onDeleteItemClicked(){

    }
    private fun onContactItemClicked(id: String, pos: Int, pno: String) {
        Log.d(TAG, "onContactItemClicked address is : $pno")
//        val newList:MutableList<SMS> = mutableListOf()
//        newList.addAll(this.smsLIst!!)
//        this.smsLIst!![pos].expanded = !(this.smsLIst!![pos].expanded)
//        this.smsListVIewModel.SMS.value = smsLIst
//        smsRecyclerAdapter?.submitList(newList)
//        smsListVIewModel.update(address) // update count
        val intent = Intent(context, IndividualSMSActivity::class.java )
        intent.putExtra(CONTACT_ADDRES, pno)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
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
            R.id.btnSmsPermission ->{
                this.permissionGivenLiveData.value = PermissionUtil.requesetPermission(this.requireActivity())
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

}