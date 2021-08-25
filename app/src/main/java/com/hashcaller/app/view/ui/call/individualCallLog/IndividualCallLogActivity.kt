package com.hashcaller.app.view.ui.call.individualCallLog

import android.annotation.SuppressLint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CallLog
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hashcaller.app.databinding.ActivityIndividualCallLogBinding
import com.hashcaller.app.utils.constants.IntentKeys
import com.hashcaller.app.view.ui.call.dialer.util.CustomLinearLayoutManager
import com.hashcaller.app.view.ui.contacts.utils.CONTACT_ADDRES
import com.hashcaller.app.view.ui.contacts.utils.loadImage
import com.hashcaller.app.view.ui.extensions.setRandomBackgroundCircle
import com.hashcaller.app.view.ui.sms.individual.util.beGone
import com.hashcaller.app.view.ui.sms.individual.util.beVisible
import com.hashcaller.app.view.utils.getDecodedBytes

class IndividualCallLogActivity : AppCompatActivity(), IndividualCallLogAdapter.LongPressHandler {

    lateinit var binding : ActivityIndividualCallLogBinding
    private lateinit var viewmodel: IndividualCallViewModel
    private var phoneNum:String? = null
    private var name = ""
    private var nameInCProvider:String? = null
    private var nameFromDb:String? = null
    private var thumbnailFromCProvider:String? = null
    private var thumbnailFromDB:String? = null
    private var spamCount = 0L
    private var isReportedByUser = false
    private var avatarColor = 0

    private lateinit var adapter:IndividualCallLogAdapter
    private lateinit var layoutMngr:LinearLayoutManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIndividualCallLogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getIntents()
        initAdapter()
        initViewmodel()
        observeCallLog()

    }

    private fun getIntents() {
        var firstLetter = ""
        phoneNum  = intent.getStringExtra(CONTACT_ADDRES)
        nameInCProvider = intent.getStringExtra(IntentKeys.FULL_NAME_IN_C_PROVIDER)
        nameFromDb = intent.getStringExtra(IntentKeys.FULL_NAME_FROM_SERVER)
        thumbnailFromCProvider = intent.getStringExtra(IntentKeys.THUMBNAIL_FROM_CPROVIDER)
        thumbnailFromDB = intent.getStringExtra(IntentKeys.THUMBNAIL_FROM_BB)
        isReportedByUser = intent.getBooleanExtra(IntentKeys.IS_REPORTED_BY_USER, false)
        spamCount = intent.getLongExtra(IntentKeys.SPAM_COUNT, 0L)
        avatarColor = intent.getIntExtra(IntentKeys.AVATAR_COLOR, 0)

        if(nameInCProvider.isNullOrEmpty()){
            name = nameFromDb?:""
//            if(!name.isNullOrEmpty()){
//
//            }
        }else {
            name = nameInCProvider!!
        }


        if(name.isNullOrEmpty()){
            //hide name view and set only phone number
           name = phoneNum?:""
            binding.tvFullName.beGone()
            binding.tvPhoneNum.text = phoneNum
            firstLetter = phoneNum!![0].toString()

        }else {
            //show both name and phone number
            binding.tvFullName.text = name
            binding.tvPhoneNum.text = phoneNum
            firstLetter = name[0].toString()
        }

       if(!thumbnailFromCProvider.isNullOrEmpty()){
           loadImage(this, binding.imgVAvatar, thumbnailFromCProvider)
       }else if(!thumbnailFromDB.isNullOrEmpty()){
           binding.imgVAvatar.setImageBitmap(getDecodedBytes(thumbnailFromDB!!))
       }else {
           binding.tvFirstLetter.beVisible()
           binding.tvFirstLetter.text = firstLetter
           binding.tvFirstLetter.setRandomBackgroundCircle(avatarColor)
       }
    }

    @SuppressLint("LongLogTag")
    private fun observeCallLog() {
        viewmodel.callLogLiveData.observe(this, Observer {
//            adapter.submitList(it)
            adapter.setList(it)
        })
    }
    private fun initAdapter() {
//        recyclerView =
//            findViewById<View>(R.id.recyclerViewSMSIndividual) as RecyclerView


        adapter = IndividualCallLogAdapter(this,  this ){ id:String -> onContactitemClicked(id) }

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.recyclerView.setHasFixedSize(true)
        layoutMngr = CustomLinearLayoutManager(this)
        layoutMngr.stackFromEnd = true
        binding.recyclerView.layoutManager = layoutMngr

        binding.recyclerView.adapter = adapter
        binding.recyclerView.isNestedScrollingEnabled = false
    }
    private fun onContactitemClicked(id: String) {

    }

    private fun initViewmodel() {
        val URI =  Uri.withAppendedPath(
            CallLog.Calls.CONTENT_FILTER_URI,
            Uri.encode(phoneNum)
        );
        viewmodel = ViewModelProvider(this, IndividualCallLogInjectorUtil.provideDialerViewModelFactory(applicationContext, lifecycleScope, URI)).get(
            IndividualCallViewModel::class.java)

    }
    companion object{
        const val TAG ="__IndividualCallLogActivity"
    }

    override fun onLongPressed(view: View, pos: Int, id: Long) {

    }
}