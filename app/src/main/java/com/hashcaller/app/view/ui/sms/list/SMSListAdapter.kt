package com.hashcaller.app.view.ui.sms.list

import android.content.Context
import android.graphics.Typeface
import android.view.*
import android.view.View.OnLongClickListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hashcaller.app.R
import com.hashcaller.app.databinding.SmsListViewBinding
import com.hashcaller.app.utils.DummYViewHolder
import com.hashcaller.app.view.ui.contacts.utils.TYPE_SPAM
import com.hashcaller.app.view.ui.contacts.utils.loadImage
import com.hashcaller.app.view.ui.extensions.setColorForText
import com.hashcaller.app.view.ui.extensions.setRandomBackgroundCircle
import com.hashcaller.app.view.ui.sms.db.SmsThreadTable
import com.hashcaller.app.view.ui.sms.individual.util.*
import com.hashcaller.app.view.ui.sms.util.SENDER_INFO_FROM_CONTENT_PROVIDER
import com.hashcaller.app.view.ui.sms.util.SENDER_INFO_FROM_DB
import com.hashcaller.app.view.ui.sms.util.SENDER_INFO_NOT_FOUND
import com.hashcaller.app.view.ui.sms.util.SENDER_INFO_SEARCHING
import com.hashcaller.app.view.utils.getDecodedBytes
import com.hashcaller.app.view.utils.getRelativeTime
import com.hashcaller.app.work.formatPhoneNumber
import kotlin.time.ExperimentalTime


/**
 * Created by Jithin KG on 22,July,2020
 */


