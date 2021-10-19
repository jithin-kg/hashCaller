package com.hashcaller.app.view.ui.contacts.individualContacts

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.hashcaller.app.R
import com.hashcaller.app.databinding.ActivityIndividualCotactViewBinding
import com.hashcaller.app.databinding.BottomSheetBlockBinding
import com.hashcaller.app.databinding.BottomSheetSuggestBinding
import com.hashcaller.app.datastore.DataStoreRepository
import com.hashcaller.app.datastore.PreferencesKeys
import com.hashcaller.app.local.db.blocklist.BlockTypes
import com.hashcaller.app.local.db.blocklist.BlockTypes.Companion.BLOCK_TYPE_FROM_CALL_LOG
import com.hashcaller.app.utils.Constants
import com.hashcaller.app.utils.constants.IntentKeys
import com.hashcaller.app.utils.extensions.requestCallPhonePermission
import com.hashcaller.app.utils.notifications.tokeDataStore
import com.hashcaller.app.view.ui.MyUndoListener
import com.hashcaller.app.view.ui.blockConfig.GeneralBlockInjectorUtil
import com.hashcaller.app.view.ui.blockConfig.GeneralblockViewmodel
import com.hashcaller.app.view.ui.contacts.individualContacts.ThumbnailImageData.Companion.IMAGE_FOUND_FROM_C_PROVIDER
import com.hashcaller.app.view.ui.contacts.individualContacts.ThumbnailImageData.Companion.IMAGE_FOUND_FROM_DB
import com.hashcaller.app.view.ui.contacts.individualContacts.ThumbnailImageData.Companion.IMAGE_FOUND_FROM_DB_GOOGLE
import com.hashcaller.app.view.ui.contacts.individualContacts.utils.IndividualContactInjectorUtil
import com.hashcaller.app.view.ui.contacts.individualContacts.utils.IndividualcontactViewModel
import com.hashcaller.app.view.ui.contacts.makeCall
import com.hashcaller.app.view.ui.contacts.toggleVerifiedBadge
import com.hashcaller.app.view.ui.contacts.utils.*
import com.hashcaller.app.view.ui.contacts.utils.CONTACT_ID
import com.hashcaller.app.view.ui.extensions.getMyPopupMenu
import com.hashcaller.app.view.ui.extensions.setRandomBackgroundCircle
import com.hashcaller.app.view.ui.extensions.startContactEditActivity
import com.hashcaller.app.view.ui.sms.individual.IndividualSMSActivity
import com.hashcaller.app.view.ui.sms.individual.util.*
import com.hashcaller.app.view.utils.getDecodedBytes
import com.hashcaller.app.view.utils.scaleOutAnim
import com.hashcaller.app.work.formatPhoneNumber
import com.vmadalin.easypermissions.EasyPermissions
import kotlinx.coroutines.delay


