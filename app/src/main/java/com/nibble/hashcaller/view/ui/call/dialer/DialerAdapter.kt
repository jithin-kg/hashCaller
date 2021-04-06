package com.nibble.hashcaller.view.ui.call.dialer

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.R
import com.nibble.hashcaller.utils.DummYViewHolder
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogData
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.containsItem
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.getExpandedLayoutId
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.getExpandedLayoutView
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.setExpandedLayoutId
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.setExpandedLayoutView
import com.nibble.hashcaller.view.ui.contacts.utils.INTENT_TYPE_MAKE_CALL
import com.nibble.hashcaller.view.ui.contacts.utils.INTENT_TYPE_MORE_INFO
import com.nibble.hashcaller.view.ui.contacts.utils.INTENT_TYPE_START_INDIVIDUAL_SMS
import com.nibble.hashcaller.view.ui.contacts.utils.TYPE_SPAM
import com.nibble.hashcaller.view.ui.extensions.setColorForText
import com.nibble.hashcaller.view.ui.extensions.setRandomBackgroundCircle
import com.nibble.hashcaller.view.ui.sms.individual.util.beGone
import com.nibble.hashcaller.view.ui.sms.individual.util.beInvisible
import com.nibble.hashcaller.view.ui.sms.individual.util.beVisible
import com.nibble.hashcaller.view.ui.sms.util.SENDER_INFO_SEARCHING
import kotlinx.android.synthetic.main.call_list.view.*
import kotlinx.android.synthetic.main.call_list_item_spam.view.*

/**
 * Created by Jithin KG on 22,July,2020
 */

class DialerAdapter(private val context: Context,
                    private val longPressHandler: CallItemLongPressHandler,
                    private val onContactItemClickListener: (id:Long, postition:Int, view:View, btn:Int, callLog:CallLogData)->Int
                   ) :
    androidx.recyclerview.widget.ListAdapter<CallLogData, RecyclerView.ViewHolder>(CallItemDiffCallback()) {

    private val VIEW_TYPE_NO_SPAM = 0;
    private val VIEW_TYPE_SPAM = 1;
    private val VIEW_TYPE_LOADING = 3
    private var callLogs = emptyList<CallLogData>()
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
        if(viewType == VIEW_TYPE_NO_SPAM){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.call_list, parent, false)

            return ViewHolderCallNoSpam(view)
        }else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.call_list_item_loading, parent, false)
            return DummYViewHolder(view)
        }

    }

    override fun getItemViewType(position: Int): Int {
        if(this.callLogs.isNotEmpty() && position < callLogs.size)
             if(this.callLogs[position].id == null){
                return VIEW_TYPE_LOADING
            }

        return VIEW_TYPE_NO_SPAM
    }
