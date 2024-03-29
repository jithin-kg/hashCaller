package com.hashcaller.app.view.ui.sms.search

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.*
import android.view.View.OnLongClickListener
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hashcaller.app.R
import com.hashcaller.app.view.ui.extensions.setRandomBackgroundCircle
import com.hashcaller.app.view.ui.sms.individual.util.beInvisible
import com.hashcaller.app.view.ui.sms.individual.util.beVisible
import com.hashcaller.app.view.ui.sms.list.SMSListAdapter
import com.hashcaller.app.view.ui.sms.util.*
import com.hashcaller.app.work.formatPhoneNumber
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


class SMSSearchAdapter(private val context: Context,
                       private val longPresHandler:LongPressHandler, private val  networkHandler: SMSListAdapter.NetworkHandler,
                       private val onContactItemClickListener: (view:View, threadId:Long, pos:Int, pno:String, id:Long?)->Unit
 ) :
    androidx.recyclerview.widget.ListAdapter<SMS, RecyclerView.ViewHolder>(SMSSearchItemDiffCallback()) {
    private val VIEW_TYPE_DELETE = 1;
    private val VIEW_TYPE_SMS = 2;
    private var smsList:List<SMS> = mutableListOf()
    companion object{
        private const val TAG = "__SMSSearchAdapter";
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

        }else {

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

            (holder as SmsViewHolder).bind(item,context, onContactItemClickListener, position, networkHandler)

        }
        VIEW_TYPE_DELETE ->{
            val item =  getItem(position)

            (holder as DeleteViewHolder).bind(item,context, onContactItemClickListener, position)
        }


    }
}

    fun setList(it: List<SMS>) {
        this.smsList = it
//        val copy:MutableList<SMS> = mutableListOf()
//        this.smsList.forEach{copy.add(it)}
        this.submitList(it)

    }




    inner class DeleteViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val btnEmptySms = view.btnEmptySpamSMS

        fun bind(
            sms: SMS, context: Context,
            onDeleteItemclickLIstener: (view:View, threadId: Long, pos: Int, pno: String, id:Long?) -> Unit,
            position: Int
        ) {
            btnEmptySms.setOnClickListener{
//                view.tvUnreadSMSCount.text = ""
//                view.tvUnreadSMSCount.visibility = View.INVISIBLE

                onDeleteItemclickLIstener(it,0L, 0, ",", sms.id)

            }
        }
    }
    inner class SmsViewHolder(private val view: View) : RecyclerView.ViewHolder(view),View.OnCreateContextMenuListener {
         var layoutExpandable: ConstraintLayout = view.layoutExpandable
         private val name = view.textVSMSCntctName
//         private val circle = view.textViewSMScontactCrclr;

//        private val image = view.findViewById<ImageView>(R.id.contact_image)

        fun bind(
            sms: SMS, context: Context,
            onContactItemClickListener: (view: View, threadId: Long, position: Int, pno: String, id: Long?) -> Unit,
            position: Int,
            networkHandler: SMSListAdapter.NetworkHandler
        ) {
            Log.d(TAG, "bind: ")
//            layoutExpandable?.visibility   = if(sms.expanded) View.VISIBLE else View.GONE
//            if(!sms.isSpam){
                //        Log.i(TAG, String.valueOf(no));

//            highlightSearhcField(sms) // to highlight the search result
//            if(searchQry == null){
            var isSpam = false
            var senderInforFrom = SENDER_INFO_SEARCHING
            var nameStr = ""
            var firstLetter = ""

            if(!sms.firstName.isNullOrEmpty()){
                nameStr = sms.firstName!!
                senderInforFrom = SENDER_INFO_FROM_CONTENT_PROVIDER
            }else if(sms.firstNameFromServer!=null){
                if(sms.firstNameFromServer!!.isNotEmpty()){
                    nameStr = sms.firstNameFromServer!!
                    senderInforFrom = SENDER_INFO_FROM_DB
                }else{
                    senderInforFrom = SENDER_INFO_NOT_FOUND
                }

            }else {
                nameStr = sms.addressString!!
                senderInforFrom = SENDER_INFO_NOT_FOUND
            }
                 setSpanForNameSearch(sms)


            if(senderInforFrom== SENDER_INFO_SEARCHING &&  networkHandler.isInternetAvailable()){
              view.pgBarSmsListItem.beVisible()
                view.imgvIdentifiedByHash.beInvisible()
                view.imgvIdentifiedByHash.beInvisible()
                Log.d(TAG, "bind: searching for ${sms.addressString}")
            }else if(senderInforFrom == SENDER_INFO_FROM_DB){
                view.imgvIdentifiedByHash.beVisible()
                view.imgvIdentifiedByHash.beVisible()

            }

            else{
                view.pgBarSmsListItem.beInvisible()
                view.imgvIdentifiedByHash.beInvisible()
                view.imgvIdentifiedByHash.beInvisible()

            }
            /**
             * This is important to check else double/ duplicate marking of items occur
             */

            setSpanForBody(sms)

//                view.tvUnreadSMSCount.text = sms.unReadSMSCount.toString()
//                if(sms.unReadSMSCount == 0 ){
//                    view.tvUnreadSMSCount.visibility = View.GONE
//                }else{
//                    view.tvUnreadSMSCount.visibility = View.VISIBLE
//                }

//            }


                setTimeInView(sms.time)



//            val pNo = sms.address.toString()
//            Log.d(TAG, "phone num $pNo ")
//            Glide.with(context).load(R.drawable.ic_account_circle_24px).into(image)
//                generateCircleView(context);


            view.setOnLongClickListener(OnLongClickListener { v ->
//                listener.onLongItemClick(v, viewHolder.getAdapterPosition())
                Log.d(TAG, "bind: threadID ${sms.threadID}")
                //todo deprecated
                    longPresHandler.onLongPressed(v, this.adapterPosition, sms.threadID,
                        sms.addressString!!
                    )
                true
            })
                view.setOnClickListener{v->
                  onContactItemClickListener(v, sms.threadID, this.adapterPosition,
                      sms.addressString!!, sms.id
                  )

//                    if(SMSListAdapter.prevView !=null ){
//
//                        SMSListAdapter.prevView!!.findViewById<ConstraintLayout>(R.id.layoutExpandable).visibility = View.GONE
//                        v.findViewById<ConstraintLayout>(R.id.layoutExpandable).visibility = View.VISIBLE
//                    }else if(SMSListAdapter.prevView == v ){
//                        if(v.findViewById<ConstraintLayout>(R.id.layoutExpandable).visibility == View.VISIBLE){
//                            v.findViewById<ConstraintLayout>(R.id.layoutExpandable).visibility = View.GONE
//                        }else{
//                            v.findViewById<ConstraintLayout>(R.id.layoutExpandable).visibility = View.VISIBLE
//                        }
//                    }
//                    else{
//                        SMSListAdapter.prevView = v
//                        v.findViewById<ConstraintLayout>(R.id.layoutExpandable).visibility = View.VISIBLE
//
//                    }

//                view.tvUnreadSMSCount.text = ""
//                view.tvUnreadSMSCount.visibility = View.INVISIBLE
//                    onContactItemClickListener(sms.addressString!!)
//                    onContactItemClickListener(sms.addressString!!, pos)
//
//                    sms.expanded = true
                }
            }

        private fun setSpanForBody(sms: SMS) {
            if(sms.spanEndPosMsgPeek!=0){
                view.tvSMSMPeek.text = getSpannedString(sms.body, sms.spanStartPosMsgPeek, sms.spanEndPosMsgPeek)
            }else{
                view.tvSMSMPeek.text = sms.msg

            }
        }

        private fun getSpannedString(str: String, startPos: Int, endPos: Int): SpannableStringBuilder {
            val yellow =
                ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorPrimary))
            StyleSpan(R.style.TextStyle)
            val span =  SpannableStringBuilder(str)
            span.setSpan(
                yellow,
                startPos,
                endPos,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
            )

            span.setSpan(StyleSpan(android.graphics.Typeface.BOLD), startPos,
                endPos,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE )

            return span
        }

        private fun setSpanForNameSearch(sms: SMS) {
            var firstChar = ""
            if(sms.spanEndPosNameCp!=0 && sms.firstName!=null ){
                name.text = getSpannedString(sms.firstName!!, sms.spanStartPosNameCp, sms.spanEndPosNameCp)
                firstChar = sms.firstName!![0].toString().toUpperCase()
            }else if(sms.addressString!=null && sms.spanEndPos !=0){
                name.text = getSpannedString(sms.addressString!!, sms.spanStartPos, sms.spanEndPos)
                firstChar = formatPhoneNumber(sms.addressString!!).replace("+","")[0].toString().toUpperCase()
            }
            else{
                name.text = sms.addressString
                firstChar = formatPhoneNumber(sms.addressString!!).replace("+","")[0].toString().toUpperCase()

            }
            if(firstChar.isNullOrEmpty()){
                setNameFirstChar("+")
            }else{
                setNameFirstChar(firstChar)
            }
        }
