package com.nibble.hashcaller.view.ui.smsview.individual

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.contacts.utils.CONTACT_ADDRES


class IndividualSMSActivity : AppCompatActivity(), SMSIndividualAdapter.ItemPositionTracker {
    private lateinit var viewModel:SMSIndividualViewModel
    private lateinit var  recyclerView:RecyclerView
    private var oldSize = 0
    private var recyclerViewAtEnd = true
    private var firstime = true
//    private var newSize = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_individual_s_m_s)

        val contactAddress = intent.getStringExtra(CONTACT_ADDRES)
        contact = contactAddress

        viewModel = ViewModelProvider(this, SMSIndividualInjectorUtil.provideViewModelFactory(this)).get(
            SMSIndividualViewModel::class.java)

        
        recyclerView =
            findViewById<View>(R.id.recyclerViewSMSIndividual) as RecyclerView
        

        val adapter = SMSIndividualAdapter(this, applicationContext ){ id:String -> onContactitemClicked(id) }

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        recyclerView.setHasFixedSize(true)
        val linearLayout = LinearLayoutManager(this)
        linearLayout.stackFromEnd = true
        recyclerView.layoutManager = linearLayout

        recyclerView.adapter = adapter
        recyclerView.isNestedScrollingEnabled = false



        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)

                val msgCount = adapter.getItemCount()
                val lastVisiblePosition =
                    linearLayout.findLastCompletelyVisibleItemPosition()

                if (lastVisiblePosition == -1 || positionStart >= msgCount - 1 &&
                    lastVisiblePosition == positionStart - 1) {
                    recyclerView.scrollToPosition(positionStart)
                } else {
                    if(recyclerViewAtEnd)
                        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                }
            }
        })

        viewModel.SMS.observe(this, Observer { sms->
            sms.let {
//                smsRecyclerAdapter?.setSMSList(it, searchQry)
                Log.d(TAG, "onCreate: ${it.size}")
//                adapter.submitList(it)
//                newSize = it.size
                adapter.setList(it)
                if(firstime){
                    recyclerView.scrollToPosition(it.size - 1);
                    firstime = false
                }

//                recyclerView.scrollToPosition(adapter.itemCount -1)
              //  adapter.notifyItemRangeInserted(adapter.itemCount, it!!.size -1 )
                if(recyclerViewAtEnd){
//                    recyclerView.scrollToPosition(it.size-1)

                }

//                adapter.notifyItemRangeChanged(oldSize, it.size)
//                oldSize = it.size
//                recyclerView.scrollToPosition(recyclerView.adapter!!.itemCount -1 )
//                val recyclerViewState =
//                    recyclerView.layoutManager!!.onSaveInstanceState()
//// apply diff result here (dispatch updates to the adapter)
//// apply diff result here (dispatch updates to the adapter)
//                recyclerView.layoutManager!!.onRestoreInstanceState(recyclerViewState)
//                adapter.registerAdapterDataObserver(object : AdapterDataObserver() {
//                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
//                        super.onItemRangeInserted(positionStart, itemCount)
//                        recyclerView.smoothScrollToPosition(0)
//                    }
//                })



            }
        })


//        adapter.registerAdapterDataObserver(object : AdapterDataObserver() {
//            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
//                super.onItemRangeInserted(positionStart, itemCount)
//                recyclerView.smoothScrollToPosition(0)
//            }
//        })


    }

    private fun onContactitemClicked(id: String) {

    }

    companion object{
        var contact:String? = null
        const val TAG = "__IndividualSMSActivity"
    }

    override fun lastItemReached() {

        this.recyclerViewAtEnd = true
    }

    override fun otherPosition() {
        this.recyclerViewAtEnd = false
    }

    override fun shouldWeScroll() {
        if(this.recyclerViewAtEnd){
//            recyclerView.scrollToPosition(newSize-1)
        }
    }
}