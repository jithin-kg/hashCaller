package com.nibble.hashcaller.view.ui.call.dialer

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogData
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.containsItem
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.getExpandedLayoutId
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.getExpandedLayoutView
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.setExpandedLayoutId
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.setExpandedLayoutView
import com.nibble.hashcaller.view.ui.sms.individual.util.beGone
import com.nibble.hashcaller.view.ui.sms.individual.util.beInvisible
import com.nibble.hashcaller.view.ui.sms.individual.util.beVisible
import com.nibble.hashcaller.view.ui.sms.util.SENDER_INFO_SEARCHING
import kotlinx.android.synthetic.main.call_list.view.*
import kotlinx.android.synthetic.main.call_list_item_spam.view.*
import java.util.*

/**
 * Created by Jithin KG on 22,July,2020
 */

class DialerAdapter(private val context: Context,
                    private val longPressHandler: CallItemLongPressHandler,
                    private val onContactItemClickListener: (id:Long, postition:Int, view:View, btn:Int, callLog:CallLogData)->Int
                   ) :
    androidx.recyclerview.widget.ListAdapter<CallLogData, RecyclerView.ViewHolder>(CallItemDiffCallback()){
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
        }else if(viewType == VIEW_TYPE_LOADING){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.call_list_item_loading, parent, false)
            return ViewHolderCallLoading(view)
        }
        else{
            val view = LayoutInflater.from(parent.context).inflate(R.layout.call_list_item_spam, parent, false)

            return ViewHolderCallSpam(view)
        }

    }

//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val contact = contacts[position]
//        holder.bind(contact, context, onContactItemClickListener)
//    }

    override fun getItemViewType(position: Int): Int {
        if(this.callLogs.isNotEmpty() && position < callLogs.size)
            if(this.callLogs[position].spamCount > 0){
                return VIEW_TYPE_SPAM
            }else if(this.callLogs[position].id == null){
                return VIEW_TYPE_LOADING
            }

        return VIEW_TYPE_NO_SPAM
    }
override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val contact = callLogs[position]
//    holder.bind(contact, context, onContactItemClickListener)
    when(holder.itemViewType) {

         VIEW_TYPE_NO_SPAM -> {
//            val name = callLogs[position].name
//            Log.d(TAG, "onBindViewHolder:  name $name")

             (holder as ViewHolderCallNoSpam).bind(callLogs[position],context, onContactItemClickListener)
        }
        VIEW_TYPE_SPAM ->{
            (holder as ViewHolderCallSpam).bind(callLogs[position],context, onContactItemClickListener)

        }
        VIEW_TYPE_LOADING ->{
            (holder as ViewHolderCallLoading).bind(callLogs[position],context, onContactItemClickListener)
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
    inner class ViewHolderCallSpam(private val view: View) : RecyclerView.ViewHolder(view) {
        private val name = view.textVcallerNameSpam
        private val circle = view.textViewCallCrclrSpam;

//        private val image = view.findViewById<ImageView>(R.id.contact_image)

        fun bind(
            callLog: CallLogData, context: Context,
            onContactItemClickListener: (id: Long, postition: Int, view: View, btn: Int, callLog: CallLogData) -> Int
        ) {
//            view.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).setTag(callLog.dateInMilliseconds )
            if(prevTime!= null)
                if(prevTime == callLog.dateInMilliseconds){
                    view.findViewById<ConstraintLayout>(R.id.layoutExpandableCallSpam).visibility = View.VISIBLE

                }else{
                    view.findViewById<ConstraintLayout>(R.id.layoutExpandableCallSpam).visibility = View.GONE

                }

            name.text = if(callLog.name == null || callLog!!.name!!.isEmpty()) callLog.number else callLog.name
            //        Log.i(TAG, String.valueOf(no));
            setNameFirstChar(callLog)
            val pNo = callLog.number
//            Glide.with(context).load(R.drawable.ic_account_circle_24px).into(image)
            generateCircleView(context);

            //call type
            setCallTypeImage(callLog, view.imgVCallTypeSpam, view.txtViewDirectionSpam)
            //setDate
//            view.textViewTimeSpam.text = callLog.date
//            view.txtViewDirectionSpam.text = callLog.
            view.textViewTimeSpam.text = callLog.relativeTime

            view.findViewById<ConstraintLayout>(R.id.layoutExpandableCallSpam).findViewById<ImageButton>(R.id.imgBtnCallExpandSpam) .setOnClickListener {
                onContactItemClickListener(callLog.id!!, this.adapterPosition, it, BUTTON_SIM_1,callLog)
            }

            view.setOnClickListener(View.OnClickListener {v->
//                onContactItemClickListener("2", this.adapterPosition, view)
                prevTime = callLog.dateInMilliseconds
                toggleExpandableView(v, this.adapterPosition, callLog.id!!)


            })
        }

        private fun toggleExpandableView(v: View, pos: Int, id:Long) {
            var expandedLyoutId = getExpandedLayoutId()
            if(expandedLyoutId==null){
                //no views has not yet expanded, so expand the current layout
                setExpandedLayoutId(id)
                setExpandedLayoutView(v.findViewById<ConstraintLayout>(R.id.layoutExpandableCallSpam))
                view.findViewById<ConstraintLayout>(R.id.layoutExpandableCallSpam).beVisible()

            }else if(expandedLyoutId == id){
                //the layout is already expaned so, hide it
                view.findViewById<ConstraintLayout>(R.id.layoutExpandableCallSpam).beGone()
                setExpandedLayoutId(null)
                setExpandedLayoutView(null)
            }else{
                //new item expanded
                getExpandedLayoutView()!!.beGone()
                view.findViewById<ConstraintLayout>(R.id.layoutExpandableCallSpam).beVisible()
                setExpandedLayoutView(v.findViewById<ConstraintLayout>(R.id.layoutExpandableCallSpam))
                setExpandedLayoutId(id)


            }
        }



        private fun setNameFirstChar(callLog: CallLogData) {
            val name: String = if(callLog.name == null || callLog.name!!.isEmpty()) callLog.number else callLog.name!!
            val firstLetter = name[0]
            val firstLetterString = firstLetter.toString().toUpperCase()
//            circle.text = firstLetterString
            view.imgViewCallSpamIcon.setImageResource(R.drawable.ic_baseline_block_red)

        }

        private fun generateCircleView(context: Context) {
            circle.background = ContextCompat.getDrawable(context, R.drawable.contact_circular_background_spam)

        }

    }
    inner class ViewHolderCallNoSpam(private val view: View) : RecyclerView.ViewHolder(view) {
        private val name = view.textVcallerName
         private val circle = view.textViewCrclr;

//        private val image = view.findViewById<ImageView>(R.id.contact_image)

        fun bind(
            callLog: CallLogData, context: Context,
            onContactItemClickListener: (id: Long, postition: Int, view: View, btn: Int, callLog: CallLogData) -> Int
        ) {
            Log.d(TAG, "bind: ")
            view.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).setTag(callLog.dateInMilliseconds )
            if(prevTime!= null)
                if(prevTime == callLog.dateInMilliseconds){
                    view.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).visibility = View.VISIBLE

                }else{
                    view.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).visibility = View.GONE

                }
            if(callLog.callerInfoFoundFrom == SENDER_INFO_SEARCHING){
                view.pgBarCallItem.beVisible()
            }else{
                view.pgBarCallItem.beInvisible()

            }
            name.text = if(callLog.name == null || callLog!!.name!!.isEmpty()) callLog.number else callLog.name
            //        Log.i(TAG, String.valueOf(no));
            setNameFirstChar(callLog)
            val pNo = callLog.number
//            Glide.with(context).load(R.drawable.ic_account_circle_24px).into(image)
           generateCircleView(context);

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
                view.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).beVisible()
            }else{
                view.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).beGone()


            }
            }