//        }

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
                 view.tvSMSTime.text = time
             }else{
                 view.tvSMSTime.text = date
             }
//             Log.d(TAG, "today: $todayDate")
//             Log.d(TAG, "setTimeInView: $d2")


         }

         private fun setNameFirstChar(firstLetterString:String) {
             view.textViewSMScontactCrclr.text = firstLetterString
             view.textViewSMScontactCrclr.setTextColor(ContextCompat.getColor(view.context, R.color.colorWhite))
             view.textViewSMScontactCrclr.setRandomBackgroundCircle()
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
     class SMSSearchItemDiffCallback : DiffUtil.ItemCallback<SMS>() {
        override fun areItemsTheSame(oldItem: SMS, newItem: SMS): Boolean {
            return oldItem.expanded == newItem.expanded &&  oldItem.threadID == newItem.threadID
        }

        override fun areContentsTheSame(oldItem: SMS, newItem: SMS): Boolean {
            return  oldItem.firstName== newItem.firstName && oldItem.msg == newItem.msg && oldItem.senderInfoFoundFrom == newItem.senderInfoFoundFrom
        }

    }

    interface LongPressHandler{
        fun onLongPressed(view:View, pos:Int, id: Long, address:String)
    }

    interface NetworkHandler {
        fun isInternetAvailable(): Boolean
    }


}



