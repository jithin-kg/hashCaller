package com.nibble.hashcaller.view.ui.call.search

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.CallListBinding
import com.nibble.hashcaller.utils.DummYViewHolder
import com.nibble.hashcaller.view.ui.call.db.CallLogTable
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.getExpandedLayoutId
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.getExpandedLayoutView
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.setExpandedLayoutId
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.setExpandedLayoutView
import com.nibble.hashcaller.view.ui.contacts.utils.TYPE_SPAM
import com.nibble.hashcaller.view.ui.extensions.setColorForText
import com.nibble.hashcaller.view.ui.extensions.setRandomBackgroundCircle
import com.nibble.hashcaller.view.ui.sms.individual.util.*
import com.nibble.hashcaller.view.ui.sms.individual.util.TYPE_CLICK
import com.nibble.hashcaller.view.ui.sms.util.SENDER_INFO_FROM_DB
import com.nibble.hashcaller.view.ui.sms.util.SENDER_INFO_SEARCHING
import com.nibble.hashcaller.view.utils.getRelativeTime
import kotlinx.android.synthetic.main.call_list.view.*
import kotlin.time.ExperimentalTime

/**
 * Created by Jithin KG on 22,July,2020
 */

class CallSearchAdapter(private val context: Context,
                        private val viewMarkingHandler: ViewMarkHandler,
                        private val onContactItemClickListener:
                    (id:Long, postition:Int, view:View, btn:Int, callLog:CallLogTable, clickType:Int)->Int
                   ) :
    androidx.recyclerview.widget.ListAdapter<CallLogTable, RecyclerView.ViewHolder>(CallItemDiffCallback()) {

    private val VIEW_TYPE_LOG = 0;
//    private val VIEW_TYPE_SPAM = 1;
    private val VIEW_TYPE_LOADING = 1
    private var callLogs: List<CallLogTable> = mutableListOf()
    companion object{
        private const val TAG = "__DialerAdapter";
        var prevView:View? = null
        var prevPos:Int? = null
        var prevTag:String? = null
        var prevTime : String? = null

        const val BUTTON_SIM_1 = 0;
        const val BUTTON_SIM_2 = 1;
        const val BUTTON_SMS = 2;
        const val BUTTON_INFO = 3;

    }

     
    

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  RecyclerView.ViewHolder {
        if(viewType == VIEW_TYPE_LOG){
            //create binding here and pass it to viewholder
//            val view = LayoutInflater.from(parent.context).inflate(R.layout.call_list, parent, false)
            val logBinding =  CallListBinding.inflate(LayoutInflater.from(parent.context), parent, false)


            return ViewHolderCallLog(logBinding)
        }else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.call_list_item_loading, parent, false)
            return DummYViewHolder(view)
        }

    }

    override fun getItemViewType(position: Int): Int {
        if( callLogs.size > 0  && callLogs.isNotEmpty() )
             if(this.callLogs[position].id == null){
                return VIEW_TYPE_LOADING
            }
        return VIEW_TYPE_LOG
    }
