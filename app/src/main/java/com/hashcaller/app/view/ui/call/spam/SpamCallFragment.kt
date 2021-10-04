package com.hashcaller.app.view.ui.call.spam

import android.Manifest.permission.*
import android.app.ActivityOptions
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Pair
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.hashcaller.app.R
import com.hashcaller.app.databinding.SpamCallsFragmentBinding
import com.hashcaller.app.datastore.DataStoreRepository
import com.hashcaller.app.datastore.PreferencesKeys
import com.hashcaller.app.utils.Constants
import com.hashcaller.app.utils.Constants.Companion.SPAMMER_TYPE_BUSINESS
import com.hashcaller.app.utils.PermisssionRequestCodes.Companion.REQUEST_CODE_CALL_LOG
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.utils.constants.IntentKeys
import com.hashcaller.app.utils.extensions.requestCallPhonePermission
import com.hashcaller.app.utils.extensions.startSearchActivity
import com.hashcaller.app.utils.internet.ConnectionLiveData
import com.hashcaller.app.utils.notifications.HashCaller.Companion.CHANNEL_1_ID
import com.hashcaller.app.utils.notifications.HashCaller.Companion.CHANNEL_2_ID
import com.hashcaller.app.utils.notifications.HashCaller.Companion.CHANNEL_3_CALL_SERVICE_ID
import com.hashcaller.app.utils.notifications.tokeDataStore
import com.hashcaller.app.view.ui.MainActivity
import com.hashcaller.app.view.ui.MainActivityInjectorUtil
import com.hashcaller.app.view.ui.MyUndoListener
import com.hashcaller.app.view.ui.auth.getinitialInfos.UserInfoViewModel
import com.hashcaller.app.view.ui.blockConfig.blockList.BlockListActivity
import com.hashcaller.app.view.ui.call.CallFragment
import com.hashcaller.app.view.ui.call.RelativeTime
import com.hashcaller.app.view.ui.call.RelativeTime.Companion.OLDER
import com.hashcaller.app.view.ui.call.RelativeTime.Companion.YESTERDAY
import com.hashcaller.app.view.ui.call.db.CallLogTable
import com.hashcaller.app.view.ui.call.dialer.CallLogAdapter
import com.hashcaller.app.view.ui.call.dialer.util.CustomLinearLayoutManager
import com.hashcaller.app.view.ui.call.floating.NOTIFICATION_CHANNEL_GENERAL
import com.hashcaller.app.view.ui.call.individualCallLog.IndividualCallLogActivity
import com.hashcaller.app.view.ui.call.utils.CallContainerInjectorUtil
import com.hashcaller.app.view.ui.call.work.CallContainerViewModel
import com.hashcaller.app.view.ui.contacts.individualContacts.IndividualContactViewActivity
import com.hashcaller.app.view.ui.contacts.isDarkThemeOn
import com.hashcaller.app.view.ui.contacts.makeCall
import com.hashcaller.app.view.ui.contacts.utils.*
import com.hashcaller.app.view.ui.extensions.getSpannableString
import com.hashcaller.app.view.ui.extensions.isScreeningRoleHeld
import com.hashcaller.app.view.ui.sms.individual.util.*
import com.hashcaller.app.view.ui.sms.list.SMSListAdapter
import com.hashcaller.app.view.utils.ConfirmDialogFragment
import com.hashcaller.app.view.utils.ConfirmationClickListener
import com.hashcaller.app.view.utils.IDefaultFragmentSelection
import com.hashcaller.app.view.utils.getRelativeTime
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.models.PermissionRequest
import kotlinx.android.synthetic.main.activity_individual_cotact_view.*
import kotlinx.android.synthetic.main.bottom_sheet_block.*
import kotlinx.android.synthetic.main.bottom_sheet_block_feedback.*
import kotlinx.android.synthetic.main.call_list.*
import kotlinx.android.synthetic.main.call_list.view.*
import kotlinx.android.synthetic.main.contact_list.*
import kotlinx.android.synthetic.main.fragment_call.*
import kotlinx.android.synthetic.main.fragment_call.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * A simple [Fragment] subclass.
 * Use the [SpamCallFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SpamCallFragment : Fragment(), CallLogAdapter.ViewHandlerHelper,
    SMSListAdapter.NetworkHandler {

    private  lateinit var binding: SpamCallsFragmentBinding
    private var isDflt = false
    private var isScreeningApp = false

    var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    var layoutBottomSheet: ConstraintLayout? = null

    private  lateinit var viewmodel :SpamCallViewModel
    private lateinit var sharedUserInfoViewmodel: UserInfoViewModel
    private  var lastOperationPerformed: Int ? = null
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var bottomSheetDialogfeedback: BottomSheetDialog
    private  var spammerType:Int = SPAMMER_TYPE_SCAM
    private lateinit var recyclerV : RecyclerView
    private lateinit var layoutMngr: LinearLayoutManager

    private  var radioSales:RadioButton?= null
    private  var radioScam:RadioButton?= null
    private  var radioBusiness:RadioButton?= null
    private  var radioPerson:RadioButton?= null
    private  var selectedRadioButton: RadioButton? = null
    private var radioGroupOne: RadioGroup? = null
    private var radioGroupTwo: RadioGroup? = null
    private var isCallLogsCpEmpty = false
    var callLogAdapter: CallLogAdapter? = null
    private var permissionGivenLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private var tokenHelper: TokenHelper? = null
    private var btnBlock:Button? = null


    
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
    binding = SpamCallsFragmentBinding.inflate(inflater, container, false)
       tokenHelper =  TokenHelper(FirebaseAuth.getInstance().currentUser)
//        registerForContextMenu(binding.rcrViewCallHistoryLogs) //in oncreatView
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        initSwipeHandler()
        initRecyclerView()
        //important to call following functions to be launched when created
        lifecycleScope.launchWhenCreated {
            initViewmodel()
            observeSpamCallLogs()
            initListeners()
        }
    }
    private fun observeSpamCallLogs() {
        viewmodel.spamCalllivedata.observe(viewLifecycleOwner, Observer {
            callLogAdapter?.submitCallLogs(it, false)
            binding.pgBarCallSpam.beGone()
            if(it.isEmpty()){

//                binding.tvSpamInfoCall.beVisible()
            }else {
//                binding.tvSpamInfoCall.beGone()
            }
        })
    }
    private suspend fun initViewmodel() {
//            lifecycleScope.launchWhenCreated {
        SPAM_THRESHOLD_VALUE = DataStoreRepository(requireContext().tokeDataStore).getInt(
            PreferencesKeys.SPAM_THRESHOLD
        )?: Constants.DEFAULT_SPAM_THRESHOLD
        viewmodel = ViewModelProvider(this,
            SpamCallInjectorUtil.provideViewmodelFactory(
                requireContext(),
                lifecycleScope,
                SPAM_THRESHOLD_VALUE
            ),

            ).get(
            SpamCallViewModel::class.java
        )
    }
    private fun initRecyclerView() {

        binding.rclrViewSpam.apply {
            layoutManager = CustomLinearLayoutManager(context)
//            ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.recyvlerV);
            layoutMngr = layoutManager as CustomLinearLayoutManager


            callLogAdapter = CallLogAdapter(context, this@SpamCallFragment, this@SpamCallFragment, requireContext().isDarkThemeOn()) {

                    id: Long, position: Int, view: View, btn: Int, callLog: CallLogTable, clickType: Int, visibility: Int ->onCallItemClicked(
                id,
                position,
                view,
                btn,
                callLog,
                clickType,
                visibility
            )}

            adapter = callLogAdapter
            itemAnimator = null

        }
    }

    private fun onCallItemClicked(
        id: Long,
        position: Int,
        view: View,
        btn: Int,
        callLog: CallLogTable,
        clickType: Int,
        visibility: Int
    ): Int {
        when(clickType){
            TYPE_LONG_PRESS -> {
//                toast("Slide left or right to delete")
            }

            TYPE_CLICK_VIEW_CALL_HISTORY -> {
                startCallHistoryActivity(callLog, view)
                return COMPRESS_LAYOUT
            }

            TYPE_CLICK_VIWE_INDIVIDUAL_CONTACT -> {
                if (getMarkedItemsSize() == 0) {
                    startIndividualContactActivity(callLog, view)
                    return UNMARK_ITEM
                }
//                else {
//                    return markItem(id, TYPE_CLICK, position, callLog.number) // mark item
//                }
            }

            else ->{
                if(getMarkedItemsSize() == 0){
//                   startIndividualContactActivity(callLog, view)
                    val prevExpandedLyoutId = viewmodel.markeditemsHelper.getPreviousExpandedLayout()
                    if(prevExpandedLyoutId==null){
                        viewmodel.markeditemsHelper.setExpandedLayout(id, position)
                        return EXPAND_LAYOUT
                    }else if(prevExpandedLyoutId != id){
                        val oldPos = viewmodel.markeditemsHelper.getPrevExpandedPosition()
                        viewmodel.markeditemsHelper.setExpandedLayout(id, position)
                        if(oldPos!=null){
//                            callLogAdapter?.notifyItemChanged(oldPos)
                        }
                        return EXPAND_LAYOUT
                    }else{

                        viewmodel.markeditemsHelper.setExpandedLayout(null, null)
                        return COMPRESS_LAYOUT

                    }
                }
//                else{
//                    return markItem(id, clickType, position, callLog.number)
//                }
            }
        }
        return UNMARK_ITEM
    }

    private fun startIndividualContactActivity(log: CallLogTable, view: View) {

        val intent = getContactIntent(log, CallFragment.INDIVIDUAL_CONTACT_ACTIVITY)
        val options = getOptions(view, log)
        startActivity(intent, options.toBundle())
    }

    private fun getOptions(view: View, log: CallLogTable): ActivityOptions {
        val pairList = ArrayList<android.util.Pair<View, String>>()
        val imgViewUserPhoto = view.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.imgVThumbnail)
        val textViewCrclr = view.findViewById<TextView>(R.id.textViewCrclr)
        var pair:android.util.Pair<View, String>? = null
        if(log.thumbnailFromCp.isNotEmpty()){
            pair = android.util.Pair(imgViewUserPhoto as View, "contactImageTransition")
        }else if(log.imageFromDb.isNotEmpty()){
            pair = android.util.Pair(imgViewUserPhoto as View, "contactImageTransition")

        }else{
            pair = android.util.Pair(textViewCrclr as View, "firstLetterTransition")
        }
        pairList.add(pair)
        val options = ActivityOptions.makeSceneTransitionAnimation(requireActivity(), pairList[0])
        return options
    }

    fun getMarkedItemsSize(): Int {
        return  viewmodel.markeditemsHelper.getmarkedItemSize()
    }

    private fun startCallHistoryActivity(callLog: CallLogTable, view: View) {
        viewmodel?.markeditemsHelper?.setExpandedLayout(null, null)
        val intent = getContactIntent(callLog, CallFragment.INDIVIDUAL_CALL_LOG_ACTIVITY)
        startActivity(intent)
    }

    private fun getContactIntent(
        log: CallLogTable,
        destinationActivity: Int
    ): Intent {
        var name = log.nameInPhoneBook
        if(name.isNullOrEmpty()){
            name = log?.nameFromServer
        }
        if(name.isNullOrEmpty()){
            name = log.number
        }
        var intent: Intent? = null
        when(destinationActivity){
            CallFragment.INDIVIDUAL_CALL_LOG_ACTIVITY -> {
                intent = Intent(requireContext(), IndividualCallLogActivity::class.java)
                intent.putExtra(CONTACT_ADDRES, log.number)
            }else ->{
            intent = Intent(requireContext(), IndividualContactViewActivity::class.java)
        }
        }
        intent.putExtra(com.hashcaller.app.view.ui.contacts.utils.CONTACT_ID, log.number)
        intent.putExtra("name", name)
        intent.putExtra("photo", log.thumbnailFromCp)
        intent.putExtra("color", log.color)
        return intent
    }
    private fun initListeners() {

    }


    override fun onResume() {
        super.onResume()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(savedInstanceState == null){

        }

    }





    /**
     * important to prevent memory leak
     */
    override fun onDestroyView() {
        super.onDestroyView()
//        viewmodel = null
    }






    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }
    

    companion object {
            private const val TAG ="__CallFragment"

        var SPAM_THRESHOLD_VALUE = Constants.DEFAULT_SPAM_THRESHOLD

    
    }




    private fun requestRequiredPermissions() {
        val request = PermissionRequest.Builder(this.context)
            .code(REQUEST_CODE_CALL_LOG)
            .perms(arrayOf(READ_CALL_LOG,
            ))
            .rationale("HashCaller needs access to call logs to identify unknown callers in call log.")
            .positiveButtonText("Continue")
            .negativeButtonText("Cancel")
            .build()
        EasyPermissions.requestPermissions(requireActivity(), request)
    }

    override fun isMarked(id: Long?): Boolean {
        return false
    }

    override fun isViewExpanded(id: Long): Boolean {
        return false
    }

    override fun isInternetAvailable(): Boolean {
        //todo, update this shit
        return false
    }


}