//            else{
//                view.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).beGone()
//
//            }

            //setDate
            view.textViewTime.text = callLog.relativeTime

//            view.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).findViewById<ImageButton>(R.id.imgBtnCallExpand) .setOnClickListener {
//                onContactItemClickListener(callLog.id.toString(), this.adapterPosition, it, BUTTON_SIM_1,callLog)
//            }

            view.setOnLongClickListener{v->
                longPressHandler.onLongPressed(v,
                this.adapterPosition, callLog.id!!, callLog.number)
                true

            }
            view.setOnClickListener(View.OnClickListener {v->
//                onContactItemClickListener("2", this.adapterPosition, view)
//               prevTime = callLog.dateInMilliseconds
//                toggleExpandableView(v, this.adapterPosition)
               val viewExpanded =  onContactItemClickListener(callLog.id!!, this.adapterPosition, v, BUTTON_SIM_1, callLog)
                if(viewExpanded== 1){
                    //iff marking not started expand the layout
                    toggleExpandableView(v, this.adapterPosition, callLog.id!!)
                }

            })
        }

         private fun toggleExpandableView(v: View, pos: Int, id: Long) {

             var expandedLyoutId = getExpandedLayoutId()
             if(expandedLyoutId==null){
                 //no views has not yet expanded, so expand the current layout
                 setExpandedLayoutId(id)
                 setExpandedLayoutView(v.findViewById<ConstraintLayout>(R.id.layoutExpandableCall))
                 view.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).beVisible()

             }else if(expandedLyoutId == id){
                 //the layout is already expaned so, hide it
                 view.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).beGone()
                 setExpandedLayoutId(null)
                 setExpandedLayoutView(null)
             }else{
                 //new item expanded
                 getExpandedLayoutView()!!.beGone()
                 view.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).beVisible()
                 setExpandedLayoutView(v.findViewById<ConstraintLayout>(R.id.layoutExpandableCall))
                 setExpandedLayoutId(id)


             }
         }



         private fun setNameFirstChar(callLog: CallLogData) {
             val name: String = if(callLog.name == null || callLog.name!!.isEmpty()) callLog.number else callLog.name!!
             val firstLetter = name[0]
             val firstLetterString = firstLetter.toString().toUpperCase()
             circle.text = firstLetterString
         }

         private fun generateCircleView(context: Context) {
             val rand = Random()
             when (rand.nextInt(5 - 1) + 1) {
                 1 -> {
                     circle.background = ContextCompat.getDrawable(context, R.drawable.contact_circular_background)
                     circle.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary)
                     )
                 }
                 2 -> {
                     circle.background = ContextCompat.getDrawable(context, R.drawable.contact_circular_background2)
                     circle.setTextColor(ContextCompat.getColor(context, R.color.colorlightBlueviking)
                     )
                 }
                 3 -> {
                     circle.background = ContextCompat.getDrawable(context, R.drawable.contact_circular_background3)
                     circle.setTextColor(ContextCompat.getColor(context, R.color.colorbrightTurquoiseLightBlue
                     )
                     )
                 }
                 else -> {
                     circle.background = ContextCompat.getDrawable(context, R.drawable.contact_circular_background4)
                     circle.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark)
                     )
                 }
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
    interface CallItemLongPressHandler{
        fun onLongPressed(view:View, pos:Int, id: Long, address:String)
    }


}



