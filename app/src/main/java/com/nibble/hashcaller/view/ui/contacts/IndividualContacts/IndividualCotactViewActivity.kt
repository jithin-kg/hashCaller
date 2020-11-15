package com.nibble.hashcaller.view.ui.contacts.IndividualContacts

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.contacts.IndividualContacts.utils.IndividualContactFactory
import com.nibble.hashcaller.view.ui.contacts.IndividualContacts.utils.IndividualContactInjectorUtil
import com.nibble.hashcaller.view.ui.contacts.IndividualContacts.utils.IndividualcontactViewModel
import com.nibble.hashcaller.view.ui.contacts.search.utils.SearchInjectorUtil
import com.nibble.hashcaller.view.ui.contacts.search.utils.SearchViewModel
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ID
import kotlinx.android.synthetic.main.activity_individual_cotact_view.*

class IndividualCotactViewActivity : AppCompatActivity() {
    private lateinit var viewModel:IndividualcontactViewModel

    @SuppressLint("LongLogTag")
//    private  var contactId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_individual_cotact_view)

        val phoneNum = intent.getStringExtra(CONTACT_ID)
        Log.d(TAG, "phone num is : $phoneNum")
//        Log.d(TAG, "onCreate: $contactId")
//        val viewModelFactory = IndividualContactFactory(application, phoneNum )
//        viewModel = ViewModelProvider(this,viewModelFactory).get(IndividualcontactViewModel::class.java)
        viewModel =ViewModelProvider(this, IndividualContactInjectorUtil.provideUserInjectorUtil(this)).get(
            IndividualcontactViewModel::class.java)


        viewModel.mt?.observe(this, Observer { contact->
            if(contact!=null){
                Log.d(TAG, "onCreate: ${contact?.number}")
                textViewCntcName.text = contact.name
               textViewNumber.text = contact.number
                textViewCarrier.text = contact.carrier
                textViewLocation.text = contact.location
                textViewMail.text = "sample@gmail.com"

            }else{
                Log.d(TAG, "contact is null ")
            }

        })
        viewModel.getContactsFromDb(phoneNum)


    }
    companion object{
        private const val TAG = "__IndividualCotactViewActivity"
    }
}