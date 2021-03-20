package com.nibble.hashcaller.view.ui.sms.individual

import android.annotation.SuppressLint
import android.content.*
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.klinker.android.send_message.Message
import com.klinker.android.send_message.Settings
import com.klinker.android.send_message.Transaction
import com.nibble.hashcaller.R
import com.nibble.hashcaller.utils.SmsStatusDeliveredReceiver
import com.nibble.hashcaller.utils.SmsStatusSentReceiver
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ADDRES
import com.nibble.hashcaller.view.ui.contacts.utils.LAST_SMS_SENT
import com.nibble.hashcaller.view.ui.contacts.utils.QUERY_STRING
import com.nibble.hashcaller.view.ui.contacts.utils.SMS_CHAT_ID
import com.nibble.hashcaller.view.ui.sms.individual.util.*
import com.nibble.hashcaller.view.ui.sms.util.SMS
import com.nibble.hashcaller.view.utils.HorizontalDottedProgress
import com.nibble.hashcaller.view.utils.spam.SpamLocalListManager
import kotlinx.android.synthetic.main.activity_individual_s_m_s.*
import kotlinx.android.synthetic.main.bottom_sheet_block.*
import kotlinx.android.synthetic.main.bottom_sheet_block_feedback.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timerTask


class IndividualSMSActivity : AppCompatActivity(),
    SMSIndividualAdapter.ItemPositionTracker, View.OnClickListener,
    AdapterView.OnItemSelectedListener, android.widget.PopupMenu.OnMenuItemClickListener,
    PopupMenu.OnMenuItemClickListener, SearchView.OnQueryTextListener {
    private lateinit var viewModel:SMSIndividualViewModel
    private lateinit var  recyclerView:RecyclerView
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
    private var SPAMMER_CATEGORY = SpamLocalListManager.SPAMMER_BUISINESS
    private var isTheNumberBlocked:MutableLiveData<Boolean> = MutableLiveData(false)


    private  var spinnerSelected: MutableLiveData<Boolean> = MutableLiveData(false);
    private  var selectedRadioButton:RadioButton? = null
    private var isSmsChannelBusy = false // to know whether there is an sms is currently sending


    private  var spammerType:Int = -1
    var spamTypes:MutableList<String> = ArrayList<String>()
//    var  smsLiveData:MutableLiveData<MutableList<SMS>> = MutableLiveData()
    private var allTypeOfSmsList:MutableList<SMS> = mutableListOf()
    private var smsQueueLiveData:MutableLiveData<Queue<SMS>> = MutableLiveData()
    private var smsQueue:Queue<SMS> = LinkedList<SMS>()
    private var participants = ArrayList<SimpleContact>()




    //    private var messageSent: MutableLiveData<Boolean> = MutableLiveData()
//    private var time:String? = null
    private var address = ""

//    private var sendBroadcastReceiver: BroadcastReceiver = SentReceiver()
//    private var deliveryBroadcastReceiver: BroadcastReceiver = DeliverReceiver()

    //    private var newSize = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_individual_s_m_s)
        val itemAnimator: SimpleItemAnimator? =
            recyclerViewSMSIndividual.itemAnimator as SimpleItemAnimator?
//        itemAnimator?.setSupportsChangeAnimations(false)
        recyclerViewSMSIndividual.itemAnimator = null
        spinnerSelected.value = false

//        messageSent.value = false
        setSupportActionBar(toolbarSMSIndividual)
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
            Log.d(TAG, "onCreate: chatId $chatId")
            Log.d(TAG, "onCreate: contactAdderss $contactAddress")
        }
        }

        contact = contactAddress
        Log.d(TAG, "onCreate: contact is $contact")
        setupBottomSheet()
        observerSmsSent()
        initViewModel()
        initAdapter()
        initListners()
        setupClickListerner()
        registerAdapterListener()
        observeViewmodelSms()
        observeSmsLiveData()
        observeSpinnerSelected()
        addOrRemoveMenuItem()
        getContactInformation()
        observeContactname()
        configureToolbar()
        setupSIMSelector()
