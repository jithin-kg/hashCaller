package com.nibble.hashcaller.view.ui.contacts.individualContacts

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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivityIndividualCotactViewBinding
import com.nibble.hashcaller.view.ui.MyUndoListener
import com.nibble.hashcaller.view.ui.contacts.individualContacts.ThumbnailImageData.Companion.IMAGE_FOUND_FROM_C_PROVIDER
import com.nibble.hashcaller.view.ui.contacts.individualContacts.ThumbnailImageData.Companion.IMAGE_FOUND_FROM_DB
import com.nibble.hashcaller.view.ui.contacts.individualContacts.utils.IndividualContactInjectorUtil
import com.nibble.hashcaller.view.ui.contacts.individualContacts.utils.IndividualcontactViewModel
import com.nibble.hashcaller.view.ui.contacts.isBlockTopSpammersAutomaticallyEnabled
import com.nibble.hashcaller.view.ui.contacts.makeCall
import com.nibble.hashcaller.view.ui.contacts.utils.*
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ID
import com.nibble.hashcaller.view.ui.extensions.getMyPopupMenu
import com.nibble.hashcaller.view.ui.extensions.setRandomBackgroundCircle
import com.nibble.hashcaller.view.ui.extensions.startContactEditActivity
import com.nibble.hashcaller.view.ui.sms.individual.IndividualSMSActivity
import com.nibble.hashcaller.view.ui.sms.individual.util.*
import com.nibble.hashcaller.view.utils.getDecodedBytes
import com.nibble.hashcaller.view.utils.spam.SpamLocalListManager


