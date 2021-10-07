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
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.hashcaller.app.R
import com.hashcaller.app.databinding.ActivityIncommingCallViewUpdatedBinding
import com.hashcaller.app.datastore.DataStoreRepository
import com.hashcaller.app.datastore.PreferencesKeys.Companion.SPAM_THRESHOLD
import com.hashcaller.app.local.db.blocklist.BlockTypes.Companion.BLOCK_TYPE_FROM_CALL_LOG
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
import com.hashcaller.app.utils.constants.IntentKeys.Companion.NAME_IN_SERVER_PHONE_BOOK
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
import com.hashcaller.app.view.ui.contacts.toggleUserBadge
import com.hashcaller.app.view.ui.contacts.utils.CONTACT_ID
import com.hashcaller.app.view.ui.contacts.utils.loadImage
import com.hashcaller.app.view.ui.sms.individual.util.*
import com.hashcaller.app.view.utils.*
import com.hashcaller.app.work.formatPhoneNumber
import com.vmadalin.easypermissions.EasyPermissions
import kotlinx.android.synthetic.main.activity_incomming_call_view_updated.*
import kotlinx.android.synthetic.main.activity_incomming_call_view_updated.view.*
import kotlinx.android.synthetic.main.bottom_sheet_block.*
import kotlinx.coroutines.delay
import java.util.*


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
    private var fullNameInCP: String = ""
    private var fullNameserver: String = ""
    private var nameInServerPhoneBook: String=""

    private var imageFromCp: String = ""
    private var imageFromDB: String = ""
    private var avatarGoogle: String = ""
    private var hUid: String = ""
    private var spamCount: Long = 0L
    private var isVerifiedUser:Boolean = false
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
    private var radioGroupOne: RadioGroup? = null
    private var radioGroupTwo: RadioGroup? = null
    private var finalName = ""


    //    private var country = ""
    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSpamThreshold()
        isVisible = true
        callHandledState = intent.getStringExtra(CALL_HANDLED_STATE) ?: ""
        registerForBroadCastReceiver()

        binding = ActivityIncommingCallViewUpdatedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getIntentxras(intent)

        stopFltinServiceFromActiivtyIncomming()
        configurePopupActivity()
        initViewmodel()
        setupBottomSheet()
        initListeners()
        phoneNumber = intent.getStringExtra(PHONE_NUMBER) ?: ""
        callHandledSim = intent.getIntExtra(IntentKeys.CALL_HANDLED_SIM, NO_SIM_DETECTED)
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
            binding.actionsCard.beVisible()
            binding.actionsCard.topToBottomAnim(500L, 100f)
            if(!isVerifiedUser){
                delay(300)
                binding.helpfulMessage.beVisible()
                binding.helpfulMessage.bottomToTopAnim(500L, 500f)
            }else {
                binding.helpfulMessage.beGone()
            }

        }

    }

    private fun getIntentxras(intent: Intent) {

        phoneNumber = intent.getStringExtra(PHONE_NUMBER) ?: ""
        fullNameInCP = intent.getStringExtra(IntentKeys.FULL_NAME_IN_C_PROVIDER) ?: ""
        fullNameserver = intent.getStringExtra(IntentKeys.FULL_NAME_FROM_SERVER) ?: ""
        nameInServerPhoneBook = intent.getStringExtra(NAME_IN_SERVER_PHONE_BOOK)?:""
        imageFromCp = intent.getStringExtra(IntentKeys.THUMBNAIL_FROM_CPROVIDER) ?: ""
        imageFromDB = intent.getStringExtra(IntentKeys.THUMBNAIL_FROM_DB) ?: ""
        avatarGoogle = intent.getStringExtra(IntentKeys.AVATAR_GOOGLE) ?: ""
        hUid = intent.getStringExtra(IntentKeys.H_UID) ?: ""
        showfeedbackView = intent.getBooleanExtra(SHOW_FEEDBACK_VIEW, false)
        isVerifiedUser = intent.getBooleanExtra(IntentKeys.IS_VERIFIED_USER, false)
        intent.getLongExtra(IntentKeys.SPAM_COUNT, 0L)

        val contact = CntctitemForView(informationReceivedDate = Date())
        contact.nameInLocalPhoneBook = fullNameInCP
        contact.phoneNumber = phoneNumber
        contact.fullNameServer = fullNameserver
        contact.nameInPhoneBook = nameInServerPhoneBook
        contact.thumbnailImgCp = imageFromCp
        contact.thumbnailImgServer = imageFromDB
        contact.avatarGoogle = avatarGoogle
        contact.hUid = hUid
        contact.avatarGoogle = avatarGoogle
        contact.isVerifiedUser = isVerifiedUser

        setViewContent(contact)



//        statusCode = intent.getIntExtra(STATUS_CODE, STATUS_SEARHING_IN_PROGRESS)
    }
    private fun setViewContent(contact: CntctitemForView) {
        with(binding){
            tvPhoneNumIncomming.text = phoneNumber

            if(contact.nameInLocalPhoneBook.isNotEmpty()){
                finalName = contact.nameInLocalPhoneBook

            }else if(contact.fullNameServer.isNotEmpty()){
                finalName = contact.fullNameServer

            }else if(contact.nameInPhoneBook.isNotEmpty()){
                finalName = contact.nameInPhoneBook
            }else {
                finalName = formatPhoneNumber(phoneNumber)
            }
            txtVcallerName.text = finalName
            if(contact.thumbnailImgCp.isNotEmpty()){
                imgVAvatarIncomming.beVisible()
                tvFirstLetter.beInvisible()
                loadImage(this@ActivityIncommingCallViewUpdated, imgVAvatarIncomming, contact.thumbnailImgCp)
            }else if(contact.thumbnailImgServer.isNotEmpty()){
                imgVAvatarIncomming.beVisible()
                tvFirstLetter.beInvisible()
                imgVAvatarIncomming.setImageBitmap(getDecodedBytes(contact.thumbnailImgServer))
            }else if(contact.avatarGoogle.isNotEmpty()){
                imgVAvatarIncomming.beVisible()
                tvFirstLetter.beInvisible()
                Glide.with(this@ActivityIncommingCallViewUpdated).load(contact.avatarGoogle)
                    .into(imgVAvatarIncomming)
            }else {
//                imgVAvatarIncomming.beInvisible()
                tvFirstLetter.beVisible()
                imgVAvatarIncomming.setImageDrawable(ContextCompat.getDrawable(this@ActivityIncommingCallViewUpdated, R.drawable.circular_avatar_main_background))
                imgVAvatarIncomming.beVisible()
                binding.tvFirstLetter.text = finalName[0].toString()
            }
            if(contact.isVerifiedUser){
                txtVcallerName.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(this@ActivityIncommingCallViewUpdated, R.drawable.ic_baseline_verified_2), null)
            }else if(contact.hUid.isNotEmpty()){
                //registed user
                txtVcallerName.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                
            }else {
                txtVcallerName.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(this@ActivityIncommingCallViewUpdated, R.drawable.ic_baseline_edit_18_white), null)
            }


            toggleUserBadge(imgUserIconBg, imgUserIcon, contact.hUid)

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
            if (contact.spammCount > spamThreshold) {
                Log.d(TAG, "onCreate: spammer calling");
                materialCardView.setCardBackgroundColor(getColor(R.color.spamText))
            } else {
                materialCardView.setCardBackgroundColor(0xff3398ED.toInt())

            }

