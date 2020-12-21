package com.nibble.hashcaller.view.ui.sms.identifiedspam

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ADDRES
import com.nibble.hashcaller.view.ui.sms.SMSContainerFragment
import com.nibble.hashcaller.view.ui.sms.individual.IndividualSMSActivity
import com.nibble.hashcaller.view.ui.sms.list.SMSListAdapter
import com.nibble.hashcaller.view.utils.TopSpacingItemDecoration
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_message_container.view.*
import kotlinx.android.synthetic.main.fragment_spam_messages.*
import kotlinx.android.synthetic.main.fragment_spam_messages.view.*


class SMSIdentifiedAsSpamFragment : Fragment(), View.OnClickListener {
    var smsRecyclerAdapter: SMSListAdapter? = null
    private lateinit var viewmodel: SMSSpamViewModel
    private var searchQry:String? = null

    private lateinit var viewMesages:View
    private lateinit var sView:SearchView
    private var smsListSize:MutableLiveData<Int> = MutableLiveData(0)




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
        observeSMSList()
        observeLoadinState()
        observeSmsSize()

        return  viewMesages
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
         sView = viewMesages.rootView.findViewById(R.id.searchViewMessages) as SearchView
        


        Log.d(TAG, "onCreateView: $sView")

        SMSContainerFragment.recyclerViewSpamSms = this.viewMesages.rcrViewSMSSpamList
        setScrollViewListener()
        searchViewListener()
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
    private fun searchViewListener() {
        sView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }

            @SuppressLint("LongLogTag")
            override fun onQueryTextChange(searchQuery: String?): Boolean {
                Log.d(TAG, "onQueryTextChange: $searchQuery")
                searchQry = searchQuery

                viewmodel.search(searchQuery)

                return true

            }
        })
    }

    private fun initListeners() {
//        viewMesages.btnEmptySpamSMS.setOnClickListener(this)
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
            smsRecyclerAdapter = SMSListAdapter(context,::onContactItemClicked,::onDeleteItemClicked)
//            smsRecyclerAdapter = SMSListAdapter(context)
            adapter = smsRecyclerAdapter

//                setContacts()

//                adapter.onItemClick =
        }
    }

    private fun onContactItemClicked(address: String) {
        viewmodel.update(address) // update count
        val intent = Intent(context, IndividualSMSActivity::class.java )
        intent.putExtra(CONTACT_ADDRES, address)
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

    }

    private fun deleteAllSpamSms() {
        viewmodel.deleteSpamSMS()
    }
}