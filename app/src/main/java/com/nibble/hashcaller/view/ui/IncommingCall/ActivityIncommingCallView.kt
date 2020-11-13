package com.nibble.hashcaller.view.ui.IncommingCall

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.R
import com.nibble.hashcaller.network.search.model.Cntct
import kotlinx.android.synthetic.main.activity_incomming_call_view.*
import kotlinx.android.synthetic.main.activity_phone_auth.*

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
        txtVPhoneNum.text = callerInfo.phoneNumber
        txtVCarrier.text =  callerInfo.carrier
        txtVCity.text =  callerInfo.location
        txtVCountry.text =  callerInfo.country
        if(callerInfo.spammerStatus !=null)
        if(callerInfo.spammerStatus?.spamCount > 10){
            Log.d(TAG, "onCreate: spammer calling");
            layoutIncommingCall.setBackgroundColor(Color.parseColor("#E80000"))
        }else{
            Log.d(TAG, "onCreate: ${callerInfo.spammerStatus.spamCount}")
            layoutIncommingCall.setBackgroundColor(Color.parseColor("#0CBDEA"))

        }

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