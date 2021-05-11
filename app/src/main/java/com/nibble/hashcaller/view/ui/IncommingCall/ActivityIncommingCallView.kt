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
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
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
import com.nibble.hashcaller.view.ui.contacts.search.utils.SearchInjectorUtil
import com.nibble.hashcaller.view.ui.contacts.search.utils.SearchViewModel
import com.nibble.hashcaller.view.ui.contacts.utils.SPAM_THREASHOLD
import com.nibble.hashcaller.view.ui.sms.individual.IndividualSMSActivity
import com.nibble.hashcaller.view.ui.sms.individual.util.beGone
import com.nibble.hashcaller.view.ui.sms.individual.util.beVisible
import kotlinx.android.synthetic.main.bottom_sheet_block.*


/**
 * !!important to have theme Theme.Holo.Dialog.NoActionBar,
 * the activity should be inheriting Activity not AppcompactActivity
 */

class ActivityIncommingCallView : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityIncommingCallViewBinding
    @SuppressLint("LongLogTag")
    private lateinit var viewModel: SearchViewModel
    private lateinit var callerInfo:Cntct
    @SuppressLint("LongLogTag")
    private  var firstName :String = ""
    private var showfeedbackView = false
    private  var phoneNumber :String = ""
    private  var location :String = ""
    private  var carrier :String = ""
    private  var spamcount : Int = 0
    private var statusCode = STATUS_SEARHING_IN_PROGRESS
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var bottomSheetDialogfeedback: BottomSheetDialog
    private  var selectedRadioButton: RadioButton? = null

    private  var mMessageReceiver: BroadcastReceiver? = null
    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")
        isVisible = true
//        registerForBroadCastReceiver()
       getIntentxras(intent)
        binding = ActivityIncommingCallViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurePopupActivity()
        initViewmodel()
        setupBottomSheet()
        initListeners()
        setViewElements()
        getUserInfoFromContacts()
        getUserInfoFromServer()


    }

    @SuppressLint("LongLogTag")
    private fun getUserInfoFromContacts() {
        viewModel.findOnecontact(phoneNumber).observe(this, Observer {
            Log.d(TAG, "getUserInfoFromContacts: ${it}")
            binding.txtVcallerName.text = it
        })
    }
    private fun getUserInfoFromServer(){

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



    private fun initViewmodel() {
        viewModel = ViewModelProvider(this, SearchInjectorUtil.provideUserInjectorUtil(applicationContext)).get(
            SearchViewModel::class.java)
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

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));


//        this.setFinishOnTouchOutside(true)
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
            binding.cnstraintlyoutInner.background = ContextCompat.getDrawable(
                this,
                R.drawable.incomming_call_background
            )


        }

//        if(showfeedbackView){
////            binding.layoutExpandedIncomming.beVisible()
//        }else{
////            binding.layoutExpandedIncomming.beGone()
//        }
    }


    private fun initListeners() {
        binding.imgBtnCloseIncommin.setOnClickListener(this)
        binding.imgBtnCallIncomingBlock.setOnClickListener(this)
        binding.imgBtnCallIncomingSMS.setOnClickListener(this)
        binding.imgBtnCallIncomming.setOnClickListener(this)



        bottomSheetDialog.radioS.setOnClickListener(this)
        bottomSheetDialog.radioScam.setOnClickListener(this)
        bottomSheetDialog.imgExpand.setOnClickListener(this)
        bottomSheetDialog.btnBlock.setOnClickListener(this)
//        binding.layoutIncommingCall.setOnClickListener(this)
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
            R.id.imgBtnCallIncomingBlock ->{
                showBottomSheetDialog()
            }
            R.id.imgBtnCallIncomingSMS ->{

            }

            R.id.imgBtnCallIncomming ->{

            }
        }
    }

    private fun showBottomSheetDialog() {
        bottomSheetDialog.show()

    }
    private fun setupBottomSheet() {
        bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialogfeedback = BottomSheetDialog(this)
        val viewSheet = layoutInflater.inflate(R.layout.bottom_sheet_block, null)
        val viewSheetFeedback = layoutInflater.inflate(R.layout.bottom_sheet_block_feedback, null)

        bottomSheetDialog.setContentView(viewSheet)
        bottomSheetDialogfeedback.setContentView(viewSheetFeedback)

        selectedRadioButton = bottomSheetDialog.radioScam
        bottomSheetDialog.imgExpand.setOnClickListener(this)



        bottomSheetDialog.setOnDismissListener {
            Log.d(IndividualSMSActivity.TAG, "bottomSheetDialogDismissed")

        }
    }

    private fun closeActivity() {

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        finishAfterTransition()
//        val anim = AnimationUtils.loadAnimation(this, R.anim.exit_to_bottom)
//        binding.cnstraintlyoutInner.startAnimation(anim)
    }


    @SuppressLint("LongLogTag")
    private fun reportuser() {

//        viewModel.report(phoneNumber, packageName).observe(this, Observer {
//            Log.d(TAG, "reportuser: observing")
//        })
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
    companion object{
        // property to check whether the Activity is visible,
        var  isVisible: Boolean? = false
        private const val TAG = "__ActivityIncommingCallView"
    }
}