package com.hashcaller.app.view.ui.call.individualCallLog


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hashcaller.app.R
import com.hashcaller.app.databinding.IndividualCallLogItemBinding
import com.hashcaller.app.view.ui.extensions.setColorForText
import com.hashcaller.app.view.utils.getRelativeDuration
import com.hashcaller.app.view.utils.getRelativeTime


/**
 * Created by Jithin KG on 22,July,2020
 */
class IndividualCallLogAdapter(
           private val  longPresHandler:LongPressHandler, private val context: Context,
           private val onContactItemClickListener: (id:String)->Unit
) :
    androidx.recyclerview.widget.ListAdapter<IndividualCallLogObj, RecyclerView.ViewHolder>(SMSIndividualDiffCallback()) {
    private var smsList:MutableList<IndividualCallLogObj>? = mutableListOf()


    companion object{
        private const val TAG = "__IndividualCallLogAdapter";
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val binding = IndividualCallLogItemBinding.inflate(LayoutInflater.from(context), parent, false)
            return LogViewHolder(binding)
    }

    //    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val contact = contacts[position]
//        holder.bind(contact, context, onContactItemClickListener)
//    }
//override fun getItemCount(): Int {
//    return smsList.size
//    override fun getItemViewType(position: Int): Int {
////        return super.getItemViewType(position)
//
//        if(smsList.isNotEmpty()){
//            if(smsList[position].type == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT){
//                //sent message 2
//                return VIEW_TYPE_MESSAGE_SENT
//
//            }else if(smsList[position].type == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_INBOX ){
//                return VIEW_TYPE_MESSAGE_RECEIVED
//            }else if(smsList[position].type == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX){
//                return VIEW_TYPE_MESSAGE_OUTBOX
//            }
//            else if(smsList[position].type == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_ALL){
//                return VIEW_TYPE_MESSAGE_RECEIVED
//
//            }
//            else{
//                return  VIEW_TYPE_DATE
//            }
//        }
//
//        else{
//            return VIEW_TYPE_DATE
//
//        }
//
//
//    }
    //}
    override fun onBindViewHolder(holder:  RecyclerView.ViewHolder, position: Int) {
        (holder as LogViewHolder).bind(getItem(position),context, onContactItemClickListener, position, holder)
    }
    fun setList(it: List<IndividualCallLogObj>?) {
        smsList?.clear()
        if (it != null) {
            smsList?.addAll(it)
        }
        this.submitList(smsList)
    }


    inner class LogViewHolder(private val binding: IndividualCallLogItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            log: IndividualCallLogObj, context: Context,
            onContactItemClickListener: (id: String) -> Unit,
            position: Int,
            holderReceivedMessage: RecyclerView.ViewHolder
        ) {

            setCallDirection(log, binding)
            binding.tvTime.text = getRelativeTime(log.date!!).relativeTime
            binding.tvDuration.text = getRelativeDuration(log.duration)


//            view.setOnLongClickListener(View.OnLongClickListener { v ->
//                longPresHandler.onLongPressed(
//                    v, this.adapterPosition, sms.id)
//                true
//            })
        }
    }

    private fun setCallDirection(log: IndividualCallLogObj, binding: IndividualCallLogItemBinding) {
        when(log.type){
            1 -> { // incomming call
                binding.imgVDirection.setImageResource(R.drawable.ic_baseline_call_received_24)
                binding.tvDirection.text = "Incoming call"
                binding.tvDirection.setColorForText(R.color.textColor)

//                textView.text = "Incoming call"

            }
            2 -> { // outgoing call
                binding.imgVDirection.setImageResource(R.drawable.ic_baseline_call_made_24)
                binding.tvDirection.text = "Outgoing call"
//                textView.text = "Outgoing call"
                binding.tvDirection.setColorForText(R.color.textColor)


            }
            3 -> {
                binding.imgVDirection.setImageResource(R.drawable.ic_baseline_call_missed_24)
                binding.tvDirection.text = "Missed call"
                binding.tvDirection.setColorForText(R.color.spamText)
//                textView.text = "Missed call"
            }
            5->{
//                textView.text = "Rejected"
                binding.imgVDirection.setImageResource(R.drawable.ic_baseline_call_missed_24)
                binding.tvDirection.text = "Rejected"
                binding.tvDirection.setColorForText(R.color.textColor)

            }
            6 ->{
//                textView.text = "Blocked"
                binding.imgVDirection.setImageResource(R.drawable.ic_baseline_block_no_color)
                binding.tvDirection.text = "Blocked"
                binding.tvDirection.setColorForText(R.color.textColor)

            }
        }
    }

    class SMSIndividualDiffCallback : DiffUtil.ItemCallback<IndividualCallLogObj>() {


        override fun areItemsTheSame(oldItem: IndividualCallLogObj, newItem: IndividualCallLogObj): Boolean {
            return oldItem.id == newItem.id

        }

        override fun areContentsTheSame(oldItem: IndividualCallLogObj, newItem: IndividualCallLogObj): Boolean {
            return oldItem == newItem

        }

    }

    interface LongPressHandler{
        fun onLongPressed(view:View, pos:Int, id: Long)
    }

}



