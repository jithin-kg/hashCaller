package com.nibble.hashcaller.view.ui.call.dialer

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogData
import kotlinx.android.synthetic.main.call_list.view.*
import kotlinx.android.synthetic.main.sms_individual_sent_item.view.*
import java.util.*

/**
 * Created by Jithin KG on 22,July,2020
 */
class DialerAdapter(private val context: Context, private val onContactItemClickListener: (id:String, postition:Int, view:View)->Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var callLogs = emptyList<CallLogData>()
    companion object{
        private const val TAG = "__DialerAdapter";
        var prevView:View? = null
        var prevPos:Int? = null
        var prevTag:String? = null
        var prevTime : String? = null
    }

     
    

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.call_list, parent, false)

        return ViewHolder(view)
    }

//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val contact = contacts[position]
//        holder.bind(contact, context, onContactItemClickListener)
//    }
override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val contact = callLogs[position]
//    holder.bind(contact, context, onContactItemClickListener)
    when(holder) {

        is ViewHolder -> {
            val name = callLogs[position].name
            Log.d(TAG, "onBindViewHolder:  name $name")

            holder.bind(callLogs[position],context, onContactItemClickListener)
//            holder.itemView.setOnClickListener {
//                Log.d(TAG, "clicked")
//                it.visibility = View.GONE
//            }
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
     class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val name = view.textVcallerName
         private val circle = view.textViewCrclr;

//        private val image = view.findViewById<ImageView>(R.id.contact_image)

        fun bind(
            callLog: CallLogData, context: Context,
            onContactItemClickListener:(id:String, posoitin:Int, view:View)->Unit ) {
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
            //setDate
            view.textViewTime.text = callLog.date

//            view.setOnClickListener{
//
//                onContactItemClickListener(pNo)
//            }

            view.setOnClickListener(View.OnClickListener {v->
//                onContactItemClickListener("2", this.adapterPosition, view)
               prevTime = callLog.dateInMilliseconds

                if(v.findViewById<ConstraintLayout>(R.id.layoutcallMain).findViewById<View>(R.id.dividerCall).visibility== View.VISIBLE)
                    v.findViewById<ConstraintLayout>(R.id.layoutcallMain).findViewById<View>(R.id.dividerCall).visibility = View.GONE
                else if(v.findViewById<ConstraintLayout>(R.id.layoutcallMain).findViewById<View>(R.id.dividerCall).visibility== View.GONE)
                    v.findViewById<ConstraintLayout>(R.id.layoutcallMain).findViewById<View>(R.id.dividerCall).visibility = View.VISIBLE

                toggleExpandableView(v, this.adapterPosition)


////               if( view.isFocused){
//////                   view.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).visibility = View.VISIBLE
////               }
////                view.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).visibility = View.GONE
//                val pos = this.layoutPosition
//                val pos2 = this.adapterPosition
//
////                Log.d(TAG, "bind: tag ${v.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).tag}")
//
////               toggleExpandableView(v, pos)
//                //Log.d(TAG, "bind: onclick")
//               // callLog.expanded = true
////                notifyItemChanged(adapterPosition)
            })
        }

         private fun toggleExpandableView(v: View, pos: Int) {
             val tag:String = v.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).tag  as String
             if(prevView == null){
                 //first click
                 Log.d(TAG, "toggleExpandableView: first click")
                 v.findViewWithTag<ConstraintLayout>(tag).visibility = View.VISIBLE
                 v.findViewById<ConstraintLayout>(R.id.layoutcallMain).findViewById<View>(R.id.dividerCall).visibility = View.GONE
//                 v.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).visibility = View.VISIBLE
//                 prevPos = pos
                 prevTag = tag
                 prevView = v

             }else if(!tag.equals(prevView!!.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).tag)){
                //clicked on new item
                 Log.d(TAG, "toggleExpandableView: not euqals")
                 prevView!!.findViewWithTag<ConstraintLayout>(prevView!!.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).tag).visibility = View.GONE
                 prevView!!.findViewById<ConstraintLayout>(R.id.layoutcallMain).findViewById<View>(R.id.dividerCall).visibility = View.VISIBLE
//                 prevView!!.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).visibility = View.GONE
//                 v.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).visibility = View.VISIBLE
                 v.findViewWithTag<ConstraintLayout>(tag).visibility = View.VISIBLE
                 v.findViewById<ConstraintLayout>(R.id.layoutcallMain).findViewById<View>(R.id.dividerCall).visibility = View.GONE

                 prevView = v
//                 prevPos = pos
                 prevTag = tag
//             }else if(tag.equals (prevView!!.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).tag)){
             }else if(prevView == v){
                 Log.d(TAG, "toggleExpandableView: euqals")
//                 if (v.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).visibility == View.VISIBLE){
                 if (v.findViewWithTag<ConstraintLayout>(tag).visibility == View.VISIBLE){
//                     v.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).visibility = View.GONE
                     v.findViewWithTag<ConstraintLayout>(tag).visibility = View.GONE
                     v.findViewById<ConstraintLayout>(R.id.layoutcallMain).findViewById<View>(R.id.dividerCall).visibility = View.VISIBLE
//                     prevPos = pos
                     prevTag = tag
                     prevView = v
                 }else{
                     v.findViewWithTag<ConstraintLayout>(tag).visibility = View.VISIBLE
                     v.findViewById<ConstraintLayout>(R.id.layoutcallMain).findViewById<View>(R.id.dividerCall).visibility = View.GONE
//                 prevPos = pos
                     prevTag = tag
                     prevView = v
                 }
//                     v.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).visibility = View.VISIBLE

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




}