class IndividualContactViewActivity : AppCompatActivity(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener, MyUndoListener.SnackBarListner,
    PopupMenu.OnMenuItemClickListener {
    //devhashcaller@gmail.com : newDevHashCaller@535
    private lateinit var binding:ActivityIndividualCotactViewBinding
    private lateinit var viewModel:IndividualcontactViewModel
    private lateinit var generalBlockViewmodel: GeneralblockViewmodel
    private lateinit var photoURI:String
    private  var color  = 1
    private var prevColor = 1
    private var phoneNum:String = ""
    private var name:String = ""
    private var count  = 0
    private var isBlocked = false
    private lateinit var bottomSheetDialogfeedback: BottomSheetDialog
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var bottmSheetSuggest: BottomSheetDialog
    private  var selectedRadioButton: RadioButton? = null
    private  var spammerType:Int = SPAMMER_TYPE_SCAM
    private var intentSource : Int = BLOCK_TYPE_FROM_CALL_LOG
//    private lateinit var imgExpand:ImageView
    private lateinit var radioScam:RadioButton
    private lateinit var radioSales:RadioButton
    private lateinit var radioBusiness:RadioButton
    private lateinit var radioPerson:RadioButton
    private var radioGroupOne: RadioGroup? = null
    private var radioGroupTwo: RadioGroup? = null

    private lateinit var btnBlock:Button
    private lateinit var tvSpamfeedbackMsg : TextView
    private lateinit var tvblockedFeedback : TextView
    private var  popup: PopupMenu? = null
    private var finalName = ""
    private lateinit var bindingSuggest : BottomSheetSuggestBinding
    @SuppressLint("LongLogTag")
//    private  var contactId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        binding = ActivityIndividualCotactViewBinding.inflate(layoutInflater)
//        setStatusBarColor(this, )
//        setStatusBarColorRes(R.color.colorBackground)


        setDataStoreValues()
        setContentView(binding.root)
        getIntentExtras()
        setupBottomSheet()
        initListeners()
        initViewmodel()
        viewModel.getContactsFromDb(phoneNum)
        getContactMutedInformation()
        setClearImage(photoURI)
        getAggregatedContactInfo()
        obseveContactForView()
        observeAllBlockedList()
        observeIsthisNumberBlocked()

    }
    private fun setDataStoreValues() {
        lifecycleScope.launchWhenCreated {
            SPAM_THRESHOLD_VALUE = DataStoreRepository(this@IndividualContactViewActivity.tokeDataStore).getInt(
                PreferencesKeys.SPAM_THRESHOLD)?: Constants.DEFAULT_SPAM_THRESHOLD
        }
    }

    private fun observeIsthisNumberBlocked() {
        generalBlockViewmodel.isThisNumberBlocked.observe(this, Observer { isNumberBlocked->
            Log.d(TAG, "observeIsthisNumberBlocked: $isNumberBlocked")
            if(isNumberBlocked){
                binding.btnBlockIndividualContact.beGone()
                binding.btnUnblock.beVisible()
                color = TYPE_SPAM
                setClearImage(photoURI)
                setSpamTheme()
//                popup?.menu?.findItem(R.id.itemUnblockNumber)?.isVisible = true
                
            } else {
                setNormalTheme()
                binding.btnBlockIndividualContact.beVisible()
                binding.btnUnblock.beGone()

                if(prevColor != TYPE_SPAM ){
                    color = prevColor
                }else {
                    prevColor = getRandomColor()
                    color = prevColor
                }
                setClearImage(photoURI)
//                popup?.menu?.findItem(R.id.itemUnblockNumber)?.isVisible = false
//                binding.btnBlockIndividualContact.beVisible()
            }
        })

    }

    private fun setNormalTheme() {
        val window = window;
// clear FLAG_TRANSLUCENT_STATUS flag:
//        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
// finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorBackgroundAppBar))
        binding.imgVSpamHead.setBackgroundColor( ContextCompat.getColor(this, R.color.colorBackgroundAppBar))
    }

    private fun setSpamTheme() {
        val window =getWindow();

// clear FLAG_TRANSLUCENT_STATUS flag:
//        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

// finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this,R.color.spamText))
        binding.imgVSpamHead.setBackgroundColor( ContextCompat.getColor(this, R.color.spamText))

    }

    private fun observeAllBlockedList() {
        generalBlockViewmodel.allBlockListLivedata?.observe(this, Observer {
            generalBlockViewmodel.updateBlockListOfIndividual(it, phoneNum)
        })
    }



    private fun getAggregatedContactInfo() {
        viewModel.getAgregatedContactInformation(phoneNum)
    }

    private fun obseveContactForView() {
        viewModel.contactForViewLivedata.observe(this, Observer {
            if(it.isInInContacts){
                binding.tvisInContact.text = "This person is in your contact"
            }else {
                binding.tvisInContact.text = "This person is not in your contact"
            }
            if(it.spammCount > SPAM_THRESHOLD_VALUE){
                setSpamTheme()
                binding.layoutSpamCountt.beVisible()
                binding.tvSpamCountValue.text = it.spammCount.toString()
            }else {
                setNormalTheme()
                binding.layoutSpamCountt.beGone()
            }

//            binding.tvFirstLetter.text = it.firstName[0].toString()
            var nameOfContact = ""
            if(it.hUid.isNotEmpty() &&  (it.firstName.isNotEmpty() || it.lastName.isNotEmpty())){
                nameOfContact += it.firstName
                if(it.lastName.isNotEmpty()){
                    nameOfContact += " "+ it.lastName
                }
            }else if(it.nameInLocalPhoneBook.isNotEmpty()){
                nameOfContact = it.nameInLocalPhoneBook
            }else if(it.nameInPhoneBook.isNotEmpty()){
                nameOfContact = it.nameInPhoneBook
            }else {
                nameOfContact = it.phoneNumber
            }
            if(nameOfContact.isNotEmpty()){
                binding.tvName.text = nameOfContact
                finalName = nameOfContact
                binding.tvNameSmall.text = nameOfContact
                setFirstLetter(nameOfContact)

            }
//            binding.tvName.text = nameOfContact

            finalName = name
            binding.tvLocationValues.text = it.country + " " + it.location
            binding.tvLocationValues.text = it.spammCount.toString()
//            if(it.firstName==phoneNum){
//                binding.layoutNumber.beGone()
//            }

            if(it.hUid.isNotEmpty()){
                binding.imgUserIconBg.beVisible()
                binding.imgUserIcon.beVisible()
            }else {
                binding.imgUserIcon.beInvisible()
                binding.imgUserIconBg.beInvisible()
            }
            toggleVerifiedBadge(binding.imgVerifiedBadge, it.isVerifiedUser)
            setClearImage(null)

            hideLastSmallDivider()
        })
    }

    private fun hideLastSmallDivider() {
        var visibleDividers:MutableList<View> = mutableListOf()
        with(binding){
            if(imgVDivider1.visibility == View.VISIBLE)
                visibleDividers.add(imgVDivider1)
            if(imgVDivider2.visibility == View.VISIBLE)
                visibleDividers.add(imgVDivider2)
            if(imgVDivider3.visibility == View.VISIBLE)
                visibleDividers.add(imgVDivider3)
            if(imgVDivider4.visibility == View.VISIBLE)
                visibleDividers.add(imgVDivider4)
            if(imgVDivider5.visibility == View.VISIBLE)
                visibleDividers.add(imgVDivider5)

         if(visibleDividers.isNotEmpty()){
             visibleDividers[visibleDividers.size - 1].beInvisible()
         }
        }



//        val listDivider =
    }

    private fun setFirstLetter(nameOfContact: String) {
        var firstLetter = ""
        if(nameOfContact == phoneNum){
             firstLetter = formatPhoneNumber(nameOfContact)[0].toString()

        }else {
            firstLetter = nameOfContact[0].toString()
        }
        binding.tvFirstLetter.text = firstLetter
    }

    private fun getIntentExtras() {
        phoneNum = intent.getStringExtra(CONTACT_ID)?:""
//        +918848233258 normal
        Log.d(TAG, "getIntentExtras: $phoneNum")
         name = intent.getStringExtra("name")?:""
        if(name.isEmpty()){
            name = phoneNum
        }
//        val id = intent.getLongExtra("id",0L)
        photoURI = intent.getStringExtra("photo")?:""
        color = intent.getIntExtra("color", 1)
        prevColor = color
        binding.tvNumberValue.post {
            binding.tvNumberValue.text = phoneNum
        }
        intentSource = intent.getIntExtra(IntentKeys.INTENT_SOURCE,BLOCK_TYPE_FROM_CALL_LOG)
    }

    private fun initViewmodel() {
        IndividualContactInjectorUtil.phoneNumber = phoneNum
        viewModel =ViewModelProvider(
            this, IndividualContactInjectorUtil.provideUserInjectorUtil(
                this,phoneNum, lifecycleScope
            )
        ).get(
            IndividualcontactViewModel::class.java
        )

        generalBlockViewmodel = ViewModelProvider(this, GeneralBlockInjectorUtil.provideViewModel(
            this
         )).get(GeneralblockViewmodel::class.java)
    }
    @SuppressLint("LongLogTag")
    private fun setClearImage(photoURI: String?) {
//        binding.tvName.text = name
//        finalName = name
//        binding.tvNameSmall.text = name

//        binding.txtViewNumber.text = phoneNum

//        if(!photoURI.isNullOrEmpty()){
            viewModel.getClearImage(phoneNum).observe(this, Observer {
                when(it.imageFoundFrom){
                    IMAGE_FOUND_FROM_C_PROVIDER ->{
                        loadImage(this, binding.ivAvatar, it.imageStr)
                        binding.tvFirstLetter.beGone()
                        //because when using motin layout somehow  unable to set  invisible visibility to tvFirstLetter
                        binding.tvFirstLetter.text = ""
                    }
                    IMAGE_FOUND_FROM_DB ->{
                        binding.ivAvatar.beVisible()
                        binding.ivAvatar.setImageBitmap(getDecodedBytes(it.imageStr))
                        binding.tvFirstLetter.text = ""
                        binding.tvFirstLetter.beInvisible()
                    }
                    IMAGE_FOUND_FROM_DB_GOOGLE -> {
                        binding.ivAvatar.beVisible()
                        Glide.with(this).load(it.avatarGoogle)
                            .into(binding.ivAvatar)
                        binding.tvFirstLetter.text = ""
                        binding.tvFirstLetter.beInvisible()
                    }
                    else->{
                        binding.tvFirstLetter.setRandomBackgroundCircle(color)
                        binding.ivAvatar.beInvisible()
                        if(finalName.isNotEmpty()){
                            if(color == TYPE_SPAM){
                                binding.tvFirstLetter.text = ""
                            }else {
                                setFirstLetter(finalName)
                                binding.tvFirstLetter.beVisible()
                            }
                        }

                    }
                }
            })
//        }
    }

    private fun setupBottomSheet() {
        bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialogfeedback = BottomSheetDialog(this)
        val viewSheet = BottomSheetBlockBinding.inflate(layoutInflater)
//        val viewSheet = layoutInflater.inflate(R.layout.bottom_sheet_block, null)
//        val viewSheet = layoutInflater.inflate(R.layout.bottom_sheet_block, null)

        val viewSheetFeedback = layoutInflater.inflate(R.layout.bottom_sheet_block_feedback, null)

        bottomSheetDialog.setContentView(viewSheet.root)
        bottomSheetDialogfeedback.setContentView(viewSheetFeedback)
        tvSpamfeedbackMsg = bottomSheetDialogfeedback.findViewById<TextView>(R.id.tvSpamfeedbackMsg) as TextView
        tvblockedFeedback = bottomSheetDialogfeedback.findViewById<TextView>(R.id.tvblocked_feedback) as TextView

//        imgExpand = bottomSheetDialog.findViewById<ImageView>(R.id.imgExpand) as ImageView
        radioScam = bottomSheetDialog.findViewById<RadioButton>(R.id.radioScam) as RadioButton
        radioSales = bottomSheetDialog.findViewById<RadioButton>(R.id.radioSales) as RadioButton
        radioBusiness = bottomSheetDialog.findViewById<RadioButton>(R.id.radioBusiness) as RadioButton
        radioPerson = bottomSheetDialog.findViewById<RadioButton>(R.id.radioPerson) as RadioButton

        radioGroupOne = bottomSheetDialog.findViewById<RadioGroup>(R.id.radioGroupOne) as RadioGroup
        radioGroupTwo = bottomSheetDialog.findViewById<RadioGroup>(R.id.radioPersonOrBusiness) as RadioGroup

        btnBlock = bottomSheetDialog.findViewById<Button>(R.id.btnBlock) as Button
        selectedRadioButton = radioScam

        //bottom sheet suggest
        bindingSuggest = BottomSheetSuggestBinding.inflate(layoutInflater)
        bottmSheetSuggest = BottomSheetDialog(this)
        bottmSheetSuggest.setContentView(bindingSuggest.root)

//        imgExpand.setOnClickListener(this)


//        if(this.view?.visibility == View.VISIBLE){
//            bottomSheetDialog.hide()

//        }

//        bottomSheetDialog.setOnDismissListener {
//            Log.d(IndividualSMSActivity.TAG, "bottomSheetDialogDismissed")
//
//        }
    }



    private fun getContactMutedInformation() {

        viewModel.mutedContacts.observe(this, Observer { lst ->
            viewModel.isThisAddressMuted(phoneNum, lst).observe(this, Observer {
                binding.switchIndividualContact.isChecked = it
            })
        })
    }

    @SuppressLint("LongLogTag")
    override fun onBackPressed() {
//        this.finishAfterTransition()
//        super.onBackPressed()
        finishAfterTransition()
    }



//    private fun getMoreInfoForNumber(phoneNum: String?) {
//        viewModel.getMoreInfoforNumber(phoneNum)
//    }

    private fun initListeners() {


        with(binding){
            switchIndividualContact.setOnClickListener(this@IndividualContactViewActivity)
            btnBlockIndividualContact.setOnClickListener(this@IndividualContactViewActivity)
            imgBtnBack.setOnClickListener(this@IndividualContactViewActivity)
            btnUnblock.setOnClickListener(this@IndividualContactViewActivity)
            imgBtnCallindividual.setOnClickListener(this@IndividualContactViewActivity)
            imgBtnMoreIndividualCntct.setOnClickListener(this@IndividualContactViewActivity)
            layoutNumber.setOnLongClickListener {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("label",phoneNum)
                clipboard.setPrimaryClip(clip)
                toast("Number copied to Clipboard")
                return@setOnLongClickListener true
            }
            thumbsUpButton.setOnClickListener {
                it.scaleOutAnim(600)
            }
            thumbsDownButton.setOnClickListener {
                it.scaleOutAnim(600)
                lifecycleScope.launchWhenStarted {
                    delay(600)
                    bottmSheetSuggest.show()
                }

            }


        }
        radioSales.setOnClickListener(this@IndividualContactViewActivity)
        radioScam.setOnClickListener(this@IndividualContactViewActivity)
        radioBusiness.setOnClickListener(this@IndividualContactViewActivity)
        radioPerson.setOnClickListener(this@IndividualContactViewActivity)
        btnBlock.setOnClickListener(this@IndividualContactViewActivity)



    }

    private fun setImage(photoUri: String?) {
        binding.ivAvatar.beVisible()
        binding.tvFirstLetter.beInvisible()

        if(!photoUri.isNullOrEmpty()){
            loadImage(this, binding.ivAvatar, photoUri)
            binding.tvFirstLetter.beInvisible()
        }else{
//            binding.ivAvatar.beInvisible()
//            binding.tvFirstLetter.beVisible()
        }
    }





    @SuppressLint("LongLogTag")
    override fun onClick(v: View?) {

        when(v?.id){
            R.id.btnBlockIndividualContact -> {

                if (!isBlocked) {
                    showBottomSheetDialog()
                }

//                else {
//                    reportSpamAndblock()
//                }


            }
            R.id.imgBtnMoreIndividualCntct -> {
                popup =  getMyPopupMenu(R.menu.individual_contact_popup_menu,binding.imgBtnMoreIndividualCntct)
                popup?.setOnMenuItemClickListener(this)
                popup?.show()

            }
            R.id.imgBtnBack -> {
                finishAfterTransition()
            }
            R.id.imgBtnCallindividual -> {
                if(EasyPermissions.hasPermissions(this, Manifest.permission.CALL_PHONE)){
                    makeCall(phoneNum)
                }else {
                    requestCallPhonePermission()
                }
            }
//            R.id.imgBtnSMS -> {
//                startIndividualSMS()
//            }
            R.id.switchIndividualContact -> {
                muteOrUnmute()
            }
            R.id.btnBlock -> {
                blockThisAddress()
            }
            R.id.btnUnblock -> {
                unblockThisAddres()
            }
            R.id.layoutNumber -> {

            }
            else -> {
            this.radioButtonClickPerformed(v)
            }
//            R.id.imgViewAvatar->{
//               popupImage()
//            }
        }
    }

    private fun unblockThisAddres() {

        generalBlockViewmodel.removeFromBlockList(phoneNum,
            BlockTypes.BLOCK_TYPE_EXACT_NUMBER,
            getRandomColor(),
            applicationContext
        )
            if(finalName.isEmpty())
                finalName = phoneNum
            toast("You have unblocked  $finalName")
       
    }



    /**
     * called when user clicks block button in bottom sheet
     */
    private fun blockThisAddress() {
        //todo, while saving spam count in chat threads the spam count is having large number, fix it
        generalBlockViewmodel.blockThisAddress(
            spammerType,
            phoneNum,
            applicationContext,
            intentSource,
            finalName
        ).observe(this, Observer {
            when(it){
                ON_COMPLETED -> {
                    bottomSheetDialog.hide()
                    bottomSheetDialog.dismiss()
                    if(finalName.isEmpty())
                        finalName = phoneNum
                    tvblockedFeedback.text = "All Calls from $finalName will be blocked"
                    bottomSheetDialogfeedback.show()
                }
            }
        })
    }
    private fun radioButtonClickPerformed(v: View?) {
        if(v is RadioButton){
            when(v.id){
                R.id.radioScam -> {
                    radioGroupTwo?.clearCheck()
                    this.spammerType = SPAMMER_TYPE_SCAM
                }
                R.id.radioSales -> {
                    this.spammerType = SPAMMER_TYPE_SALES
                    radioGroupTwo?.clearCheck()

                }
                R.id.radioBusiness -> {
                    radioGroupOne?.clearCheck()
                    spammerType = Constants.SPAMMER_TYPE_BUSINESS
                }
                R.id.radioPerson -> {
                    radioGroupOne?.clearCheck()
                    spammerType = SPAMMER_TYPE_PEERSON
                }
            }
        }
    }



    private fun showBottomSheetDialog() {
        bottomSheetDialog.show()

    }

    private fun muteOrUnmute() {
        viewModel.muteThisAddress(phoneNum).observe(this, Observer {
            when (it) {
                OPERATION_COMPLETED -> {

                    val sbar = Snackbar.make(
                        binding.layoutIndividualContact,
                        "You no longer notified on call from $phoneNum",
                        Snackbar.LENGTH_SHORT
                    )
                    sbar.setAction("Undo", MyUndoListener(this))
//        sbar.anchorView = bottomNavigationView

                    sbar.show()
                }


            }
        })
    }

    private fun startIndividualSMS() {
        val intent = Intent(this, IndividualSMSActivity::class.java)
        val bundle = Bundle()
        bundle.putString(CONTACT_ADDRES, phoneNum)
        bundle.putString(SMS_CHAT_ID, "")

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.putExtras(bundle)
        startActivity(intent)
    }





    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        count++
        if(isChecked){
            viewModel.muteThisAddress(phoneNum).observe(this, Observer {
                when (it) {
                    OPERATION_COMPLETED -> {
                        if (count > 1) {
                            val sbar = Snackbar.make(
                               binding.layoutIndividualContact,
                                "You no longer notified on from $phoneNum",
                                Snackbar.LENGTH_SHORT
                            )
                            sbar.setAction("Undo", MyUndoListener(this))
//        sbar.anchorView = bottomNavigationView
                            sbar.show()
                        }

                    }
                }
            })
        }else{
            viewModel.unMuteByAddress(phoneNum)
        }


    }

    override fun onUndoClicked() {
        viewModel.unmute(phoneNum)
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.itemEditContact -> {
                startContactEditActivity(viewModel.contactId)
            }
            R.id.itemUnblockNumber -> {
//                generalBlockViewmodel.removeFromBlockList(phoneNum,
//                    BlockTypes.BLOCK_TYPE_EXACT_NUMBER,
//                 getRandomColor()
//                )
            }

        }
        return true
    }


    companion object{
        private const val TAG = "__IndividualCotactViewActivity"
        var SPAM_THRESHOLD_VALUE = Constants.DEFAULT_SPAM_THRESHOLD
        private const val TYPE_SPAM  = -1
    }
}