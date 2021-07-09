package com.nibble.hashcaller.view.ui.contactSelector

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.nibble.hashcaller.R
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.utils.auth.TokenHelper
import com.nibble.hashcaller.view.ui.contacts.ContactAdapter
import com.nibble.hashcaller.view.ui.contacts.utils.*
import com.nibble.hashcaller.view.ui.sms.individual.IndividualSMSActivity
import com.nibble.hashcaller.view.utils.TopSpacingItemDecoration
import com.nibble.hashcaller.work.DESTINATION_ACTIVITY
import com.nibble.hashcaller.work.INDIVIDUAL_SMS_ACTIVITY
import com.nibble.hashcaller.work.formatPhoneNumber
import kotlinx.android.synthetic.main.activity_contact_selector.*

class ContactSelectorActivity : AppCompatActivity() {

    private lateinit  var contactViewModel: ContactsViewModel
    private var permissionGivenLiveData: MutableLiveData<Boolean> = MutableLiveData(false)
    private var contactsRecyclerAdapter: ContactAdapter? = null
    private lateinit var destinationActivity:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // decides which activity to go when user click on a contact item
         destinationActivity = intent.getStringExtra(DESTINATION_ACTIVITY)
        setContentView(R.layout.activity_contact_selector)
        if(checkContactPermission()){
            initViewmodel()
            observerContactList()
            initRecyclerView()

        }
    }

    private fun initViewmodel() {
        contactViewModel = ViewModelProvider(this, ContactSelectorInjectorUtil.provideContactsViewModelFactory(
            applicationContext,
            lifecycleScope,
            TokenHelper(FirebaseAuth.getInstance().currentUser)
        )).get(ContactsViewModel::class.java)
    }

    private fun initRecyclerView() {
        rclrViewContactSelector?.apply {
            layoutManager = LinearLayoutManager(this@ContactSelectorActivity)
            val topSpacingDecorator =
                TopSpacingItemDecoration(
                    30
                )
//                addItemDecoration(topSpacingDecorator)
//            contactsRecyclerAdapter = ContactAdapter(context) { id: com.nibble.hashcaller.network.user.Contact ->onContactItemClicked(id)}
            adapter = contactsRecyclerAdapter

        }
    }
    private fun onContactItemClicked(contactItem: Contact){
        Log.d(TAG, "onContactItemClicked: ${contactItem.phoneNumber}")


        when(destinationActivity){
            INDIVIDUAL_SMS_ACTIVITY->{
             startindidualSmsActivity(contactItem)
            }
        }

    }

    private fun startindidualSmsActivity(contactItem: Contact) {
        val intent = Intent(this, IndividualSMSActivity::class.java )
        val num = formatPhoneNumber(contactItem.phoneNumber)
        intent.putExtra(CONTACT_ADDRES, num)

        startActivity(intent)
        finish()
    }

    private fun observerContactList() {
        try {
            contactViewModel.contacts?.observe(this, Observer{contacts->
                contacts.let {
                    this.pgBarContactSelector.visibility = View.GONE
                    contactsRecyclerAdapter?.setContactList(it)
                    ContactGlobalHelper.size = contacts.size // setting the size in ContactsGlobalHelper
                }
            })
        }catch (e:Exception){
            Log.d(TAG, "observerContactList: exception $e")
        }
    }

    private fun checkContactPermission(): Boolean {
        val permissionContact =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALL_LOG)
        if(permissionContact!= PackageManager.PERMISSION_GRANTED){
            return false
        }
        return true
    }

    companion object{
        const val TAG = "ContactSelectorActivity"
    }
}