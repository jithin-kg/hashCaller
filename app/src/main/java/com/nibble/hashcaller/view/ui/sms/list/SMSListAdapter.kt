package com.nibble.hashcaller.view.ui.sms.list

import android.content.Context
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.util.Log
import android.view.*
import android.view.View.OnLongClickListener
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.contacts.utils.MESSAGE_STRING
import com.nibble.hashcaller.view.ui.sms.util.MarkedItemsHandler
import com.nibble.hashcaller.view.ui.sms.util.SENDER_INFO_SEARCHING
import com.nibble.hashcaller.view.ui.sms.util.SMS
import kotlinx.android.synthetic.main.sms_list_item_spam.view.*
import kotlinx.android.synthetic.main.sms_list_view.view.*
import kotlinx.android.synthetic.main.sms_list_view.view.layoutExpandable
import kotlinx.android.synthetic.main.sms_list_view.view.pgBarSmsListItem
import kotlinx.android.synthetic.main.sms_list_view.view.smsMarked
import kotlinx.android.synthetic.main.sms_list_view.view.textVSMSContactName
import kotlinx.android.synthetic.main.sms_list_view.view.tvSMSMPeek
import kotlinx.android.synthetic.main.sms_list_view.view.tvSMSTime
import kotlinx.android.synthetic.main.sms_list_view.view.tvUnreadSMSCount
import kotlinx.android.synthetic.main.sms_spam_delete_item.view.*
import java.util.*


/**
 * Created by Jithin KG on 22,July,2020
 */


