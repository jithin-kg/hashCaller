package com.nibble.hashcaller.view.ui.IncommingCall

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
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

class ActivityIncommingCallView : AppCompatActivity(), View.OnClickListener, View.OnTouchListener  {
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
    var dX = 0f
    var dY = 0f
    var lastAction = 0
    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

       getIntents()
        binding = ActivityIncommingCallViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurePopupActivity()
       initViewmodel()
        initListeners()
       setViewElements()
//
    }

    private fun initListeners() {
        binding.cnstraintlyoutInner.setOnTouchListener(this)
    }

    private fun initViewmodel() {
        viewModel = ViewModelProvider(this, IncommingCallInjectorUtil.provideUserInjectorUtil(this)).get(
            IncommingCallViewModel::class.java
        )
    }

    private fun getIntents() {
        name = intent.getStringExtra("name")?:""
        phoneNumber = intent.getStringExtra("phoneNumber")?:""
        spamcount = intent.getIntExtra("spamcount", 0)
        location  = intent.getStringExtra("location")?:""
        carrier  = intent.getStringExtra("carrier")?:""
    }

    private fun configurePopupActivity() {
        /**
         * important to setLayout outherwise activity goes full screen
         */
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

        this.setFinishOnTouchOutside(true)
    }

    @SuppressLint("LongLogTag")
    private fun setViewElements() {
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

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event!!.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                dX = binding.cnstraintlyoutInner.x - event!!.rawX
                dY = binding.cnstraintlyoutInner.y - event!!.rawY
                lastAction = MotionEvent.ACTION_DOWN
            }
            MotionEvent.ACTION_MOVE -> {
                binding.cnstraintlyoutInner.y = event!!.rawY + dY
                binding.cnstraintlyoutInner.setX(event!!.rawX + dX)
                lastAction = MotionEvent.ACTION_MOVE
            }
            MotionEvent.ACTION_UP -> if (lastAction === MotionEvent.ACTION_DOWN) Toast.makeText(
                this,
                "Clicked!",
                Toast.LENGTH_SHORT
            ).show()
            else -> return false
        }
        return true
    }
}