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
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ADDRES
import com.nibble.hashcaller.view.ui.sms.individual.IndividualSMSActivity
import com.nibble.hashcaller.view.ui.sms.list.SMSListAdapter
import com.nibble.hashcaller.view.utils.TopSpacingItemDecoration
import kotlinx.android.synthetic.main.activity_main.*
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
                } else {
                    viewMesages.imgViewNoSpam.visibility = View.VISIBLE
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
                smsRecyclerAdapter?.submitList(it)
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
       val height = activity?.bottomNavigationView?.height
        Log.d(TAG, "onViewCreated: height $height")
         sView = viewMesages.rootView.findViewById(R.id.searchViewMessages) as SearchView
        
//        val params = CoordinatorLayout.LayoutParams(
//            CoordinatorLayout.LayoutParams.WRAP_CONTENT,
//            CoordinatorLayout.LayoutParams.WRAP_CONTENT
//        )
//        params.marginEnd = 100
//        params.bottomMargin = height!!
////        params.setMargins(0, 0, 100, height!!)
//        params.gravity = Gravity.BOTTOM or Gravity.END

//        view.fabBtnDeleteSMS.layoutParams = params

        Log.d(TAG, "onCreateView: $sView")




        searchViewListener()
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
        viewMesages.btnEmptySpamSMS.setOnClickListener(this)
    }

    private fun initRecyclerView() {
        rcrViewSMSSpamList?.apply {
            layoutManager = LinearLayoutManager(activity)
            val topSpacingDecorator =
                TopSpacingItemDecoration(
                    30
                )
            addItemDecoration(topSpacingDecorator)
            smsRecyclerAdapter = SMSListAdapter(context) { id:String->onContactItemClicked(id)}
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

    private fun initVieModel() {
        viewmodel = ViewModelProvider(this, SMSListSpamInjectorUtil.provideDialerViewModelFactory(context)).get(
            SMSSpamViewModel::class.java)
    }

    companion object {
        private const val TAG = "__SMSIdentifiedAsSpamFragment"
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btnEmptySpamSMS->{
                deleteAllSpamSms()
            }
        }
    }

    private fun deleteAllSpamSms() {
        viewmodel.deleteSpamSMS()
    }
}