class SMSListAdapter(private val context: Context,  private val viewMarkingHandler: ViewMarkHandler,
                     private val longPresHandler:LongPressHandler, private val  networkHandler: NetworkHandler,
                     private val onContactItemClickListener: (view:View, threadId:Long, pos:Int, pno:String, clickType:Int)->Int
) :
    androidx.recyclerview.widget.ListAdapter<SmsThreadTable, RecyclerView.ViewHolder>(SMSItemDiffCallback()) {
    private val VIEW_TYPE_LOADING = 0
    private val VIEW_TYPE_SMS = 2;

    private var smsList:MutableList<SmsThreadTable> = mutableListOf()
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
            return SmsViewHolder(SmsListViewBinding.inflate(LayoutInflater.from(parent.context ), parent, false))
        }


    }

    override fun getItemViewType(position: Int): Int {
//        if(this.smsList.isNotEmpty() && position < smsList.size)
//             if(this.smsList[position]){
//                return VIEW_TYPE_LOADING
//            }

                return VIEW_TYPE_SMS
    }

    @ExperimentalTime
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder.itemViewType) {

            VIEW_TYPE_SMS -> {
                val item =  getItem(position)

                (holder as SmsViewHolder).bind(item,context, onContactItemClickListener, position, networkHandler)

            }
            VIEW_TYPE_LOADING ->{
                val item =  getItem(position)

                (holder as DummYViewHolder).bind()
            }

        }
    }

    fun setList(it: MutableList<SmsThreadTable>) {
        this.smsList = it!!
//        val copy:MutableList<SMS> = mutableListOf()
//        this.smsList.forEach{copy.add(it)}
        this.submitList(it)

    }

    fun getSMSAt(adapterPosition: Int): SmsThreadTable {
        return smsList[adapterPosition]
    }

    inner class SmsViewHolder(val binding:SmsListViewBinding) : RecyclerView.ViewHolder(binding.root),View.OnCreateContextMenuListener {
        var layoutExpandable: ConstraintLayout = binding.layoutExpandable
        private val name = binding.textVSMSCntctName
        private val circle = binding.textViewSMScontactCrclr;
//        private val image = view.findViewById<ImageView>(R.id.contact_image)

        @ExperimentalTime
        fun bind(
            sms: SmsThreadTable, context: Context,
            onContactItemClickListener: (view: View, threadId: Long, pos: Int, pno: String, clickType: Int) -> Int,
            position: Int,
            networkHandler: NetworkHandler
        ) {
            var isSpam = false
            var senderInforFrom = SENDER_INFO_SEARCHING
            var nameStr = ""
            var firstLetter = ""

                if(sms.firstName.isNotEmpty()){
                    nameStr = sms.firstName!!
                    senderInforFrom = SENDER_INFO_FROM_CONTENT_PROVIDER
                }else if(sms.firstNameFromServer !=null ){
                    senderInforFrom = SENDER_INFO_NOT_FOUND
                    if(!sms.firstNameFromServer!!.isEmpty()){
                        nameStr = sms.firstNameFromServer!!
                        senderInforFrom = SENDER_INFO_FROM_DB
                    }
                    //todo make info found from hash caller visible here
                }else if(sms.firstNameFromServer==null){
                    senderInforFrom = SENDER_INFO_SEARCHING
                }
                isSpam = sms.spamCount!! > 15L || sms.isReportedByUser
            if(nameStr.isEmpty()){
                //name is not found in server on content provider so, set name as number
                nameStr = formatPhoneNumber(sms.contactAddress)
            }
            if(senderInforFrom == SENDER_INFO_FROM_DB){
                binding.imgvIdentifiedByHash.beVisible()
                binding.imgvIdentifiedByHash.beVisible()
                binding.pgBarSmsListItem.beInvisible()
            }else if(senderInforFrom == SENDER_INFO_SEARCHING){
                binding.pgBarSmsListItem.beVisible()
                binding.imgvIdentifiedByHash.beInvisible()
            }
            else {
                binding.pgBarSmsListItem.beInvisible()
                binding.imgvIdentifiedByHash.beInvisible()
                binding.imgvIdentifiedByHash.beInvisible()
            }

            if(isSpam){
                binding.textViewSMScontactCrclr.setRandomBackgroundCircle(TYPE_SPAM)
                binding.imgVBlkIconSms.beVisible()
                binding.textVSMSCntctName.setColorForText(R.color.spamText)
                binding.imgVThumbnail.beInvisible()
                binding.card.beInvisible()
                binding.card.beInvisible()
            }else {
                if(sms.imageFromDb.isNotEmpty()){
                    binding.imgVThumbnail.setImageBitmap(getDecodedBytes(sms.imageFromDb))
                    binding.imgVThumbnail.beVisible()
                    binding.textViewSMScontactCrclr.beInvisible()
                }else if(sms.thumbnailFromCp.isNotEmpty()){
                    loadImage(
                        context,
                        binding.imgVThumbnail,
                        sms.thumbnailFromCp,
                        textViewToHide = binding.textViewSMScontactCrclr
                        )
                    binding.imgVThumbnail.beVisible()
                    binding.textViewSMScontactCrclr.beInvisible()
                }else {
                    binding.textViewSMScontactCrclr.setRandomBackgroundCircle()
                    binding.imgVThumbnail.beInvisible()
                    binding.textViewSMScontactCrclr.beVisible()
                    binding.card.beInvisible()
                    firstLetter = nameStr[0].toString()
                }
            }

//            if(isSpam){
//                binding.textViewSMScontactCrclr.setRandomBackgroundCircle(TYPE_SPAM)
//                binding.imgVBlkIconSms.beVisible()
//                binding.textVSMSCntctName.setColorForText(R.color.spamText)
//                binding.imgVThumbnail.beInvisible()
//                binding.card.beInvisible()
//            }else if(sms.thumbnailFromCp.isEmpty()){
//                binding.textViewSMScontactCrclr.setRandomBackgroundCircle()
//                binding.imgVBlkIconSms.beInvisible()
//                firstLetter = nameStr[0].toString()
//                binding.textVSMSCntctName.setColorForText(R.color.textColor)
//                binding.imgVThumbnail.beInvisible()
//                binding.card.beInvisible()
//            }else{
//                loadImage(context, binding.imgVThumbnail, sms.thumbnailFromCp)
//                binding.imgVThumbnail.beVisible()
//                binding.imgVBlkIconSms.beInvisible()
//                binding.card.beVisible()
//                firstLetter = nameStr[0].toString()
//                binding.textVSMSCntctName.setColorForText(R.color.textColor)
//            }
            binding.textViewSMScontactCrclr.text = firstLetter
            binding.textVSMSCntctName.text = nameStr
            binding.tvSMSMPeek.text = sms.body

            if(senderInforFrom== SENDER_INFO_SEARCHING && this@SMSListAdapter.networkHandler.isInternetAvailable()){

                binding.pgBarSmsListItem.beVisible()
            }else{
                binding.pgBarSmsListItem.beInvisible()
            }


            /**
             * This is important to check else double/ duplicate marking of items occur
             */
            if(viewMarkingHandler.isMarked(sms.threadId)){
                binding.smsMarked.beVisible()
            }else{
                binding.smsMarked.beInvisible()

            }
//            if(MarkedItemsHandler.markedItems.contains(sms.threadID)){
//                view.smsMarked.visibility = View.VISIBLE
//            }else{
//                view.smsMarked.visibility = View.INVISIBLE
//
//            }

            binding.tvSMSMPeek.text = sms.body
                if(sms.readState ==SMS_NOT_READ){
                    binding.tvSMSMPeek.typeface = Typeface.DEFAULT_BOLD
//                    binding.tvSMSMPeek.alpha = 0.87f
                    binding.tvSMSMPeek.setColorForText(R.color.textColor)

                }else{
                    binding.tvSMSMPeek.typeface = Typeface.DEFAULT
//                    binding.tvSMSMPeek.alpha = 0.60f
                    binding.tvSMSMPeek.setColorForText(R.color.textColor)
                }


            binding.tvSMSTime.text = getRelativeTime(sms.dateInMilliseconds).relativeTime



            binding.parentLayout.setOnLongClickListener(OnLongClickListener { v ->
                var isToBeMarked =   onContactItemClickListener(v, sms.threadId, this.adapterPosition,
                    sms.contactAddress!!, TYPE_LONG_PRESS
                )
                when(isToBeMarked){
                    MARK_ITEM ->{
                        binding.smsMarked.beVisible()
                    }else ->{
                    binding.smsMarked.beInvisible()
                }
                }
                true
            })
            binding.parentLayout.setOnClickListener{v->
                var isToBeMarked =   onContactItemClickListener(v, sms.threadId, this.adapterPosition,
                    sms.contactAddress!!,TYPE_CLICK
                )
                when(isToBeMarked){
                    MARK_ITEM ->{
                        binding.smsMarked.beVisible()
                    }else ->{
                    binding.smsMarked.beInvisible()
                }
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
    class SMSItemDiffCallback : DiffUtil.ItemCallback<SmsThreadTable>() {
        override fun areItemsTheSame(oldItem: SmsThreadTable, newItem: SmsThreadTable): Boolean {
            return  oldItem.contactAddress == newItem.contactAddress


        }


        override fun areContentsTheSame(oldItem: SmsThreadTable, newItem: SmsThreadTable): Boolean {
            return oldItem == newItem
        }

    }

    interface LongPressHandler{
        fun onLongPressed(view:View, pos:Int, id: Long, address:String)
    }

    interface ViewMarkHandler {
        fun isMarked(id:Long): Boolean

    }

    interface NetworkHandler{
        fun isInternetAvailable(): Boolean
    }

}



