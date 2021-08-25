package com.hashcaller.app.view.ui.call.dialer

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hashcaller.app.R
import com.hashcaller.app.databinding.CallListBinding
import com.hashcaller.app.databinding.ItemFinishSettingUpBinding
import com.hashcaller.app.utils.DummYViewHolder
import com.hashcaller.app.view.ui.call.CallFragment.Companion.ID_SHOW_SCREENING_ROLE
import com.hashcaller.app.view.ui.call.db.CallLogTable
import com.hashcaller.app.view.ui.contacts.utils.SPAM_THREASHOLD
import com.hashcaller.app.view.ui.contacts.utils.TYPE_SPAM
import com.hashcaller.app.view.ui.contacts.utils.loadImage
import com.hashcaller.app.view.ui.extensions.setColorForText
import com.hashcaller.app.view.ui.extensions.setRandomBackgroundCircle
import com.hashcaller.app.view.ui.sms.individual.util.*
import com.hashcaller.app.view.ui.sms.individual.util.TYPE_CLICK
import com.hashcaller.app.view.ui.sms.list.SMSListAdapter
import com.hashcaller.app.view.ui.sms.util.SENDER_INFO_FROM_CONTENT_PROVIDER
import com.hashcaller.app.view.ui.sms.util.SENDER_INFO_FROM_DB
import com.hashcaller.app.view.ui.sms.util.SENDER_INFO_NOT_FOUND
import com.hashcaller.app.view.ui.sms.util.SENDER_INFO_SEARCHING
import com.hashcaller.app.view.utils.getDecodedBytes
import com.hashcaller.app.view.utils.getRelativeTime
import kotlinx.android.synthetic.main.call_list.view.*

/**
 * Created by Jithin KG on 22,July,2020
 */

class CallLogAdapter(private val context: Context,
                     private val viewMarkingHandlerHelper: ViewHandlerHelper,
                     private val  networkHandler: SMSListAdapter.NetworkHandler,
                     private val isDarkThemOn:Boolean,
                     private val onContactItemClickListener:
                    (id:Long, postition:Int, view:View, btn:Int, callLog:CallLogTable, clickType:Int, visibility:Int)->Int
                   ) :
    androidx.recyclerview.widget.ListAdapter<CallLogTable, RecyclerView.ViewHolder>(CallItemDiffCallback()) {

    private val VIEW_TYPE_LOG = 0;
//    private val VIEW_TYPE_SPAM = 1;
    private val VIEW_TYPE_LOADING = 1
    private val VIEW_TYPE_SET_AS_DEFAULT_CALLER_ID = 2
    private var callLogs: MutableList<CallLogTable> = mutableListOf()
    private var showDefCallerIdLayout = false
    companion object {
        private const val TAG = "__CallLogAdapter";
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
        if(viewType == VIEW_TYPE_LOG){
            //create binding here and pass it to viewholder
//            val view = LayoutInflater.from(parent.context).inflate(R.layout.call_list, parent, false)
            val logBinding =  CallListBinding.inflate(LayoutInflater.from(parent.context), parent, false)


            return ViewHolderCallLog(logBinding)
        }
        else if(viewType == VIEW_TYPE_SET_AS_DEFAULT_CALLER_ID){
            val logBinding =  ItemFinishSettingUpBinding.inflate(LayoutInflater.from(parent.context), parent, false)


            return ViewHolderDefaultCallerId(logBinding)
        }
        else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.call_list_item_loading, parent, false)
            return DummYViewHolder(view)
        }

    }

    override fun getItemViewType(position: Int): Int {
        if(callLogs.isNotEmpty() ){
            if (callLogs[position].id == ID_SHOW_SCREENING_ROLE && position==0){
                return VIEW_TYPE_SET_AS_DEFAULT_CALLER_ID
            }
         else if( position < callLogs.size)
             if(this.callLogs[position].id == null){
                return VIEW_TYPE_LOADING
            }
        }

        return VIEW_TYPE_LOG
    }

