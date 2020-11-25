package com.nibble.hashcaller.view.ui.SMS.list

import android.os.Bundle
import android.text.style.ForegroundColorSpan
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
import com.nibble.hashcaller.view.ui.SMS.SMSContainerFragment
import com.nibble.hashcaller.view.ui.SMS.util.SMSViewModel
import com.nibble.hashcaller.view.utils.TopSpacingItemDecoration
import kotlinx.android.synthetic.main.fragment_messages_list.*


class SMSListFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var viewMesages:View
    private lateinit var smsListVIewModel:SMSViewModel
    var smsRecyclerAdapter: SMSListAdapter? = null
    private lateinit var searchV: SearchView
    private var searchQry:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewMesages = inflater.inflate(R.layout.fragment_messages_list, container, false)

        smsListVIewModel = ViewModelProvider(this, SMSListInjectorUtil.provideDialerViewModelFactory(context)).get(
            SMSViewModel::class.java)

        val parent: Fragment? = (parentFragment as SMSContainerFragment).parentFragment


        smsListVIewModel.SMS.observe(viewLifecycleOwner, Observer { sms->
            sms.let {
                smsRecyclerAdapter?.setSMSList(it, searchQry)

            }
        })
        return  viewMesages
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
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

    private fun onContactItemClicked(id: String) {

    }


    companion object {
    private const val TAG = "__SMSListFragment"
    }
}