override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val contact = callLogs[position]
    when(holder.itemViewType) {

         VIEW_TYPE_NO_SPAM -> {


             (holder as ViewHolderCallNoSpam).bind(callLogs[position],context, onContactItemClickListener)
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

    fun setCallLogs(newContactList: List<CallLogData>) {
        callLogs = newContactList
        this.submitList(newContactList)
    }

    inner class ViewHolderCallLoading(private val view: View) : RecyclerView.ViewHolder(view) {
        private val name = view.textVcallerNameSpam
        private val circle = view.textViewCallCrclrSpam;

//        private val image = view.findViewById<ImageView>(R.id.contact_image)

        fun bind(
            callLog: CallLogData, context: Context,
            onContactItemClickListener: (id: Long, postition: Int, view: View, btn: Int, callLog: CallLogData) -> Int
        ) {

        }
    }


    inner class ViewHolderCallNoSpam(private val view: View) : RecyclerView.ViewHolder(view) {
        private val name = view.textVcallerName
         private val circle = view.textViewCrclr;
            private val expandableView = view.findViewById<ConstraintLayout>(R.id.layoutExpandableCall)

//        private val image = view.findViewById<ImageView>(R.id.contact_image)

        fun bind(
            callLog: CallLogData, context: Context,
            onContactItemClickListener: (id: Long, postition: Int, view: View, btn: Int, callLog: CallLogData) -> Int
        ) {
            Log.d(TAG, "bind: ")
            expandableView.setTag(callLog.dateInMilliseconds )
            if(prevTime!= null)
                if(prevTime == callLog.dateInMilliseconds){
                    expandableView.beVisible()

                }else{
                   expandableView.beGone()

                }
            if(callLog.callerInfoFoundFrom == SENDER_INFO_SEARCHING){
                view.pgBarCallItem.beVisible()
            }
           if(callLog.spamCount > 0){

               name.setColorForText(R.color.spamText)

           }else{
               name.setColorForText(R.color.textColor)

           }

            name.text = if(callLog.name == null || callLog!!.name!!.isEmpty()) callLog.number else callLog.name
            //        Log.i(TAG, String.valueOf(no));
            setNameFirstChar(callLog)
            val pNo = callLog.number
//            Glide.with(context).load(R.drawable.ic_account_circle_24px).into(image)
           generateCircleView( callLog);

            //call type
            setCallTypeImage(callLog,  view.imgVCallType,view.textVCallDirection)


            /**
             * This is important to check else double/ duplicate marking of items occur
             */
            if(containsItem(callLog.id!!)){
                view.imgViewCallMarked.beVisible()
            }else{
                view.imgViewCallMarked.beInvisible()

            }
            var id = getExpandedLayoutId()
            if(id!=null) {
            if(id == callLog.id){
                expandableView.beVisible()
            }else{
                expandableView.beGone()


            }
            }
            view.textViewTime.text = callLog.relativeTime
            expandableView.tvExpandNumCall.text = callLog.number


            setClickListener(view, callLog)

        }

        private fun setClickListener(view: View, callLog: CallLogData) {
            view.setOnLongClickListener{v->
                longPressHandler.onLongPressed(v,
                    this.adapterPosition, callLog.id!!, callLog.number)
                true

            }
            view.imgBtnCallExpand.setOnClickListener{
                longPressHandler.onCallButtonClicked(it, INTENT_TYPE_MAKE_CALL, callLog)
            }

            view.imgBtnSmsExpand.setOnClickListener{
                longPressHandler.onCallButtonClicked(it, INTENT_TYPE_START_INDIVIDUAL_SMS, callLog)
            }

            view.imgBtnInfoExpand.setOnClickListener{
                longPressHandler.onCallButtonClicked(it, INTENT_TYPE_MORE_INFO, callLog)
            }


            view.setOnClickListener(View.OnClickListener {v->
                val viewExpanded =  onContactItemClickListener(callLog.id!!, this.adapterPosition, v, BUTTON_SIM_1, callLog)
                if(viewExpanded== 1){
                    //iff marking not started expand the layout
                    toggleExpandableView(v, this.adapterPosition, callLog.id!!, expandableView)
                }

            })
        }

        private fun toggleExpandableView(
             v: View,
             pos: Int,
             id: Long,
             expandableView: ConstraintLayout
         ) {

             var expandedLyoutId = getExpandedLayoutId()
             if(expandedLyoutId==null){
                 //no views has not yet expanded, so expand the current layout
                 setExpandedLayoutId(id)
                 setExpandedLayoutView(expandableView)
                 expandableView.beVisible()

             }else if(expandedLyoutId == id){
                 //the layout is already expaned so, hide it
                 expandableView.beGone()
                 setExpandedLayoutId(null)
                 setExpandedLayoutView(null)
             }else{
                 //new item expanded
                 getExpandedLayoutView()!!.beGone()
                 expandableView.beVisible()
                 setExpandedLayoutView(expandableView)
                 setExpandedLayoutId(id)


             }
         }



         private fun setNameFirstChar(callLog: CallLogData) {
             if(callLog.spamCount > 0){
                 view.imgViewCallSpamIcon.beVisible()
                 view.imgViewCallSpamIcon.setImageResource(R.drawable.ic_baseline_block_red)
                 circle.text = ""
             }else{
                 view.imgViewCallSpamIcon.beInvisible()
                 val name: String = if(callLog.name == null || callLog.name!!.isEmpty()) callLog.number else callLog.name!!
                 val firstLetter = name[0]
                 val firstLetterString = firstLetter.toString().toUpperCase()
                 circle.text = firstLetterString
             }



         }


         private fun generateCircleView( callLog: CallLogData) {
             if(callLog.spamCount > 0){
                 callLog.color =   circle.setRandomBackgroundCircle(TYPE_SPAM)
             }else{
                 callLog.color = circle.setRandomBackgroundCircle()
             }

         }

     }


    class CallItemDiffCallback : DiffUtil.ItemCallback<CallLogData>() {
        override fun areItemsTheSame(oldItem: CallLogData, newItem: CallLogData): Boolean {
            return  oldItem.id == newItem.id


        }


        override fun areContentsTheSame(oldItem: CallLogData, newItem: CallLogData): Boolean {


            return oldItem.spamCount == newItem.spamCount && oldItem.callerInfoFoundFrom == newItem.callerInfoFoundFrom
            //TODO compare both messages and if the addres is same and message
        }

    }


    private fun setCallTypeImage(callLog: CallLogData, imageView: ImageView, textView:TextView) {
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

        if(callLog.spamCount > 0 ){
            textView.setColorForText( R.color.spamText)

        }else{
            textView.setColorForText(R.color.textColor)

        }
        when (callLog.type) {
            1 -> { // incomming call
                imageView.setImageResource(R.drawable.ic_baseline_call_received_24)
                textView.text = "Incoming call"

            }
            2 -> { // outgoing call
                imageView.setImageResource(R.drawable.ic_baseline_call_made_24)
                textView.text = "Outgoing call"
            }
            3 -> {
                imageView.setImageResource(R.drawable.ic_baseline_call_missed_24)
                textView.text = "Missed call"
            }
            5->{
                textView.text = "Rejected"
            }
            6 ->{
                textView.text = "Blocked"
            }

        }
    }
    interface CallItemLongPressHandler {
        fun onLongPressed(view:View, pos:Int, id: Long, address:String)
        fun onCallButtonClicked(view: View, type:Int, log: CallLogData)



    }


}