//            if(contact.nameInLocalPhoneBook.isNotEmpty()){
//                Log.d(TAG, "setViewContent:nameInLocalPhoneBook not empty ")
//                helpfulMessage.beGone()
//            }else {
//                Log.d(TAG, "setViewContent:nameInLocalPhoneBook is empty ")
//                helpfulMessage.beVisible()
//            }
        }
    }

    private fun checkIfUserBlockedThisNumber() {
        viewModel?.isthisNumberBlocked(phoneNumber)
    }


    private fun getCallerInfo() {
        viewModel.getCallerInfo(phoneNumber).observe(this, Observer {
            if (!it.shoudlSearchInServer) {
//                setViewElements(it)
                setViewContent(it)
            }else {
                viewModel.getCallerInfoFromServer(phoneNumber).observe(this, Observer {searchRes->
                    searchRes?.let { it1 ->
                        setViewContent(it1)
//                        setViewElements(it1)
                    }
                })
            }

        })
    }

    @SuppressLint("LongLogTag")
    private fun setViewElements(callersInfo: CntctitemForView) {
        setViewContent(callersInfo)
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
            GeneralBlockInjectorUtil.provideViewModel(this)
        ).get(
            GeneralblockViewmodel::class.java
        )

        incommingCallViewUpdatedModel = ViewModelProvider(
            this,
            IncommingCallInjectorUtil.provideFactory(application, tokenHelper)
        )[IncommingCallViewUpdatedModel::class.java]

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
                if(hUid.isEmpty()){
                    //only a non registerd user
                    binding.suggestCard.showAnim()
                    binding.txtVcallerName.post {
                        showKeyboard(binding.suggestedNameEdittext)
                    }
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
                intent.putExtra(IntentKeys.INTENT_SOURCE, BLOCK_TYPE_FROM_CALL_LOG)
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
            contactAddress = phoneNumber,
            applicationContext = applicationContext,
            intentSource = BLOCK_TYPE_FROM_CALL_LOG,
            name= finalName
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
                    radioGroupTwo?.clearCheck()
                    val checked = v.isChecked
                    if (checked) {
                        selectedRadioButton = radioScam
                        this.spammerType = SPAMMER_TYPE_SCAM

//                                spinnerSelected.value = fals

                    }
                }
                R.id.radioSales -> {
                    radioGroupTwo?.clearCheck()
                    val checked = v.isChecked
                    if (checked) {
                        selectedRadioButton = radioSales
                        this.spammerType = SPAMMER_TYPE_SALES
//                                spinnerSelected.value = false

                    }
                }
                R.id.radioBusiness -> {
                    radioGroupOne?.clearCheck()
                    val checked = v.isChecked
                    if (checked) {
                        spammerType = Constants.SPAMMER_TYPE_BUSINESS
                    }
                }
                R.id.radioPerson -> {
                    radioGroupOne?.clearCheck()
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

        radioGroupOne = bottomSheetDialog.findViewById<RadioGroup>(R.id.radioGroupOne) as RadioGroup
        radioGroupTwo = bottomSheetDialog.findViewById<RadioGroup>(R.id.radioPersonOrBusiness) as RadioGroup
        selectedRadioButton = radioScam
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