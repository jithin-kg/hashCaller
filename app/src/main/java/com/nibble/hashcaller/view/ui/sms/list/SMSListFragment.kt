package com.nibble.hashcaller.view.ui.sms.list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.sms.SMSContainerFragment
import com.nibble.hashcaller.view.ui.sms.util.SMSViewModel
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ADDRES
import com.nibble.hashcaller.view.ui.sms.individual.IndividualSMSActivity
import com.nibble.hashcaller.view.utils.TopSpacingItemDecoration
import kotlinx.android.synthetic.main.fragment_messages_list.*


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cntx = this!!.context!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewMesages = inflater.inflate(R.layout.fragment_messages_list, container, false)

       initVieModel()
        val parent: Fragment? = (parentFragment as SMSContainerFragment).parentFragment
    

       observeSMSList()
       observeLoadinState()  
        return  viewMesages
    }

    private fun observeLoadinState() {
        SMSViewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading->
            if(isLoading){
                pgBarSMSList.visibility = View.VISIBLE

            }else{
                pgBarSMSList.visibility = View.GONE
            }
            
        })
    }

    private fun initListeners() {

    }

    private fun initVieModel() {
        smsListVIewModel = ViewModelProvider(this, SMSListInjectorUtil.provideDialerViewModelFactory(context)).get(
            SMSViewModel::class.java)
    }

    private fun observeSMSList() {
        smsListVIewModel.SMS.observe(viewLifecycleOwner, Observer { sms->
            sms.let {
//                smsRecyclerAdapter?.setSMSList(it, searchQry)
                smsRecyclerAdapter?.submitList(it)
                SMSListAdapter.searchQry = searchQry


            }
        })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        rcrViewSMSList.adapter  = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initListeners()
        val sView = viewMesages.rootView.findViewById(R.id.searchViewMessages) as SearchView

        Log.d(TAG, "onCreateView: $sView")




        sView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(searchQuery: String?): Boolean {
                Log.d(TAG, "onQueryTextChange: $searchQuery")
                searchQry = searchQuery

                smsListVIewModel.search(searchQuery)
//                GlobalScope.launch {
//                    val smslist = smsListVIewModel.SMS.value
//                    val newSms:MutableList<SMS> = emptyList<SMS>().toMutableList()
//                    smslist?.forEach {
//                       if(it.msg?.contains(searchQuery.toString())!!){
//                           val yellow =
//                               BackgroundColorSpan(Color.YELLOW)
//                           val spannableStringBuilder =
//                               SpannableStringBuilder(it.msg)
////                           val startPos = it.msg!!.toLowerCase().indexOf(searchQuery!!.toLowerCase())
////                           val endPos = startPos + searchQuery.length
////                           spannableStringBuilder.setSpan(yellow,startPos, endPos, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
//                           it.msg = spannableStringBuilder
//                           newSms.add(it)
//                       }
//                    }
//                    smsRecyclerAdapter?.submitList(newSms)
//                    SMSListAdapter.searchQry = searchQry
//
//                }


                return true

            }
        })
    }

    private fun initRecyclerView() {
        rcrViewSMSList?.apply {
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
        smsListVIewModel.update(address)
        val intent = Intent(context, IndividualSMSActivity::class.java )
        intent.putExtra(CONTACT_ADDRES, address)
        startActivity(intent)
    }


    companion object {
    private const val TAG = "__SMSListFragment"
    }

    override fun onClick(p0: View?) {
  smsListVIewModel.getUnrealMsgCount()
    }

}