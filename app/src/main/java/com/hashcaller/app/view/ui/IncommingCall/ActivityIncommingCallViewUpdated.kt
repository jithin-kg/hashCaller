package com.hashcaller.app.view.ui.IncommingCall

import android.Manifest
import android.animation.Animator
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.hashcaller.app.R
import com.hashcaller.app.databinding.ActivityIncommingCallViewUpdatedBinding
import com.hashcaller.app.datastore.DataStoreRepository
import com.hashcaller.app.datastore.PreferencesKeys
import com.hashcaller.app.datastore.PreferencesKeys.Companion.SPAM_THRESHOLD
import com.hashcaller.app.network.HttpStatusCodes.Companion.STATUS_SEARHING_IN_PROGRESS
import com.hashcaller.app.network.search.model.Cntct
import com.hashcaller.app.network.search.model.CntctitemForView
import com.hashcaller.app.utils.Constants
import com.hashcaller.app.utils.Constants.Companion.DEFAULT_SPAM_THRESHOLD
import com.hashcaller.app.utils.Constants.Companion.NO_SIM_DETECTED
import com.hashcaller.app.utils.Constants.Companion.SIM_ONE
import com.hashcaller.app.utils.Constants.Companion.SIM_TWO
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.utils.constants.IntentKeys
import com.hashcaller.app.utils.constants.IntentKeys.Companion.CALL_HANDLED_STATE
import com.hashcaller.app.utils.constants.IntentKeys.Companion.CLOSE_INCOMMING_VIEW
import com.hashcaller.app.utils.constants.IntentKeys.Companion.PHONE_NUMBER
import com.hashcaller.app.utils.constants.IntentKeys.Companion.SHOW_FEEDBACK_VIEW
import com.hashcaller.app.utils.extensions.requestCallPhonePermission
import com.hashcaller.app.utils.notifications.tokeDataStore
import com.hashcaller.app.view.ui.blockConfig.GeneralBlockInjectorUtil
import com.hashcaller.app.view.ui.blockConfig.GeneralblockViewmodel
import com.hashcaller.app.view.ui.contacts.individualContacts.IndividualContactViewActivity
import com.hashcaller.app.view.ui.contacts.makeCall
import com.hashcaller.app.view.ui.contacts.search.utils.SearchInjectorUtil
import com.hashcaller.app.view.ui.contacts.search.utils.SearchViewModel
import com.hashcaller.app.view.ui.contacts.stopFltinServiceFromActiivtyIncomming
import com.hashcaller.app.view.ui.contacts.utils.CONTACT_ID
import com.hashcaller.app.view.ui.sms.individual.util.*
import com.hashcaller.app.view.utils.*
import com.hashcaller.app.work.formatPhoneNumber
import com.vmadalin.easypermissions.EasyPermissions
import kotlinx.android.synthetic.main.activity_incomming_call_view_updated.*
import kotlinx.android.synthetic.main.activity_incomming_call_view_updated.view.*
import kotlinx.android.synthetic.main.bottom_sheet_block.*
import kotlinx.coroutines.delay


/**
 * !!important to have theme Theme.Holo.Dialog.NoActionBar,
 * the activity should be inheriting Activity not AppcompactActivity
 */

