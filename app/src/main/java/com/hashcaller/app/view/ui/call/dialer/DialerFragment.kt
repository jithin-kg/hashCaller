package com.hashcaller.app.view.ui.call.dialer

import android.Manifest
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hashcaller.app.R
import com.hashcaller.app.databinding.ContactSearchResultItemBinding
import com.hashcaller.app.databinding.FragmentDialerBinding
import com.hashcaller.app.datastore.DataStoreRepository
import com.hashcaller.app.datastore.PreferencesKeys
import com.hashcaller.app.stubs.Contact
import com.hashcaller.app.utils.Constants
import com.hashcaller.app.utils.extensions.requestCallPhonePermission
import com.hashcaller.app.utils.notifications.tokeDataStore
import com.hashcaller.app.view.ui.MainActivity
import com.hashcaller.app.view.ui.call.dialer.util.CustomLinearLayoutManager
import com.hashcaller.app.view.ui.contacts.individualContacts.IndividualContactViewActivity
import com.hashcaller.app.view.ui.contacts.makeCall
import com.hashcaller.app.view.ui.contacts.utils.CONTACT_ID
import com.hashcaller.app.view.ui.contacts.utils.isNumericOnlyString
import com.hashcaller.app.view.ui.sms.individual.util.TYPE_MAKE_CALL
import com.hashcaller.app.view.utils.IDefaultFragmentSelection
import com.hashcaller.app.view.utils.TopSpacingItemDecoration
import com.vmadalin.easypermissions.EasyPermissions
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_dialer.*
import kotlinx.android.synthetic.main.fragment_dialer.view.*
import kotlinx.coroutines.Job


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DialerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DialerFragment : Fragment(), View.OnClickListener, IDefaultFragmentSelection,
    CallLogAdapter.ViewHandlerHelper, View.OnLongClickListener {
    private var _binding: FragmentDialerBinding ? = null
    private val binding get() = _binding!!
    private var isDflt = false
    private var param1: String? = null
    private var param2: String? = null
//    private lateinit var dialerFragment: View
    var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    private lateinit var bottomSheetDialog:BottomSheetDialog
    private var permissionGivenLiveData: MutableLiveData<Boolean> = MutableLiveData(false)
//    var phoneNumberViewModel: PhoneNumber? = null
    private lateinit var viewmodel: DialerViewModel
    private lateinit var  nameObserver: Observer<String?>
    private  var job:Job?=null

    private var lastEditPosition = 0
    var callLogAdapter: DialerAdapter? = null
    var newPos = 0
    var subStringLen = 0
    private var searchQueryPhone = ""


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //save state of this fragment, list...
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onResume() {
        super.onResume()
        this.permissionGivenLiveData.value  = checkCallPermission()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDialerBinding.inflate(inflater, container, false)


        initRecyclerView()
        setupBottomSheet()
        initListeners()
        /**
         * Observes the numbers entered in the dialpad
         * and updates the Edittext
         */
        


//        observePermissionLiveData()
        return binding.root
    }



    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if(!hidden &&  !this::viewmodel.isInitialized ){
            if(checkCallPermission()){
                initViewModel()
                Log.d(TAG, "onCreateView: ")
                observeEditTextnum()
                getFirst10items()
                observeContacts()

            }
        }
    }

    private fun initViewModel() {

            viewmodel = ViewModelProvider(this@DialerFragment, DialerInjectorUtil.provideDialerViewModelFactory(requireContext(), lifecycleScope)).get(
                DialerViewModel::class.java )

    }

    private fun observeContacts() {
        viewmodel.searchResultLivedata.observe(viewLifecycleOwner, Observer {
            callLogAdapter?.setQuery(searchQueryPhone)
            callLogAdapter?.setList(it)
//            (activity as MainActivity).hideBottomNav()

        })
    }

    private fun getFirst10items() {
        viewmodel.getFirst10Logs()
    }



    private fun checkCallPermission(): Boolean {
        val permissionSms =
            ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_CALL_LOG)
        if(permissionSms!= PackageManager.PERMISSION_GRANTED){
            return false
        }
        return true
    }

    private fun observeEditTextnum() {

        viewmodel.getPhoneNumber()?.observe(viewLifecycleOwner, Observer { phoneNumber ->
            if(phoneNumber.isNotEmpty()){
                callLogAdapter?.submitList(emptyList())
                lifecycleScope.launchWhenStarted {
                    job?.cancel()
                    DialerViewModel.cancelJob = false
                    job =   viewmodel.searchContactsInDb(phoneNumber)
                    binding.tvDialerToolbar.text = "All contacts"

                }
            }else{
                binding.tvDialerToolbar.text = "Suggested"
                viewmodel.getFirst10Logs()
            }
            searchQueryPhone = phoneNumber
            if (phoneNumber != null) {
                bottomSheetDialog.imgBtnBackspace.isEnabled = phoneNumber.isNotEmpty()
            }else{
                bottomSheetDialog.imgBtnBackspace.isEnabled = false
            }
            bottomSheetDialog.editTextTextDigits.setText(phoneNumber)
            bottomSheetDialog.editTextTextDigits.setSelection(subStringLen + 1)
        })


    }

    override fun onDestroyView() {
        super.onDestroyView()
        //THIS IS IMPORTANT !! otherwise memory leak occurs
        binding.rcrViewCallLogs.adapter  = null
    }
    private fun initEditTextPhoneNumberObserver() {

    }

    private fun setupBottomSheet() {
        bottomSheetDialog = BottomSheetDialog(this.requireActivity(), R.style.BottomSheetDialog)
        val viewSheet = layoutInflater.inflate(R.layout.bottom_sheet, null)
        bottomSheetDialog.setContentView(viewSheet)
        if(this.view?.visibility == View.VISIBLE){
            bottomSheetDialog.show()
        }
        bottomSheetDialog.setOnDismissListener{
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun initRecyclerView() {
        binding.rcrViewCallLogs?.apply {
            layoutManager = CustomLinearLayoutManager(context)
            val topSpacingDecorator =
                TopSpacingItemDecoration(
                    30
                )
            addItemDecoration(topSpacingDecorator)
            callLogAdapter = DialerAdapter(context) { binding: ContactSearchResultItemBinding, contact: Contact, clickType:Int ->onContactItemClicked(binding, contact, clickType)}
            adapter = callLogAdapter
            itemAnimator = null
        }
    }

    private fun onContactItemClicked(
        binding: ContactSearchResultItemBinding,
        contactItem: Contact,
        clickType: Int
    ){
        when(clickType){
            TYPE_MAKE_CALL ->{
                if(EasyPermissions.hasPermissions(context, Manifest.permission.CALL_PHONE)){
                    context?.makeCall(contactItem.phoneNumber)
                }else {
                    EasyPermissions.hasPermissions(context, Manifest.permission.CALL_PHONE)
                }
            }
            else ->{
                val intent = Intent(context, IndividualContactViewActivity::class.java )
                intent.putExtra(CONTACT_ID, contactItem.phoneNumber)
                intent.putExtra("name", contactItem.firstName )
//        intent.putExtra("id", contactItem.id)
                intent.putExtra("photo", contactItem.photoURI)
                intent.putExtra("color", contactItem.drawable)
                Log.d(TAG, "onContactItemClicked: ${contactItem.photoURI}")
                val pairList = ArrayList<android.util.Pair<View, String>>()
//        val p1 = android.util.Pair(imgViewCntct as View,"contactImageTransition")
                var pair:android.util.Pair<View, String>? = null
                if(contactItem.photoURI.isEmpty()){
                    pair = android.util.Pair(binding.textViewcontactCrclr as View, "firstLetterTransition")
                }else{
                    pair = android.util.Pair(binding.imgViewCntct as View,"contactImageTransition")
                }
                pairList.add(pair)
                val options = ActivityOptions.makeSceneTransitionAnimation(activity,pairList[0])
                startActivity(intent, options.toBundle())
            }
        }

    }

    private fun initListeners() {
        binding.fabShoDialPad2.setOnClickListener(this)

        bottomSheetDialog.layoutNum0.setOnClickListener(this)
        bottomSheetDialog.layoutNum0.setOnLongClickListener(this)
        bottomSheetDialog.layoutNum1.setOnClickListener(this)
        bottomSheetDialog.layoutNum2.setOnClickListener(this)
        bottomSheetDialog.layoutNum3.setOnClickListener(this)
        bottomSheetDialog.layoutNum4.setOnClickListener(this)
        bottomSheetDialog.layoutNum5.setOnClickListener(this)
        bottomSheetDialog.layoutNum6.setOnClickListener(this)
        bottomSheetDialog.layoutNum7.setOnClickListener(this)
        bottomSheetDialog.layoutNum8.setOnClickListener(this)
        bottomSheetDialog.layoutNum9.setOnClickListener(this)
        bottomSheetDialog.layoutStar.setOnClickListener(this)
        bottomSheetDialog.layoutPound.setOnClickListener(this)
        bottomSheetDialog.imgBtnBackspace.setOnClickListener(this)
        bottomSheetDialog.fabBtnMakeCall.setOnClickListener(this)

        bottomSheetDialog.imgBtnBackspace.isEnabled = false
        bottomSheetDialog.editTextTextDigits.showSoftInputOnFocus = false;
        bottomSheetDialog.editTextTextDigits.append("")
        binding.imgBtnCloseDialer.setOnClickListener(this)
    }


    companion object {
        private const val TAG = "__DialerFragment"
            }
    fun showDialPad(){
        bottomSheetDialog.show()
    }

    override fun onClick(v: View?) {
        Log.d(TAG, "onClick: ")
        when(v?.id){
            R.id.layoutNum0->{
                keypadClicked("0")
            }
            R.id.layoutNum1->{
                keypadClicked("1")
            }
            R.id.layoutNum2->{
                keypadClicked("2")
            }
            R.id.layoutNum3->{
                keypadClicked("3")
            }
            R.id.layoutNum4->{
                keypadClicked("4")
            }
            R.id.layoutNum5->{
                keypadClicked("5")
            }
            R.id.layoutNum6->{
                keypadClicked("6")
            }
            R.id.layoutNum7->{
                keypadClicked("7")
            }
            R.id.layoutNum8->{
                keypadClicked("8")
            }
            R.id.layoutNum9->{
            keypadClicked("9")
            }
            R.id.layoutStar->{
                keypadClicked("*")
            }
            R.id.layoutPound->{
                keypadClicked("#")
            }
            R.id.fabShoDialPad2->{
                bottomSheetDialog.show()
            }R.id.imgBtnCloseDialer->{
            
            closeDialerFragment()
            
        }R.id.fabBtnMakeCall->{
            makeCallFromEditTextNum()

        }
            else->{
                backspacePhoneNumEditText()
            }
        }

    }

    private fun makeCallFromEditTextNum() {
        val num: String? =  getPhoneNumFromViewModel()
        if(!num.isNullOrEmpty())
        {
            if(EasyPermissions.hasPermissions(context, Manifest.permission.CALL_PHONE)){
                context?.makeCall(num)
            }else {
                requireActivity().requestCallPhonePermission()
            }



        }else{
            Toast.makeText(context, "Please enter a number to call", Toast.LENGTH_SHORT).show()
        }
    }



    private fun closeDialerFragment() {
        (activity as MainActivity?)?.showCallFragment()
    }

    private fun backspacePhoneNumEditText() {

        val pos: Int = bottomSheetDialog.editTextTextDigits.selectionEnd
//
        //
        val num: String?
        num = getPhoneNumFromViewModel()
        val cursorPos: Int = bottomSheetDialog.editTextTextDigits.selectionEnd
        val trimmedString: String?

        if (cursorPos == num!!.length && num.length > 0) {
            trimmedString = num.substring(0, cursorPos - 1)
            subStringLen = num.substring(0, cursorPos - 1).length - 1
        } else if (cursorPos > 0) {
            //cursor in between
            trimmedString = num.substring(0, cursorPos - 1) + num.substring(cursorPos)
            subStringLen = num.substring(0, cursorPos - 1).length - 1
        } else {
            trimmedString = num
        }
        viewmodel.getPhoneNumber()?.value = trimmedString
    }

    override fun onLongClick(v: View?): Boolean {
        var str = ""
        when(v?.id){
            R.id.layoutNum0 -> {
                getPhoneNumFromViewModel()?.let {
                    keypadClicked("+")
//                    if(!isNumericOnlyString(it)){
//                        keypadClicked("+")
////                        str = it
////                        str+= "+"
////                        viewmodel?.getPhoneNumber()?.value = str
//                    }
                
                }
                
            }
        }

        return true
    }
    
    private fun keypadClicked(s: String) {
        val num: String? = getPhoneNumFromViewModel()
        val currentNum: String
        var prevPosition: Int
        val pos: Int = bottomSheetDialog.editTextTextDigits.selectionEnd
        if (pos > 0 && pos < num?.length!!) {
            //editing in between
            lastEditPosition = pos

//                Log.d(TAG, "keypadClicked: In between" + pos);
            currentNum = num.substring(0, pos) + s + num.substring(pos)
            subStringLen = num.substring(0, pos).length

        } else if (pos == 0) {
            currentNum = s + num
            subStringLen = 0
        } else {
            currentNum = num + s
            subStringLen = bottomSheetDialog.editTextTextDigits.selectionEnd
        }
        viewmodel?.getPhoneNumber()?.value = currentNum
    }

    private fun getPhoneNumFromViewModel(): String? {
        val no: MutableLiveData<String>? = viewmodel.getPhoneNumber()
        val num = no?.value
        if(num.equals(null)){
            return ""
        }
        return num;
    }

    override var isDefaultFgmnt: Boolean
        get() = isDflt
        set(value) {isDflt = value}


    override fun isMarked(id: Long?): Boolean {
        return false
    }

    override fun isViewExpanded(id: Long): Boolean {
        return false
    }

    

}