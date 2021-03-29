package com.nibble.hashcaller.view.ui.call.dialer

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogData
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall
import com.nibble.hashcaller.view.ui.call.utils.IndividualMarkedItemHandlerCall.containsItem
import com.nibble.hashcaller.view.ui.sms.individual.util.beInvisible
import com.nibble.hashcaller.view.ui.sms.individual.util.beVisible
import com.nibble.hashcaller.view.ui.sms.list.SMSListAdapter
import com.nibble.hashcaller.view.ui.sms.util.MarkedItemsHandler
import com.nibble.hashcaller.view.ui.sms.util.SMS
import kotlinx.android.synthetic.main.call_list.view.*
import kotlinx.android.synthetic.main.call_list_item_spam.view.*
import kotlinx.android.synthetic.main.sms_list_view.view.*
import java.util.*

/**
 * Created by Jithin KG on 22,July,2020
 */

class DialerAdapter(private val context: Context,
                    private val longPressHandler: CallItemLongPressHandler,
                    private val onContactItemClickListener: (id:String, postition:Int, view:View, btn:Int, callLog:CallLogData)->Unit
                   ) :
    androidx.recyclerview.widget.ListAdapter<CallLogData, RecyclerView.ViewHolder>(CallItemDiffCallback()){
    private val VIEW_TYPE_NO_SPAM = 0;
    private val VIEW_TYPE_SPAM = 1;
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
        }else{
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

    }

}

    override fun getItemCount(): Int {
//        Log.d("__ContactAdapter", "getItemCount: ${contacts.size}")
       return callLogs.size
    }

    fun setCallLogs(newContactList: List<CallLogData>) {
        callLogs = newContactList

        notifyDataSetChanged()
    }

    inner class ViewHolderCallSpam(private val view: View) : RecyclerView.ViewHolder(view) {
        private val name = view.textVcallerNameSpam
        private val circle = view.textViewCallCrclrSpam;

//        private val image = view.findViewById<ImageView>(R.id.contact_image)

        fun bind(
            callLog: CallLogData, context: Context,
            onContactItemClickListener:(id:String, posoitin:Int, view:View, btn:Int, callLog:CallLogData)->Unit ) {
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
            setCallTypeImage(callLog)
            //setDate
            view.textViewTimeSpam.text = callLog.date

            view.findViewById<ConstraintLayout>(R.id.layoutExpandableCallSpam).findViewById<ImageButton>(R.id.imgBtnCallExpandSpam) .setOnClickListener {
                onContactItemClickListener(callLog.id.toString(), this.adapterPosition, it, BUTTON_SIM_1,callLog)
            }

            view.setOnClickListener(View.OnClickListener {v->
//                onContactItemClickListener("2", this.adapterPosition, view)
                prevTime = callLog.dateInMilliseconds
                toggleExpandableView(v, this.adapterPosition)


            })
        }

        private fun toggleExpandableView(v: View, pos: Int) {
            val tag:String = v.findViewById<ConstraintLayout>(R.id.layoutExpandableCallSpam).tag  as String
            if(prevView == null){
                //first click
                Log.d(TAG, "toggleExpandableView: first click")
                v.findViewWithTag<ConstraintLayout>(tag).visibility = View.VISIBLE
                v.findViewById<ConstraintLayout>(R.id.layoutcallMainSpam).findViewById<View>(R.id.dividerCallSpam).visibility = View.GONE

                prevTag = tag
                prevView = v

            }else if(!tag.equals(prevView!!.findViewById<ConstraintLayout>(R.id.layoutExpandableCallSpam).tag)){
                //clicked on new item
                Log.d(TAG, "toggleExpandableView: not euqals")
                prevView!!.findViewWithTag<ConstraintLayout>(prevView!!.findViewById<ConstraintLayout>(R.id.layoutExpandableCallSpam).tag).visibility = View.GONE
                prevView!!.findViewById<ConstraintLayout>(R.id.layoutcallMainSpam).findViewById<View>(R.id.dividerCallSpam).visibility = View.VISIBLE
                v.findViewWithTag<ConstraintLayout>(tag).visibility = View.VISIBLE
                v.findViewById<ConstraintLayout>(R.id.layoutcallMainSpam).findViewById<View>(R.id.dividerCallSpam).visibility = View.GONE
                prevView = v
                prevTag = tag

            }else if(prevView == v){
                Log.d(TAG, "toggleExpandableView: euqals")
                if (v.findViewWithTag<ConstraintLayout>(tag).visibility == View.VISIBLE){
                    v.findViewWithTag<ConstraintLayout>(tag).visibility = View.GONE
                    v.findViewById<ConstraintLayout>(R.id.layoutcallMainSpam).findViewById<View>(R.id.dividerCallSpam).visibility = View.VISIBLE
                    prevTag = tag
                    prevView = v
                }else{
                    v.findViewWithTag<ConstraintLayout>(tag).visibility = View.VISIBLE
                    v.findViewById<ConstraintLayout>(R.id.layoutcallMainSpam).findViewById<View>(R.id.dividerCallSpam).visibility = View.GONE
                    prevTag = tag
                    prevView = v
                }
            }
        }

        private fun setCallTypeImage(callLog: CallLogData) {
            when (callLog.type) {
                1 -> { // incomming call
                    view.imgVCallType.setImageResource(R.drawable.ic_baseline_call_received_24)
                }
                2 -> { // outgoing call
                    view.imgVCallType.setImageResource(R.drawable.ic_baseline_call_made_24)
                }
                else -> {
                    view.imgVCallType.setImageResource(R.drawable.ic_baseline_call_missed_24)
                }
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
    inner class ViewHolderCallNoSpam(private val view: View) : RecyclerView.ViewHolder(view) {
        private val name = view.textVcallerName
         private val circle = view.textViewCrclr;

//        private val image = view.findViewById<ImageView>(R.id.contact_image)

        fun bind(
            callLog: CallLogData, context: Context,
            onContactItemClickListener:(id:String, posoitin:Int, view:View, btn:Int, callLog:CallLogData)->Unit ) {
            view.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).setTag(callLog.dateInMilliseconds )
            if(prevTime!= null)
                if(prevTime == callLog.dateInMilliseconds){
                    view.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).visibility = View.VISIBLE

                }else{
                    view.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).visibility = View.GONE

                }

            name.text = if(callLog.name == null || callLog!!.name!!.isEmpty()) callLog.number else callLog.name
            //        Log.i(TAG, String.valueOf(no));
            setNameFirstChar(callLog)
            val pNo = callLog.number
//            Glide.with(context).load(R.drawable.ic_account_circle_24px).into(image)
           generateCircleView(context);

            //call type
            setCallTypeImage(callLog)

            /**
             * This is important to check else double/ duplicate marking of items occur
             */
            if(containsItem(callLog.id)){
                view.imgViewCallMarked.beVisible()
            }else{
                view.imgViewCallMarked.beInvisible()

            }

            //setDate
            view.textViewTime.text = callLog.date

            view.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).findViewById<ImageButton>(R.id.imgBtnCallExpand) .setOnClickListener {
                onContactItemClickListener(callLog.id.toString(), this.adapterPosition, it, BUTTON_SIM_1,callLog)
            }

            view.setOnLongClickListener{v->
                longPressHandler.onLongPressed(v,
                this.adapterPosition, callLog.id, callLog.number)
                true

            }
            view.setOnClickListener(View.OnClickListener {v->
//                onContactItemClickListener("2", this.adapterPosition, view)
               prevTime = callLog.dateInMilliseconds
                toggleExpandableView(v, this.adapterPosition)


            })
        }

         private fun toggleExpandableView(v: View, pos: Int) {
             val tag:String = v.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).tag  as String
             if(prevView == null){
                 //first click
                 Log.d(TAG, "toggleExpandableView: first click")
                 v.findViewWithTag<ConstraintLayout>(tag).visibility = View.VISIBLE
                 v.findViewById<ConstraintLayout>(R.id.layoutcallMain).findViewById<View>(R.id.dividerCall).visibility = View.GONE

                 prevTag = tag
                 prevView = v

             }else if(!tag.equals(prevView!!.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).tag)){
                //clicked on new item
                 Log.d(TAG, "toggleExpandableView: not euqals")
                 prevView!!.findViewWithTag<ConstraintLayout>(prevView!!.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).tag).visibility = View.GONE
                 prevView!!.findViewById<ConstraintLayout>(R.id.layoutcallMain).findViewById<View>(R.id.dividerCall).visibility = View.VISIBLE
                 v.findViewWithTag<ConstraintLayout>(tag).visibility = View.VISIBLE
                 v.findViewById<ConstraintLayout>(R.id.layoutcallMain).findViewById<View>(R.id.dividerCall).visibility = View.GONE
                 prevView = v
                 prevTag = tag

             }else if(prevView == v){
                 Log.d(TAG, "toggleExpandableView: euqals")
                 if (v.findViewWithTag<ConstraintLayout>(tag).visibility == View.VISIBLE){
                     v.findViewWithTag<ConstraintLayout>(tag).visibility = View.GONE
                     v.findViewById<ConstraintLayout>(R.id.layoutcallMain).findViewById<View>(R.id.dividerCall).visibility = View.VISIBLE
                     prevTag = tag
                     prevView = v
                 }else{
                     v.findViewWithTag<ConstraintLayout>(tag).visibility = View.VISIBLE
                     v.findViewById<ConstraintLayout>(R.id.layoutcallMain).findViewById<View>(R.id.dividerCall).visibility = View.GONE
                     prevTag = tag
                     prevView = v
                 }
             }
         }

         private fun setCallTypeImage(callLog: CallLogData) {
             when (callLog.type) {
                 1 -> { // incomming call
                     view.imgVCallType.setImageResource(R.drawable.ic_baseline_call_received_24)
                 }
                 2 -> { // outgoing call
                     view.imgVCallType.setImageResource(R.drawable.ic_baseline_call_made_24)
                 }
                 else -> {
                    view.imgVCallType.setImageResource(R.drawable.ic_baseline_call_missed_24)
                 }
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
//            else
//                Log.d(TAG, "areItemsTheSame: no")
            return  oldItem.id == newItem.id


        }


        override fun areContentsTheSame(oldItem: CallLogData, newItem: CallLogData): Boolean {


            return oldItem.spamCount == newItem.spamCount
            //TODO compare both messages and if the addres is same and message
        }

    }
    interface CallItemLongPressHandler{
        fun onLongPressed(view:View, pos:Int, id: Long, address:String)
    }


}



