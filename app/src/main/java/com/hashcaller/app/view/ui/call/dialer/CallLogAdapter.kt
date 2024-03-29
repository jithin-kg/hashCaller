package com.hashcaller.app.view.ui.call.dialer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hashcaller.app.R
import com.hashcaller.app.databinding.CallListBinding
import com.hashcaller.app.databinding.ItemFinishSettingUpBinding
import com.hashcaller.app.utils.DummYViewHolder
import com.hashcaller.app.view.ui.MainActivity
import com.hashcaller.app.view.ui.call.CallFragment.Companion.ID_SHOW_SCREENING_ROLE
import com.hashcaller.app.view.ui.call.db.CallLogTable
import com.hashcaller.app.view.ui.contacts.toggleUserBadge
import com.hashcaller.app.view.ui.contacts.toggleVerifiedBadge
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
import java.util.*

/**
 * Created by Jithin KG on 22,July,2020
 */

class CallLogAdapter(private val context: Context,
                     private val viewMarkingHandlerHelper: ViewHandlerHelper,
                     private val  networkHandler: SMSListAdapter.NetworkHandler,
                     private val isDarkThemOn:Boolean,
                     private val onContactItemClickListener:
                         (id:Long, postition:Int, view:View, btn:Int, callLog:CallLogTable, clickType:Int, visibility:Int, nameStr:String?)->Int
) :
    androidx.recyclerview.widget.ListAdapter<CallLogTable, RecyclerView.ViewHolder>(CallItemDiffCallback()) {

    private val VIEW_TYPE_LOG = 0;
    //    private val VIEW_TYPE_SPAM = 1;
    private val VIEW_TYPE_LOADING = 1
    private val VIEW_TYPE_SET_AS_DEFAULT_CALLER_ID = 2
    private var callLogs: MutableList<CallLogTable> = mutableListOf()
    private var isCompleteCallLogsRetrieved = false
    private var showDefCallerIdLayout = false
    companion object {
        private const val TAG = "__CallLogAdapter";



        private var todayDayNumber:String? = null
        private var yesterDayNumber:String? = null
        private var olderDayNumber:String? = null
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
        return callLogs.size
    }

    fun submitCallLogs(newContactList: MutableList<CallLogTable>, isFromFirst10Items: Boolean) {
        if(!isFromFirst10Items){
            isCompleteCallLogsRetrieved = true
        }
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
                    bindingAdapterPosition,
                    it,
                    BUTTON_SIM_1,
                    CallLogTable(id=ID_SHOW_SCREENING_ROLE, hUid = ""),
                    TYPE_CLICK_SCREENING_ROLE,
                    it.visibility,
                    null
                )
                true
            }

            binding.btnDismiss.setOnClickListener {
                onContactItemClickListener(
                    ID_SHOW_SCREENING_ROLE,
                    bindingAdapterPosition,
                    it,
                    BUTTON_SIM_1,
                    CallLogTable(id=ID_SHOW_SCREENING_ROLE, hUid=""),
                    TYPE_CLICK_DISMISS_SCREENING_ROLE,
                    it.visibility,
                    null
                )
                true
            }
        }
    }
    inner class ViewHolderCallLog(private val logBinding:  CallListBinding) : RecyclerView.ViewHolder(logBinding.root) {

//        private val image = view.findViewById<ImageView>(R.id.contact_image)


        fun bind(
            callLog: CallLogTable, context: Context,
            onContactItemClickListener: (id: Long, postition: Int, view: View, btn: Int, callLog: CallLogTable, clickType: Int, visibility: Int, nameStr: String?) -> Int,
            networkHandler: SMSListAdapter.NetworkHandler
        ) {

            var isImageThumbnailAvaialble = false
            logBinding.layoutExpandableCall.setTag(callLog.dateInMilliseconds)
            context.toggleVerifiedBadge(logBinding.imgVerifiedBadge, callLog.isVerifiedUser)
            val isRegisterdUser = context.toggleUserBadge(logBinding.imgUserIconBg, logBinding.imgUserIcon, callLog.hUid)
            if (viewMarkingHandlerHelper.isMarked(callLog.id)) {
                logBinding.imgViewCallMarked.beVisible()
            } else {
                logBinding.imgViewCallMarked.beInvisible()
            }
            if(viewMarkingHandlerHelper.isViewExpanded(callLog.id!!)){
                showExpandableLayout(logBinding)
            }else {
                hideExpandableLayout(logBinding)

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
            }else {
                logBinding.imgVSimIcon.beInvisible()
            }

            var nameStr:String = ""
            var infoFoundFrom = SENDER_INFO_SEARCHING
            if(!callLog.nameInPhoneBook.isNullOrEmpty()){
                infoFoundFrom = SENDER_INFO_FROM_CONTENT_PROVIDER
                nameStr = callLog.nameInPhoneBook!!
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
                        logBinding.textViewCrclr.beVisible()
                        logBinding.pgBarCallItem.beInvisible()
                    }

                }

                SENDER_INFO_FROM_DB -> {
                    if(!callLog.isVerifiedUser){
                        logBinding.imgVIdentfByHash.beVisible()
                    }
                    if(callLog.imageFromDb.isNotEmpty()){
                        isImageThumbnailAvaialble = true
                        logBinding.imgVThumbnail.setImageBitmap(getDecodedBytes(callLog.imageFromDb))
                        logBinding.imgVThumbnail.beVisible();
                        logBinding.textViewCrclr.beInvisible()
                    }else if(callLog.avatarGoogle.isNotEmpty()){
                        logBinding.imgVThumbnail.beVisible();
                        logBinding.textViewCrclr.beInvisible()
                        Glide.with(context).load(callLog.avatarGoogle)
                            .into(logBinding.imgVThumbnail)
                    }
                    else{
                        logBinding.imgVThumbnail.beInvisible()
                        logBinding.textViewCrclr.beVisible()
                        logBinding.pgBarCallItem.beInvisible()
                    }
                }
                SENDER_INFO_SEARCHING ->{
                    logBinding.imgVIdentfByHash.beInvisible()
                    logBinding.imgVThumbnail.beInvisible()
                    logBinding.textViewCrclr.beVisible()
                    logBinding.pgBarCallItem.beVisible()
                }

                else ->{
                    logBinding.imgVIdentfByHash.beInvisible()
                    logBinding.imgVThumbnail.beInvisible()
                    logBinding.textViewCrclr.beVisible()
                    logBinding.pgBarCallItem.beInvisible()
                }
            }
            val firstLetter = nameStr[0]
            val firstLetterString = firstLetter.toString().uppercase(Locale.getDefault())
            if (callLog.spamCount > MainActivity.SPAM_THRESHOLD_VALUE || callLog.isReportedByUser) {
                if(!isImageThumbnailAvaialble){
                    logBinding.textVcallerName.setColorForText(R.color.spamText)
//                    logBinding.imgViewCallSpamIcon.beVisible()
                    logBinding.textViewCrclr.setRandomBackgroundCircle(TYPE_SPAM)
                    logBinding.textViewCrclr.text = ""
                }


            } else {
                if(!isImageThumbnailAvaialble){
                    logBinding.imgViewCallSpamIcon.beInvisible()
                    logBinding.textViewCrclr.setRandomBackgroundCircle(callLog.color)
                    logBinding.textVcallerName.setColorForText(R.color.textColor)
                    logBinding.textViewCrclr.text = firstLetterString
                }
            }
            logBinding.textVcallerName.text = nameStr


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
            if(callLog.relativeDay.isNotEmpty()){
                logBinding.tvRelativeDay.text = callLog.relativeDay
                logBinding.tvRelativeDay.beVisible()
            }else {
                logBinding.tvRelativeDay.beGone()
            }
            val relativeTime  = getRelativeTime(callLog.dateInMilliseconds)
            logBinding.textViewTime.text = relativeTime.relativeTime
            logBinding.layoutExpandableCall.tvExpandNumCall.text = callLog.numberFormated
            setClickListener(logBinding.root, callLog,nameStr)
        }


        private fun setClickListener(view: View, callLog: CallLogTable, nameStr: String) {
            view.imgBtnCall.setOnClickListener{
//                val visibility = logBinding.layoutExpandableCall.visibility

                val visibility =  view.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).visibility

                onContactItemClickListener(
                    callLog.id!!,
                    bindingAdapterPosition,
                    it,
                    BUTTON_SIM_1,
                    callLog,
                    TYPE_MAKE_CALL,
                    visibility,
                    nameStr
                )
            }
            view.setOnLongClickListener { v ->

                val visibility =  view.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).visibility

                var isToBeMarked = onContactItemClickListener(
                    callLog.id!!,
                    bindingAdapterPosition,
                    v,
                    BUTTON_SIM_1,
                    callLog,
                    TYPE_LONG_PRESS,
                    visibility,
                    nameStr
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

            logBinding.layoutHistory.setOnClickListener {
                val visibility = view.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).visibility

                val isToBeMarked = onContactItemClickListener(
                    callLog.id!!,
                    bindingAdapterPosition,
                    it,
                    BUTTON_SIM_1,
                    callLog,
                    TYPE_CLICK_VIEW_CALL_HISTORY,
                    visibility,
                    nameStr
                )
                toggleMarkingAndExpand(isToBeMarked, view, logBinding)
                true
            }
            logBinding.textViewCrclr.setOnClickListener {

                val visibility =  logBinding.layoutExpandableCall.visibility

                val isToBeMarked = onContactItemClickListener(
                    callLog.id!!,
                    bindingAdapterPosition,
                    logBinding.root,
                    BUTTON_SIM_1,
                    callLog,
                    TYPE_CLICK_VIWE_INDIVIDUAL_CONTACT,
                    visibility,
                    nameStr
                )
                toggleMarkingAndExpand(isToBeMarked, view, logBinding)

            }
            logBinding.imgVThumbnail.setOnClickListener{

                val visibility =  logBinding.layoutExpandableCall.visibility
                val isToBeMarked = onContactItemClickListener(
                    callLog.id!!,
                    bindingAdapterPosition,
                    it,
                    BUTTON_SIM_1,
                    callLog,
                    TYPE_CLICK_VIWE_INDIVIDUAL_CONTACT,
                    visibility,
                    nameStr
                )
                toggleMarkingAndExpand(isToBeMarked, view, logBinding)

            }
