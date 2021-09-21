package com.hashcaller.app.view.ui.contacts.individualContacts

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
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
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.hashcaller.app.R
import com.hashcaller.app.databinding.ActivityIndividualCotactViewBinding
import com.hashcaller.app.datastore.DataStoreRepository
import com.hashcaller.app.datastore.PreferencesKeys
import com.hashcaller.app.local.db.blocklist.BlockTypes
import com.hashcaller.app.utils.Constants
import com.hashcaller.app.utils.extensions.requestCallPhonePermission
import com.hashcaller.app.utils.notifications.tokeDataStore
import com.hashcaller.app.view.ui.MainActivity
import com.hashcaller.app.view.ui.MyUndoListener
import com.hashcaller.app.view.ui.blockConfig.GeneralBlockInjectorUtil
import com.hashcaller.app.view.ui.blockConfig.GeneralblockViewmodel
import com.hashcaller.app.view.ui.contacts.individualContacts.ThumbnailImageData.Companion.IMAGE_FOUND_FROM_C_PROVIDER
import com.hashcaller.app.view.ui.contacts.individualContacts.ThumbnailImageData.Companion.IMAGE_FOUND_FROM_DB
import com.hashcaller.app.view.ui.contacts.individualContacts.ThumbnailImageData.Companion.IMAGE_FOUND_FROM_DB_GOOGLE
import com.hashcaller.app.view.ui.contacts.individualContacts.utils.IndividualContactInjectorUtil
import com.hashcaller.app.view.ui.contacts.individualContacts.utils.IndividualcontactViewModel
import com.hashcaller.app.view.ui.contacts.makeCall
import com.hashcaller.app.view.ui.contacts.toggleUserBadge
import com.hashcaller.app.view.ui.contacts.toggleVerifiedBadge
import com.hashcaller.app.view.ui.contacts.utils.*
import com.hashcaller.app.view.ui.contacts.utils.CONTACT_ID
import com.hashcaller.app.view.ui.extensions.getMyPopupMenu
import com.hashcaller.app.view.ui.extensions.setRandomBackgroundCircle
import com.hashcaller.app.view.ui.extensions.startContactEditActivity
import com.hashcaller.app.view.ui.sms.individual.IndividualSMSActivity
import com.hashcaller.app.view.ui.sms.individual.util.*
import com.hashcaller.app.view.utils.getDecodedBytes
import com.vmadalin.easypermissions.EasyPermissions


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
    private  var selectedRadioButton: RadioButton? = null
    private  var spammerType:Int = SPAMMER_TYPE_SCAM
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
    @SuppressLint("LongLogTag")
//    private  var contactId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        binding = ActivityIndividualCotactViewBinding.inflate(layoutInflater)
        setDataStoreValues()
        //todo if number not in contact dont show edit option/ instead show create contact option
        setContentView(binding.root)
        getIntentExtras()
        setupBottomSheet()
        initListeners()
        initViewmodel()
//        setDetailsInview(phoneNum, name)

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
                color = -1
                setClearImage(photoURI)
//                popup?.menu?.findItem(R.id.itemUnblockNumber)?.isVisible = true
                
            } else {
                binding.btnBlockIndividualContact.beVisible()
                binding.btnUnblock.beGone()

                if(prevColor != -1 ){
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
                binding.layoutSpamCountt.beVisible()
                binding.tvSpamCountValue.text = it.spammCount.toString()
            }else {
                binding.layoutSpamCountt.beGone()
            }

            binding.tvFirstLetter.text = it.firstName[0].toString()
            binding.tvName.text = name
            binding.tvLocationValues.text = it.country + " " + it.location
            binding.tvLocationValues.text = it.spammCount.toString()
            if(it.firstName==phoneNum){
                binding.layoutNumber.beGone()
            }

            if(it.hUid.isNotEmpty()){
                binding.imgUserIconBg.beVisible()
                binding.imgUserIcon.beVisible()
            }else {
                binding.imgUserIcon.beInvisible()
                binding.imgUserIconBg.beInvisible()
            }
            toggleVerifiedBadge(binding.imgVerifiedBadge, it.isVerifiedUser)
        })
    }

    private fun getIntentExtras() {
        phoneNum = intent.getStringExtra(CONTACT_ID)?:""
         name = intent.getStringExtra("name")?:""
        if(name.isEmpty()){
            name = phoneNum
        }
//        val id = intent.getLongExtra("id",0L)
        photoURI = intent.getStringExtra("photo")?:""
        color = intent.getIntExtra("color", 1)
        prevColor = color
        binding.tvNumberValue.text = phoneNum
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
            this,
            phoneNum
         )).get(GeneralblockViewmodel::class.java)
    }
    @SuppressLint("LongLogTag")
    private fun setClearImage(photoURI: String?) {
        binding.tvName.text = name
        binding.tvNameSmall.text = name

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
                        if(name.isNotEmpty()){
                            binding.tvFirstLetter.beVisible()
                            binding.tvFirstLetter.text = name[0].toString()
                        }

                    }
                }

            })
