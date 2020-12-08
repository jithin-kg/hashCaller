package com.nibble.hashcaller.view.ui.contacts.IndividualContacts

import android.annotation.SuppressLint
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
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

        setContentView(R.layout.activity_individual_cotact_view)

        val phoneNum = intent.getStringExtra(CONTACT_ID)
        val name = intent.getStringExtra("name")
        val id = intent.getLongExtra("id",0L)
         photoURI = intent.getStringExtra("photo")
        setImage(photoURI)
        initListeners()
        Log.d(TAG, "onCreate: name $name")
        IndividualContactInjectorUtil.phoneNumber = phoneNum
            Log.d(TAG, "phone num is : $phoneNum")


        viewModel =ViewModelProvider(this, IndividualContactInjectorUtil.provideUserInjectorUtil(this)).get(
            IndividualcontactViewModel::class.java)
        setDetailsInview(phoneNum, name)
        viewModel.photoUri.observe(this, Observer { photoUri->
            Log.d(TAG, "observer photo uri $photoUri")

        })
        viewModel.getPhoto(id, phoneNum)
//
//        viewModel.livedata.observe(this, Observer { contact->
//            Log.d(TAG, "onCreate: $contact")
//            Log.d(TAG, "onCreate: name ${contact.name}")
//        })
//        viewModel.mt?.observe(this, Observer { contact->
//            if(contact!=null){
//                Log.d(TAG, "onCreate: ${contact?.number}")
//                textViewCntcName.text = contact.name
//               textViewNumber.text = contact.number
//                textViewCarrier.text = contact.carrier
//                textViewLocation.text = contact.location
//                textViewMail.text = "sample@gmail.com"
//
//            }else{
//                Log.d(TAG, "contact is null ")
//            }
//
//        })
        viewModel.getContactsFromDb(phoneNum)


    }

    private fun initListeners() {
        imgViewAvatar.setOnClickListener(this)
    }

    private fun setImage(photo: String?) {
        imgViewAvatar.setImageURI(Uri.parse(photo))
    }

    private fun setDetailsInview(phoneNum: String?, name: String?) {
        textViewCntcName.text = name
        textViewNumber.text = phoneNum

    }



    @SuppressLint("LongLogTag")
    override fun onClick(v: View?) {

        when(v?.id){
            R.id.imgViewAvatar->{
               popupImage()
            }
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