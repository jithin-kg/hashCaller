package com.nibble.hashcaller.view.ui.IncommingCall

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.R
import com.nibble.hashcaller.network.search.model.Cntct
import com.nibble.hashcaller.view.ui.contacts.search.utils.SearchInjectorUtil
import com.nibble.hashcaller.view.ui.contacts.search.utils.SearchViewModel
import kotlinx.android.synthetic.main.activity_incomming_call_view.*

class ActivityIncommingCallView : AppCompatActivity(), View.OnClickListener {
    @SuppressLint("LongLogTag")
    private lateinit var viewModel:IncommingCallViewModel
    private lateinit var callerInfo:Cntct
    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var i = intent
        callerInfo = i.getSerializableExtra("SerachRes") as Cntct

        Log.d(TAG, "onCreate: $callerInfo")

        viewModel = ViewModelProvider(this, IncommingCallInjectorUtil.provideUserInjectorUtil(this)).get(
            IncommingCallViewModel::class.java)

        setContentView(R.layout.activity_incomming_call_view)
        btnReportCaller.setOnClickListener(this)
        callerName.text = callerInfo.phoneNumber

    }

    companion object{
        private const val TAG = "__ActivityIncommingCallView"
    }

    @SuppressLint("LongLogTag")
    override fun onClick(v: View?) {
        Log.d(TAG, "onClick: ")
        when(v?.id){
            R.id.btnReportCaller->{
                Log.d(TAG, "onClick: btn")
                reportuser()
            }
        }
    }

    @SuppressLint("LongLogTag")
    private fun reportuser() {
        viewModel.report(callerInfo.phoneNumber).observe(this, Observer {
            Log.d(TAG, "reportuser: observing")
        })
    }
}