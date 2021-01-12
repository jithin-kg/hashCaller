package com.nibble.hashcaller.view.ui.sms.individual


import android.content.Context
import android.graphics.Color
import android.os.Build
import android.provider.Telephony
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.sms.util.SMS
import kotlinx.android.synthetic.main.sms_indiivdual_date.view.*
import kotlinx.android.synthetic.main.sms_individual_outbox_item.view.*
import kotlinx.android.synthetic.main.sms_individual_recived_item.view.*
import kotlinx.android.synthetic.main.sms_individual_sent_item.view.*
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*


/**
 * Created by Jithin KG on 22,July,2020
 */
class SMSIndividualAdapter( private val positionTracker:ItemPositionTracker, private val context: Context,
                            private val onContactItemClickListener: (id:String)->Unit
) :
    androidx.recyclerview.widget.ListAdapter<SMS, RecyclerView.ViewHolder>(SMSIndividualDiffCallback()) {
    private var smsList:MutableList<SMS> = mutableListOf()
    private val VIEW_TYPE_MESSAGE_SENT = 2
    private val VIEW_TYPE_MESSAGE_RECEIVED = 1
    private val VIEW_TYPE_MESSAGE_OUTBOX  = 4
    private val VIEW_TYPE_DATE = 6

    companion object{
        private const val TAG = "__SMSIndividualAdapter";
        public var searchQry:String? = null
        private var prevDate = ""
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == VIEW_TYPE_MESSAGE_RECEIVED){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.sms_individual_recived_item, parent, false)

            return ReceivedSMSViewHolder(view)
        }else if(viewType == VIEW_TYPE_DATE) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.sms_indiivdual_date, parent, false)

            return SMSDateViewHolder(view)
        }
        else if(viewType == VIEW_TYPE_MESSAGE_OUTBOX){
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.sms_individual_outbox_item, parent, false)

            return OutBoxSMSViewHolder(view)
        }
        else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.sms_individual_sent_item, parent, false)
            return SentSMSViewHolder(view)
        }
    }

    //    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val contact = contacts[position]