//
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
            if(!it.isNullOrBlank()){
                name = it
                Log.d(TAG, "observeContactname: $it")
                contactAddress = it
                tvSMSAddress.text = contactAddress

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
        bottomSheetDialog.imgExpand.setOnClickListener(this)




//        if(this.view?.visibility == View.VISIBLE){
//            bottomSheetDialog.hide()

//        }

        bottomSheetDialog.setOnDismissListener{
            Log.d(TAG, "bottomSheetDialogDismissed")

        }
    }

    private fun configureToolbar() {
        toolbarSMSIndividual.inflateMenu(R.menu.individual_sms_menu)
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
            if(it!=null){
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
        this.isTheNumberBlocked.observe(this, Observer {it->
            if(it == true){
                toggleBlockMenu(false)
            }else if (it == false){
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

            R.id.itemBlock->{
                Log.d(TAG, "onOptionsItemSelected: block")
                showBlockBottomSheet()
                true

            }
            R.id.itemUnBlock->{
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
        imgBtnSendSMS.setOnClickListener(this)
        imgBtnBackSmsIndividual.setOnClickListener(this)
        bottomSheetDialog.radioS.setOnClickListener(this)
        bottomSheetDialog.radioScam.setOnClickListener(this)
        bottomSheetDialog.imgExpand.setOnClickListener(this)
        bottomSheetDialog.btnBlock.setOnClickListener(this)
        imgViewCallBtn.setOnClickListener(this)
        imgBtnSearchSMS.setOnClickListener(this)
        sViewIndividualSMS.setOnQueryTextListener(this)

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
//            if(!isSmsChannelBusy && !smsQueue.isEmpty()){
//                sendSmsToClient(smsQueue.remove())
//                isSmsChannelBusy = true
//            }
            if(chatId.isNotEmpty()){
                SCROLL_TO_POSITION = null
                Log.d(TAG, "observeSmsLiveData:chat id  $chatId")
                //intent from sms search activity
//                recyclerView.scrollToPosition(chatScrollToPosition);
                scrollTOPosition(chatScrollToPosition, layoutMngr)
            }else if(SCROLL_TO_POSITION!=null){
                scrollTOPosition(SCROLL_TO_POSITION!!, layoutMngr)

            }

            else{
                if(firstime){
                    scrollTOPosition(it.size - 1 , layoutMngr)
//                    recyclerView.scrollToPosition(it.size - 1);
                    firstime = false
                }
            }


            if(!recyclerViewAtEnd){
                countNewItem = it.size - oldLIstSize
                tvcountShow.text = countNewItem.toString()
                if(countNewItem>0){
                    tvcountShow.beVisible()
                }else{
                    tvcountShow.beInvisible()
                }
            }else{
                clearNewMessageIndication()
                oldLIstSize = it.size
            }

//                recyclerView.scrollToPosition(adapter.itemCount -1)
            //  adapter.notifyItemRangeInserted(adapter.itemCount, it!!.size -1 )
            if(recyclerViewAtEnd){
//                    recyclerView.scrollToPosition(it.size-1)

            }
        })
    }


    private fun observeViewmodelSms() {
        viewModel.SMS.observe(this, Observer { sms->
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

                if(it.size>1)
                this.threadID = it[it.size-1].threadID
//                Log.d(TAG, "observeViewmodelSms: sms changed")
//                Log.d(TAG, "observeViewmodelSms: last item sms  ${it[it.size-1].msgString} ")
//                Log.d(TAG, "observeViewmodelSms: last item type  ${it[it.size-1].type} ")
//                Log.d(TAG, "observeViewmodelSms: last item msgtype  ${it[it.size-1].msgType} ")
            }
        })
        isThisNumBlocked()

    }
    private fun sendSmsToClient(sms: SMS?) {
        Log.d(TAG, "sendSmsToClient: ")
        Timer().schedule(timerTask {
            LAST_SMS_SENT = false
            this@IndividualSMSActivity.isSmsChannelBusy = true
            val settings = Settings()
            settings.useSystemSending = true;
            settings.deliveryReports = true //it is importatnt to set this for the sms delivered status
            val msg = sms!!.msgString

            val transaction = Transaction(this@IndividualSMSActivity, settings)
            val message = Message(msg, "919495617494")
//        message.setImage(mBitmap);

            val smsSentIntent = Intent(this@IndividualSMSActivity, SmsStatusSentReceiver::class.java)
            val deliveredIntent = Intent(this@IndividualSMSActivity, SmsStatusDeliveredReceiver::class.java)
            transaction.setExplicitBroadcastForSentSms(smsSentIntent)
            transaction.setExplicitBroadcastForDeliveredSms(deliveredIntent)

            transaction.sendNewMessage(message, 133)

        }, 5000)


    }

    @SuppressLint("MissingPermission")
    private fun setupSIMSelector() {
        val availableSIMs = SubscriptionManager.from(this).activeSubscriptionInfoList ?: return
        if (availableSIMs.size > 1) {
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
//            participants.forEach {
//                it.phoneNumbers.forEach {
//                    numbers.add(it)
//                }
//            }

//            if (numbers.isEmpty()) {
//                return
//            }
//            for(it in availableSIMs){
//                if(it.subscriptionId == )
//            }
            viewModel.currentSIMCardIndex = 0
//            currentSIMCardIndex = availableSIMs.indexOfFirstOrNull { it.subscriptionId == config.getUseSIMIdAtNumber(numbers.first()) } ?: 0

            thread_select_sim_icon.applyColorFilter(config.textColor)
            thread_select_sim_icon.beVisible()
            thread_select_sim_number.beVisible()

            if (viewModel.availableSIMCards.isNotEmpty()) {
                thread_select_sim_icon.setOnClickListener {
                    Log.d(TAG, "setupSIMSelector: ")
                    viewModel.currentSIMCardIndex = (viewModel.currentSIMCardIndex + 1) % viewModel.availableSIMCards.size
                    val currentSIMCard = viewModel.availableSIMCards[viewModel.currentSIMCardIndex]
                    thread_select_sim_number.text = currentSIMCard.id.toString()
                    toast(currentSIMCard.label)
                }
            }

            thread_select_sim_number.setTextColor(config.textColor.getContrastColor())
            thread_select_sim_number.text = (viewModel.availableSIMCards[viewModel.currentSIMCardIndex].id).toString()
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
        smsObj.msgString = edtTxtMSg.text.toString()
        smsObj.msgType = 4
        smsObj.type = 4
        viewModel.smsLiveData.value!!.add(smsObj)
        viewModel.smsLiveData.value = viewModel.smsLiveData.value

//        this.smsQueue.add(smsObj)
//        if(!smsQueue.isNullOrEmpty()){
//            for (qitem in smsQueue){
//                this.smsLiveData.value!!.add(qitem)
//            }
//        }
//        this.smsLiveData.value = this.smsLiveData.value

        this.viewModel.sendSmsToClient(smsObj, this, this.threadID, contact)
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
        tvcountShow.visibility = View.GONE
        tvcountShow.text = ""
        smsGoDownIndication.beGone()

    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this, SMSIndividualInjectorUtil.provideViewModelFactory(this)).get(
            SMSIndividualViewModel::class.java)
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
                    lastVisiblePosition == positionStart - 1) {
                    recyclerView.scrollToPosition(positionStart)
                } else {
                    if(recyclerViewAtEnd)
                        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                }
            }
        })
    }

    private fun initAdapter() {
        recyclerView =
            findViewById<View>(R.id.recyclerViewSMSIndividual) as RecyclerView


        adapter = SMSIndividualAdapter(this, applicationContext ){ id:String -> onContactitemClicked(id) }

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        recyclerView.setHasFixedSize(true)
        layoutMngr = LinearLayoutManager(this)
        layoutMngr.stackFromEnd = true
        recyclerView.layoutManager = layoutMngr

        recyclerView.adapter = adapter
        recyclerView.isNestedScrollingEnabled = false
    }

    private fun setupClickListerner() {
        smsGoDownIndication.setOnClickListener(this)
    }

    private fun onContactitemClicked(id: String) {

    }

    companion object{
        var name = ""
        var chatId = ""
        var chatScrollToPosition = 0 //incase intent from SearchActivity we need
                                    // to scroll to thatexact sms
        var queryText:String? =null
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
        smsGoDownIndication.beGone()
        clearNewMessageIndication()

    }

     override fun otherPosition() {
        this.recyclerViewAtEnd = false
        smsGoDownIndication.beVisible()
    }

    override fun shouldWeScroll() {
        if(this.recyclerViewAtEnd){
//            recyclerView.scrollToPosition(newSize-1)
        }
    }

    override fun onClick(v: View?) {

        when(v?.id){
            
            R.id.smsGoDownIndication->{
                recyclerView.scrollToPosition(adapter.itemCount - 1)
                clearNewMessageIndication()
                smsGoDownIndication.beGone()

            }R.id.imgBtnSendSMS->{
            Log.d(TAG, "onClick: ")
                    sendSms()
            }
            R.id.imgBtnBackSmsIndividual->{
                finish()
            }
            R.id.btnBlock->{
                Log.d(TAG, "onClick: ")
                addToBlockList(contact!!)
            }
            R.id.imgExpand->{
                Log.d(TAG, "onClick: img button")
                val popup = PopupMenu(this, bottomSheetDialog.viewPopup)
                popup.inflate(R.menu.image_chooser_popup)
                popup.setOnMenuItemClickListener(this)
                popup.show()

            }
            R.id.imgViewCallBtn->{
                call(contactAddress)
            }
            R.id.imgBtnSearchSMS->{
                Log.d(TAG, "onClick: imgBtnSearchSMS")
                showSearchView()
            }
//            R.id.btnUpdate->{
//            viewModel.update()
//        }
            else->{
                this.radioButtonClickPerformed(v)
            }

        }


    }

    private fun showSearchView() {
        sViewIndividualSMS.requestFocus()
        sViewIndividualSMS.beVisible()
        imgBtnBackSmsIndividual.beInvisible()
        tvSMSAddress.beGone()
        imgViewCallBtn.beGone()
        imgBtnSearchSMS.beGone()
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
                R.id.radioScam->{
                    val checked = v.isChecked
                    if(checked){
                        selectedRadioButton= bottomSheetDialog.radioScam
                        Log.d(TAG, "radio button clicked")
                        this.spammerType = SpamLocalListManager.SPAMM_TYPE_SCAM

//                                spinnerSelected.value = false


                    }
                }
                R.id.radioS->{

                    val checked = v.isChecked
                    if(checked){
                        selectedRadioButton= bottomSheetDialog.radioS
                        this.spammerType = SpamLocalListManager.SPAMM_TYPE_SALES
                        Log.d(TAG, "onClick: radio scam")
//                                spinnerSelected.value = false

                    }
                }
                R.id.radioBusiness->{
                    val checked = v.isChecked
                    if(checked){
                        this.SPAMMER_CATEGORY = SpamLocalListManager.SPAMMER_BUISINESS
                    }
                }
                R.id.radioPerson->{
                    val checked = v.isChecked
                    if(checked){
                        this.SPAMMER_CATEGORY = SpamLocalListManager.SPAMMER_PEERSON

                    }
                }
            }
        }
    }

    private fun observeSpinnerSelected() {
        this.spinnerSelected.observe(this, Observer {spinnerSelected->
            if(spinnerSelected){
//                selectedRadioButton?.isChecked = false
            }
        })
    }

    override fun onItemSelected(parent: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
        this.spammerType = position
        Log.d(TAG, "onItemSelected: ")
        if(parent?.getItemAtPosition(position)?.equals("Spam type")!!){
//            spinnerSelected.value = true

        }else{

            selectedRadioButton?.isChecked = false
        }
    }
    override fun onNothingSelected(p0: AdapterView<*>?) {
//        selectedRadioButton?.isChecked = true
    }
    private fun addToBlockList(no: String) {

        viewModel.blockThisAddress(no, this.threadID, this.spammerType, this.SPAMMER_CATEGORY )
        Toast.makeText(this, "Number added to spamlist", Toast.LENGTH_LONG)
        bottomSheetDialog.hide()
        bottomSheetDialog.dismiss()

        bottomSheetDialogfeedback.show()
        var txt = "$contactAddress can no longer send SMS or call you."

        val  sb =  SpannableStringBuilder(txt);

        val bss =  StyleSpan(Typeface.BOLD); // Span to make text bold
        sb.setSpan(bss, 0, contactAddress.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE); // make first 4 characters Bold
//        sb.setSpan(normal, txt.length -1, normal.length-1,Spannable.SPAN_INCLUSIVE_INCLUSIVE)

        bottomSheetDialogfeedback.tvSpamfeedbackMsg.text = sb




    }


    override fun onResume() {
        super.onResume()
//            LocalBroadcastManager.getInstance(this).registerReceiver(
//                messagesReceiver,
//                IntentFilter("myhashcallersms")
//            )
        registerReceiver(messagesReceiver, IntentFilter("myhashcallersms"))

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
           this.spammerType = SpamLocalListManager.menuItemClickPerformed(menuItem, bottomSheetDialog)


            return false
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        viewModel.searchForSMS(query)
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return false
    }


}

