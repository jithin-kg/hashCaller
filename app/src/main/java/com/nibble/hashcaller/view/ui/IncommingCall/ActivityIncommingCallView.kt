package com.nibble.hashcaller.view.ui.IncommingCall

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.nibble.hashcaller.R
import com.nibble.hashcaller.network.search.model.Cntct
import com.nibble.hashcaller.network.search.model.SpammerStatus
import kotlinx.android.synthetic.main.activity_incomming_call_view.*
import kotlinx.android.synthetic.main.fragment_call.*

/**
 * !!important to have theme Theme.Holo.Dialog.NoActionBar,
 * the activity should be inheriting Activity not AppcompactActivity
 */

class ActivityIncommingCallView : AppCompatActivity(), View.OnClickListener {
    @SuppressLint("LongLogTag")
    private lateinit var viewModel:IncommingCallViewModel
    private lateinit var callerInfo:Cntct
    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        requestWindowFeature(Window.FEATURE_NO_TITLE)
        var i = intent

//        setTheme(R.style.AppTheme)
//        callerInfo = i.getSerializableExtra("SerachRes") as Cntct

//        Log.d(TAG, "onCreate: $callerInfo")
        val dialog = IncommingDialog(this)

//        dialog.showDialog("hi")
//        getWindow().setBackgroundDrawable( ColorDrawable(android.graphics.Color.TRANSPARENT))
        setContentView(R.layout.activity_incomming_call_view)

//        appbar.isEnabled = false
//        val toolbar: Toolbar = findViewById<View>(R.id.toolbar) as Toolbar
//        setSupportActionBar(toolbar)
//        supportActionBar!!.setDisplayShowTitleEnabled(false)


//        supportActionBar?.title = ""

        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

//        this.setFinishOnTouchOutside(false)
        viewModel = ViewModelProvider(this, IncommingCallInjectorUtil.provideUserInjectorUtil(this)).get(
            IncommingCallViewModel::class.java)

        val callerInfo = Cntct("jithin", "803830",
            SpammerStatus(0, false), "vodafone",
        "banglore", "IN")



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
//
    }

    companion object{
        private const val TAG = "__ActivityIncommingCallView"
    }

    @SuppressLint("LongLogTag")
    override fun onClick(v: View?) {
        Log.d(TAG, "onClick: ")
        when(v?.id){
//            R.id.btnReportCaller->{
//                Log.d(TAG, "onClick: btn")
//                reportuser()
//            }
        }
    }

    @SuppressLint("LongLogTag")
    private fun reportuser() {
//        viewModel.report(callerInfo.phoneNumber).observe(this, Observer {
//            Log.d(TAG, "reportuser: observing")
//        })
    }
}