//        holder.bind(contact, context, onContactItemClickListener)
//    }
//override fun getItemCount(): Int {
//    return smsList.size
    override fun getItemViewType(position: Int): Int {
//        return super.getItemViewType(position)
        if(smsList[position].type == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT){
            //sent message 2
            return VIEW_TYPE_MESSAGE_SENT

        }else if(smsList[position].type == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX ){
            return VIEW_TYPE_MESSAGE_RECEIVED
        }else if(smsList[position].type == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX){
//            Log.d(TAG, "getItemViewType: outbox msg ${Telephony.TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX}")
            return VIEW_TYPE_MESSAGE_OUTBOX
        }
        else{
            return  VIEW_TYPE_DATE
        }


    }
    //}
    override fun onBindViewHolder(holder:  RecyclerView.ViewHolder, position: Int) {
//    val contact = smsList[position]
//    holder.bind(contact, context, onContactItemClickListener)

//        Log.d(TAG, "onBindViewHolder: ")
        when(holder.itemViewType) {

            VIEW_TYPE_MESSAGE_SENT -> {
                (holder as SentSMSViewHolder).bind(getItem(position),context, onContactItemClickListener, position, holder)
            }
            VIEW_TYPE_MESSAGE_RECEIVED->{
                (holder as ReceivedSMSViewHolder).bind(getItem(position),context, onContactItemClickListener, position, holder)
            }
            VIEW_TYPE_DATE->{
                (holder as SMSDateViewHolder).bind(getItem(position),context, onContactItemClickListener, position, holder)
            }
            VIEW_TYPE_MESSAGE_OUTBOX->{
                (holder as OutBoxSMSViewHolder).bind(getItem(position),context, onContactItemClickListener, position, holder)
            }
        }
    }

    fun setList(it: List<SMS>?) {
        smsList.clear()
        smsList.addAll(it as MutableList<SMS>)
//        Log.d(TAG, "setList: ${smsList.size}")
        this.submitList(it)
//        if(smsList.isNotEmpty() && smsList.size < it!!.size){
//            positionTracker.shouldWeScroll()
//        }
//        notifyDataSetChanged()

    }


    inner class SMSDateViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {


        fun bind(
            sms: SMS, context: Context,
            onContactItemClickListener: (id: String) -> Unit,
            position: Int,
            holderReceivedMessage: RecyclerView.ViewHolder
        ) {

            if(position  == smsList.size - 1){
                positionTracker.lastItemReached()
            }else if(position < smsList.size - 1){
                positionTracker.otherPosition()
            }

//            if(sms.currentDate!=null){
                val date =  sms.currentDate
//                if(prevDate != date ){
                    if(date!=null)
                        prevDate = date!!
                     view.tvSMSDate.text = sms.currentDate
//                    Log.d(TAG, "bind: setting date ${sms.currentDate}")

//                }
//            }
            
            view.setOnClickListener{

//                onContactItemClickListener(pNo)
            }
        }

    }

    inner class ReceivedSMSViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val msg = view.tvRecivedMsg


        fun bind(
            sms: SMS, context: Context,
            onContactItemClickListener: (id: String) -> Unit,
            position: Int,
            holderReceivedMessage: RecyclerView.ViewHolder
        ) {
//            Log.d(TAG, "bind: called")

            if(position  == smsList.size - 1){
                positionTracker.lastItemReached()
            }else if(position < smsList.size - 1){
                positionTracker.otherPosition()
            }
            msg.text = if (sms.msgString == null) " null" else sms.msgString

            val date =  SimpleDateFormat("dd/MM/yyyy").format(Date(sms.time!!))

            setTimeInView(sms.time)

            view.setOnClickListener{

//                onContactItemClickListener(pNo)
            }
        }

        private fun highlightSearhcField(sms: SMS) {


        }

        private fun setSpan(str:String, startPos:Int, endPos:Int, v: TextView) {
            val yellow =
                BackgroundColorSpan(Color.YELLOW)
            val spannableStringBuilder =
                SpannableStringBuilder(str)
//            Log.d(TAG, "setSpan: startPos:$startPos")
//            Log.d(TAG, "setSpan: endPos:$endPos")
            try{
                spannableStringBuilder.setSpan(yellow,startPos, endPos, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
            }catch (e:IndexOutOfBoundsException){
//                Log.d(TAG, "setSpan: $e")
            }

            v.text = spannableStringBuilder
        }

        private fun setTimeInView(time: Long?) {
            val date =  SimpleDateFormat("dd/MM/yyyy").format(Date(time!!))
            val time =   SimpleDateFormat("hh:mm:ss").format(time * 1000)
//             Log.d(TAG, "date: $date")
//             Log.d(TAG, "time: $time")
//             val now: ZonedDateTime  = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));(
            /**
             * now(),ofPattern(), format() requires min api 26
             */
            val now =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ZonedDateTime.now(ZoneId.of("Asia/Kolkata"))
                } else {
                    TODO("VERSION.SDK_INT < O")
                }
            val todayDate =
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    .format(now)
            if(date.equals(todayDate)){
//                only set time in view
                 view.tvRecivedTime.text = time
             }else{
                 view.tvRecivedTime.text = date
            }
//             Log.d(TAG, "today: $todayDate")
//             Log.d(TAG, "setTimeInView: $d2")


        }




    }

    /**
     * Viewhodler for messages of type MESSAGE_TYPE_OUTBOX Constant Value: 4 (0x00000004)
     */
    inner class OutBoxSMSViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val msg = view.tvSentMsgOutbox


        fun bind(
            sms: SMS, context: Context,
            onContactItemClickListener: (id: String) -> Unit,
            position: Int,
            holderReceivedMessage: RecyclerView.ViewHolder
        ) {
//            Log.d(TAG, "bind: called")

            if(position  == smsList.size - 1){
                positionTracker.lastItemReached()
            }else if(position < smsList.size - 1){
                positionTracker.otherPosition()
            }
            msg.text = if (sms.msgString == null) " null" else sms.msgString

//            val date =  SimpleDateFormat("dd/MM/yyyy").format(Date(sms.time!!))

//            setTimeInView(sms.time)

            view.setOnClickListener{

//                onContactItemClickListener(pNo)
            }
        }




    }

    inner class SentSMSViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val msg = view.tvSentMsg


        fun bind(
            sms: SMS,
            context: Context,
            onContactItemClickListener: (id: String) -> Unit,
            position: Int,
            holderReceivedMessage: RecyclerView.ViewHolder
        ) {
//            Log.d(TAG, "bind: called")

            if(position  == smsList.size - 1){
                positionTracker.lastItemReached()
            }else if(position < smsList.size - 1){
                positionTracker.otherPosition()
            }
            msg.text =  sms.msgString

            setTimeInView(sms.time)
            view.setOnClickListener{

//                onContactItemClickListener(pNo)
            }
        }

        private fun highlightSearhcField(sms: SMS) {


        }

        private fun setSpan(str:String, startPos:Int, endPos:Int, v: TextView) {
            val yellow =
                BackgroundColorSpan(Color.YELLOW)
            val spannableStringBuilder =
                SpannableStringBuilder(str)
//            Log.d(TAG, "setSpan: startPos:$startPos")
//            Log.d(TAG, "setSpan: endPos:$endPos")
            try{
                spannableStringBuilder.setSpan(yellow,startPos, endPos, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
            }catch (e:IndexOutOfBoundsException){
//                Log.d(TAG, "setSpan: $e")
            }

            v.text = spannableStringBuilder
        }

        private fun setTimeInView(time: Long?) {
            val date =  SimpleDateFormat("dd/MM/yyyy").format(Date(time!!))
            val time =   SimpleDateFormat("hh:mm").format(time * 1000)
//             Log.d(TAG, "date: $date")
//             Log.d(TAG, "time: $time")
//             val now: ZonedDateTime  = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));(
            /**
             * now(),ofPattern(), format() requires min api 26
             */
            val now =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ZonedDateTime.now(ZoneId.of("Asia/Kolkata"))
                } else {
                    TODO("VERSION.SDK_INT < O")
                }
            val todayDate =
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    .format(now)
            if(date.equals(todayDate)){
//                only set time in view
//                 view.tvSenttime.text = time
//             }else{
//                 view.tvTime.text = date
            }
//             Log.d(TAG, "today: $todayDate")
//             Log.d(TAG, "setTimeInView: $d2")


        }




    }
    class SMSIndividualDiffCallback : DiffUtil.ItemCallback<SMS>() {


        override fun areItemsTheSame(oldItem: SMS, newItem: SMS): Boolean {
            if(oldItem.time == newItem.time){
//                Log.d(TAG, "areItemsTheSame: time are same")
            }else{
//                Log.d(TAG, "areItemsTheSame: time not same")
            }

//            Log.d(TAG, "areItemsTheSame: ${newItem.currentDate}")
            return oldItem.id == newItem.id

        }

        override fun areContentsTheSame(oldItem: SMS, newItem: SMS): Boolean {
//            Log.d(TAG, "areContentsTheSame: type ${oldItem.type == newItem.type}")
//            Log.d(TAG, "areContentsTheSame: ")
            if(oldItem == newItem){
//                Log.d(TAG, "areContentsTheSame:  equal")
            }else{
//                Log.d(TAG, "areContentsTheSame: not equal")

            }
            val b = oldItem == newItem && oldItem.time == newItem.time

//            Log.d(TAG, "areContentsTheSame: b : $b")
            return oldItem.time == newItem.time &&
                    oldItem.msgString == newItem.msgString && oldItem.type == newItem.type

        }

    }

    interface ItemPositionTracker{
        fun lastItemReached()
        fun otherPosition()
        fun shouldWeScroll()
    }


}



