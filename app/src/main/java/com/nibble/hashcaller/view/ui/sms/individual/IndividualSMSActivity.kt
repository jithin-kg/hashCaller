package com.nibble.hashcaller.view.ui.sms.individual

import android.Manifest
import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.telephony.SubscriptionManager
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.klinker.android.send_message.Message
import com.klinker.android.send_message.Settings
import com.klinker.android.send_message.Transaction
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivityIndividualSMSBinding
import com.nibble.hashcaller.utils.SmsStatusDeliveredReceiver
import com.nibble.hashcaller.utils.SmsStatusSentReceiver
import com.nibble.hashcaller.utils.auth.TokenHelper
import com.nibble.hashcaller.utils.extensions.requestDefaultSMSrole
import com.nibble.hashcaller.view.ui.call.dialer.util.CustomLinearLayoutManager
import com.nibble.hashcaller.view.ui.contacts.isDefaultSMSHandler
import com.nibble.hashcaller.view.ui.contacts.utils.*
import com.nibble.hashcaller.view.ui.sms.individual.util.*
import com.nibble.hashcaller.view.ui.sms.util.SMS
import com.nibble.hashcaller.view.utils.HorizontalDottedProgress
import kotlinx.android.synthetic.main.activity_individual_s_m_s.*
import kotlinx.android.synthetic.main.bottom_sheet_block.*
import kotlinx.android.synthetic.main.bottom_sheet_block_feedback.*
import kotlinx.coroutines.flow.collect
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timerTask


class IndividualSMSActivity : AppCompatActivity(),
    SMSIndividualAdapter.ItemPositionTracker, View.OnClickListener,
    AdapterView.OnItemSelectedListener, android.widget.PopupMenu.OnMenuItemClickListener,
    PopupMenu.OnMenuItemClickListener, SearchView.OnQueryTextListener,
    SMSIndividualAdapter.LongPressHandler {
    private lateinit var binding: ActivityIndividualSMSBinding
    private lateinit var viewModel:SMSIndividualViewModel
    private var oldList = mutableListOf<SMS>()
    private var contactAddress = ""
    private var oldLIstSize = 0
    private var countNewItem = 0
    private var recyclerViewAtEnd = true
    private var firstime = true
    private lateinit var adapter:SMSIndividualAdapter
    private lateinit var layoutMngr:LinearLayoutManager
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var bottomSheetDialogfeedback: BottomSheetDialog
    private var threadID = -1L // sms thread id not java thread
    private  var menuSMS:Menu? = null
    private var isTheNumberBlocked:MutableLiveData<Boolean> = MutableLiveData(false)
    private  var spinnerSelected: MutableLiveData<Boolean> = MutableLiveData(false);
    private  var selectedRadioButton:RadioButton? = null
    private var isSmsChannelBusy = false // to know whether there is an sms is currently sending
    private var permissionGivenLiveDAta: MutableLiveData<Boolean> = MutableLiveData(false)
    private var defaultSMSHandlerLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private  var spammerType:Int = SPAMMER_TYPE_SCAM
    var spamTypes:MutableList<String> = ArrayList<String>()
    private var participants = ArrayList<SimpleContact>()
    private var address = ""
    private var queryText:String? = ""

//    private var sendBroadcastReceiver: BroadcastReceiver = SentReceiver()
//    private var deliveryBroadcastReceiver: BroadcastReceiver = DeliverReceiver()

    //    private var newSize = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIndividualSMSBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val itemAnimator: SimpleItemAnimator? =
            binding.recyclerViewSMSIndividual.itemAnimator as SimpleItemAnimator?
//        itemAnimator?.setSupportsChangeAnimations(false)
        binding.recyclerViewSMSIndividual.itemAnimator = null
        spinnerSelected.value = false

//        messageSent.value = false
        setSupportActionBar(binding.toolbarSMSIndividual)
        this.spamTypes.add(0, "Spam type")
        this.spamTypes.add("Public service")
        this.spamTypes.add("Robocall")
        this.spamTypes.add("Survey")
        when{
            intent.action == Intent.ACTION_SENDTO->{
                //intent from invite, etc
                Log.d(TAG, "onCreate: send sms to ")
                val body = intent.getStringExtra("sms_body")
                contactAddress = intent.data.toString()


            }else->{
            //normal intent within app intent
//            contactAddress = "+"+ intent.getStringExtra(CONTACT_ADDRES)
            val bundle = intent.getExtras()

            contactAddress = bundle!!.getString(CONTACT_ADDRES)!!
            chatId = bundle!!.getString(SMS_CHAT_ID)!!
             queryText = bundle!!.getString(QUERY_STRING)


            Log.d(TAG, "onCreate:queryText $queryText ")
            Log.d(TAG, "onCreate: chatId $chatId")
            Log.d(TAG, "onCreate: contactAdderss $contactAddress")
        }

        }

        contact = contactAddress
        Log.d(TAG, "onCreate: contact is $contact")
        if(checkRequiredPermission()){
            setupBottomSheet()
            observerSmsSent()
            initViewModel()
            initAdapter()
            initListners()
            checkDefaultSMSHandlerSettings()
            setupClickListerner()
            registerAdapterListener()
            observeViewmodelSms()
            setupSIMSelector()
            observeSmsLiveData()
            observeDefaultSMSHandlerPermission()
            observeSpinnerSelected()
            addOrRemoveMenuItem()
            getContactInformation()
            observeContactname()
            configureToolbar()
            observeMarkedViews()
            observerPermission()
            observeNoSimCardException()
            markAsRead(contactAddress)

        }


//       GlobalScope.launch(Dispatchers.IO) {
//          val time = measureTimeMillis {
//              val res1 = async {suspendOne()  }
//              val res2 =  async { suspendTwo() }
//              Log.d("__suspend", "res1 is ${res1.await()}")
//              Log.d("__suspend", "res2 is ${res2.await()}")
//          }
//           Log.d("__suspend", "request took $time milliseconds")
//       }
//        Log.d("__suspend", "after of launch: ")
    }

    private fun markAsRead(contactAddress: String) {
        viewModel.markAsRead(contactAddress)
    }


    private fun observeNoSimCardException() {
        this.viewModel.noSimCardException.observe(this, Observer {
            if (it == true) {
                toast("Please make sure sim card are inserted")
            }
        })
    }

    private fun checkRequiredPermission(): Boolean {
        val permissionContact =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
        if(permissionContact!= PackageManager.PERMISSION_GRANTED){
            return false
        }
        return true
    }

    private fun observeDefaultSMSHandlerPermission() {
        this.defaultSMSHandlerLiveData.observe(this, Observer {
            if (it == true) {
                binding.btnMakeDefaultSMS.beInvisible()
                binding.layoutSend.beVisible()
            } else {
                binding.btnMakeDefaultSMS.beVisible()
                binding.layoutSend.beInvisible()
            }
        })
    }

    private fun observerPermission() {
        permissionGivenLiveDAta.observe(this, Observer { it ->
//            run {
            if (it == true) {
//                setSharedPref(true)
                val i = Intent()
                setResult(PERMISSION_RESULT_CODE, i)
//                finish()

            }
//            }
        })
    }

    private fun observeMarkedViews() {
        this.viewModel.markedViewsLiveData.observe(this, Observer {
            Log.d(TAG, "observeMarkedViews: $it")
            if (it != null) {
                val view = it.findViewById<ConstraintLayout>(R.id.layoutSMSReceivedItem)
                view.setBackgroundColor(resources.getColor(R.color.numbersInnerTextColor))
            }
        })
    }