class ActivityIncommingCallViewUpdated : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityIncommingCallViewUpdatedBinding

    @SuppressLint("LongLogTag")
    private lateinit var viewModel: SearchViewModel
    private lateinit var incommingCallViewUpdatedModel: IncommingCallViewUpdatedModel
    private lateinit var callerInfo: Cntct

    @SuppressLint("LongLogTag")
    private var showfeedbackView = false
    private var phoneNumber: String = ""
    private var callHandledState: String = ""
    private var callHandledSim = NO_SIM_DETECTED
    private lateinit var userInfo: CntctitemForView
    private var statusCode = STATUS_SEARHING_IN_PROGRESS
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var bottomSheetDialogfeedback: BottomSheetDialog
    private var selectedRadioButton: RadioButton? = null
    private var radioSales: RadioButton? = null
    private var radioScam: RadioButton? = null
    private var radioBusiness: RadioButton? = null
    private var radioPerson: RadioButton? = null
    private var mMessageReceiver: BroadcastReceiver? = null
    private var previousCheckedRadioButton: RadioButton? = null
    private var btnBlock: Button? = null

    private var rcfirebaseAuth: FirebaseAuth? = null
    private var user: FirebaseUser? = null
    private var tokenHelper: TokenHelper? = null
    private lateinit var generalBlockViewmodel: GeneralblockViewmodel
    private var spammerType: Int = SPAMMER_TYPE_SCAM

    private var callersInfo: CntctitemForView? = null
    private var  dataStoreRepository: DataStoreRepository? = null
    private var spamThreshold:Int = DEFAULT_SPAM_THRESHOLD



    //    private var country = ""
    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSpamThreshold()
        isVisible = true
        callHandledState = intent.getStringExtra(CALL_HANDLED_STATE) ?: ""
        registerForBroadCastReceiver()
        getIntentxras(intent)
        binding = ActivityIncommingCallViewUpdatedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        stopFltinServiceFromActiivtyIncomming()
        configurePopupActivity()
        initViewmodel()
        setupBottomSheet()
        initListeners()
        phoneNumber = intent.getStringExtra(PHONE_NUMBER) ?: ""
        callHandledSim = intent.getIntExtra(IntentKeys.CALL_HANDLED_SIM, NO_SIM_DETECTED)
        setViewFromIntent()
        getCallerInfo()
        checkIfUserBlockedThisNumber()
        animateCard()

    }
    private fun setSpamThreshold() {
        dataStoreRepository = DataStoreRepository(this.tokeDataStore)
        lifecycleScope.launchWhenStarted {
            spamThreshold = dataStoreRepository?.getInt(SPAM_THRESHOLD)?: DEFAULT_SPAM_THRESHOLD
        }
    }

    private fun animateCard() {
        lifecycleScope.launchWhenCreated {

            binding.suggestCard.visibility = View.GONE

            binding.imgVAvatarIncomming.fadeInAnim(350L)
            binding.materialCardView.topToBottomAnim(660L, 500f)

            binding.helpfulMessage.visibility = View.INVISIBLE
            binding.actionsCard.visibility = View.INVISIBLE
            delay(500)

            binding.actionsCard.visibility = View.VISIBLE
            binding.actionsCard.topToBottomAnim(500L, 100f)

            delay(300)
            binding.helpfulMessage.visibility = View.VISIBLE
            binding.helpfulMessage.bottomToTopAnim(500L, 500f)
        }

    }

    private fun setViewFromIntent() {
        binding.tvPhoneNumIncomming.text = phoneNumber
        binding.txtVcallerName.text = phoneNumber

        if (callHandledState.isEmpty()) {
            callHandledState = "Call Ended"
        }
        binding.tvCallEndState.text = callHandledState

        when (callHandledSim) {
            SIM_TWO -> {
                binding.sim.setImageResource(R.drawable.ic_sim_2_line_white)
            }
            SIM_ONE -> {
                binding.sim.setImageResource(R.drawable.ic_sim_1_line_white)
            }
        }

    }

    private fun checkIfUserBlockedThisNumber() {
        viewModel?.isthisNumberBlocked(phoneNumber)
    }


    private fun getCallerInfo() {
        viewModel.getCallerInfo(phoneNumber).observe(this, Observer {
            setViewElements(it)
            if (!it.isSearchedForCallerInserver) {
                //search fo
            }
//            if(it.in)
        })
    }

    @SuppressLint("LongLogTag")
    private fun setViewElements(callersInfo: CntctitemForView) {

        this.callersInfo = callersInfo

        binding.txtVLocaltion.text = callersInfo.country + " " + callersInfo.location
        binding.txtVcallerName.text =
            if (callersInfo.firstName.isEmpty()) phoneNumber else callersInfo.firstName + " " + callersInfo.lastName
        if (callersInfo.spammCount > spamThreshold) {
            Log.d(TAG, "onCreate: spammer calling");
            binding.materialCardView.setCardBackgroundColor(getColor(R.color.spamText))
        } else {
            binding.materialCardView.setCardBackgroundColor(0xff3398ED.toInt())

        }
    }


    private fun registerForBroadCastReceiver() {
        mMessageReceiver = object : BroadcastReceiver() {
            @SuppressLint("LongLogTag")
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d(TAG, "onReceive: broadcast")
                when (intent?.action) {
                    CLOSE_INCOMMING_VIEW -> {
//                        updateViewWithData(intent)
                        finishAfterTransition()
                    }

                }

            }
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(CLOSE_INCOMMING_VIEW)
        registerReceiver(mMessageReceiver, intentFilter);
    }

    private fun updateViewWithData(intent: Intent) {
//        getIntentxras(intent)
//        setViewElements()
    }


    private fun initViewmodel() {
        rcfirebaseAuth = FirebaseAuth.getInstance()
        user = rcfirebaseAuth?.currentUser
        tokenHelper = TokenHelper(user)

        viewModel = ViewModelProvider(
            this,
            SearchInjectorUtil.provideUserInjectorUtil(applicationContext, tokenHelper)
        ).get(
            SearchViewModel::class.java
        )

        generalBlockViewmodel = ViewModelProvider(
            this,
            GeneralBlockInjectorUtil.provideViewModel(this, phoneNumber)
        ).get(
            GeneralblockViewmodel::class.java
        )

        incommingCallViewUpdatedModel = ViewModelProvider(
            this,
            IncommingCallInjectorUtil.provideFactory(application, tokenHelper)
        )[IncommingCallViewUpdatedModel::class.java]

    }

    private fun getIntentxras(intent: Intent) {
//        var firstName = intent.getStringExtra(FIRST_NAME)?:""
//        val lastName = intent.getStringExtra(LAST_NAME)?:""
        phoneNumber = intent.getStringExtra(PHONE_NUMBER) ?: ""
//        val spamcount = intent.getIntExtra(SPAM_COUNT, 0)
//        val location  = intent.getStringExtra(LOCATION)?:""
//        val carrier  = intent.getStringExtra(CARRIER)?:""
//        userInfo = CntctitemForView(
//            firstName= firstName,
//            lastName = l
//        )

        showfeedbackView = intent.getBooleanExtra(SHOW_FEEDBACK_VIEW, false)
//        statusCode = intent.getIntExtra(STATUS_CODE, STATUS_SEARHING_IN_PROGRESS)
    }

    private fun configurePopupActivity() {
        /**
         * important to setLayout outherwise activity goes full screen
         */

        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));


