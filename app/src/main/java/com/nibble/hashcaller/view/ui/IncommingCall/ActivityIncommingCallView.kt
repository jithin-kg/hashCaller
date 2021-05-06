package com.nibble.hashcaller.view.ui.IncommingCall

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivityIncommingCallViewBinding
import com.nibble.hashcaller.network.search.model.Cntct
import kotlinx.android.synthetic.main.activity_incomming_call_view.*

/**
 * !!important to have theme Theme.Holo.Dialog.NoActionBar,
 * the activity should be inheriting Activity not AppcompactActivity
 */

class ActivityIncommingCallView : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityIncommingCallViewBinding
    @SuppressLint("LongLogTag")
    private lateinit var viewModel:IncommingCallViewModel
    private lateinit var callerInfo:Cntct
    @SuppressLint("LongLogTag")
    private  var name :String = ""
    private  var phoneNumber :String = ""
    private  var location :String = ""
    private  var carrier :String = ""
    private  var spamcount : Int = 0
    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

         name = intent.getStringExtra("name")?:""
         phoneNumber = intent.getStringExtra("phoneNumber")?:""
         spamcount = intent.getIntExtra("spamcount", 0)
        location  = intent.getStringExtra("location")?:""
        carrier  = intent.getStringExtra("carrier")?:""

//        setTheme(R.style.AppTheme)
//        callerInfo = i.getSerializableExtra("SerachRes") as Cntct

//        Log.d(TAG, "onCreate: $callerInfo")
//        val dialog = IncommingDialog(this)

//        dialog.showDialog("hi")
//        getWindow().setBackgroundDrawable( ColorDrawable(android.graphics.Color.TRANSPARENT))
        binding = ActivityIncommingCallViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        /**
         * important to setLayout outherwise activity goes full screen
         */
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable( ColorDrawable(Color.TRANSPARENT));
//        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

//        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//        (this as AppCompatActivity).supportActionBar!!.title ="My APP Title"
////        supportActionBar?.hide();
//        val actionbar: ActionBar? = supportActionBar
//        actionbar?.title = ""
//        actionbar?.hide()


//        appbar.isEnabled = false
//        val toolbar: Toolbar = findViewById<View>(R.id.toolbar) as Toolbar
//        setSupportActionBar(toolbar)
//        supportActionBar!!.setDisplayShowTitleEnabled(false)


//        supportActionBar?.title = ""


        this.setFinishOnTouchOutside(true)
        viewModel = ViewModelProvider(this, IncommingCallInjectorUtil.provideUserInjectorUtil(this)).get(
            IncommingCallViewModel::class.java
        )

//        val callerInfo = Cntct(, "803830",
//            "", "vodafone",
//        "banglore", "IN")


        binding.imgBtnCloseIncommin.setOnClickListener(this)
        binding.tvPhoneNumIncomming.text = "+918086176336"
        binding.txtVcallerName.text =  "Jithin Kg"
        binding.txtVLocaltion.text =  "Kerala india"
//        if(callerInfo.spammerStatus !=null)
        if(spamcount > 0){
            Log.d(TAG, "onCreate: spammer calling");
            layoutIncommingCall.setBackgroundColor(Color.parseColor("#E80000"))
        }else{
            Log.d(TAG, "onCreate: spam count less than 1 ${spamcount}")
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
            R.id.imgBtnCloseIncommin -> {
                Log.d(TAG, "onClick: btn")
                finish()
//                reportuser()
            }
        }
    }

    @SuppressLint("LongLogTag")
    private fun reportuser() {

        viewModel.report(phoneNumber, packageName).observe(this, Observer {
            Log.d(TAG, "reportuser: observing")
        })
    }
}