//    private suspend fun suspendTwo(): String {
//        delay(3000L)
//        return "1"
//    }
//
//    private suspend fun suspendOne(): String {
//        delay(3000L)
//        return "1"
//    }


    private fun observeContactname() {
        this.viewModel.nameLiveData.observe(this, Observer {
            if (!it.isNullOrBlank()) {
                name = it
                Log.d(TAG, "observeContactname: $it")
                contactAddress = it
                binding.tvSMSAddress.text = contactAddress

            }
        })
    }

    private fun getContactInformation() {
        Log.d(TAG, "getContactInformation: ")
        this.viewModel.getContactInfoForNumber(contactAddress)
    }


    private fun addOrRemoveMenuItem() {




    }


    private fun setupBottomSheet() {
        bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialogfeedback = BottomSheetDialog(this)
        val viewSheet = layoutInflater.inflate(R.layout.bottom_sheet_block, null)
        val viewSheetFeedback = layoutInflater.inflate(R.layout.bottom_sheet_block_feedback, null)

        bottomSheetDialog.setContentView(viewSheet)
        bottomSheetDialogfeedback.setContentView(viewSheetFeedback)

        selectedRadioButton = bottomSheetDialog.radioScam
//        bottomSheetDialog.imgExpand.setOnClickListener(this)




//        if(this.view?.visibility == View.VISIBLE){
//            bottomSheetDialog.hide()

//        }

        bottomSheetDialog.setOnDismissListener{
            Log.d(TAG, "bottomSheetDialogDismissed")

        }
    }

    private fun configureToolbar() {
            binding.toolbarSMSIndividual.inflateMenu(R.menu.individual_sms_menu)
        Log.d(TAG, "configureToolbar: name $contactAddress")
        tvSMSAddress.text = contactAddress

//        toolbarSMSIndividual.add
    }

    private fun observerSmsSent() {
//        messageSent.observe(this, Observer {
//                it->
//            run {
//            if (it == true) {
//                viewModel.moveToSent(time, address)

//                GlobalScope.launch {
//
//                    val values = ContentValues()
//                    values.put("type", "2")
//                    Log.d(TAG, "observerSmsSent: date $time")
//                    Log.d(TAG, "observerSmsSent: address $address")
////                    viewModel.moveToSent(time, address)
////                    contentResolver.update(SMSContract.ALL_SMS_URI, values, "_id='$id'  AND address='$address'",null)
////                   val selctionClause =  Telephony.TextBasedSmsColumns.DATE + " LIKE '$time' AND  "
//                    contentResolver.update(SMSContract.ALL_SMS_URI, values, "date = '$time' and address='$address'",null)
//
//                }

//            }
//            }
//        })
    }

    private fun isThisNumBlocked() {
        viewModel.getblockedStatusOfThenumber(contactAddress)
        viewModel.blockedStatusOfThenumber?.observe(this, Observer {
            if (it != null) {
                Log.d(TAG, "isThisNumBlocked: contact is $contact")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    isTheNumberBlocked.value = it.stream()
                        .anyMatch { t -> t.contactAddress.equals(contact) }
                    Log.d(TAG, "isThisNumBlocked: ")

                }

            }

        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.individual_sms_menu, menu)
//        super.onCreateOptionsMenu(menu);
         this.menuSMS = menu
//        if(isTheNumberBlocked.value!!){
//           toggleBlockMenu()
//        }
        observeIsContactBlocked()


        return true
    }

    private fun observeIsContactBlocked() {
        this.isTheNumberBlocked.observe(this, Observer { it ->
            if (it == true) {
                toggleBlockMenu(false)
            } else if (it == false) {
                toggleBlockMenu(true)
            }
        })
    }

    private fun toggleBlockMenu(value: Boolean) {
        val item = this.menuSMS!!.findItem(R.id.itemBlock)
        val unblockItem = this.menuSMS?.findItem(R.id.itemUnBlock)

//        item?.isVisible = false
//        item?
        this.menuSMS!!.getItem(0)!!.isVisible = value
        this.menuSMS?.getItem(1)?.isVisible = !value
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected: ")
        return when(item.itemId){

            R.id.itemBlock -> {
                Log.d(TAG, "onOptionsItemSelected: block")
                showBlockBottomSheet()
                true

            }
            R.id.itemUnBlock -> {
                viewModel.unblock(this.contactAddress)
                true
            }

            else->{
                super.onOptionsItemSelected(item)
            }
        }

    }

    private fun showBlockBottomSheet() {
        bottomSheetDialog.show()
//        val sheet = BlockDialogFragment.newInstance(1)
//        sheet.show(supportFragmentManager,"ItemListDialogFragment" )

    }


    private fun initListners() {
        binding.imgBtnSendSMS.setOnClickListener(this)
        binding.imgBtnBackSmsIndividual.setOnClickListener(this)
        bottomSheetDialog.radioSales.setOnClickListener(this)
        bottomSheetDialog.radioScam.setOnClickListener(this)
//        bottomSheetDialog.imgExpand.setOnClickListener(this)
        bottomSheetDialog.btnBlock.setOnClickListener(this)
        binding.imgViewCallBtn.setOnClickListener(this)
        binding.imgBtnSearchSMS.setOnClickListener(this)
        binding.sViewIndividualSMS.setOnQueryTextListener(this)
        binding.imgBtnSMSUp.setOnClickListener(this)
        binding.imgBtnSMSDown.setOnClickListener(this)
        binding.btnMakeDefaultSMS.setOnClickListener(this)

//        bottomSheetDialog.btnBlock.setOnClickListener(this)
//        imgViewCallBtn.setOnClickListener(this)
//        bottomSheetDialog.radioButtonSales.setOnClickListener(this)
//        bottomSheetDialog.radioButtonScam.setOnClickListener(this)
////        bottomSheetDialog.radioButtonSales.setOnCheckedChangeListener(this)
//        bottomSheetDialog.radioGroupSpamType.setOnClickListener(this)

//        btnUpdate.setOnClickListener(this)
    }

    private fun observeSmsLiveData() {
        viewModel.smsLiveData.observe(this, Observer {

            adapter.setList(it)
            viewModel.setHashMap(it)

//            if(!isSmsChannelBusy && !smsQueue.isEmpty()){
//                sendSmsToClient(smsQueue.remove())
//                isSmsChannelBusy = true
//            }
            if (chatId.isNotEmpty()) {
                SCROLL_TO_POSITION = null
                Log.d(TAG, "observeSmsLiveData:chat id  $chatId")
                //intent from sms search activity
//                recyclerView.scrollToPosition(chatScrollToPosition);
                scrollTOPosition(chatScrollToPosition, layoutMngr)
            } else if (SCROLL_TO_POSITION != null) {
//                scrollTOPosition(null, layoutMngr)
                SearchUpAndDownHandler.scrollUp(layoutMngr)

            } else {
                if (firstime) {
                    scrollTOPosition(it.size - 1, layoutMngr)
//                    recyclerView.scrollToPosition(it.size - 1);
                    firstime = false
                }
            }


            if (!recyclerViewAtEnd) {
                countNewItem = it.size - oldLIstSize
                binding.tvcountShow.text = countNewItem.toString()
                if (countNewItem > 0) {
                    binding.tvcountShow.beVisible()
                } else {
                    binding.tvcountShow.beInvisible()
                }
            } else {
                clearNewMessageIndication()
                oldLIstSize = it.size
            }

//                recyclerView.scrollToPosition(adapter.itemCount -1)
            //  adapter.notifyItemRangeInserted(adapter.itemCount, it!!.size -1 )
            if (recyclerViewAtEnd) {
//                    recyclerView.scrollToPosition(it.size-1)

            }
        })
    }


    private fun observeViewmodelSms() {
        viewModel.SMS.observe(this, Observer { sms ->
            sms.let {
//                smsRecyclerAdapter?.setSMSList(it, searchQry)
//                adapter.submitList(it)
//                newSize = it.size
//                Log.d(TAG, "setupViewmodelObserver: size : ${it.size}")
//                Log.d(TAG, "setupViewmodelObserver:  type of last item ${it[it.size-1].type}")
//                Log.d(TAG, "setupViewmodelObserver: msg of last item ${it[it.size-1].msgString}")
//                Log.d(TAG, "setupViewmodelObserver: msg address ${it[it.size-1].addressString}")
//                Log.d(TAG, "setupViewmodelObserver: msg time ${it[it.size-1].time}")
                viewModel.smsLiveData.value = sms as MutableList<SMS>?
//                val list:MutableList<SMS> = mutableListOf()
//                list.addAll(it)
//                for (item in smsQueue){
//                    list.add(item)
//                }
//                this.allTypeOfSmsList.addAll(this.)

                if (it.size > 1)
                    this.threadID = it[it.size - 1].threadID
//                Log.d(TAG, "observeViewmodelSms: sms changed")
//                Log.d(TAG, "observeViewmodelSms: last item sms  ${it[it.size-1].msgString} ")
//                Log.d(TAG, "observeViewmodelSms: last item type  ${it[it.size-1].type} ")
//                Log.d(TAG, "observeViewmodelSms: last item msgtype  ${it[it.size-1].msgType} ")
            }
        })
        isThisNumBlocked()

    }

    private fun checkDefaultSMSHandlerSettings(): Boolean {
        var requestCode=  222
        var resultCode = 232
        var isDefault = false
            if(isDefaultSMSHandler()){
                binding.layoutSend.beVisible()
                binding. btnMakeDefaultSMS.beInvisible()
                isDefault = true
            }else {
                binding.layoutSend.beInvisible()
                binding.btnMakeDefaultSMS.beVisible()
            }

        return isDefault
    }

    private fun isSMSSendPermissionAvailable(): Boolean {
        val permissionContact =
            ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
        if(permissionContact!= PackageManager.PERMISSION_GRANTED){
            return false
        }
        return true
    }

    private fun requesetPermission(): Boolean {
        var permissionGiven = false

        //persmission
//        Dexter.withContext(this)
//            .withPermissions(
//                Manifest.permission.CALL_PHONE,
//                Manifest.permission.READ_PHONE_STATE,
//                Manifest.permission.ANSWER_PHONE_CALLS,
//                Manifest.permission.READ_CALL_LOG,
//                Manifest.permission.RECEIVE_MMS,
//                Manifest.permission.SEND_SMS,
//                Manifest.permission.READ_SMS
//
//            ).withListener(object : MultiplePermissionsListener {
//                override fun onPermissionsChecked(report: MultiplePermissionsReport?) { /* ... */
////
//                    report.let {
//                        if(report?.areAllPermissionsGranted()!!){
//                            permissionGiven = true
////                            setSharedPref(true)
//                            permissionGivenLiveDAta.value = true
////                            Toast.makeText(applicationContext, "thank you", Toast.LENGTH_SHORT).show()
//
//                        }
//                    }
//                }
//
//                override fun onPermissionRationaleShouldBeShown(
//                    permissions: List<PermissionRequest?>?,
//                    token: PermissionToken?
//                ) { /* ... */
//                    token?.continuePermissionRequest()
////                    Toast.makeText(applicationContext, "onPermissionRationaleShouldBeShown", Toast.LENGTH_SHORT).show()
//                }
//            }).check()
        return permissionGiven
    }


    @SuppressLint("LongLogTag")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        /**
         * Set as default SMS app onActivityResult if user chosen as deafult SMS app
         * is -1
         * else the result is 0
         */

        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== -1 && requestCode == 222){
            permissionGivenLiveDAta.value  = true
            defaultSMSHandlerLiveData.value = requesetPermission()
        }
        Log.d(TAG, "onActivityResult: requestCode :$requestCode")
        Log.d(TAG, "onActivityResult: resultCode :$resultCode")

    }
    private fun sendSmsToClient(sms: SMS?) {
        Log.d(TAG, "sendSmsToClient: ")
        Timer().schedule(timerTask {
            LAST_SMS_SENT = false
            this@IndividualSMSActivity.isSmsChannelBusy = true
            val settings = Settings()
            settings.useSystemSending = true;
            settings.deliveryReports =
                true //it is importatnt to set this for the sms delivered status
            val msg = sms!!.msgString

            val transaction = Transaction(this@IndividualSMSActivity, settings)
            val message = Message(msg, "919495617494")
//        message.setImage(mBitmap);

            val smsSentIntent = Intent(
                this@IndividualSMSActivity,
                SmsStatusSentReceiver::class.java
            )
            val deliveredIntent = Intent(
                this@IndividualSMSActivity,
                SmsStatusDeliveredReceiver::class.java
            )
            transaction.setExplicitBroadcastForSentSms(smsSentIntent)
            transaction.setExplicitBroadcastForDeliveredSms(deliveredIntent)

            transaction.sendNewMessage(message, 133)

        }, 5000)


    }

    @SuppressLint("MissingPermission")
    private fun setupSIMSelector() {
        val subscriptionManager = getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val availableSIMs  = subscriptionManager.activeSubscriptionInfoList
//        val res1 = subscriptionManager.getSubscriptionIds(0)
//        val res2 = subscriptionManager.getSubscriptionIds(1)
        //89918620400015105496, 8991462161032218326F
//        availableSIMs[0].iccId
//        val availableSIMs = SubscriptionManager.from(this).activeSubscriptionInfoList ?: return

        if (availableSIMs.size > 0) {
            var index = 0
            for(subscriptionInfo in availableSIMs){
                var label = subscriptionInfo.displayName?.toString() ?: ""
                if (subscriptionInfo.number?.isNotEmpty() == true) {
                    label += " (${subscriptionInfo.number})"
                }
                val SIMCard =
                    SIMCard(
                        index + 1,
                        subscriptionInfo.subscriptionId,
                        label
                    )

                viewModel.availableSIMCards.add(SIMCard)
                index++

            }
//            availableSIMs.forEachIndexed { index, subscriptionInfo ->
//                var label = subscriptionInfo.displayName?.toString() ?: ""
//                if (subscriptionInfo.number?.isNotEmpty() == true) {
//                    label += " (${subscriptionInfo.number})"
//                }
//                val SIMCard =
//                    SIMCard(
//                        index + 1,
//                        subscriptionInfo.subscriptionId,
//                        label
//                    )
//                availableSIMCards.add(SIMCard)
//            }

            val numbers = ArrayList<String>()
            for(simpleContact in participants){
                for(num in simpleContact.phoneNumbers){
                    numbers.add(num)

                }
            }
            viewModel.currentSIMCardIndex = 0
//            currentSIMCardIndex = availableSIMs.indexOfFirstOrNull { it.subscriptionId == config.getUseSIMIdAtNumber(numbers.first()) } ?: 0

            binding.threadSelectSimIcon.applyColorFilter(config.textColor)
            binding.threadSelectSimIcon.beVisible()
            binding.threadSelectSimIcon.beVisible()

            if (viewModel.availableSIMCards.isNotEmpty()) {
                threadSelectSimIcon.setOnClickListener {
                    Log.d(TAG, "setupSIMSelector: ")
                    viewModel.currentSIMCardIndex = (viewModel.currentSIMCardIndex + 1) % viewModel.availableSIMCards.size
                    val currentSIMCard = viewModel.availableSIMCards[viewModel.currentSIMCardIndex]
                    binding.threadSelectSimNumber.text = currentSIMCard.id.toString()
                    toast(currentSIMCard.label)
                }
            }

            binding.threadSelectSimNumber.setTextColor(config.textColor.getContrastColor())
           binding. threadSelectSimNumber.text = (viewModel.availableSIMCards[viewModel.currentSIMCardIndex].id).toString()
        }
    }
    private fun sendSms() {
//        messageSent.value = false
        /**
         * When there is no network the messages is added to the queue
         * and at the moment user clicks send the message is added to outbox
         * and later updated to sended in table ,meanwhile when user starts typing the messsage is added to draft
         */
        val smsObj = SMS()
        smsObj.msgString = binding.edtTxtMSg.text.toString()
        Log.d(TAG, "sendSms: ${edtTxtMSg.text}")
        if(binding.edtTxtMSg.text.isNotEmpty()){
            smsObj.msgType = 4
            smsObj.type = 4

            try {
                this.viewModel.sendSmsToClient(smsObj, this, this.threadID, contact)
//                viewModel.smsLiveData.value!!.add(smsObj)
//                viewModel.smsLiveData.value = viewModel.smsLiveData.value
            }catch (e: java.lang.Exception){
                toast("No sim detected ")
            }

        }else{
            toast("please Enter a message to send ", Toast.LENGTH_SHORT)
        }


//        this.smsQueue.add(smsObj)
//        if(!smsQueue.isNullOrEmpty()){
//            for (qitem in smsQueue){
//                this.smsLiveData.value!!.add(qitem)
//            }
//        }
//        this.smsLiveData.value = this.smsLiveData.value

//            sendSmsToClient(smsObj)


//        Log.d(TAG, "sendSms: clicked contact address $contactAddress")
//        this.allTypeOfSmsList.add(SMS())
//        val msg = edtTxtMSg.text.toString()
////        viewModel.sendSms(msg, applicationContext, contactAddress)
//        val settings = Settings()
//        settings.useSystemSending = true;
//        settings.deliveryReports = true //it is importatnt to set this for the sms delivered status
//
//        val transaction = Transaction(this, settings)
//        val message = Message(msg, "919495617494")
////        message.setImage(mBitmap);
//
//        val smsSentIntent = Intent(this, SmsStatusSentReceiver::class.java)
//        val deliveredIntent = Intent(this, SmsStatusDeliveredReceiver::class.java)
//        transaction.setExplicitBroadcastForSentSms(smsSentIntent)
//        transaction.setExplicitBroadcastForDeliveredSms(deliveredIntent)
//
//        transaction.sendNewMessage(message, 133)






    }

    private fun clearNewMessageIndication() {
        binding.tvcountShow.visibility = View.GONE
        binding.tvcountShow.text = ""
        binding.smsGoDownIndication.beGone()

    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(
            this, SMSIndividualInjectorUtil.provideViewModelFactory(
                applicationContext,
                lifecycleScope,
                TokenHelper(FirebaseAuth.getInstance().currentUser)
            )
        ).get(
            SMSIndividualViewModel::class.java
        )
    }

    private fun registerAdapterListener() {
        //for auto scrolling
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)

                val msgCount = adapter.getItemCount()
                val lastVisiblePosition =
                    layoutMngr.findLastCompletelyVisibleItemPosition()

                if (lastVisiblePosition == -1 || positionStart >= msgCount - 1 &&
                    lastVisiblePosition == positionStart - 1
                ) {
                    binding.recyclerViewSMSIndividual.scrollToPosition(positionStart)
                } else {
                    if (recyclerViewAtEnd)
                        binding.recyclerViewSMSIndividual.scrollToPosition(adapter.getItemCount() - 1);
                }
            }
        })
    }

    private fun initAdapter() {
//        recyclerView =
//            findViewById<View>(R.id.recyclerViewSMSIndividual) as RecyclerView


        adapter = SMSIndividualAdapter(this, this, this, queryText, chatId){ id: String -> onContactitemClicked(
            id
        ) }

        binding.recyclerViewSMSIndividual.setHasFixedSize(true)
        binding.recyclerViewSMSIndividual.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewSMSIndividual.adapter = adapter

        binding.recyclerViewSMSIndividual.setHasFixedSize(true)
        layoutMngr = CustomLinearLayoutManager(this)
        layoutMngr.stackFromEnd = true
        binding.recyclerViewSMSIndividual.layoutManager = layoutMngr

        binding.recyclerViewSMSIndividual.adapter = adapter
        binding.recyclerViewSMSIndividual.isNestedScrollingEnabled = false
    }

    private fun setupClickListerner() {
        binding.smsGoDownIndication.setOnClickListener(this)
    }

    private fun onContactitemClicked(id: String) {

    }

    companion object{
        var name = ""
        var chatId = ""
        var chatScrollToPosition = 0 //incase intent from SearchActivity we need
                                    // to scroll to thatexact sms
        var contact:String? = null
        const val TAG = "__IndividualSMSActivity"
        lateinit var dotedPg:HorizontalDottedProgress

        private fun stopDotedAnimation() {
//            dotedPgSMSSenging.clearAnimation()
//            dotedPgSMSSenging.visibility = View.GONE
        }
    }

    override fun lastItemReached() {

        this.recyclerViewAtEnd = true
        binding.smsGoDownIndication.beGone()
        clearNewMessageIndication()

    }

     override fun otherPosition() {
        this.recyclerViewAtEnd = false
        binding.smsGoDownIndication.beVisible()
    }

    override fun shouldWeScroll() {
        if(this.recyclerViewAtEnd){
//            recyclerView.scrollToPosition(newSize-1)
        }
    }

    override fun onClick(v: View?) {

        when(v?.id){

            R.id.smsGoDownIndication -> {
                binding.recyclerViewSMSIndividual.scrollToPosition(adapter.itemCount - 1)
                clearNewMessageIndication()
                binding.smsGoDownIndication.beGone()

            }
            R.id.imgBtnSendSMS -> {
                Log.d(TAG, "onClick: ")
                sendSms()
            }
            R.id.imgBtnBackSmsIndividual -> {
                onBackPressedIn()
            }
            R.id.btnBlock -> {
                Log.d(TAG, "onClick: ")
                addToBlockList(contact!!)
            }
//            R.id.imgExpand->{
//                Log.d(TAG, "onClick: img button")
//                val popup = PopupMenu(this, bottomSheetDialog.viewPopup)
//                popup.inflate(R.menu.image_chooser_popup)
//                popup.setOnMenuItemClickListener(this)
//                popup.show()
//
//            }
            R.id.imgViewCallBtn -> {
                call(contactAddress)
            }
            R.id.imgBtnSearchSMS -> {
                Log.d(TAG, "onClick: imgBtnSearchSMS")
                showSearchView()
            }
            R.id.imgBtnSMSUp -> {
                scrollUp()

            }
            R.id.imgBtnSMSDown -> {
                scrollDown()

            }
            R.id.btnMakeDefaultSMS -> {
                requestDefaultSMSrole()
            }
//            R.id.btnUpdate->{
//            viewModel.update()
//        }
            else->{
                this.radioButtonClickPerformed(v)
            }

        }
    }




    private fun scrollUp() {
        Log.d(TAG, "scrollUp: clicked")
        SearchUpAndDownHandler.scrollUp(layoutMngr)
        
    }

    private fun scrollDown() {
        SearchUpAndDownHandler.scrollDown(layoutMngr)



//        if(scrollToPositions.isNotEmpty()){
//            val index = scrollToPositions.indexOf(SCROLL_TO_POSITION)
//            if(index - 1 > 0){
//                scrollTOPosition(scrollToPositions[index-1], layoutMngr)
//
//            }
//        }
    }

    private fun onBackPressedIn() {
        if(binding.sViewIndividualSMS.visibility == View.VISIBLE){

            binding.sViewIndividualSMS.beGone()
            binding.imgBtnBackSmsIndividual.beGone()
            binding.imgBtnSMSUp.beInvisible()
            binding.imgBtnSMSDown.beInvisible()
            binding.tvSMSAddress.beVisible()
            binding.imgViewCallBtn.beVisible()
            binding.imgBtnSearchSMS.beVisible()

            binding.toolbarSMSIndividual.menu.findItem(R.id.itemBlock).isVisible = true
            binding.toolbarSMSIndividual.menu.findItem(R.id.itemUnBlock).isVisible = true
            binding.toolbarSMSIndividual.menu.findItem(R.id.itemSettings).isVisible = true

        }else{
            finishAfterTransition()

        }
    }

    private fun showSearchView() {
//        sViewIndividualSMS.requestFocus()
        binding.sViewIndividualSMS.beVisible()
        binding.imgBtnBackSmsIndividual.beVisible()
        binding.imgBtnSMSUp.beVisible()
        binding.imgBtnSMSDown.beVisible()
        binding.tvSMSAddress.beGone()
        binding.imgViewCallBtn.beGone()
        binding.imgBtnSearchSMS.beGone()

        binding.toolbarSMSIndividual.menu.findItem(R.id.itemBlock).isVisible = false
        binding.toolbarSMSIndividual.menu.findItem(R.id.itemUnBlock).isVisible = false
        binding.toolbarSMSIndividual.menu.findItem(R.id.itemSettings).isVisible = false
       requestSearchViewFocus(binding.sViewIndividualSMS, this)

    }

    private fun startSearchActivity() {
//        val intent = Intent(this, SearchActivity::class.java)
//        intent.putExtra("animation", "explode")
////        val extras = Bundle()
////        extras.putString(CONTACT_ADDRES, contactAddress)
////        extras.putString("animation", "explode")
//        intent.putExtra(CONTACT_ADDRES, contactAddress)
//        startActivity(intent)
    }

    private fun radioButtonClickPerformed(v: View?) {
        if(v is RadioButton){

            when(v.id){
                R.id.radioScam -> {
                    val checked = v.isChecked
                    if (checked) {
                        selectedRadioButton = bottomSheetDialog.radioScam
                        this.spammerType = SPAMMER_TYPE_SCAM
                    }
                }
                R.id.radioSales -> {

                    val checked = v.isChecked
                    if (checked) {
                        selectedRadioButton = bottomSheetDialog.radioSales
                        this.spammerType = SPAMMER_TYPE_SALES
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

    private fun observeSpinnerSelected() {
        this.spinnerSelected.observe(this, Observer { spinnerSelected ->
            if (spinnerSelected) {
//                selectedRadioButton?.isChecked = false
            }
        })
    }

    override fun onItemSelected(parent: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
        this.spammerType = position
        if(parent?.getItemAtPosition(position)?.equals("Spam type")!!){
        }else{
            selectedRadioButton?.isChecked = false
        }
    }
    override fun onNothingSelected(p0: AdapterView<*>?) {
    }
    private fun addToBlockList(no: String) {
        viewModel.blockThisAddress(no, this.threadID, this.spammerType)
        Toast.makeText(this, "Number added to spamlist", Toast.LENGTH_LONG)
        bottomSheetDialog.hide()
        bottomSheetDialog.dismiss()

        bottomSheetDialogfeedback.show()
        var txt = "$contactAddress can no longer send SMS or call you."

        val  sb =  SpannableStringBuilder(txt);

        val bss =  StyleSpan(Typeface.BOLD); // Span to make text bold
        sb.setSpan(bss, 0, contactAddress.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE); // make first 4 characters Bold
        bottomSheetDialogfeedback.tvSpamfeedbackMsg.text = sb
    }


    override fun onResume() {
        super.onResume()
        registerReceiver(messagesReceiver, IntentFilter("myhashcallersms"))
        checkDefaultSMSHandlerSettings()

    }

    override fun onDestroy() {
        unregisterReceiver(messagesReceiver)
        super.onDestroy()
    }

    //receiver as a global variable in your Fragment class
    private val messagesReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.extras != null) {
//                val smsMessageStr = intent.extras!!.getString("idvalue")
                Log.d("messagesReceiver", "onReceive: intent ")
                val extras= intent.extras
                 var time = extras?.getString("date")!!
                 address = extras?.getString("address")!!
//                Log.d(TAG, "onReceive:id $id")
                Log.d(TAG, "onReceive: address $address")
                Log.d(TAG, "onReceive: time $time")
                val values = ContentValues().apply {
                    put("type", "2")
                }
//                viewModel.unregister()
                viewModel.moveToSent(time, address)

//                    Log.d(TAG, "observerSmsSent: date $time")
//                    Log.d(TAG, "observerSmsSent: address $address")
//                contentResolver.update(SMSContract.ALL_SMS_URI, values, "date = '$time'",null)
//                contentResolver.update(SMSContract.ALL_SMS_URI, values, "date = '$time' and address='$address'",null)
//                messageSent.value = true
            }
        }
    }

    override fun onMenuItemClick(menuItem: MenuItem?): Boolean {
        Log.d(TAG, "onMenuItemClick:id ${menuItem!!.itemId}")
//           this.spammerType = SpamLocalListManager.menuItemClickPerformed(menuItem, bottomSheetDialog)


            return false
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        viewModel.searchForSMS(query)
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return false
    }


    override fun onLongPressed(view: View, pos: Int, id: Long) {
        Log.d(TAG, "onLongPressed: $pos , id : $id")


        lifecycleScope.launchWhenStarted {
            viewModel.markItem(id, view, pos).collect{
                if(it!=null){
                    val view = it.findViewById<ConstraintLayout>(R.id.layoutSMSReceivedItem)
                    view.setBackgroundColor(
                        ContextCompat.getColor(
                            this@IndividualSMSActivity,
                            R.color.numbersInnerTextColor
                        )
                    )
                }
            }
        }
//        if(!isMarkedViewsEmpty()){
//            val flow = flow<View> {
//                viewModel.markItem(id, view, pos)
//
//                for(view in getMarkedViews()){
//                    emit(view)
//                }
//
//            }
//            //important to call launch when started there by
//            //only launch coroutine when app is running,
//            //launch will work even if app is in background, and app will
//            //crash if we access view elements from launch
//            lifecycleScope.launchWhenStarted {
//                flow.collect{
//                    if(it!=null){
//                       val view = it.findViewById<ConstraintLayout>(R.id.layoutSMSReceivedItem)
//                        view.setBackgroundColor(ContextCompat.getColor(this@IndividualSMSActivity, R.color.numbersInnerTextColor))
//
//                    }
//            }
//
//            }
//        }




    }

    override fun onBackPressed() {
        finishAfterTransition()
    }
}

