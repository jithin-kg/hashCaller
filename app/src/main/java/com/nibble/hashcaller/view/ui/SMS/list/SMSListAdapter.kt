package com.nibble.hashcaller.view.ui.SMS.list

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.SMS.list.SMSListAdapter.ViewHolder
import com.nibble.hashcaller.view.ui.SMS.util.SMS
import kotlinx.android.synthetic.main.sms_list_view.view.*
import java.lang.IndexOutOfBoundsException
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*


/**
 * Created by Jithin KG on 22,July,2020
 */
class SMSListAdapter(private val context: Context, private val onContactItemClickListener: (id:String)->Unit) :
    androidx.recyclerview.widget.ListAdapter<SMS, ViewHolder>(SMSItemDiffCallback()) {

    private var smsList = emptyList<SMS>()

    companion object{
        private const val TAG = "__SMSListAdapter";
        public var searchQry:String? = null
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sms_list_view, parent, false)

        return ViewHolder(view)
    }

//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val contact = contacts[position]
//        holder.bind(contact, context, onContactItemClickListener)
//    }
override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//    val contact = smsList[position]
//    holder.bind(contact, context, onContactItemClickListener)
    when(holder) {

        is ViewHolder -> {
            holder.bind(getItem(position),context, onContactItemClickListener)
        }

    }
}


//    override fun getItemCount(): Int {
////        Log.d("__ContactAdapter", "getItemCount: ${contacts.size}")
//       return smsList.size
//    }

//    fun setSMSList(
//        newSMSList: List<SMS>,
//        query: String?
//    ) {
////        smsList = newSMSList
//        searchQry = query
//        Log.d(TAG, "setSMSList query: $query ")
//        val oldlist = smsList
////        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(SMSItemDiffCallback(oldlist, newSMSList) )
//       smsList = newSMSList
////        diffResult.dispatchUpdatesTo(this)
////        notifyDataSetChanged()
//    }
     class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val name = view.textVSMSContactName
         private val circle = view.textViewSMScontactCrclr;
//        private val image = view.findViewById<ImageView>(R.id.contact_image)

        fun bind(
            sms: SMS, context: Context,
            onContactItemClickListener:(id:String)->Unit ) {

            //        Log.i(TAG, String.valueOf(no));

//            highlightSearhcField(sms) // to highlight the search result
//            if(searchQry == null){
                name.text = sms.address
                view.tvSMSMPeek.text = sms.msg

//            }


            setTimeInView(sms.time)


            setNameFirstChar(sms)

//            val pNo = sms.address.toString()
//            Log.d(TAG, "phone num $pNo ")
//            Glide.with(context).load(R.drawable.ic_account_circle_24px).into(image)
           generateCircleView(context);


            view.setOnClickListener{

//                onContactItemClickListener(pNo)
            }
        }

         private fun highlightSearhcField(sms: SMS) {

//             if(searchQry!=null){
////                 val lowercaseMsg = sms.msg!!.toLowerCase()
//                 val lowerSearchQuery = searchQry!!.toLowerCase()
//                 if(sms.address!!.contains(searchQry!!)){
//                     Log.d(TAG, "address pattern matches")
//                     val startPos = sms.address!!.indexOf(searchQry!!)
//                     val endPos = startPos + searchQry!!.length
//                     view.tvSMSMPeek.text = sms.msg
//                    setSpan(sms.address!!, startPos, endPos, view.textVSMSContactName)
//
//                 }else if(lowercaseMsg.contains(lowerSearchQuery)){
//
//                     Log.d(TAG, "lowercase: $lowercaseMsg")
//                     val startPos = lowercaseMsg.indexOf(lowerSearchQuery)
//                     val endPos = startPos +lowerSearchQuery.length
//                     name.text = sms.address
////                     setSpan(sms.msg!!, startPos, endPos, view.tvSMSMPeek)
//
//                 }else{
//                     name.text = sms.address
//                     view.tvSMSMPeek.text = sms.msg
//                 }
//             }else{
//                 name.text = sms.address
//                 view.tvSMSMPeek.text = sms.msg
//             }

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
                 view.tvSMSTime.text = time
             }else{
                 view.tvSMSTime.text = date
             }
//             Log.d(TAG, "today: $todayDate")
//             Log.d(TAG, "setTimeInView: $d2")


         }

         private fun setNameFirstChar(sms: SMS) {
             val name: String = sms.address.toString()
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
    class SMSItemDiffCallback : DiffUtil.ItemCallback<SMS>() {
        override fun areItemsTheSame(oldItem: SMS, newItem: SMS): Boolean {
            return oldItem.id == newItem.id

        }

        override fun areContentsTheSame(oldItem: SMS, newItem: SMS): Boolean {
            return oldItem.equals(newItem) && oldItem.msg == newItem.msg
        }

    }




}



