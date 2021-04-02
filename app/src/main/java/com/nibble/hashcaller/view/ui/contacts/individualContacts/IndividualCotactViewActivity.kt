package com.nibble.hashcaller.view.ui.contacts.individualContacts

import android.annotation.SuppressLint
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.contacts.individualContacts.utils.IndividualContactInjectorUtil
import com.nibble.hashcaller.view.ui.contacts.individualContacts.utils.IndividualcontactViewModel
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ID
import com.nibble.hashcaller.view.ui.extensions.setRandomBackgroundCircle
import com.nibble.hashcaller.view.ui.sms.individual.util.beInvisible
import com.nibble.hashcaller.view.ui.sms.individual.util.beVisible
import kotlinx.android.synthetic.main.activity_individual_cotact_view.*
import kotlinx.android.synthetic.main.image_layout.*


class IndividualCotactViewActivity : AppCompatActivity(), View.OnClickListener,
    CompoundButton.OnCheckedChangeListener {
    private lateinit var viewModel:IndividualcontactViewModel
    private lateinit var photoURI:String
    private  var color  = 1
    var phoneNum:String = ""
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

        initListeners()
        Log.d(TAG, "onCreate: name $name")
        IndividualContactInjectorUtil.phoneNumber = phoneNum
            Log.d(TAG, "phone num is : $phoneNum")


        viewModel =ViewModelProvider(this, IndividualContactInjectorUtil.provideUserInjectorUtil(this)).get(
            IndividualcontactViewModel::class.java)
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

    private fun observeBlockedDetails() {
        viewModel.callersinfoLivedata.observe(this, Observer { lst->
            viewModel.isThisAddressBlockedByUser(phoneNum).observe(this, Observer {
                if(it == true){
                    tvBlockBtnInfo.text = "Unblock"
                }else{
                    tvBlockBtnInfo.text = "Block"
                }
            })
        })
    }

    private fun getContactMutedInformation() {

        viewModel.mutedContacts.observe(this, Observer {lst->
            viewModel.isThisAddressMuted(phoneNum, lst).observe(this, Observer {
                switchIndividualContact.isChecked = it
            })
        })
    }

    private fun getContactFromContentProvider(phoneNum: String?) {
        viewModel.getContactFromContentProvider(phoneNum).observe(this, Observer {
            if(it!=null){
                tvisInContact.text = "This person is in your contacts"
            }else{
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
            if(it!=null){
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
        switchIndividualContact.setOnCheckedChangeListener(this)
        imgBtnBlockIndividualContact.setOnClickListener(this)
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
            R.id.imgBtnBlockIndividualContact ->{
                viewModel.blockOrUnblockByAdderss(phoneNum)

            }
//            R.id.imgViewAvatar->{
//               popupImage()
//            }
        }
    }

    private fun popupImage() {
        val settingsDialog = Dialog(this)
        settingsDialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        settingsDialog.setContentView(
            layoutInflater.inflate(
                R.layout.image_layout
                 , null
            )
        )
        settingsDialog.imgVCntctPop.setImageURI(Uri.parse(photoURI))
        settingsDialog.show()
    }

    companion object{
        private const val TAG = "__IndividualCotactViewActivity"
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if(isChecked){
            viewModel.muteThisAddress(phoneNum)
        }else{
            viewModel.unMuteByAddress(phoneNum)
        }
    }

}