class SMSListAdapter(private val context: Context,
                     private val longPresHandler:LongPressHandler,
                     private val onContactItemClickListener: (view:View, threadId:Long, pos:Int, pno:String)->Unit
) :
    androidx.recyclerview.widget.ListAdapter<SMS, RecyclerView.ViewHolder>(SMSItemDiffCallback()) {
    private val VIEW_TYPE_SMS = 2;
    private val VIEW_TYPE_SPAM = 3;
    private var smsList:MutableList<SMS> = mutableListOf()
    companion object {
        private const val TAG = "__SMSListAdapter";
        public var searchQry:String? = null
        var prevView:View? = null
        var prevPos:Int? = null

    }
    fun getps(){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
       if(viewType == VIEW_TYPE_SPAM){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.sms_list_item_spam, parent, false)
            return SpamViewHolder(view)
        }
        else{
            val view = LayoutInflater.from(parent.context).inflate(R.layout.sms_list_view, parent, false)
            return SmsViewHolder(view)
        }


    }

    override fun getItemViewType(position: Int): Int {
        if(this.smsList.isNotEmpty() && position < smsList.size)
            if(this.smsList[position].spamCount > 0){
                return VIEW_TYPE_SPAM
            }
            else{
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
            VIEW_TYPE_SPAM ->{
//                ED4133 -> letter red  FAE0DE-> light

                val item =  getItem(position)

                (holder as SpamViewHolder).bind(item,context, onContactItemClickListener, position)
            }


        }
    }

    fun setList(it: MutableList<SMS>?) {
        this.smsList = it!!
//        val copy:MutableList<SMS> = mutableListOf()
//        this.smsList.forEach{copy.add(it)}
        this.submitList(it)

    }




    inner class SpamViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val btnEmptySms = view.btnEmptySpamSMS
        private val name = view.textVSMSContactName
        private val circle = view.falseTextSMScontactCrclr;
        private val circleImg = view.imgViewSMScontactCrclr;
        fun bind(
            sms: SMS, context: Context,
            onDeleteItemclickLIstener: (view:View, threadId: Long, pos: Int, pno: String) -> Unit,
            position: Int
        ) {
            if(!sms.name.isNullOrEmpty())
                name.text = sms.name
            else
                name.text = sms.address

            if(sms.senderInfoFoundFrom == SENDER_INFO_SEARCHING){
                view.pgBarSmsListItem.visibility = View.VISIBLE
//                Log.d(TAG, "bind: searching for ${sms.addressString}")
            }else{
                view.pgBarSmsListItem.visibility = View.INVISIBLE
            }
            /**
             * This is important to check else double/ duplicate marking of items occur
             */
            if(MarkedItemsHandler.markedItems.contains(sms.threadID)){
                view.smsMarked.visibility = View.VISIBLE
            }else{
                view.smsMarked.visibility = View.INVISIBLE

            }
            view.tvSMSMPeek.text = sms.msgString
            view.tvUnreadSMSCount.text = sms.unReadSMSCount.toString()
            if(sms.unReadSMSCount == 0 ){
                view.tvUnreadSMSCount.visibility = View.GONE
            }else{
                view.tvUnreadSMSCount.visibility = View.VISIBLE
            }



            view.tvSMSTime.text = sms.relativeTime

            setNameFirstChar(sms)
            generateCircleView(context);


            view.setOnLongClickListener(OnLongClickListener { v ->
                longPresHandler.onLongPressed(v, this.adapterPosition, sms.threadID,
                    sms.addressString!!
                )
                true
            })
            view.setOnClickListener{v->
                onContactItemClickListener(v, sms.threadID, this.adapterPosition,
                    sms.addressString!!
                )

            }
        }

        private fun generateCircleView(context: Context) {
            circle.background = ContextCompat.getDrawable(context, R.drawable.contact_circular_background_spam)
        }

        private fun setNameFirstChar(sms: SMS) {
            val name: String = sms.address.toString()
//             val firstLetter = name[0]
            val firstLetter = sms.addressString!![0]
            val firstLetterString = firstLetter.toString().toUpperCase()
            circleImg.setImageResource(R.drawable.ic_baseline_block_red)
        }
    }
    inner class SmsViewHolder(private val view: View) : RecyclerView.ViewHolder(view),View.OnCreateContextMenuListener {
        var layoutExpandable: ConstraintLayout = view.layoutExpandable
        private val name = view.textVSMSContactName
        private val circle = view.textViewSMScontactCrclr;
//        private val image = view.findViewById<ImageView>(R.id.contact_image)

        fun bind(
            sms: SMS, context: Context,
            onContactItemClickListener: (view:View, threadId: Long, position:Int, pno:String) -> Unit,
            position: Int
        ) {

            if(!sms.name.isNullOrEmpty())
                name.text = sms.name
            else
                name.text = sms.address

            if(sms.senderInfoFoundFrom == SENDER_INFO_SEARCHING){
                view.pgBarSmsListItem.visibility = View.VISIBLE
//                Log.d(TAG, "bind: searching for ${sms.addressString}")
            }else{
                view.pgBarSmsListItem.visibility = View.INVISIBLE
            }

            /**
             * This is important to check else double/ duplicate marking of items occur
             */
            if(MarkedItemsHandler.markedItems.contains(sms.threadID)){
                view.smsMarked.visibility = View.VISIBLE
            }else{
                view.smsMarked.visibility = View.INVISIBLE

            }
//            view.tvSMSMPeek.text = MESSAGE_STRING
            view.tvSMSMPeek.text = sms.msgString
            Log.d(TAG, "bind: messageString is ${sms.msgString}")

            view.tvUnreadSMSCount.text = sms.unReadSMSCount.toString()
            if(sms.unReadSMSCount == 0 ){
                view.tvUnreadSMSCount.visibility = View.GONE
            }else{
                view.tvUnreadSMSCount.visibility = View.VISIBLE
            }



            view.tvSMSTime.text = sms.relativeTime

            setNameFirstChar(sms)
            generateCircleView(context);


            view.setOnLongClickListener(OnLongClickListener { v ->
                longPresHandler.onLongPressed(v, this.adapterPosition, sms.threadID,
                    sms.addressString!!
                )
                true
            })
            view.setOnClickListener{v->
                onContactItemClickListener(v, sms.threadID, this.adapterPosition,
                    sms.addressString!!
                )

            }


        }
//        }

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
//             Log.d(TAG, "setSpan: startPos:$startPos")
//             Log.d(TAG, "setSpan: endPos:$endPos")
            try{
                spannableStringBuilder.setSpan(yellow,startPos, endPos, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
            }catch (e:IndexOutOfBoundsException){
                Log.d(TAG, "setSpan: $e")
            }

            v.text = spannableStringBuilder
        }

        private fun setNameFirstChar(sms: SMS) {
            val name: String = sms.address.toString()
//             val firstLetter = name[0]
            val firstLetter = sms.addressString!![0]
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

        //        https://stackoverflow.com/questions/26466877/how-to-create-context-menu-for-recyclerview
        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            menu!!.add(
                Menu.NONE, R.id.deleteMenuItem,
                Menu.NONE, "delete");

        }


    }
    class SMSItemDiffCallback : DiffUtil.ItemCallback<SMS>() {
        override fun areItemsTheSame(oldItem: SMS, newItem: SMS): Boolean {
//            else
//                Log.d(TAG, "areItemsTheSame: no")
            return  oldItem.addressString == newItem.addressString


        }


        override fun areContentsTheSame(oldItem: SMS, newItem: SMS): Boolean {

            val res =  oldItem.msgString == newItem.msgString && oldItem.senderInfoFoundFrom == newItem.senderInfoFoundFrom
            Log.d(TAG, "areContentsTheSame: ${oldItem.unReadSMSCount} ${newItem.unReadSMSCount} & res:$res")

//            return oldItem.expanded == newItem.expanded and oldItem.msgString.equals(newItem.msgString)
//            Log.d(TAG, "areContentsTheSame: old senderInfoFoundFrom ${oldItem.senderInfoFoundFrom } new senderInfoFoundFRom${oldItem.senderInfoFoundFrom }")
            return  oldItem.unReadSMSCount == newItem.unReadSMSCount &&
                    oldItem.spamCount == newItem.spamCount && oldItem.msgString == newItem.msgString && oldItem.senderInfoFoundFrom == newItem.senderInfoFoundFrom
            //TODO compare both messages and if the addres is same and message
        }

    }

    interface LongPressHandler{
        fun onLongPressed(view:View, pos:Int, id: Long, address:String)
    }




}