override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val contact = callLogs[position]
    when(holder.itemViewType) {

         VIEW_TYPE_LOG -> {


             (holder as ViewHolderCallLog).bind(callLogs[position],context, onContactItemClickListener, networkHandler)
        }
        VIEW_TYPE_SET_AS_DEFAULT_CALLER_ID -> {
            (holder as ViewHolderDefaultCallerId).bind()
        }

        VIEW_TYPE_LOADING ->{
            (holder as DummYViewHolder).bind()
        }


    }

}

    override fun getItemCount(): Int {
//        Log.d("__ContactAdapter", "getItemCount: ${contacts.size}")
       return callLogs.size
    }

    fun submitCallLogs(newContactList: MutableList<CallLogTable>) {
        callLogs = newContactList!!
        this.submitList(newContactList)
    }

    inner class ViewHolderDefaultCallerId(private val binding:  ItemFinishSettingUpBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(){
//            if(showDefCallerIdLayout){
//                binding.root.beVisible()
//            }else {
//                callLogs.removeAt(0)
////                binding.root.beGone()
//            }
            binding.btnSetup.setOnClickListener {
                onContactItemClickListener(
                    ID_SHOW_SCREENING_ROLE,
                    this.adapterPosition,
                    it,
                    BUTTON_SIM_1,
                    CallLogTable(id=ID_SHOW_SCREENING_ROLE),
                    TYPE_CLICK_SCREENING_ROLE,
                    it.visibility
                )
                true
            }

            binding.btnDismiss.setOnClickListener {
                onContactItemClickListener(
                    ID_SHOW_SCREENING_ROLE,
                    this.adapterPosition,
                    it,
                    BUTTON_SIM_1,
                    CallLogTable(id=ID_SHOW_SCREENING_ROLE),
                    TYPE_CLICK_DISMISS_SCREENING_ROLE,
                    it.visibility
                )
                true
            }
        }
    }
    inner class ViewHolderCallLog(private val logBinding:  CallListBinding) : RecyclerView.ViewHolder(logBinding.root) {
        private val name = logBinding.textVcallerName
        private val circle = logBinding.textViewCrclr;
        private val expandableView = logBinding.layoutExpandableCall

//        private val image = view.findViewById<ImageView>(R.id.contact_image)


        fun bind(
            callLog: CallLogTable, context: Context,
            onContactItemClickListener: (id: Long, postition: Int, view: View, btn: Int, callLog: CallLogTable, clickType: Int, visibility: Int) -> Int,
            networkHandler: SMSListAdapter.NetworkHandler
        ) {
            var isImageThumbnailAvaialble = false
            Log.d(TAG, "bind: ")
            expandableView.setTag(callLog.dateInMilliseconds)

            if (viewMarkingHandlerHelper.isMarked(callLog.id)) {
                Log.d(TAG, "bind: ismarked")
                logBinding.imgViewCallMarked.beVisible()
            } else {
                logBinding.imgViewCallMarked.beInvisible()
            }
            if(viewMarkingHandlerHelper.isViewExpanded(callLog.id!!)){
             logBinding.layoutExpandableCall.beVisible()
            }else {
                logBinding.layoutExpandableCall.beGone()
            }
            val sim = callLog.simId
            //todo simid can be -1 then, do not show this, invisisble
            if(sim == 0){
                if(isDarkThemOn){
                    logBinding.imgVSimIcon.setImageResource(R.drawable.ic_sim_1_line_white)
                }else {
                    logBinding.imgVSimIcon.setImageResource(R.drawable.ic_sim_1_line)
                }

            }else if(sim == 1) {
                if(isDarkThemOn){
                    logBinding.imgVSimIcon.setImageResource(R.drawable.ic_sim_2_line_white)
                }else {
                    logBinding.imgVSimIcon.setImageResource(R.drawable.ic_sim_2_line)
                }
            }

//            if (prevTime != null)
//                if(prevTime == callLog.dateInMilliseconds){
//                    expandableView.beVisible()
//
//                }else{
//                   expandableView.beGone()
//
//                }
//                if (callLog.callerInfoFoundFrom == SENDER_INFO_SEARCHING && networkHandler.isInternetAvailable()) {
//                    logBinding.pgBarCallItem.beVisible()
//                }else{
//                    logBinding.pgBarCallItem.beInvisible()
//
//                }

                var nameStr:String = ""
                var infoFoundFrom = SENDER_INFO_SEARCHING
                if(!callLog.name.isNullOrEmpty()){
                    infoFoundFrom = SENDER_INFO_FROM_CONTENT_PROVIDER
                    nameStr = callLog.name!!
                }else if(!callLog.nameFromServer.isNullOrEmpty()){
                    infoFoundFrom = SENDER_INFO_FROM_DB
                    nameStr = callLog.nameFromServer!!
                }else if(callLog.nameFromServer== null){
                    infoFoundFrom = SENDER_INFO_SEARCHING
                    nameStr = callLog.numberFormated
                }else{
                    infoFoundFrom = SENDER_INFO_NOT_FOUND
                    nameStr = callLog.numberFormated
                }

            when (infoFoundFrom) {
                SENDER_INFO_FROM_CONTENT_PROVIDER -> {
                    logBinding.imgVIdentfByHash.beInvisible()
                    if(callLog.thumbnailFromCp.isNotEmpty()){
                        isImageThumbnailAvaialble = true
                        showImageInCircle(logBinding, callLog.thumbnailFromCp)
                    }else{
                        logBinding.imgVThumbnail.beInvisible()
                        circle.beVisible()
                        logBinding.pgBarCallItem.beInvisible()
                    }

                }

                SENDER_INFO_FROM_DB -> {
                    logBinding.imgVIdentfByHash.beVisible()
                    if(callLog.imageFromDb.isNotEmpty()){
                        isImageThumbnailAvaialble = true
                        logBinding.imgVThumbnail.setImageBitmap(getDecodedBytes(callLog.imageFromDb))
                    }else{
                        logBinding.imgVThumbnail.beInvisible()
                        circle.beVisible()
                        logBinding.pgBarCallItem.beInvisible()
                    }
                }
                SENDER_INFO_SEARCHING ->{
                    logBinding.imgVIdentfByHash.beInvisible()
                    logBinding.imgVThumbnail.beInvisible()
                    circle.beVisible()
                    logBinding.pgBarCallItem.beVisible()
                }

                else ->{
                    logBinding.imgVIdentfByHash.beInvisible()
                    logBinding.imgVThumbnail.beInvisible()
                    circle.beVisible()
                    logBinding.pgBarCallItem.beInvisible()
                }
            }
            val firstLetter = nameStr[0]
            val firstLetterString = firstLetter.toString().toUpperCase()

                if (callLog.spamCount > SPAM_THREASHOLD || callLog.isReportedByUser) {
                    if(!isImageThumbnailAvaialble){
                        name.setColorForText(R.color.spamText)
//                    logBinding.imgViewCallSpamIcon.beVisible()
                        circle.setRandomBackgroundCircle(TYPE_SPAM)
                        circle.text = ""
                    }


                } else {
                    if(!isImageThumbnailAvaialble){
                        logBinding.imgViewCallSpamIcon.beInvisible()
                        circle.setRandomBackgroundCircle(callLog.color)
                        name.setColorForText(R.color.textColor)
                        circle.text = firstLetterString
                    }


                }
            name.text = nameStr


//            if(callLog.color!=0){
//                circle.setRandomBackgroundCircle(callLog.color)
//            }else{
//                callLog.color = circle.setRandomBackgroundCircle()
//
//            }

            //call type
            setCallTypeImage(callLog, logBinding.imgVCallType)


            /**
             * This is important to check else double/ duplicate marking of items occur
             */
//            var id = getExpandedLayoutId()
//            if (id != null) {
//                if (id == callLog.id) {
//                    expandableView.beVisible()
//                } else {
//                    expandableView.beGone()
//
//
//                }
//            }
            logBinding.textViewTime.text = getRelativeTime(callLog.dateInMilliseconds)
            expandableView.tvExpandNumCall.text = callLog.numberFormated
            setClickListener(logBinding.root, callLog)
        }


        private fun setClickListener(view: View, callLog: CallLogTable) {
            view.imgBtnCall.setOnClickListener{
//                val visibility = logBinding.layoutExpandableCall.visibility

                val visibility =  view.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).visibility

                onContactItemClickListener(
                    callLog.id!!,
                    this.adapterPosition,
                    it,
                    BUTTON_SIM_1,
                    callLog,
                    TYPE_MAKE_CALL,
                    visibility
                )
            }
            view.setOnLongClickListener { v ->

                val visibility =  view.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).visibility

                var isToBeMarked = onContactItemClickListener(
                    callLog.id!!,
                    this.adapterPosition,
                    v,
                    BUTTON_SIM_1,
                    callLog,
                    TYPE_LONG_PRESS,
                    visibility
                )
//                when (isToBeMarked) {
//                    MARK_ITEM -> {
//                        view.imgViewCallMarked.beVisible()
//                    }
//                    else -> {
//                        view.imgViewCallMarked.beInvisible()
//
//                    }
//                }
                toggleMarkingAndExpand(isToBeMarked, view, logBinding)

                true

            }

            logBinding.imgBtnExpandHistory.setOnClickListener {
                val visibility = view.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).visibility

                val isToBeMarked = onContactItemClickListener(
                    callLog.id!!,
                    this.adapterPosition,
                    it,
                    BUTTON_SIM_1,
                    callLog,
                    TYPE_CLICK_VIEW_CALL_HISTORY,
                    visibility
                )
                Log.d(TAG, "setClickListener: imagBtnInfoExpand")
                toggleMarkingAndExpand(isToBeMarked, view, logBinding)
                true
            }
            logBinding.textViewCrclr.setOnClickListener {

                val visibility =  logBinding.layoutExpandableCall.visibility

                val isToBeMarked = onContactItemClickListener(
                    callLog.id!!,
                    this.adapterPosition,
                    logBinding.root,
                    BUTTON_SIM_1,
                    callLog,
                    TYPE_CLICK_VIWE_INDIVIDUAL_CONTACT,
                    visibility
                )
                toggleMarkingAndExpand(isToBeMarked, view, logBinding)

            }
            logBinding.imgVThumbnail.setOnClickListener{

                val visibility =  logBinding.layoutExpandableCall.visibility
                val isToBeMarked = onContactItemClickListener(
                    callLog.id!!,
                    this.adapterPosition,
                    it,
                    BUTTON_SIM_1,
                    callLog,
                    TYPE_CLICK_VIWE_INDIVIDUAL_CONTACT,
                    visibility
                )
                toggleMarkingAndExpand(isToBeMarked, view, logBinding)

            }
