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
import android.widget.CompoundButton
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.MyUndoListener
import com.nibble.hashcaller.view.ui.contacts.individualContacts.utils.IndividualContactInjectorUtil
import com.nibble.hashcaller.view.ui.contacts.individualContacts.utils.IndividualcontactViewModel
import com.nibble.hashcaller.view.ui.contacts.makeCall
import com.nibble.hashcaller.view.ui.contacts.utils.*
import com.nibble.hashcaller.view.ui.extensions.getMyPopupMenu
import com.nibble.hashcaller.view.ui.extensions.setRandomBackgroundCircle
import com.nibble.hashcaller.view.ui.extensions.startContactEditActivity
import com.nibble.hashcaller.view.ui.sms.individual.IndividualSMSActivity
import com.nibble.hashcaller.view.ui.sms.individual.util.beInvisible
import com.nibble.hashcaller.view.ui.sms.individual.util.beVisible
import com.nibble.hashcaller.view.utils.spam.SpamLocalListManager
import kotlinx.android.synthetic.main.activity_individual_cotact_view.*
import kotlinx.android.synthetic.main.bottom_sheet_block.*
import kotlinx.android.synthetic.main.bottom_sheet_block_feedback.*
import kotlinx.android.synthetic.main.image_layout.*


class IndividualCotactViewActivity : AppCompatActivity(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener, MyUndoListener.SnackBarListner,
    PopupMenu.OnMenuItemClickListener {
    private lateinit var viewModel:IndividualcontactViewModel
    private lateinit var photoURI:String
    private  var color  = 1
    var phoneNum:String = ""
    var count  = 0
    private var isBlocked = false
    private lateinit var bottomSheetDialogfeedback: BottomSheetDialog
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private  var selectedRadioButton: RadioButton? = null
    private  var spammerType:Int = -1
    private var SPAMMER_CATEGORY = SpamLocalListManager.SPAMMER_BUISINESS



    @SuppressLint("LongLogTag")
//    private  var contactId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        setContentView(R.layout.activity_individual_cotact_view)
         phoneNum = intent.getStringExtra(CONTACT_ID)
        val name = intent.getStringExtra("name")
//        val id = intent.getLongExtra("id",0L)
         photoURI = intent.getStringExtra("photo")
        color = intent.getIntExtra("color", 1)
//        getMoreInfoForNumber(phoneNum)
        Log.d(TAG, "onCreate: photouri is $photoURI")
        setupBottomSheet()

        initListeners()
        Log.d(TAG, "onCreate: name $name")
        IndividualContactInjectorUtil.phoneNumber = phoneNum
            Log.d(TAG, "phone num is : $phoneNum")


        viewModel =ViewModelProvider(
            this, IndividualContactInjectorUtil.provideUserInjectorUtil(
                this
            )
        ).get(
            IndividualcontactViewModel::class.java
        )
        setDetailsInview(phoneNum, name)
//        viewModel.photoUri.observe(this, Observer { photoUri->
//            Log.d(TAG, "observer photo uri $photoUri")
//
//        })
//        viewModel.getPhoto(id, phoneNum)

        viewModel.getContactsFromDb(phoneNum)
        getContactMutedInformation()
        getContactFromContentProvider(phoneNum)
        observeContactMoreInfo()
        observeBlockedDetails()



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


//        if(this.view?.visibility == View.VISIBLE){
//            bottomSheetDialog.hide()

//        }

        bottomSheetDialog.setOnDismissListener {
            Log.d(IndividualSMSActivity.TAG, "bottomSheetDialogDismissed")

        }
    }

    private fun observeBlockedDetails() {
        viewModel.callersinfoLivedata.observe(this, Observer { lst ->
            viewModel.isThisAddressBlockedByUser(phoneNum).observe(this, Observer {
                if (it == true) {
                    tvBlockBtnInfo.text = "Unblock"
                    imgBtnBlockIndividualContact.setBackgroundResource(R.drawable.circular_button_unblock)
                    isBlocked = true
                } else {
                    tvBlockBtnInfo.text = "Block"
                    imgBtnBlockIndividualContact.setBackgroundResource(R.drawable.circular_button_block)

                    isBlocked = false
                }
            })
        })
    }

    private fun getContactMutedInformation() {

        viewModel.mutedContacts.observe(this, Observer { lst ->
            viewModel.isThisAddressMuted(phoneNum, lst).observe(this, Observer {
                switchIndividualContact.isChecked = it
            })
        })
    }

    private fun getContactFromContentProvider(phoneNum: String?) {
        viewModel.getContactFromContentProvider(phoneNum).observe(this, Observer {
            if (it != null) {
                tvisInContact.text = "This person is in your contacts"
            } else {
                tvisInContact.text = "This person is not in your contacts"

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
        switchIndividualContact.setOnClickListener(this)
        imgBtnBlockIndividualContact.setOnClickListener(this)
        imgBtnBack.setOnClickListener(this)
        imgBtnCallIndividualContact.setOnClickListener(this)
        imgBtnSMS.setOnClickListener(this)
        imgBtnMoreIndividualCntct.setOnClickListener(this)


        bottomSheetDialog.radioS.setOnClickListener(this)
        bottomSheetDialog.radioScam.setOnClickListener(this)
        bottomSheetDialog.imgExpand.setOnClickListener(this)
        bottomSheetDialog.btnBlock.setOnClickListener(this)
        bottomSheetDialog.btnBlock.setOnClickListener(this)
    }

    private fun setImage(photo: String?) {
        ivAvatar.beVisible()
        tvFirstLetter.beInvisible()

        if(!photo.isNullOrEmpty()){
            Glide.with(this).load(photo).into(ivAvatar)
            tvFirstLetter.beInvisible()
        }else{
            ivAvatar.beInvisible()
            tvFirstLetter.beVisible()
        }
    }

    private fun setDetailsInview(phoneNum: String?, name: String?) {
        if(photoURI.isEmpty()){
            tvName.text = name
            txtViewNumber.text = phoneNum
            if(!name.isNullOrEmpty()){
                tvFirstLetter.text = name[0].toString()
                tvFirstLetter.setRandomBackgroundCircle(color)
                ivAvatar.beInvisible()
            }
        }else{
            setImage(photoURI)

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
                val popup =  getMyPopupMenu(R.menu.individual_contact_popup_menu, imgBtnMoreIndividualCntct)
                popup.setOnMenuItemClickListener(this)
                popup.show()
            }
            R.id.imgBtnBack -> {
                finish()
            }
            R.id.imgBtnCallIndividualContact -> {
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
                        selectedRadioButton = bottomSheetDialog.radioScam
                        Log.d(IndividualSMSActivity.TAG, "radio button clicked")
                        this.spammerType = SpamLocalListManager.SPAMM_TYPE_SCAM

//                                spinnerSelected.value = false


                    }
                }
                R.id.radioS -> {

                    val checked = v.isChecked
                    if (checked) {
                        selectedRadioButton = bottomSheetDialog.radioS
                        this.spammerType = SpamLocalListManager.SPAMM_TYPE_SALES
                        Log.d(IndividualSMSActivity.TAG, "onClick: radio scam")
//                                spinnerSelected.value = false

                    }
                }
                R.id.radioBusiness -> {
                    val checked = v.isChecked
                    if (checked) {
                        this.SPAMMER_CATEGORY = SpamLocalListManager.SPAMMER_BUISINESS
                    }
                }
                R.id.radioPerson -> {
                    val checked = v.isChecked
                    if (checked) {
                        this.SPAMMER_CATEGORY = SpamLocalListManager.SPAMMER_PEERSON

                    }
                }
            }
        }
    }

    private fun blockOrUnBlock() {

        viewModel.blockOrUnblockByAdderss(phoneNum, this.spammerType, this.SPAMMER_CATEGORY).observe(
            this,
            Observer {
                when (it) {
                    OPERATION_UNBLOCKED -> {
                        bottomSheetDialog.hide()
                        val sbar = Snackbar.make(
                            layoutIndividualContact,
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
                        bottomSheetDialogfeedback.tvSpamfeedbackMsg.text = sb

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
                        layoutIndividualContact,
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
        val settingsDialog = Dialog(this)
        settingsDialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        settingsDialog.setContentView(
            layoutInflater.inflate(
                R.layout.image_layout, null
            )
        )
        settingsDialog.imgVCntctPop.setImageURI(Uri.parse(photoURI))
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
                                layoutIndividualContact,
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