//        this.setFinishOnTouchOutside(true)
    }


    private fun initListeners() {

        binding.txtVcallerName.setOnClickListener(this)
        binding.thumbsDownButton.setOnClickListener(this)
        binding.thumbsUpButton.setOnClickListener(this)

        binding.applySuggestionButton.applySuggestionButton.setOnClickListener(this)

        binding.imgBtnCloseIncommin.setOnClickListener(this)
        binding.blockButton.setOnClickListener(this)
        binding.callButton.setOnClickListener(this)
        binding.detailsButton.setOnClickListener(this)
//
        radioBusiness?.setOnClickListener(this)
        radioPerson?.setOnClickListener(this)
        radioSales?.setOnClickListener(this)
        radioScam?.setOnClickListener(this)

        btnBlock?.setOnClickListener(this)

        binding.suggestedNameEdittext.doOnTextChanged { text, _, _, count ->
            if (text != null) {

                when {
                    text.length > 100 -> {
                        binding.textInputLayout3.isErrorEnabled = true
                        binding.textInputLayout3.error = "Name too long"
                        binding.applySuggestionButton.apply {
                            visibility = View.GONE
                        }
                    }
                    text.length >= 3 -> {
                        binding.textInputLayout3.isErrorEnabled = false
                        binding.txtVcallerName.text = text
                        binding.applySuggestionButton.apply {
                            visibility = View.VISIBLE
                        }
                    }
                    else -> {
                        binding.txtVcallerName.text = callersInfo?.firstName ?: phoneNumber
                        binding.applySuggestionButton.apply {
                            visibility = View.GONE
                        }
                    }
                }
            }

        }

    }


    @SuppressLint("LongLogTag")
    override fun onClick(v: View?) {
        Log.d(TAG, "onClick: ")
        when (v?.id) {

            R.id.applySuggestionButton -> {
                hideKeyboard(binding.suggestedNameEdittext)
                incommingCallViewUpdatedModel.suggestName(
                    binding.suggestedNameEdittext.text.toString(),
                    phoneNumber
                )
                binding.suggestCard.hideAnim()
            }

            R.id.thumbsDownButton -> {
                binding.suggestCard.showAnim(600)
                binding.txtVcallerName.post {
                    showKeyboard(binding.suggestedNameEdittext)
                }
                binding.thumbsDownButton.scaleOutAnim(500)

                if (callersInfo != null) {
                    val name = callersInfo!!.firstName + " " + callersInfo!!.lastName
                    incommingCallViewUpdatedModel.upvote(name, phoneNumber)
                    binding.helpfulMessage.animateColor(800L, endColor = 0xffFFEBEE.toInt())
                        .addListener(object : Animator.AnimatorListener {
                            override fun onAnimationCancel(animation: Animator?) {}
                            override fun onAnimationEnd(animation: Animator?) {
                                helpfulMessage.slideBelowHide()

                            }

                            override fun onAnimationRepeat(animation: Animator?) {}
                            override fun onAnimationStart(animation: Animator?) {}
                        })
                }


            }
            R.id.txtVcallerName -> {
                binding.suggestCard.showAnim()
                binding.txtVcallerName.post {
                    showKeyboard(binding.suggestedNameEdittext)
                }
            }

            R.id.thumbsUpButton -> {

                binding.thumbsUpButton.scaleOutAnim(600)

                if (callersInfo != null) {
                    val name = callersInfo!!.firstName + " " + callersInfo!!.lastName
                    incommingCallViewUpdatedModel.upvote(name, phoneNumber)
                    binding.helpfulMessage.animateColor(800L, endColor = 0xffC8E6C9.toInt())
                        .addListener(object : Animator.AnimatorListener {
                            override fun onAnimationCancel(animation: Animator?) {}
                            override fun onAnimationEnd(animation: Animator?) {
                                helpfulMessage.slideBelowHide()

                            }

                            override fun onAnimationRepeat(animation: Animator?) {}
                            override fun onAnimationStart(animation: Animator?) {}
                        })
                }


            }

            R.id.imgBtnCloseIncommin -> {
                Log.d(TAG, "onClick: btn")
                closeActivity()
//                reportuser()
            }
            R.id.blockButton -> {
               showBottomSheetDialog()
            }

            R.id.callButton -> {
                if (EasyPermissions.hasPermissions(this, Manifest.permission.CALL_PHONE)) {
                    makeCall("+${formatPhoneNumber(phoneNumber)}")
                    finishAfterTransition()
                } else {
                    requestCallPhonePermission()
                }

            }
            R.id.radioScam, R.id.radioSales, R.id.radioPerson, R.id.radioBusiness -> {
//                toast("radio scan clicked")
                radioClicked(v as RadioButton)

            }
            R.id.detailsButton -> {
                val intent = Intent(this, IndividualContactViewActivity::class.java)
                intent.putExtra(CONTACT_ID, phoneNumber)
                startActivity(intent)
                finishAfterTransition()
            }
            R.id.btnBlock -> {
                blockThisAddress()
            }
        }
    }

    private fun blockThisAddress() {
        generalBlockViewmodel.blockThisAddress(
            spammerType = spammerType,
            phoneNumber,
            applicationContext
        )
            .observe(this, Observer {
                when (it) {
                    ON_COMPLETED -> {
                        bottomSheetDialog.hide()
                        bottomSheetDialog.dismiss()
                        bottomSheetDialogfeedback.show()
                    }
                }
            })
    }

    private fun radioClicked(v: RadioButton) {
        if (v is RadioButton) {
            when (v.id) {
                R.id.radioScam -> {
                    val checked = v.isChecked
                    if (checked) {
                        selectedRadioButton = radioScam
                        this.spammerType = SPAMMER_TYPE_SCAM

//                                spinnerSelected.value = fals

                    }
                }
                R.id.radioSales -> {

                    val checked = v.isChecked
                    if (checked) {
                        selectedRadioButton = radioSales
                        this.spammerType = SPAMMER_TYPE_SALES
//                                spinnerSelected.value = false

                    }
                }
                R.id.radioBusiness -> {
                    val checked = v.isChecked
                    if (checked) {
                        spammerType = Constants.SPAMMER_TYPE_BUSINESS
                    }
                }
                R.id.radioPerson -> {
                    val checked = v.isChecked
                    if (checked) {
                        spammerType = SPAMMER_TYPE_PEERSON

                    }
                }
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
//        bottomSheetDialog.imgExpand.setOnClickListener(this)

        radioBusiness = bottomSheetDialog.findViewById<RadioButton>(R.id.radioBusiness)
        radioPerson = bottomSheetDialog.findViewById<RadioButton>(R.id.radioPerson)
        radioSales = bottomSheetDialog.findViewById<RadioButton>(R.id.radioSales)
        radioScam = bottomSheetDialog.findViewById<RadioButton>(R.id.radioScam)
        previousCheckedRadioButton = radioScam
        btnBlock = bottomSheetDialog.findViewById<Button>(R.id.btnBlock)

        bottomSheetDialog.setOnDismissListener {
            Log.d(TAG, "bottomSheetDialogDismissed")

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
        if (mMessageReceiver != null) {

            unregisterReceiver(mMessageReceiver)

        }
    }

    @SuppressLint("LongLogTag")
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
        isVisible = null

    }

    companion object {


        // property to check whether the Activity is visible,
        var isVisible: Boolean? = false
        private const val TAG = "__ActivityIncommingCallView"
    }
}