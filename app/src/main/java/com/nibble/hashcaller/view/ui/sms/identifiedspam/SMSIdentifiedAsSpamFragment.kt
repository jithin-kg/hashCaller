package com.nibble.hashcaller.view.ui.sms.identifiedspam

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.contacts.IndividualContacts.utils.PermissionUtil
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ADDRES
import com.nibble.hashcaller.view.ui.sms.SMSContainerFragment
import com.nibble.hashcaller.view.ui.sms.individual.IndividualSMSActivity
import com.nibble.hashcaller.view.ui.sms.list.SMSListAdapter
import com.nibble.hashcaller.view.ui.sms.list.SMSSpamListAdapter
import com.nibble.hashcaller.view.utils.TopSpacingItemDecoration
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_spam_messages.*
import kotlinx.android.synthetic.main.fragment_spam_messages.view.*


class SMSIdentifiedAsSpamFragment : Fragment(), View.OnClickListener {
    var smsRecyclerAdapter: SMSSpamListAdapter? = null
    private lateinit var viewmodel: SMSSpamViewModel
    private var searchQry:String? = null

    private lateinit var viewMesages:View
    private lateinit var sView:EditText
    private var smsListSize:MutableLiveData<Int> = MutableLiveData(0)
    private var permissionGivenLiveData: MutableLiveData<Boolean> = MutableLiveData(false)




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        viewMesages = inflater.inflate(R.layout.fragment_spam_messages, container, false)
        initVieModel()
        if(checkContactPermission()){
            observeSMSList()
        }
        initListeners()
        observeLoadinState()
        observeSmsSize()
        observePermissionLiveData()

        return  viewMesages
    }

    private fun observePermissionLiveData() {
        this.permissionGivenLiveData.observe(viewLifecycleOwner, Observer { value->
            if(value == true){
                this.viewMesages.btnSmsReadPermission.visibility = View.GONE
                this.viewMesages.tvSMSPermissionInfo.visibility = View.GONE
                this.viewMesages.pgBarSMSSpamList.visibility = View.VISIBLE
                observeSMSList()
            }else{
                this.viewMesages.btnSmsReadPermission.visibility = View.VISIBLE
                this.viewMesages.tvSMSPermissionInfo.visibility = View.VISIBLE
                this.viewMesages.pgBarSMSSpamList.visibility = View.GONE

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

    private fun observeSmsSize() {
        this.smsListSize.observe(viewLifecycleOwner, Observer { size->
            run {
                if (size > 0) {
                    viewMesages.imgViewNoSpam.visibility = View.GONE
//                    viewMesages.layoutDeleteSpamInfo.visibility = View.VISIBLE


                } else {
                    viewMesages.imgViewNoSpam.visibility = View.VISIBLE
//                    viewMesages.layoutDeleteSpamInfo.visibility = View.GONE
                }
            }
        })
    }

    private fun observeLoadinState() {
        SMSSpamViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading->
            if(isLoading){
                pgBarSMSSpamList.visibility = View.VISIBLE
//                showSkeleton(true)

            }else{
//                showSkeleton(false)
                pgBarSMSSpamList.visibility = View.GONE
            }

        })
    }
    override fun onResume() {
        super.onResume()
        this.permissionGivenLiveData.value  = checkContactPermission()
    }
    @SuppressLint("LongLogTag")
    private fun observeSMSList() {
        viewmodel.SMS.observe(viewLifecycleOwner, Observer { sms->
            if(sms == null){
               this.smsListSize.value = 0
            }else{
              this.smsListSize.value = sms.size
            }
            sms.let {

//                smsRecyclerAdapter?.setSMSList(it, searchQry)
//                smsRecyclerAdapter?.submitList(it)
                smsRecyclerAdapter?.setList(it)
                this.smsListSize.value = it.size
                SMSListAdapter.searchQry = searchQry

            }
        })
    }

    @SuppressLint("LongLogTag")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initListeners()
        val numOfDays = 2
//        this.viewMesages.tvsmsDeleteInfo.text  = "Items that have been in Spam will be deleted automatically" +
//                "according to your spam delete cycle"
       val height = activity?.bottomNavigationView?.height
        Log.d(TAG, "onViewCreated: height $height")
         sView = viewMesages.rootView.findViewById(R.id.searchViewSms)
        


        Log.d(TAG, "onCreateView: $sView")

        SMSContainerFragment.recyclerViewSpamSms = this.viewMesages.rcrViewSMSSpamList
        setScrollViewListener()
//        searchViewListener()
    }

    private fun setScrollViewListener() {
        viewMesages.rcrViewSMSSpamList?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            @SuppressLint("LongLogTag")
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                Log.d(TAG, "onScrolled: ")
                if (dy > 0 || dy < 0 ) SMSContainerFragment.hide()
            }

            @SuppressLint("LongLogTag")
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) SMSContainerFragment.show()
                Log.d(TAG, "onScrollStateChanged: ")
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }
    private fun
            searchViewListener() {
//        sView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
//            override fun onQueryTextSubmit(p0: String?): Boolean {
//                return true
//            }
//
//            @SuppressLint("LongLogTag")
//            override fun onQueryTextChange(searchQuery: String?): Boolean {
//                Log.d(TAG, "onQueryTextChange: $searchQuery")
//                searchQry = searchQuery
//
//                viewmodel.search(searchQuery)
//
//                return true
//
//            }
//        })
    }

    private fun initListeners() {
        viewMesages.btnSmsReadPermission.setOnClickListener(this)
    }

    private fun initRecyclerView() {
        rcrViewSMSSpamList?.apply {
            layoutManager = LinearLayoutManager(activity)
            val topSpacingDecorator =
                TopSpacingItemDecoration(
                    30
                )
            addItemDecoration(topSpacingDecorator)
//            smsRecyclerAdapter = SMSListAdapter(context,o) { id:String->onContactItemClicked(id)}
            //TODO PASS FUNCTION TYPE AS PARAMETER FOR HANDLING DELETE BUTTON CLICK
            smsRecyclerAdapter = SMSSpamListAdapter(context){id:String, pos:Int,
                                                         pno:String->onContactItemClicked(id, pos, pno) }
//            smsRecyclerAdapter = SMSListAdapter(context)
            adapter = smsRecyclerAdapter

//                setContacts()

//                adapter.onItemClick =
        }
    }

    @SuppressLint("LongLogTag")
    private fun onContactItemClicked(id: String, pos: Int, pno: String) {
        viewmodel.update(pno) // update count
        val intent = Intent(context, IndividualSMSActivity::class.java )
        Log.d(TAG, "onContactItemClicked: $pno")
        intent.putExtra(CONTACT_ADDRES, pno)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }

    private fun onDeleteItemClicked(){
        deleteAllSpamSms()
    }

    private fun initVieModel() {
        viewmodel = ViewModelProvider(this, SMSListSpamInjectorUtil.provideDialerViewModelFactory(context)).get(
            SMSSpamViewModel::class.java)
    }

    companion object {
        private const val TAG = "__SMSIdentifiedAsSpamFragment"
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.btnSmsReadPermission ->{
                this.permissionGivenLiveData.value = PermissionUtil.requesetPermission(this.requireActivity())

            }
        }
    }

    private fun deleteAllSpamSms() {
        viewmodel.deleteSpamSMS()
    }
}