@ExperimentalTime
override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val contact = callLogs[position]
    when(holder.itemViewType) {

         VIEW_TYPE_LOG -> {


             (holder as ViewHolderCallLog).bind(callLogs[position],context, onContactItemClickListener)
        }

        VIEW_TYPE_LOADING ->{
            (holder as DummYViewHolder).bind()
        }


    }

}

    override fun getItemCount(): Int {
//        Log.d("__ContactAdapter", "getItemCount: ${contacts.size}")
       return callLogs.size
    }

    fun submitCallLogs(newContactList: List<CallLogTable>) {
        callLogs = newContactList
        this.submitList(newContactList)
    }



    inner class ViewHolderCallLog(private val logBinding:  CallListBinding) : RecyclerView.ViewHolder(logBinding.root) {
        private val name = logBinding.textVcallerName
        private val circle = logBinding.textViewCrclr;
        private val expandableView = logBinding.layoutExpandableCall

//        private val image = view.findViewById<ImageView>(R.id.contact_image)

        @ExperimentalTime
        fun bind(
            callLog: CallLogTable, context: Context,
            onContactItemClickListener: (id: Long, postition: Int, view: View, btn: Int, callLog: CallLogTable, clickType: Int) -> Int
        ) {
            Log.d(TAG, "bind: ")
            expandableView.setTag(callLog.dateInMilliseconds)

            if (viewMarkingHandler.isMarked(callLog.id)) {
                Log.d(TAG, "bind: ismarked")
                logBinding.imgViewCallMarked.beVisible()

            } else {
                logBinding.imgViewCallMarked.beInvisible()
            }
            val sim = callLog.simId
            //todo simid can be -1 then, do not show this, invisisble
            logBinding.tvSim.text = (sim + 1).toString()
//            if (prevTime != null)
//                if(prevTime == callLog.dateInMilliseconds){
//                    expandableView.beVisible()
//
//                }else{
//                   expandableView.beGone()
//
//                }
//                if (callLog.callerInfoFoundFrom == SENDER_INFO_SEARCHING) {
//                    logBinding.pgBarCallItem.beVisible()
//                }

                var nameStr:String = ""
                var infoFoundFrom = SENDER_INFO_SEARCHING
                if(callLog.name.isNullOrEmpty()){
                    infoFoundFrom = SENDER_INFO_FROM_DB
                    if(!callLog.nameFromServer.isNullOrEmpty()){
                        nameStr = callLog.nameFromServer!!
                    }
                }else{
                    nameStr = callLog.name!!
                }
                if(nameStr.isNullOrEmpty()){
                    nameStr = callLog.number
                }

                if (callLog.spamCount > 0) {

                    name.setColorForText(R.color.spamText)

                } else {
                    name.setColorForText(R.color.textColor)

                }



            name.text = nameStr
            logBinding.imgViewCallSpamIcon.beInvisible()
            var firstLetter = ""
            if(nameStr.length>0){
                 firstLetter = nameStr[0].toString()

            }
            val firstLetterString = firstLetter.toUpperCase()
            circle.text = firstLetterString
            callLog.color = circle.setRandomBackgroundCircle()

            //call type
            setCallTypeImage(callLog, logBinding.imgVCallType)


            /**
             * This is important to check else double/ duplicate marking of items occur
             */
            var id = getExpandedLayoutId()
            if (id != null) {
                if (id == callLog.id) {
                    expandableView.beVisible()
                } else {
                    expandableView.beGone()

                }
            }
            logBinding.textViewTime.text = getRelativeTime(callLog.dateInMilliseconds)
            expandableView.tvExpandNumCall.text = callLog.number
            setClickListener(logBinding.root, callLog)
        }


        private fun setClickListener(view: View, callLog: CallLogTable) {
            view.setOnLongClickListener { v ->
                var isToBeMarked = onContactItemClickListener(
                    callLog.id!!,
                    this.adapterPosition,
                    v,
                    BUTTON_SIM_1,
                    callLog,
                    TYPE_LONG_PRESS
                )
                when (isToBeMarked) {
                    MARK_ITEM -> {
                        view.imgViewCallMarked.beVisible()
                    }
                    else -> {
                        view.imgViewCallMarked.beInvisible()
                    }
                }

                true

            }
            view.imgBtnCallExpand.setOnClickListener {
//                viewMarkingHandler.onCallButtonClicked(it, INTENT_TYPE_MAKE_CALL, callLog)
            }

            view.imgBtnSmsExpand.setOnClickListener {
//                viewMarkingHandler.onCallButtonClicked(it, INTENT_TYPE_START_INDIVIDUAL_SMS, callLog)
            }

            view.imgBtnInfoExpand.setOnClickListener {
//                viewMarkingHandler.onCallButtonClicked(it, INTENT_TYPE_MORE_INFO, callLog)
            }


            view.setOnClickListener(View.OnClickListener { v ->
                val isToBeMarked = onContactItemClickListener(
                    callLog.id!!,
                    this.adapterPosition,
                    v,
                    BUTTON_SIM_1,
                    callLog,
                    TYPE_CLICK
                )
                when (isToBeMarked) {
                    MARK_ITEM -> {
                        view.imgViewCallMarked.beVisible()
                    }
                    else -> {
                        view.imgViewCallMarked.beInvisible()
                    }
                }


                //                if(viewExpanded== 1){
//                    //iff marking not started expand the layout
//                    toggleExpandableView(v, this.adapterPosition, callLog.id!!, expandableView)
//                }

            })
        }

        private fun toggleExpandableView(
            v: View,
            pos: Int,
            id: Long,
            expandableView: ConstraintLayout
        ) {

            var expandedLyoutId = getExpandedLayoutId()
            if (expandedLyoutId == null) {
                //no views has not yet expanded, so expand the current layout
                setExpandedLayoutId(id)
                setExpandedLayoutView(expandableView)
                expandableView.beVisible()

            } else if (expandedLyoutId == id) {
                //the layout is already expaned so, hide it
                expandableView.beGone()
                setExpandedLayoutId(null)
                setExpandedLayoutView(null)
            } else {
                //new item expanded
                getExpandedLayoutView()!!.beGone()
                expandableView.beVisible()
                setExpandedLayoutView(expandableView)
                setExpandedLayoutId(id)


            }
        }


        private fun setNameFirstChar(callLog: CallLogTable) {
//            private fun setNameFirstChar(callLog: CallLogTable) {
//                if (callLog != null) {
//                    if (callLog.spamCount > 0) {
//                        logBinding.imgViewCallSpamIcon.beVisible()
//                        logBinding.imgViewCallSpamIcon.setImageResource(R.drawable.ic_baseline_block_red)
//                        circle.text = ""
//                        callLog.color = circle.setRandomBackgroundCircle(TYPE_SPAM)
//                    } else {
//                        logBinding.imgViewCallSpamIcon.beInvisible()
//                        val name: String =
//                            if (callLog.name == null || callLog.name!!.isEmpty()) callLog.number else callLog.name!!
//                        val firstLetter = name[0]
//                        val firstLetterString = firstLetter.toString().toUpperCase()
//                        circle.text = firstLetterString
//                        callLog.color = circle.setRandomBackgroundCircle()
//
//                    }
//                } else {
//                    logBinding.imgViewCallSpamIcon.beInvisible()
//                    val name: String =
//                        if (callLog.name == null || callLog.name!!.isEmpty()) callLog.number else callLog.name!!
//                    val firstLetter = name[0]
//                    val firstLetterString = firstLetter.toString().toUpperCase()
//                    circle.text = firstLetterString
//                    callLog.color = circle.setRandomBackgroundCircle()
//                }
//
//
//            }


        }

    }
    class CallItemDiffCallback : DiffUtil.ItemCallback<CallLogTable>() {
        override fun areItemsTheSame(oldItem: CallLogTable, newItem: CallLogTable): Boolean {
            return  oldItem.id == newItem.id


        }


        override fun areContentsTheSame(oldItem: CallLogTable, newItem: CallLogTable): Boolean {
//            if(oldItem.ca)

            return oldItem.number == newItem.number && oldItem.name == newItem.name
        }

    }


    private fun setCallTypeImage(callLog: CallLogTable, imageView: ImageView) {
//             /** Call log type for incoming calls.  */
//             val INCOMING_TYPE = 1
//
//             /** Call log type for outgoing calls.  */
//             val OUTGOING_TYPE = 2
//
//             /** Call log type for missed calls.  */
//             val MISSED_TYPE = 3
//
//             /** Call log type for voicemails.  */
//             val VOICEMAIL_TYPE = 4
//
//             /** Call log type for calls rejected by direct user action.  */
//             val REJECTED_TYPE = 5
//
//             /** Call log type for calls blocked automatically.  */
//             val BLOCKED_TYPE = 6
        if(callLog!=null){
            if(callLog.spamCount > 0 ){
//                textView.setColorForText( R.color.spamText)

            }else{
//                textView.setColorForText(R.color.textColor)

            }
        }else{
//            textView.setColorForText(R.color.textColor)

        }

        when (callLog.type) {
            1 -> { // incomming call
                imageView.setImageResource(R.drawable.ic_baseline_call_received_24)
//                textView.text = "Incoming call"

            }
            2 -> { // outgoing call
                imageView.setImageResource(R.drawable.ic_baseline_call_made_24)
//                textView.text = "Outgoing call"
            }
            3 -> {
                imageView.setImageResource(R.drawable.ic_baseline_call_missed_24)
//                textView.text = "Missed call"
            }
            5->{
//                textView.text = "Rejected"
                imageView.setImageResource(R.drawable.ic_baseline_call_missed_24)

            }
            6 ->{
//                textView.text = "Blocked"
                imageView.setImageResource(R.drawable.ic_baseline_block_no_color)

            }

        }
    }
    interface ViewMarkHandler {
        fun isMarked(id:Long?): Boolean

    }


}



