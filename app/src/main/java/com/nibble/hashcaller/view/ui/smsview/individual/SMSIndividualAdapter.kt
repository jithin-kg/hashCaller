package com.nibble.hashcaller.view.ui.smsview.individual


import android.content.Context
import android.graphics.Color
import android.os.Build
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
import com.nibble.hashcaller.view.ui.smsview.util.SMS
import kotlinx.android.synthetic.main.sms_individual_list_view.view.*
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
    androidx.recyclerview.widget.ListAdapter<SMS, SMSIndividualAdapter.ViewHolder>(SMSIndividualDiffCallback()) {
    private var smsList = emptyList<SMS>()

    companion object{
        private const val TAG = "__SMSIndividualAdapter";
        public var searchQry:String? = null
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sms_individual_list_view, parent, false)
        Log.d(TAG, "onCreateViewHolder: ")
        return ViewHolder(view)
    }

//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val contact = contacts[position]
//        holder.bind(contact, context, onContactItemClickListener)
//    }
//override fun getItemCount(): Int {
//    return smsList.size
//}

override fun onBindViewHolder(holder:  ViewHolder, position: Int) {
//    val contact = smsList[position]
//    holder.bind(contact, context, onContactItemClickListener)

    Log.d(TAG, "onBindViewHolder: ")
    when(holder) {

        is ViewHolder -> {
            holder.bind(getItem(position),context, onContactItemClickListener, position, holder)
        }

    }
}

    fun setList(it: List<SMS>?) {
        smsList = it!!
        this.submitList(it)
//        if(smsList.isNotEmpty() && smsList.size < it!!.size){
//            positionTracker.shouldWeScroll()
//        }
//        notifyDataSetChanged()

    }

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val msg = view.tvMsg
//         private val circle = view.textViewSMScontactCrclr;
//        private val image = view.findViewById<ImageView>(R.id.contact_image)

        fun bind(
            sms: SMS, context: Context,
            onContactItemClickListener: (id: String) -> Unit,
            position: Int,
            holder: ViewHolder
        ) {
            Log.d(TAG, "bind: called")

            if(position  == smsList.size - 1){
            positionTracker.lastItemReached()
            }else if(position < smsList.size - 1){
                positionTracker.otherPosition()
            }
            msg.text = sms.msgString
//                view.tvTime.text = sms.time.toString()



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
             Log.d(TAG, "setSpan: startPos:$startPos")
             Log.d(TAG, "setSpan: endPos:$endPos")
             try{
                 spannableStringBuilder.setSpan(yellow,startPos, endPos, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
             }catch (e:IndexOutOfBoundsException){
                 Log.d(TAG, "setSpan: $e")
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
                 //only set time in view
                 view.tvTime.text = time
             }else{
                 view.tvTime.text = date
             }
//             Log.d(TAG, "today: $todayDate")
//             Log.d(TAG, "setTimeInView: $d2")


         }




     }
    class SMSIndividualDiffCallback : DiffUtil.ItemCallback<SMS>() {


        override fun areItemsTheSame(oldItem: SMS, newItem: SMS): Boolean {
            if(oldItem.time == newItem.time){
                Log.d(TAG, "areItemsTheSame: time are same")
            }else{
                Log.d(TAG, "areItemsTheSame: time not same")
            }
            return oldItem.time == newItem.time

        }

        override fun areContentsTheSame(oldItem: SMS, newItem: SMS): Boolean {
            if(oldItem.equals(newItem)){
                Log.d(TAG, "areContentsTheSame:  equal")
            }else{
                Log.d(TAG, "areContentsTheSame: not equal")

            }
            val b = oldItem.equals(newItem) && oldItem.time == newItem.time

            Log.d(TAG, "areContentsTheSame: b : $b")
            return oldItem.time == newItem.time && oldItem.msgString == newItem.msgString
        }

    }

interface ItemPositionTracker{
    fun lastItemReached()
    fun otherPosition()
    fun shouldWeScroll()
}


}