//        }
    }

    private fun setupBottomSheet() {
        bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialogfeedback = BottomSheetDialog(this)
        val viewSheet = layoutInflater.inflate(R.layout.bottom_sheet_block, null)
        val viewSheetFeedback = layoutInflater.inflate(R.layout.bottom_sheet_block_feedback, null)

        bottomSheetDialog.setContentView(viewSheet)
        bottomSheetDialogfeedback.setContentView(viewSheetFeedback)
        tvSpamfeedbackMsg = bottomSheetDialogfeedback.findViewById<TextView>(R.id.tvSpamfeedbackMsg) as TextView
        tvblockedFeedback = bottomSheetDialogfeedback.findViewById<TextView>(R.id.tvSpamfeedbackMsg) as TextView

//        imgExpand = bottomSheetDialog.findViewById<ImageView>(R.id.imgExpand) as ImageView
        radioScam = bottomSheetDialog.findViewById<RadioButton>(R.id.radioScam) as RadioButton
        radioSales = bottomSheetDialog.findViewById<RadioButton>(R.id.radioSales) as RadioButton
        radioBusiness = bottomSheetDialog.findViewById<RadioButton>(R.id.radioBusiness) as RadioButton
        radioPerson = bottomSheetDialog.findViewById<RadioButton>(R.id.radioPerson) as RadioButton

        radioGroupOne = bottomSheetDialog.findViewById<RadioGroup>(R.id.radioGroupOne) as RadioGroup
        radioGroupTwo = bottomSheetDialog.findViewById<RadioGroup>(R.id.radioPersonOrBusiness) as RadioGroup

        btnBlock = bottomSheetDialog.findViewById<Button>(R.id.btnBlock) as Button
        selectedRadioButton = radioScam
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
        binding.switchIndividualContact.setOnClickListener(this)
        binding.btnBlockIndividualContact.setOnClickListener(this)
        binding.imgBtnBack.setOnClickListener(this)
        binding.btnUnblock.setOnClickListener(this)
        binding.imgBtnCallindividual.setOnClickListener(this)
//        binding.imgBtnSMS.setOnClickListener(this)
        binding.imgBtnMoreIndividualCntct.setOnClickListener(this)
        radioSales.setOnClickListener(this)
        radioScam.setOnClickListener(this)
        radioBusiness.setOnClickListener(this)
        radioPerson.setOnClickListener(this)
        btnBlock.setOnClickListener(this)
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

                else {
                    reportSpamAndblock()
                }


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
       
    }

    private fun blockThisAddress() {
        //todo, while saving spam count in chat threads the spam count is having large number, fix it
        generalBlockViewmodel.blockThisAddress(
            spammerType,
            phoneNum,
            applicationContext
        ).observe(this, Observer {
            when(it){
                ON_COMPLETED -> {
                    bottomSheetDialog.hide()
                    bottomSheetDialog.dismiss()
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

    private fun reportSpamAndblock() {
        viewModel.reportSpam(
            phoneNum,
            this.spammerType,
            this
        ).observe(
            this,
            Observer {
                when (it) {
                    OPERATION_UNBLOCKED -> {

                        bottomSheetDialog.hide()
                        val sbar = Snackbar.make(
                            binding.layoutIndividualContact,
                            "You have unblocked $phoneNum",
                            Snackbar.LENGTH_LONG
                        )
                        sbar.show()
                    }
                    ON_COMPLETED -> {
                        bottomSheetDialog.hide()
                        val sb = SpannableStringBuilder(phoneNum);
                        val bss = StyleSpan(Typeface.BOLD); // Span to make text bold
                        sb.setSpan(
                            bss,
                            0,
                            phoneNum!!.length,
                            Spannable.SPAN_INCLUSIVE_INCLUSIVE
                        ); // make first 4 characters Bold
                       tvSpamfeedbackMsg.text = sb

                        bottomSheetDialogfeedback.show()
                        tvblockedFeedback.text  = "You blocked $phoneNum"
                    }

                }
            })
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

    private fun popupImage() {
        var imgVCntctPop:ImageView?
        val settingsDialog = Dialog(this)
        settingsDialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        settingsDialog.setContentView(
            layoutInflater.inflate(
                R.layout.image_layout, null
            )
        )
        imgVCntctPop = settingsDialog.findViewById<ImageView>(R.id. imgVCntctPop)
        imgVCntctPop?.setImageURI(Uri.parse(photoURI))
        settingsDialog.show()
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

    }
}