//            view.imgBtnExpandHistory.setOnClickListener {
////                viewMarkingHandler.onCallButtonClicked(it, INTENT_TYPE_MAKE_CALL, callLog)
//                true
//            }

            view.imgBtnSmsExpand.setOnClickListener {
//                viewMarkingHandler.onCallButtonClicked(it, INTENT_TYPE_START_INDIVIDUAL_SMS, callLog)
                true
            }

            view.imgBtnInfoExpand.setOnClickListener {
//                viewMarkingHandler.onCallButtonClicked(it, INTENT_TYPE_MORE_INFO, callLog)
                onContactItemClickListener(callLog.id!!, adapterPosition,view, BUTTON_SIM_1, callLog, TYPE_CLICK_VIWE_INDIVIDUAL_CONTACT, View.VISIBLE )
                true
            }



            view.setOnClickListener(View.OnClickListener { v ->
                val visibility =  v.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).visibility

                val isToBeMarked = onContactItemClickListener(
                    callLog.id!!,
                    this.adapterPosition,
                    v,
                    BUTTON_SIM_1,
                    callLog,
                    TYPE_CLICK,
                    visibility
                )
                toggleMarkingAndExpand(isToBeMarked, view, logBinding)
//                when (isToBeMarked) {
//                    MARK_ITEM -> {
//                        view.imgViewCallMarked.beVisible()
//                    }
//                    EXPAND_LAYOUT ->{
//                        logBinding.layoutExpandableCall.beVisible()
//                    }
//                    COMPRESS_LAYOUT ->{
//                        logBinding.layoutExpandableCall.beGone()
//
//                    }
//                    else -> {
//                        view.imgViewCallMarked.beInvisible()
//
//                    }
//                }


                //                if(viewExpanded== 1){
//                    //iff marking not started expand the layout
//                    toggleExpandableView(v, this.adapterPosition, callLog.id!!, expandableView)
//                }

            })
        }

        private fun toggleExpandableView(
            v: View,
            pos: Int,
            id: Long,
            expandableView: ConstraintLayout
        ) {

//            var expandedLyoutId = getExpandedLayoutId()
//            if (expandedLyoutId == null) {
//                //no views has not yet expanded, so expand the current layout
//                setExpandedLayoutId(id)
//                setExpandedLayoutView(expandableView)
//                expandableView.beVisible()
//
//            } else if (expandedLyoutId == id) {
//                //the layout is already expaned so, hide it
//                expandableView.beGone()
//                setExpandedLayoutId(null)
//                setExpandedLayoutView(null)
//            } else {
//                //new item expanded
//                getExpandedLayoutView()!!.beGone()
//                expandableView.beVisible()
//                setExpandedLayoutView(expandableView)
//                setExpandedLayoutId(id)
//
//
//            }
        }


        private fun setNameFirstChar(callLog: CallLogTable) {
//            private fun setNameFirstChar(callLog: CallLogTable) {
//                if (callLog != null) {
//                    if (callLog.spamCount > 0) {
//                        logBinding.imgViewCallSpamIcon.beVisible()
//                        logBinding.imgViewCallSpamIcon.setImageResource(R.drawable.ic_baseline_block_red)
//                        circle.text = ""
//                        callLog.color = circle.setRandomBackgroundCircle(TYPE_SPAM)
//                    } else {
//                        logBinding.imgViewCallSpamIcon.beInvisible()
//                        val name: String =
//                            if (callLog.name == null || callLog.name!!.isEmpty()) callLog.number else callLog.name!!
//                        val firstLetter = name[0]
//                        val firstLetterString = firstLetter.toString().toUpperCase()
//                        circle.text = firstLetterString
//                        callLog.color = circle.setRandomBackgroundCircle()
//
//                    }
//                } else {
//                    logBinding.imgViewCallSpamIcon.beInvisible()
//                    val name: String =
//                        if (callLog.name == null || callLog.name!!.isEmpty()) callLog.number else callLog.name!!
//                    val firstLetter = name[0]
//                    val firstLetterString = firstLetter.toString().toUpperCase()
//                    circle.text = firstLetterString
//                    callLog.color = circle.setRandomBackgroundCircle()
//                }
//
//
//            }


        }

    }

    private fun toggleMarkingAndExpand(isToBeMarked: Int, view: View, logBinding: CallListBinding) {
        Log.d(TAG, "toggleMarkingAndExpand: ")
        when (isToBeMarked) {
            MARK_ITEM -> {
                view.imgViewCallMarked.beVisible()
            }
            EXPAND_LAYOUT ->{
                logBinding.layoutExpandableCall.beVisible()
            }
            COMPRESS_LAYOUT ->{
                logBinding.layoutExpandableCall.beGone()

            }
            else -> {
                view.imgViewCallMarked.beInvisible()

            }
        }
    }

    private fun showImageInCircle(logBinding: CallListBinding, uri:String) {
        logBinding.imgVThumbnail.beVisible()
        loadImage(context, logBinding.imgVThumbnail, uri)
        logBinding.imgVThumbnail.beVisible()
        logBinding.textViewCrclr.beInvisible()
        logBinding.pgBarCallItem.beInvisible()
    }

    class CallItemDiffCallback : DiffUtil.ItemCallback<CallLogTable>() {
        override fun areItemsTheSame(oldItem: CallLogTable, newItem: CallLogTable): Boolean {
            return  oldItem.id == newItem.id


        }


        override fun areContentsTheSame(oldItem: CallLogTable, newItem: CallLogTable): Boolean {
//            if(oldItem.ca)

            return oldItem == newItem
        }

    }


    private fun setCallTypeImage(callLog: CallLogTable, imageView: ImageView) {
//             /** Call log type for incoming calls.  */
//             val INCOMING_TYPE = 1
//
//             /** Call log type for outgoing calls.  */
//             val OUTGOING_TYPE = 2
//
//             /** Call log type for missed calls.  */
//             val MISSED_TYPE = 3
//
//             /** Call log type for voicemails.  */
//             val VOICEMAIL_TYPE = 4
//
//             /** Call log type for calls rejected by direct user action.  */
//             val REJECTED_TYPE = 5
//
//             /** Call log type for calls blocked automatically.  */
//             val BLOCKED_TYPE = 6
        if(callLog!=null){
            if(callLog.spamCount > 0 ){
//                textView.setColorForText( R.color.spamText)

            }else{
//                textView.setColorForText(R.color.textColor)

            }
        }else{
//            textView.setColorForText(R.color.textColor)

        }

        when (callLog.type) {
            1 -> { // incomming call
                imageView.setImageResource(R.drawable.ic_baseline_call_received_24)
//                textView.text = "Incoming call"

            }
            2 -> { // outgoing call
                imageView.setImageResource(R.drawable.ic_baseline_call_made_24)
//                textView.text = "Outgoing call"
            }
            3 -> {
                imageView.setImageResource(R.drawable.ic_baseline_call_missed_24)
//                textView.text = "Missed call"
            }
            5->{
//                textView.text = "Rejected"
                imageView.setImageResource(R.drawable.ic_baseline_call_missed_24)

            }
            6 ->{
//                textView.text = "Blocked"
                imageView.setImageResource(R.drawable.ic_baseline_block_no_color)

            }

        }
    }

    fun getLogAt(adapterPosition: Int): CallLogTable {
        return callLogs[adapterPosition]
    }

    /**
     * removes  the first item  in list if the id is -1 -> set as default caller Id list items
     */
    fun removeCallerIdRoleItem() {
//        showDefCallerIdLayout = value
        if(callLogs.isNotEmpty()){
            if(callLogs[0].id== ID_SHOW_SCREENING_ROLE){
                callLogs.removeAt(0)
                notifyItemRemoved(0)
                notifyItemRangeChanged(0, callLogs.size)
            }
        }

    }

    fun addCallerIdRoleItem() {
//        showDefCallerIdLayout = value
        if(callLogs.isEmpty()){
            callLogs.add(0, CallLogTable(id=ID_SHOW_SCREENING_ROLE))
        }else {
            if(callLogs[0].id!= ID_SHOW_SCREENING_ROLE){
                callLogs.add(0, CallLogTable(id=ID_SHOW_SCREENING_ROLE))
            }
        }
        notifyItemInserted(0)
        notifyItemRangeChanged(0, callLogs.size)


//            }

    }

    interface ViewHandlerHelper {
        fun isMarked(id:Long?): Boolean
        fun isViewExpanded(id:Long): Boolean


    }
    interface NetworkHandler {
        fun isInternetAvailable(): Boolean
    }


}