//            view.imgBtnExpandHistory.setOnClickListener {
////                viewMarkingHandler.onCallButtonClicked(it, INTENT_TYPE_MAKE_CALL, callLog)
//                true
//            }


            view.layoutDetails.setOnClickListener {
//                viewMarkingHandler.onCallButtonClicked(it, INTENT_TYPE_MORE_INFO, callLog)
                onContactItemClickListener(
                    callLog.id!!,
                    bindingAdapterPosition,
                    view,
                    BUTTON_SIM_1,
                    callLog,
                    TYPE_CLICK_VIWE_INDIVIDUAL_CONTACT,
                    View.VISIBLE,
                    nameStr
                    )
                true
            }



            view.setOnClickListener(View.OnClickListener { v ->
                val visibility =  v.findViewById<ConstraintLayout>(R.id.layoutExpandableCall).visibility

                val isToBeMarked = onContactItemClickListener(
                    callLog.id!!,
                    bindingAdapterPosition,
                    v,
                    BUTTON_SIM_1,
                    callLog,
                    TYPE_CLICK,
                    visibility,
                    nameStr
                )
                toggleMarkingAndExpand(isToBeMarked, view, logBinding)
//                when (isToBeMarked) {
//                    MARK_ITEM -> {
//                        view.imgViewCallMarked.beVisible()
//                    }
//                    EXPAND_LAYOUT ->{
//                    }
//                    COMPRESS_LAYOUT ->{
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

    private fun hideExpandableLayout(v: CallListBinding) {
        v.layoutExpandableCall.beGone()
//        v.dividerBottum.beGone()


    }
    private fun showExpandableLayout(v: CallListBinding) {
        v.layoutExpandableCall.beVisible()
//        v.dividerBottum.background.alpha = 10
//        v.dividerBottum.beVisible()

    }

    private fun toggleMarkingAndExpand(isToBeMarked: Int, view: View, logBinding: CallListBinding) {
        when (isToBeMarked) {
            MARK_ITEM -> {
                view.imgViewCallMarked.beVisible()
            }
            EXPAND_LAYOUT ->{
                showExpandableLayout(logBinding)
            }
            COMPRESS_LAYOUT ->{
                hideExpandableLayout(logBinding)

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
            callLogs.add(0, CallLogTable(id=ID_SHOW_SCREENING_ROLE, hUid = ""))
        }else {
            if(callLogs[0].id!= ID_SHOW_SCREENING_ROLE){
                callLogs.add(0, CallLogTable(id=ID_SHOW_SCREENING_ROLE, hUid = ""))
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
