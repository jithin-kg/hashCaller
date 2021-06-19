package com.nibble.hashcaller.view.ui.sms.spam

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.databinding.ActivitySpamSMSBinding
import com.nibble.hashcaller.utils.internet.ConnectionLiveData
import com.nibble.hashcaller.view.ui.SwipeToDeleteCallback
import com.nibble.hashcaller.view.ui.call.dialer.util.CustomLinearLayoutManager
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ADDRES
import com.nibble.hashcaller.view.ui.contacts.utils.OPERATION_COMPLETED
import com.nibble.hashcaller.view.ui.contacts.utils.SMS_CHAT_ID
import com.nibble.hashcaller.view.ui.sms.db.SmsThreadTable
import com.nibble.hashcaller.view.ui.sms.individual.IndividualSMSActivity
import com.nibble.hashcaller.view.ui.sms.individual.util.*
import com.nibble.hashcaller.view.ui.sms.list.SMSListAdapter
import kotlinx.coroutines.Dispatchers

class SpamSMSActivity : AppCompatActivity(), SMSListAdapter.ViewMarkHandler,
    SMSListAdapter.LongPressHandler, SMSListAdapter.NetworkHandler {
    private lateinit var binding : ActivitySpamSMSBinding
    private lateinit var viewmodel: SpamSMSViewModel
    private var isInternetAvailable = false
    private lateinit var swipeHandler: SwipeToDeleteCallback
    private lateinit var layoutMngr: LinearLayoutManager
    private lateinit var smsadapter: SMSListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding =  ActivitySpamSMSBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observeInternetLivedata()
        initSwipeHandler()
        initRecyclerView()
        initViewmodel()
        observeSMSLivedata()


    }

    private fun observeSMSLivedata() {
        viewmodel.spamSMSLivedata.observe(this, Observer {
            Log.d(TAG, "observeSMSLivedata: size ${it.size} ")
            lifecycleScope.launchWhenStarted {
                var list: MutableList<SmsThreadTable> = mutableListOf()
                if(it.isNotEmpty()){
                    binding.tvSMSideinfiiedInfo.beGone()
                }else {
                    binding.tvSMSideinfiiedInfo.beVisible()
                }
                list.addAll(it.filter { !it.isDeleted })
                Log.d(TAG, "observeSMSLivedata: list2 size ${list.size}")
                smsadapter.setList(list)

            }
        })
    }

    private fun initSwipeHandler() {
        swipeHandler = object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding.recyvlerV.adapter
                val pos = viewHolder.adapterPosition
                val sms = smsadapter?.getSMSAt(pos)
                viewmodel.delete(sms).observe(this@SpamSMSActivity, Observer {
                    when(it){
                        OPERATION_COMPLETED ->{
                            toast("All SMS  of ${sms?.numFormated} is deleted", Toast.LENGTH_LONG)
//                            runOnUiThread{
//                                smsadapter.notifyItemChanged(pos)
//
//                            }
                        }
                    }
                })
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.recyvlerV)
    }
    private fun initViewmodel() {
        viewmodel = ViewModelProvider(this, SpamSMSInjectorUtil.provideViewmodelFactory(applicationContext)).get(
            SpamSMSViewModel::class.java
        )
    }
    private fun initRecyclerView() {

        binding.recyvlerV.apply {
            layoutManager = CustomLinearLayoutManager(context)
            ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.recyvlerV);

            layoutMngr = layoutManager as CustomLinearLayoutManager

            smsadapter = SMSListAdapter(context, this@SpamSMSActivity, this@SpamSMSActivity, this@SpamSMSActivity){ view: View, threadId:Long, pos:Int,
                                                                                                                                   pno:String, clickType:Int->onContactItemClicked(view,threadId, pos, pno, clickType)  }
            adapter = smsadapter
            itemAnimator = null

        }
    }
    private fun onContactItemClicked(view: View, threadId: Long, position: Int, address: String, clickType:Int): Int {
        when(clickType){
            TYPE_LONG_PRESS -> {
                toast("Slide left or right to delete")
            }else ->{
                startIndividualSMSActivity(address, view)
            }
        }
        return UNMARK_ITEM
    }

    private fun startIndividualSMSActivity(address: String, view: View) {
        val intent = Intent(this, IndividualSMSActivity::class.java )
        val bundle = Bundle()
        bundle.putString(CONTACT_ADDRES, address)
        bundle.putString(SMS_CHAT_ID, "")

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.putExtras(bundle)
        startActivity(intent)
    }
    private fun observeInternetLivedata() {
        val cl = ConnectionLiveData(this)
        cl?.observe(this, Observer {
            isInternetAvailable = it
        })
    }

    override fun isMarked(id: Long): Boolean {
        return false
    }

    override fun onLongPressed(view: View, pos: Int, id: Long, address: String) {
        toast("Swipe to delete a conversation")
    }

    override fun isInternetAvailable(): Boolean {
        return isInternetAvailable
    }
    companion object{
        const val TAG = "__SpamSMSActivity"
    }
    override fun onBackPressed() {
        finishAfterTransition()
        super.onBackPressed()
    }
}