package com.nibble.hashcaller.view.ui.contacts.IndividualContacts

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.contacts.IndividualContacts.utils.IndividualContactFactory
import com.nibble.hashcaller.view.ui.contacts.IndividualContacts.utils.IndividualcontactViewModel
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ID

class IndividualCotactViewActivity : AppCompatActivity() {
    private lateinit var viewModel:IndividualcontactViewModel

    @SuppressLint("LongLogTag")
    private  var contactId: Long? = null
    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_individual_cotact_view)

        val contactId = intent.getLongExtra(CONTACT_ID, 1)
        Log.d(TAG, "onCreate: $contactId")
        val viewModelFactory = IndividualContactFactory(application, contactId )
        viewModel = ViewModelProvider(this,viewModelFactory).get(IndividualcontactViewModel::class.java)


        viewModel.contact.observe(this, Observer { contact->
            Log.d(TAG, "onCreate: ${contact.phoneNumber}")
        })


    }
    companion object{
        private const val TAG = "__IndividualCotactViewActivity"
    }
}