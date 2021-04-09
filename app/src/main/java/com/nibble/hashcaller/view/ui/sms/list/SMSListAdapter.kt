package com.nibble.hashcaller.view.ui.sms.list

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
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
import com.nibble.hashcaller.utils.DummYViewHolder
import com.nibble.hashcaller.view.ui.contacts.utils.TYPE_SPAM
import com.nibble.hashcaller.view.ui.extensions.setColorForText
import com.nibble.hashcaller.view.ui.extensions.setCount
import com.nibble.hashcaller.view.ui.extensions.setRandomBackgroundCircle
import com.nibble.hashcaller.view.ui.sms.individual.util.SMS_NOT_READ
import com.nibble.hashcaller.view.ui.sms.individual.util.beGone
import com.nibble.hashcaller.view.ui.sms.individual.util.beInvisible
import com.nibble.hashcaller.view.ui.sms.individual.util.beVisible
import com.nibble.hashcaller.view.ui.sms.util.MarkedItemsHandler
import com.nibble.hashcaller.view.ui.sms.util.SENDER_INFO_SEARCHING
import com.nibble.hashcaller.view.ui.sms.util.SMS
import kotlinx.android.synthetic.main.sms_list_item_spam.view.*
import kotlinx.android.synthetic.main.sms_list_view.view.*
import kotlinx.android.synthetic.main.sms_list_view.view.layoutExpandable
import kotlinx.android.synthetic.main.sms_list_view.view.pgBarSmsListItem
import kotlinx.android.synthetic.main.sms_list_view.view.smsMarked
import kotlinx.android.synthetic.main.sms_list_view.view.tvSMSMPeek
import kotlinx.android.synthetic.main.sms_list_view.view.tvSMSTime
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
    private val VIEW_TYPE_LOADING = 0
    private val VIEW_TYPE_SMS = 2;

    private var smsList:MutableList<SMS> = mutableListOf()
    companion object {
        private const val TAG = "__SMSListAdapter";
        public var searchQry:String? = null
        var prevView:View? = null
        var prevPos:Int? = null

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

         if(viewType == VIEW_TYPE_LOADING){
           val view = LayoutInflater.from(parent.context).inflate(R.layout.call_list_item_loading, parent, false)
           return DummYViewHolder(view)
       }
        else{
            val view = LayoutInflater.from(parent.context).inflate(R.layout.sms_list_view, parent, false)
            return SmsViewHolder(view)
        }


    }

    override fun getItemViewType(position: Int): Int {
        if(this.smsList.isNotEmpty() && position < smsList.size)
             if(this.smsList[position].isDummy){
                return VIEW_TYPE_LOADING
            }

                return VIEW_TYPE_SMS
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder.itemViewType) {

            VIEW_TYPE_SMS -> {
                val item =  getItem(position)

                (holder as SmsViewHolder).bind(item,context, onContactItemClickListener, position)

            }
            VIEW_TYPE_LOADING ->{
                val item =  getItem(position)

                (holder as DummYViewHolder).bind()
            }

        }
    }

    fun setList(it: MutableList<SMS>?) {
        this.smsList = it!!
//        val copy:MutableList<SMS> = mutableListOf()
//        this.smsList.forEach{copy.add(it)}
        this.submitList(it)

    }

    inner class SmsViewHolder(private val view: View) : RecyclerView.ViewHolder(view),View.OnCreateContextMenuListener {
        var layoutExpandable: ConstraintLayout = view.layoutExpandable
        private val name = view.textVSMSCntctName
        private val circle = view.textViewSMScontactCrclr;
//        private val image = view.findViewById<ImageView>(R.id.contact_image)

        fun bind(
            sms: SMS, context: Context,
            onContactItemClickListener: (view:View, threadId: Long, position:Int, pno:String) -> Unit,
            position: Int
        ) {


                name.text = sms.nameForDisplay
            Log.d(TAG, "bind: setting name ${sms.nameForDisplay}")
            if(sms.spamCount > 0){
                view.textVSMSCntctName.setColorForText(R.color.spamText)
                circle.setRandomBackgroundCircle(TYPE_SPAM)
                view.imgVBlkIconSms.beVisible()
                view.textViewSMScontactCrclr.text = ""
            }else{
                view.textVSMSCntctName.setColorForText(R.color.textColor)
                circle.setRandomBackgroundCircle()
                view.imgVBlkIconSms.beInvisible()
                if(!sms.nameForDisplay.isNullOrEmpty()){
                    circle.text = sms.nameForDisplay[0].toString()

                }


            }
            if(sms.senderInfoFoundFrom == SENDER_INFO_SEARCHING){
                view.pgBarSmsListItem.visibility = View.VISIBLE
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
            Log.d(TAG, "bind: messageString is ${sms.msgString}")
                if(sms.readState ==SMS_NOT_READ){
                    view.tvSMSMPeek.typeface = Typeface.DEFAULT_BOLD
                    view.tvSMSMPeek.alpha = 0.87f
                    view.tvSMSMPeek.setColorForText(R.color.textColor)

                }else{
                    view.tvSMSMPeek.typeface = Typeface.DEFAULT
                    view.tvSMSMPeek.alpha = 0.60f
                    view.tvSMSMPeek.setColorForText(R.color.textColor)
                }


            view.tvSMSTime.text = sms.relativeTime



            view.setOnLongClickListener(OnLongClickListener { v ->
                longPresHandler.onLongPressed(v, this.adapterPosition, sms.threadID,
                    sms.addressString!!
                )
                true
            })
            view.setOnClickListener{v->
                onContactItemClickListener(v, sms.threadID, this.adapterPosition,
                    sms.addresStringNonFormated!!
                )

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
            return  oldItem.addressString == newItem.addressString


        }


        override fun areContentsTheSame(oldItem: SMS, newItem: SMS): Boolean {
            return oldItem == newItem
        }

    }

    interface LongPressHandler{
        fun onLongPressed(view:View, pos:Int, id: Long, address:String)
    }




}



