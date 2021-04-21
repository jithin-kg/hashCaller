package com.nibble.hashcaller.view.ui.call.individualCallLog

import android.annotation.SuppressLint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CallLog
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nibble.hashcaller.databinding.ActivityIndividualCallLogBinding
import com.nibble.hashcaller.view.ui.call.dialer.util.CustomLinearLayoutManager
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ADDRES

class IndividualCallLogActivity : AppCompatActivity(), IndividualCallLogAdapter.LongPressHandler {

    lateinit var binding : ActivityIndividualCallLogBinding
    private lateinit var viewmodel: IndividualCallViewModel
    private var num:String? = null
    private lateinit var adapter:IndividualCallLogAdapter
    private lateinit var layoutMngr:LinearLayoutManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityIndividualCallLogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        num  = intent.getStringExtra(CONTACT_ADDRES)
        initAdapter()
        initViewmodel()
        observeCallLog()

    }

    @SuppressLint("LongLogTag")
    private fun observeCallLog() {
        viewmodel.callLogLiveData.observe(this, Observer {
//            adapter.submitList(it)
            adapter.setList(it)
        })
    }
    private fun initAdapter() {
//        recyclerView =
//            findViewById<View>(R.id.recyclerViewSMSIndividual) as RecyclerView


        adapter = IndividualCallLogAdapter(this,  this ){ id:String -> onContactitemClicked(id) }

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.recyclerView.setHasFixedSize(true)
        layoutMngr = CustomLinearLayoutManager(this)
        layoutMngr.stackFromEnd = true
        binding.recyclerView.layoutManager = layoutMngr

        binding.recyclerView.adapter = adapter
        binding.recyclerView.isNestedScrollingEnabled = false
    }
    private fun onContactitemClicked(id: String) {

    }

    private fun initViewmodel() {
        val URI =  Uri.withAppendedPath(
            CallLog.Calls.CONTENT_FILTER_URI,
            Uri.encode(num)
        );
        viewmodel = ViewModelProvider(this, IndividualCallLogInjectorUtil.provideDialerViewModelFactory(this, lifecycleScope, URI)).get(
            IndividualCallViewModel::class.java)

    }
    companion object{
        const val TAG ="__IndividualCallLogActivity"
    }

    override fun onLongPressed(view: View, pos: Int, id: Long) {

    }
}