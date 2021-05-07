package com.nibble.hashcaller.view.ui.IncommingCall

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivityIncommingCallViewBinding
import com.nibble.hashcaller.network.StatusCodes.Companion.STATUS_SEARHING_IN_PROGRESS
import com.nibble.hashcaller.network.search.model.Cntct
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.CARRIER
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.FIRST_NAME
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.LOCATION
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.PHONE_NUMBER
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.SHOW_FEEDBACK_VIEW
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.SPAM_COUNT
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.STATUS_CODE
import com.nibble.hashcaller.utils.constants.IntentKeys.Companion.UPDATE_INCOMMING_VIEW
import com.nibble.hashcaller.view.ui.contacts.utils.SPAM_THREASHOLD
import com.nibble.hashcaller.view.ui.sms.individual.util.beGone
import com.nibble.hashcaller.view.ui.sms.individual.util.beVisible


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
    private  var firstName :String = ""
    private var showfeedbackView = false
    private  var phoneNumber :String = ""
    private  var location :String = ""
    private  var carrier :String = ""
    private  var spamcount : Int = 0
    private var statusCode = STATUS_SEARHING_IN_PROGRESS
    var dX = 0f
    var dY = 0f
    var lastAction = 0
    private  var mMessageReceiver: BroadcastReceiver? = null
    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isVisible = true
//        val close = intent.getIntExtra("kill", 0)
//        if(close==1){
//            finishAfterTransition()
//        }
        registerForBroadCastReceiver()
       getIntentxras(intent)
        binding = ActivityIncommingCallViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurePopupActivity()
       initViewmodel()
        initListeners()
       setViewElements()
//
    }


    private fun registerForBroadCastReceiver() {
         mMessageReceiver  = object : BroadcastReceiver() {
            @SuppressLint("LongLogTag")
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d(TAG, "onReceive: broadcast")
                when(intent?.action){
                    UPDATE_INCOMMING_VIEW ->{
                        updateViewWithData(intent)
                    }

                }

            }
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(UPDATE_INCOMMING_VIEW)
        registerReceiver(mMessageReceiver, intentFilter);
    }

    private fun updateViewWithData(intent: Intent) {
        getIntentxras(intent)
        setViewElements()
    }

    private fun initListeners() {
        if(!showfeedbackView){
            binding.cnstraintlyoutInner.setOnTouchListener(this)
        }
        binding.imgBtnCloseIncommin.setOnClickListener(this)
        binding.layoutIncommingCall.setOnClickListener(this)
    }

    private fun initViewmodel() {
        viewModel = ViewModelProvider(this, IncommingCallInjectorUtil.provideUserInjectorUtil(this)).get(
            IncommingCallViewModel::class.java
        )
    }

    private fun getIntentxras(intent: Intent) {
        firstName = intent.getStringExtra(FIRST_NAME)?:""
        phoneNumber = intent.getStringExtra(PHONE_NUMBER)?:""
        spamcount = intent.getIntExtra(SPAM_COUNT, 0)
        location  = intent.getStringExtra(LOCATION)?:""
        carrier  = intent.getStringExtra(CARRIER)?:""
        showfeedbackView = intent.getBooleanExtra(SHOW_FEEDBACK_VIEW, false)
        statusCode = intent.getIntExtra(STATUS_CODE, STATUS_SEARHING_IN_PROGRESS)
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
        binding.tvPhoneNumIncomming.text = phoneNumber

        binding.txtVLocaltion.text =  location
        if(statusCode == STATUS_SEARHING_IN_PROGRESS){
            binding.txtVcallerName.text =  "Searching.."
        }else{
            binding.txtVcallerName.text =  firstName

        }

//        if(callerInfo.spammerStatus !=null)
        if(spamcount > SPAM_THREASHOLD){
            Log.d(TAG, "onCreate: spammer calling");
            binding.cnstraintlyoutInner.background = ContextCompat.getDrawable(
                this,
                R.drawable.incomming_call_background_spam
            )

        }else{
            Log.d(TAG, "onCreate: spam count less than 1 ${spamcount}")
            binding.cnstraintlyoutInner.background = ContextCompat.getDrawable(
                this,
                R.drawable.incomming_call_background
            )


        }

        if(showfeedbackView){
//            binding.layoutExpandedIncomming.beVisible()
        }else{
//            binding.layoutExpandedIncomming.beGone()
        }
    }

    companion object{
        // property to check whether the Activity is visible,
        var  isVisible: Boolean? = false

        private const val TAG = "__ActivityIncommingCallView"
    }

    @SuppressLint("LongLogTag")
    override fun onClick(v: View?) {
        Log.d(TAG, "onClick: ")
        when(v?.id){
            R.id.imgBtnCloseIncommin -> {
                Log.d(TAG, "onClick: btn")
                closeActivity()
//                reportuser()
            }

            R.id.layoutIncommingCall -> {
                closeActivity()

            }
        }
    }

    private fun closeActivity() {
//        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        finishAfterTransition()
//        val anim = AnimationUtils.loadAnimation(this, R.anim.exit_to_bottom)
//        binding.cnstraintlyoutInner.startAnimation(anim)
    }


    @SuppressLint("LongLogTag")
    private fun reportuser() {

        viewModel.report(phoneNumber, packageName).observe(this, Observer {
            Log.d(TAG, "reportuser: observing")
        })
    }
//https://stackoverflow.com/questions/9398057/android-move-a-view-on-touch-move-action-move
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

//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
//        finish()
//        startActivity(intent)
//    }

    /**
     * This is shown by leak canary to prevent memory leak
     * use finishAfterTransition onBackPressed to prevent memory leak
     */
    override fun onBackPressed() {
        closeActivity()
        super.onBackPressed()

    }

    @SuppressLint("LongLogTag")
    override fun onPostResume() {
        super.onPostResume()
        Log.d(TAG, "onPostResume: ")
        isVisible = true
    }

    @SuppressLint("LongLogTag")
    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: ")
        isVisible = true
    }

    @SuppressLint("LongLogTag")
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: ")
        isVisible = false


    }

    override fun onStop() {
        super.onStop()
        //to prevent memory leak
        if(mMessageReceiver!=null){
            unregisterReceiver(mMessageReceiver)

        }
    }

    @SuppressLint("LongLogTag")
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
        isVisible = null
    }
}