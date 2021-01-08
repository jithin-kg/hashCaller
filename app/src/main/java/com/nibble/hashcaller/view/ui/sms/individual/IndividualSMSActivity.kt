package com.nibble.hashcaller.view.ui.sms.individual

import android.content.*
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.MainActivity
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ADDRES
import com.nibble.hashcaller.view.ui.sms.util.SMS
import com.nibble.hashcaller.view.utils.HorizontalDottedProgress
import com.nibble.hashcaller.view.utils.spam.SpamLocalListManager
import kotlinx.android.synthetic.main.activity_individual_s_m_s.*
import kotlinx.android.synthetic.main.bottom_sheet_block.*
import kotlinx.android.synthetic.main.bottom_sheet_block_feedback.*
import java.lang.reflect.InvocationTargetException
import kotlin.math.log


class IndividualSMSActivity : AppCompatActivity(),
    SMSIndividualAdapter.ItemPositionTracker, View.OnClickListener,
    AdapterView.OnItemSelectedListener, android.widget.PopupMenu.OnMenuItemClickListener,
    PopupMenu.OnMenuItemClickListener {
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


    private  var spammerType:Int = -1
    var spamTypes:MutableList<String> = ArrayList<String>()


    //    private var messageSent: MutableLiveData<Boolean> = MutableLiveData()
//    private var time:String? = null
    private var address = ""

//    private var sendBroadcastReceiver: BroadcastReceiver = SentReceiver()
//    private var deliveryBroadcastReceiver: BroadcastReceiver = DeliverReceiver()

    //    private var newSize = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_individual_s_m_s)

        spinnerSelected.value = false

//        messageSent.value = false
        setSupportActionBar(toolbarSMSIndividual)
        this.spamTypes.add(0, "Spam type")
        this.spamTypes.add("Public service")
        this.spamTypes.add("Robocall")
        this.spamTypes.add("Survey")
        when{
            intent.action == Intent.ACTION_SENDTO->{
                Log.d(TAG, "onCreate: send sms to ")
                val body = intent.getStringExtra("sms_body")
                contactAddress = intent.data.toString()


            }else->{
            contactAddress = intent.getStringExtra(CONTACT_ADDRES)
        }
        }

        contact = contactAddress
        setupBottomSheet()
        observerSmsSent()
        initViewModel()
        initAdapter()
        initListners()
        setupClickListerner()
        registerAdapterListener()
        setupViewmodelObserver()
        observeSpinnerSelected()
        addOrRemoveMenuItem()
        getContactInformation()
        observeContactname()
        configureToolbar()


    }

    private fun observeContactname() {
        this.viewModel.nameLiveData.observe(this, Observer {
            if(!it.isNullOrBlank()){
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    isTheNumberBlocked.value = it.stream()
                        .anyMatch { t -> t.contactAddress.equals(this.contactAddress) }
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
        imgBtnSend.setOnClickListener(this)
        imgBtnBackSmsIndividual.setOnClickListener(this)
        bottomSheetDialog.radioS.setOnClickListener(this)
        bottomSheetDialog.radioScam.setOnClickListener(this)
        bottomSheetDialog.imgExpand.setOnClickListener(this)
        bottomSheetDialog.btnBlock.setOnClickListener(this)
//        bottomSheetDialog.btnBlock.setOnClickListener(this)
//        bottomSheetDialog.radioButtonSales.setOnClickListener(this)
//        bottomSheetDialog.radioButtonScam.setOnClickListener(this)
////        bottomSheetDialog.radioButtonSales.setOnCheckedChangeListener(this)
//        bottomSheetDialog.radioGroupSpamType.setOnClickListener(this)

//        btnUpdate.setOnClickListener(this)
    }


    private fun setupViewmodelObserver() {
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
                if(it.size>1)
                this.threadID = it[it.size-1].threadID

                adapter.setList(it)
                if(firstime){
                    recyclerView.scrollToPosition(it.size - 1);
                    firstime = false
                }
                if(!recyclerViewAtEnd){
                         countNewItem = it.size - oldLIstSize

                    tvcountShow.text = countNewItem.toString()
                    tvcountShow.visibility = View.VISIBLE
                }else{
                    clearNewMessageIndication()
                    oldLIstSize = it.size
                }

//                recyclerView.scrollToPosition(adapter.itemCount -1)
                //  adapter.notifyItemRangeInserted(adapter.itemCount, it!!.size -1 )
                if(recyclerViewAtEnd){
//                    recyclerView.scrollToPosition(it.size-1)

                }




            }
        })
        isThisNumBlocked()

    }

    private fun clearNewMessageIndication() {
        tvcountShow.visibility = View.GONE
        tvcountShow.text = ""
        tvNewMsgIndication.visibility = View.GONE

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
        tvNewMsgIndication.setOnClickListener(this)
    }

    private fun onContactitemClicked(id: String) {

    }

    companion object{
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
        tvNewMsgIndication.visibility = View.GONE
        clearNewMessageIndication()
    }

     override fun otherPosition() {
        this.recyclerViewAtEnd = false
        tvNewMsgIndication.visibility = View.VISIBLE
    }

    override fun shouldWeScroll() {
        if(this.recyclerViewAtEnd){
//            recyclerView.scrollToPosition(newSize-1)
        }
    }

    override fun onClick(v: View?) {

        when(v?.id){
            
            R.id.tvNewMsgIndication->{

                recyclerView.scrollToPosition(adapter.itemCount - 1)
                clearNewMessageIndication()
            }R.id.imgBtnSend->{
            Log.d(TAG, "onClick: ")
                    sendSms()
            }
            R.id.imgBtnBackSmsIndividual->{
                finish()
            }
            R.id.btnBlock->{
                Log.d(TAG, "onClick: ")
                addToBlockList(contactAddress)
            }
            R.id.imgExpand->{
                Log.d(TAG, "onClick: img button")
                val popup = PopupMenu(this, bottomSheetDialog.viewPopup)
                popup.inflate(R.menu.image_chooser_popup)
                popup.setOnMenuItemClickListener(this)
                popup.show()

            }
//            R.id.btnUpdate->{
//            viewModel.update()
//        }
            else->{
                this.radioButtonClickPerformed(v)
            }

        }


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
    private fun addToBlockList(contactAddress: String) {

        viewModel.blockThisAddress(contactAddress, this.threadID, this.spammerType, this.SPAMMER_CATEGORY )
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

    private fun sendSms() {
//        messageSent.value = false
        /**
         * When there is no network the messages is added to the queue
         * and at the moment user clicks send the message is added to outbox
         * and later updated to sended in table ,meanwhile when user starts typing the messsage is added to draft
         */
        Log.d(TAG, "sendSms: clicked contact address $contactAddress")
        val msg = edtTxtMSg.text.toString()
        viewModel.sendSms(msg, applicationContext, contactAddress)





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
           this.spammerType = SpamLocalListManager.menuItemClickPerformed(menuItem, bottomSheetDialog)

            return false
    }




}

