package com.nibble.hashcaller.view.ui.contacts.IndividualContacts

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.finishAfterTransition
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.contacts.IndividualContacts.utils.IndividualContactInjectorUtil
import com.nibble.hashcaller.view.ui.contacts.IndividualContacts.utils.IndividualcontactViewModel
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ID
import kotlinx.android.synthetic.main.activity_individual_cotact_view.*
import kotlinx.android.synthetic.main.image_layout.*


class IndividualCotactViewActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var viewModel:IndividualcontactViewModel
    private lateinit var photoURI:String

    @SuppressLint("LongLogTag")
//    private  var contactId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        setContentView(R.layout.activity_individual_cotact_view)
        val phoneNum = intent.getStringExtra(CONTACT_ID)
        val name = intent.getStringExtra("name")
//        val id = intent.getLongExtra("id",0L)
         photoURI = intent.getStringExtra("photo")
//        getMoreInfoForNumber(phoneNum)

        setImage(photoURI)

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
        observeContactMoreInfo()



    }

    @SuppressLint("LongLogTag")
    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed: ")
        this.finishAfterTransition()
//        super.onBackPressed()
    }

    @SuppressLint("LongLogTag")
    private fun observeContactMoreInfo() {
        this.viewModel.mt.observe(this, Observer {
            if(it!=null){
                Log.d(TAG, "observeContactMoreInfo: ")
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
    }

    private fun setImage(photo: String?) {
//        Glide.with(this).load(photo).into(imgViewAvatar)
    }

    private fun setDetailsInview(phoneNum: String?, name: String?) {
        tvName.text = name
        txtViewNumber.text = phoneNum

    }



    @SuppressLint("LongLogTag")
    override fun onClick(v: View?) {

        when(v?.id){
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

}