class IndividualCotactViewActivity : AppCompatActivity(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener, MyUndoListener.SnackBarListner,
    PopupMenu.OnMenuItemClickListener {

    private lateinit var binding:ActivityIndividualCotactViewBinding
    private lateinit var viewModel:IndividualcontactViewModel
    private lateinit var photoURI:String
    private  var color  = 1
    var phoneNum:String = ""
    var name:String = ""
    var count  = 0
    private var isBlocked = false
    private lateinit var bottomSheetDialogfeedback: BottomSheetDialog
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private  var selectedRadioButton: RadioButton? = null
    private  var spammerType:Int = SPAMMER_TYPE_SCAM
//    private lateinit var imgExpand:ImageView
    private lateinit var radioScam:RadioButton
    private lateinit var radioS:RadioButton
    private lateinit var btnBlock:Button
    private lateinit var tvSpamfeedbackMsg : TextView


    @SuppressLint("LongLogTag")
//    private  var contactId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        binding = ActivityIndividualCotactViewBinding.inflate(layoutInflater)

        setContentView(binding.root)
        getIntentExtras()
        setupBottomSheet()
        initListeners()
        initViewmodel()
//        setDetailsInview(phoneNum, name)

        viewModel.getContactsFromDb(phoneNum)
        getContactMutedInformation()
        getContactFromContentProvider(phoneNum)
        observeContactMoreInfo()
        observeBlockedDetails()
        getinfoFromServer()
        setClearImage(photoURI)



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
    }

    private fun initViewmodel() {
        IndividualContactInjectorUtil.phoneNumber = phoneNum
        viewModel =ViewModelProvider(
            this, IndividualContactInjectorUtil.provideUserInjectorUtil(
                applicationContext,phoneNum, lifecycleScope
            )
        ).get(
            IndividualcontactViewModel::class.java
        )
    }

    private fun getinfoFromServer() {
//        viewModel.getInfoFromServer(phoneNum).observe(this, Observer {
//            binding.tvSimValue.text = it.carrier
//        })
        viewModel.infoFromServer?.observe(this, Observer {
            it.let {
                if(it!=null){
                    binding.tvSimCardValue.text = it.carrier
                    binding.tvLocationValues.text = it.location
                    binding.tvSpamCountValue.text = it.spamCount.toString()
                   // binding.tvSimValue.text = it.carrier
                    //binding.tvLocationValue.text = "large location value foferdsjshdfkljhsdflksjdfh skjdfh"
                    //binding.tvIndividualCntSpamCount.text = it.spamCount.toString()
                }
            }
        })
    }


    @SuppressLint("LongLogTag")
    private fun setClearImage(photoURI: String?) {
        binding.tvName.text = name
        binding.txtViewNumber.text = phoneNum

//        if(!photoURI.isNullOrEmpty()){
            viewModel.getClearImage(phoneNum).observe(this, Observer {
                when(it.imageFoundFrom){
                    IMAGE_FOUND_FROM_C_PROVIDER ->{
                        loadImage(this, binding.ivAvatar, it.imageStr)
                    }
                    IMAGE_FOUND_FROM_DB ->{
                        binding.ivAvatar.setImageBitmap(getDecodedBytes(it.imageStr))
                    }
                    else->{
                        binding.tvFirstLetter.setRandomBackgroundCircle(color)
                        binding.ivAvatar.beInvisible()
                        binding.tvFirstLetter.text = name[0].toString()
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

//        imgExpand = bottomSheetDialog.findViewById<ImageView>(R.id.imgExpand) as ImageView
        radioScam = bottomSheetDialog.findViewById<RadioButton>(R.id.radioScam) as RadioButton
        radioS = bottomSheetDialog.findViewById<RadioButton>(R.id.radioSales) as RadioButton
        btnBlock = bottomSheetDialog.findViewById<Button>(R.id.btnBlock) as Button
        selectedRadioButton = radioScam
//        imgExpand.setOnClickListener(this)


//        if(this.view?.visibility == View.VISIBLE){
//            bottomSheetDialog.hide()

//        }

        bottomSheetDialog.setOnDismissListener {
            Log.d(IndividualSMSActivity.TAG, "bottomSheetDialogDismissed")

        }
    }

    private fun observeBlockedDetails() {

//        viewModel.callersinfoLivedata.observe(this, Observer { lst ->
            viewModel.isThisAddressBlockedByUser(phoneNum,  isBlockTopSpammersAutomaticallyEnabled()).observe(this, Observer {
                if (it == true) {
                    binding.tvBlockBtnInfo.text = "Unblock"
                    binding.imgBtnBlockIndividualContact.setBackgroundResource(R.drawable.circular_button_unblock)
                    isBlocked = true
                } else {
                    binding.tvBlockBtnInfo.text = "Block"
                    binding.imgBtnBlockIndividualContact.setBackgroundResource(R.drawable.circular_button_block)

                    isBlocked = false
                }
            })
//        })
    }

    private fun getContactMutedInformation() {

        viewModel.mutedContacts.observe(this, Observer { lst ->
            viewModel.isThisAddressMuted(phoneNum, lst).observe(this, Observer {
                binding.switchIndividualContact.isChecked = it
            })
        })
    }

    private fun getContactFromContentProvider(phoneNum: String?) {
        viewModel.getContactFromContentProvider(phoneNum).observe(this, Observer {
            if (it != null) {
                binding.tvisInContact.text = "This person is in your contacts"
            } else {
                binding.tvisInContact.text = "This person is not in your contacts"

            }
        })
    }

    @SuppressLint("LongLogTag")
    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed: ")
//        this.finishAfterTransition()
//        super.onBackPressed()
        finish()
    }

    @SuppressLint("LongLogTag")
    private fun observeContactMoreInfo() {
        this.viewModel.mt.observe(this, Observer {
            if (it != null) {
                Log.d(TAG, "observeContactMoreInfo:  $it")
//                textViewLocation.text = it?.location
//                textViewCarrier.text = it?.carrier
//            textViewLineType.text = it.
            }

        })
    }

//    private fun getMoreInfoForNumber(phoneNum: String?) {
//        viewModel.getMoreInfoforNumber(phoneNum)
//    }

    private fun initListeners() {
//        imgViewAvatar.setOnClickListener(this)
//        switchIndividualContact.setOnCheckedChangeListener(this)
        binding.switchIndividualContact.setOnClickListener(this)
        binding.imgBtnBlockIndividualContact.setOnClickListener(this)
        binding.imgBtnBack.setOnClickListener(this)
        binding.imgBtnCallindividual.setOnClickListener(this)
        binding.imgBtnSMS.setOnClickListener(this)
        binding.imgBtnMoreIndividualCntct.setOnClickListener(this)

        radioS.setOnClickListener(this)
        radioScam.setOnClickListener(this)
//        imgExpand.setOnClickListener(this)
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
            binding.tvFirstLetter.beVisible()
        }
    }





    @SuppressLint("LongLogTag")
    override fun onClick(v: View?) {

        when(v?.id){
            R.id.imgBtnBlockIndividualContact -> {
                if (!isBlocked) {
                    showBottomSheetDialog()

                } else {
                    blockOrUnBlock()
                }


            }
            R.id.imgBtnMoreIndividualCntct -> {
                val popup =  getMyPopupMenu(R.menu.individual_contact_popup_menu,binding.imgBtnMoreIndividualCntct)
                popup.setOnMenuItemClickListener(this)
                popup.show()
            }
            R.id.imgBtnBack -> {
                finish()
            }
            R.id.imgBtnCallindividual -> {
                makeCall(phoneNum)
            }
            R.id.imgBtnSMS -> {
                startIndividualSMS()
            }
            R.id.switchIndividualContact -> {
                muteOrUnmute()
            }
            R.id.btnBlock -> {

                blockOrUnBlock()

            }else ->{
            this.radioButtonClickPerformed(v)
            }
//            R.id.imgViewAvatar->{
//               popupImage()
//            }
        }
    }

    private fun radioButtonClickPerformed(v: View?) {
        if(v is RadioButton){

            when(v.id){
                R.id.radioScam -> {
                    val checked = v.isChecked
                    if (checked) {
                        selectedRadioButton = radioScam
                        Log.d(IndividualSMSActivity.TAG, "radio button clicked")
                        this.spammerType = SPAMMER_TYPE_SCAM

//                                spinnerSelected.value = false


                    }
                }
                R.id.radioSales -> {

                    val checked = v.isChecked
                    if (checked) {
                        selectedRadioButton = radioS
                        this.spammerType = SPAMMER_TYPE_SALES
                        Log.d(IndividualSMSActivity.TAG, "onClick: radio scam")
//                                spinnerSelected.value = false

                    }
                }
                R.id.radioBusiness -> {
                    val checked = v.isChecked
                    if (checked) {
                        spammerType = SPAMMER_TYPE_BUSINESS
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

    private fun blockOrUnBlock() {

        viewModel.blockOrUnblockByAdderss(phoneNum, this.spammerType).observe(
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
//                    sbar.setAction("Undo", MyUndoListener(this))
//        sbar.anchorView = bottomNavigationView

                        sbar.show()
                    }
                    OPERATION_BLOCKED -> {
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
                    }

                }
            })
//        viewModel.blockThisAddress(
//            IndividualMarkedItemHandlerCall.getMarkedContactAddress()!!, MarkedItemsHandler.markedTheadIdForBlocking,
//            this.spammerType,
//            this.SPAMMER_CATEGORY )

//        Toast.makeText(this.requireActivity(), "Number added to spamlist", Toast.LENGTH_LONG)
//        bottomSheetDialog.hide()
//        bottomSheetDialog.dismiss()
//        bottomSheetDialogfeedback.show()
//        var txt = "${IndividualMarkedItemHandlerCall.getMarkedContactAddress()} can no longer send SMS or call you."
//        val  sb =  SpannableStringBuilder(txt);
//        val bss =  StyleSpan(Typeface.BOLD); // Span to make text bold
//        sb.setSpan(bss, 0, IndividualMarkedItemHandlerCall.getMarkedContactAddress()!!.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE); // make first 4 characters Bold
//        bottomSheetDialogfeedback.tvSpamfeedbackMsg.text = sb
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

    companion object{
        private const val TAG = "__IndividualCotactViewActivity"
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
        }
        return true
    }



}