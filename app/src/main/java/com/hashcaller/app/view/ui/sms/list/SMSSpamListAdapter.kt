package com.hashcaller.app.view.ui.sms.list

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hashcaller.app.R
import com.hashcaller.app.view.ui.sms.util.SMS
import kotlinx.android.synthetic.main.sms_list_view.view.*
import kotlinx.android.synthetic.main.sms_spam_delete_item.view.*
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*


/**
 * Created by Jithin KG on 22,July,2020
 */


class SMSSpamListAdapter(private val context: Context,
                         private val onContactItemClickListener: (id:String, pos:Int, pno:String)->Unit
 ) :
    androidx.recyclerview.widget.ListAdapter<SMS, RecyclerView.ViewHolder>(SMSItemDiffCallback()) {
    private val VIEW_TYPE_DELETE = 1;
    private val VIEW_TYPE_SMS = 2;
    private var smsList = emptyList<SMS>()
    companion object{
        private const val TAG = "__SMSListAdapter";
        public var searchQry:String? = null
        var prevView:View? = null
        var prevPos:Int? = null

    }
    fun getps(){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == VIEW_TYPE_DELETE){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.sms_spam_delete_item, parent, false)
            return DeleteViewHolder(view)

        }else{
            val view = LayoutInflater.from(parent.context).inflate(R.layout.sms_list_view, parent, false)
            return SmsViewHolder(view)
        }


    }

    override fun getItemViewType(position: Int): Int {
        if(this.smsList.isNotEmpty() && position < smsList.size)
        if(this.smsList[position].deleteViewPresent){
            return VIEW_TYPE_DELETE
        }else{
            return VIEW_TYPE_SMS
        }
        return VIEW_TYPE_SMS
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//    val contact = smsList[position]
//    holder.bind(contact, context, onContactItemClickListener)
    when(holder.itemViewType) {

        VIEW_TYPE_SMS -> {
           val item =  getItem(position)

            (holder as SmsViewHolder).bind(item,context, onContactItemClickListener, position)

        }
        VIEW_TYPE_DELETE ->{
            val item =  getItem(position)

            (holder as DeleteViewHolder).bind(item,context, onContactItemClickListener, position)
        }


    }
}

    fun setList(it: List<SMS>?) {
        this.smsList = it!!
        this.submitList(this.smsList)

    }




    class DeleteViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val btnEmptySms = view.btnEmptySpamSMS

        fun bind(
            sms: SMS, context: Context,
            onDeleteItemclickLIstener: (id: String, pos: Int, pno: String) -> Unit,
            position: Int
        ) {
            btnEmptySms.setOnClickListener{
//                view.tvUnreadSMSCount.text = ""
//                view.tvUnreadSMSCount.visibility = View.INVISIBLE

                onDeleteItemclickLIstener("", 0, ",")

            }
        }
    }
     class SmsViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
         var layoutExpandable: ConstraintLayout = view.layoutExpandable
//         private val name = view.textVSMSContactNameSpam
         private val circle = view.textViewSMScontactCrclr;
//        private val image = view.findViewById<ImageView>(R.id.contact_image)

        fun bind(
            sms: SMS, context: Context,
            onContactItemClickListener: (id: String, position:Int, pno:String) -> Unit,
            position: Int
        ) {
            Log.d(TAG, "bind: ")
//            layoutExpandable?.visibility   = if(sms.expanded) View.VISIBLE else View.GONE
//            if(!sms.isSpam){
                //        Log.i(TAG, String.valueOf(no));

//            highlightSearhcField(sms) // to highlight the search result
//            if(searchQry == null){
//                name.text = sms.address
                view.tvSMSMPeek.text = sms.msg
//                view.tvUnreadSMSCount.text = sms.unReadSMSCount.toString()
//                if(sms.unReadSMSCount == 0 ){
//                    view.tvUnreadSMSCount.visibility = View.GONE
//                }else{
//                    view.tvUnreadSMSCount.visibility = View.VISIBLE
//                }

//            }


                setTimeInView(sms.time)


                setNameFirstChar(sms)

//            val pNo = sms.address.toString()
//            Log.d(TAG, "phone num $pNo ")
//            Glide.with(context).load(R.drawable.ic_account_circle_24px).into(image)
                generateCircleView(context);


                view.setOnClickListener{v->
                  onContactItemClickListener(sms.id.toString(), this.adapterPosition,
                      sms.addressString!!
                  )
                }
            }
//        }





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
//             val firstLetter = name[0]
             val firstLetter = "h"
             val firstLetterString = firstLetter.toString().toUpperCase()
             circle.text = firstLetterString
         }

         private fun generateCircleView(context: Context) {
             val rand = Random()
             when (rand.nextInt(5 - 1) + 1) {
                 1 -> {
                     circle.background = ContextCompat.getDrawable(context, R.drawable.contact_circular_background)
                 }
                 2 -> {
                     circle.background = ContextCompat.getDrawable(context, R.drawable.contact_circular_background2)
                 }
                 3 -> {
                     circle.background = ContextCompat.getDrawable(context, R.drawable.contact_circular_background3)
                 }
                 else -> {
                     circle.background = ContextCompat.getDrawable(context, R.drawable.contact_circular_background4)
                 }
             }
         }

     }
    class SMSItemDiffCallback : DiffUtil.ItemCallback<SMS>() {
        override fun areItemsTheSame(oldItem: SMS, newItem: SMS): Boolean {
//            return oldItem.expanded == newItem.expanded &&  oldItem.id == newItem.id
            Log.d(TAG, "areItemsTheSame: oldItem ${oldItem.expanded}")
            Log.d(TAG, "areItemsTheSame: newItem ${newItem.expanded}")
            if(oldItem.expanded == newItem.expanded)
                Log.d(TAG, "areItemsTheSame: yes")
            else
                Log.d(TAG, "areItemsTheSame: no")
            return oldItem.expanded == newItem.expanded &&  oldItem.id == newItem.id


        }

        override fun areContentsTheSame(oldItem: SMS, newItem: SMS): Boolean {

            //is different we have new message for a chat
            //update the badge of that addres
            Log.d(TAG, "areContentsTheSame: oldItem ${oldItem.expanded}")
            Log.d(TAG, "areContentsTheSame: newItem ${newItem.expanded}")
            if(oldItem.address == newItem.address){
                if(oldItem.msgString != newItem.msgString){
                    //we have a new message for this addess
                    //set badge counter for this number/address
                    //or i should get the count when i listng sms and if data change compare newcount

                }
            }
            if(oldItem.expanded == newItem.expanded)
                Log.d(TAG, "areContentsTheSame: yes")
            else
                Log.d(TAG, "areContentsTheSame: no")
            return oldItem.expanded == newItem.expanded and oldItem.msgString.equals(newItem.msgString)
           //TODO compare both messages and if the addres is same and message
        }

    }




}



