package com.nibble.hashcaller.view.ui.call.dialer

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.nibble.hashcaller.R
import com.nibble.hashcaller.view.ui.MainActivity
import com.nibble.hashcaller.view.utils.IDefaultFragmentSelection
import com.nibble.hashcaller.view.utils.TopSpacingItemDecoration
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_dialer.*
import kotlinx.android.synthetic.main.fragment_dialer.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DialerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DialerFragment : Fragment(), View.OnClickListener, IDefaultFragmentSelection {
    private var isDflt = false
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var dialerFragment: View
    var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    private lateinit var bottomSheetDialog:BottomSheetDialog
//    var phoneNumberViewModel: PhoneNumber? = null
    private lateinit var dialerViewModel: DialerViewModel
    private lateinit var  nameObserver: Observer<String?>
    private var lastEditPosition = 0
    var callLogAdapter: DialerAdapter? = null
    var newPos = 0
    var subStringLen = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")
//        setHasOptionsMenu(true);


    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if(savedInstanceState!= null){
         //get state of this fragment


        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //save state of this fragment, list...
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "onAttach: ")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        dialerFragment = inflater.inflate(R.layout.fragment_dialer, container, false)

        dialerViewModel = ViewModelProvider(this, DialerInjectorUtil.provideDialerViewModelFactory(context)).get(
            DialerViewModel::class.java)

        setupBottomSheet()
        initListeners()
        /**
         * Observes the numbers entered in the dialpad
         * and updates the Edittext
         */
        initEditTextPhoneNumberObserver()

        dialerViewModel.getPhoneNumber()?.observe(viewLifecycleOwner, nameObserver)
        dialerViewModel.callLogs.observe(viewLifecycleOwner, Observer { logs->
            logs.let {
                callLogAdapter?.setCallLogs(it)
            }
        })
        return dialerFragment
    }
    override fun onDestroyView() {
        super.onDestroyView()
        //THIS IS IMPORTANT !! otherwise memory leak occurs
        rcrViewCallLogs.adapter  = null
    }
    private fun initEditTextPhoneNumberObserver() {
        /**
         * Observes the numbers entered in the dialpad
         * and updates the Edittext
         */
        nameObserver =
            Observer<String?> { phoneNumber -> // Update the UI, in this case, a TextView.
                Log.d(TAG, "onChanged: $phoneNumber")
                //                newPos = editTextPhoneNumber.getSelectionEnd();
                Log.d(TAG, "onChanged: previous position is : $newPos")
                if (phoneNumber != null) {
                    if(phoneNumber.length<=0){
                        bottomSheetDialog.imgBtnBackspace.isEnabled = false

                    }else{
                        bottomSheetDialog.imgBtnBackspace.isEnabled = true
                    }
                }else{
                    bottomSheetDialog.imgBtnBackspace.isEnabled = false
                }
                //                editTextPhoneNumber.setSelection(newPos);
                bottomSheetDialog.editTextTextDigits.setText(phoneNumber)
                Log.d(TAG, "onChanged: substringLength$subStringLen")
                bottomSheetDialog.editTextTextDigits.setSelection(subStringLen + 1)
            }
    }

    private fun setupBottomSheet() {
        bottomSheetDialog = BottomSheetDialog(this.requireActivity())

        val viewSheet = layoutInflater.inflate(R.layout.bottom_sheet, null)

        bottomSheetDialog.setContentView(viewSheet)
        if(this.view?.visibility == View.VISIBLE){
            bottomSheetDialog.show()

        }

        bottomSheetDialog.setOnDismissListener{
            Log.d(TAG, "bottomSheetDialogDismissed")

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        Log.d(TAG, "onViewCreated: ")


    }

    private fun initRecyclerView() {
        rcrViewCallLogs?.apply {
            layoutManager = LinearLayoutManager(activity)
            val topSpacingDecorator =
                TopSpacingItemDecoration(
                    30
                )
            addItemDecoration(topSpacingDecorator)
            callLogAdapter = DialerAdapter(context) { id:String,pos:Int, v:View, btn:Int->onCallLogItemClicked(id, pos, v, btn)}
            adapter = callLogAdapter

        }
    }

    private fun onCallLogItemClicked(
        id: String,
        pos: Int,
        v: View,
        btn: Int
    ) {
        Log.d(TAG, "onCallLog item clicked: $id")
//        val intent = Intent(context, IndividualCotactViewActivity::class.java )
//        intent.putExtra(CONTACT_ID, id)
//        startActivity(intent)
    }

    private fun initListeners() {
        dialerFragment.fabShoDialPad2.setOnClickListener(this)

        bottomSheetDialog.layoutNum0.setOnClickListener(this)
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
        bottomSheetDialog.layoutNumPound.setOnClickListener(this)

        bottomSheetDialog.imgBtnBackspace.setOnClickListener(this)
        bottomSheetDialog.imgBtnBackspace.isEnabled = false
        bottomSheetDialog.editTextTextDigits.showSoftInputOnFocus = false;
//        includeDialer.setOnClickListener(this)

        bottomSheetDialog.editTextTextDigits.append("")
        dialerFragment.imgBtnCloseDialer.setOnClickListener(this)



    }


    companion object {
        private const val TAG = "__DialerFragment"
            }
    fun showDialPad(){
        bottomSheetDialog.show()
        this.activity?.fabBtnShowDialpad?.visibility= View.GONE
//        this.activity?.bottomNavigationView?.visibility= View.GONE
//        this.activity?.bottomNavigationView?.removeView(bottomNavigationView)

    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.layoutNum0->{
                keypadClicked("0")
            }
            R.id.layoutNum1->{
                keypadClicked("1")
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
            R.id.layoutNumPound->{
                keypadClicked("#")
            }
            R.id.fabShoDialPad2->{
                bottomSheetDialog.show()
            }R.id.imgBtnCloseDialer->{
            Log.d(TAG, "onClick: close button clicked")
            closeDialerFragment()
        }
            else->{
                backspacePhoneNumEditText()
            }
        }

    }

    private fun closeDialerFragment() {

        (activity as MainActivity?)?.showCallFragment()
    }

    private fun backspacePhoneNumEditText() {
        Log.d(
            "BACKSPACE",
            "delete button clicked: " + bottomSheetDialog.editTextTextDigits.selectionEnd
                .toString() + " " + bottomSheetDialog.editTextTextDigits.selectionStart.toString()
        )

        val pos: Int = bottomSheetDialog.editTextTextDigits.selectionEnd
//
        //
        val num: String?
        num = getPhoneNumFromViewModel()
        val cursorPos: Int = bottomSheetDialog.editTextTextDigits.selectionEnd
        val trimmedString: String?

        if (cursorPos == num!!.length && num.length > 0) {
            Log.d("BACKSPACE", "cursor at end of the string:$cursorPos ")
            trimmedString = num.substring(0, cursorPos - 1)
            Log.d("BACKSPACE", "trimmmed Number $trimmedString")
            subStringLen = num.substring(0, cursorPos - 1).length - 1
        } else if (cursorPos > 0) {
            //cursor in between
            trimmedString = num.substring(0, cursorPos - 1) + num.substring(cursorPos)
            Log.d("BACKSPACE", "trimmmed Number $trimmedString")
            subStringLen = num.substring(0, cursorPos - 1).length - 1
        } else {
            trimmedString = num
            Log.d("BACKSPACE", "trimmmed Number $trimmedString")
        }
        Log.d("BACKSPACE", "substringLength$subStringLen")

        dialerViewModel.getPhoneNumber()?.value = trimmedString
    }

    private fun keypadClicked(s: String) {
        Log.d(
            TAG,
            "keypadClicked: current position " +bottomSheetDialog.editTextTextDigits?.selectionEnd
        )
        val num: String? = getPhoneNumFromViewModel()

//        Log.d(TAG, "keypadClicked: num "+num);

//        Log.d(TAG, "keypadClicked: num "+num);
        val currentNum: String
        var prevPosition: Int
        val pos: Int = bottomSheetDialog.editTextTextDigits.selectionEnd
        if (pos > 0 && pos < num?.length!!) {
            //editing in between
            lastEditPosition = pos

//                Log.d(TAG, "keypadClicked: In between" + pos);
            currentNum = num.substring(0, pos) + s + num.substring(pos)
            subStringLen = num.substring(0, pos).length


//                editTextPhoneNumber.setSelection(subStringLen + 1);

//                editTextPhoneNumber.setSelection(pos + 1);
        } else if (pos == 0) {
            currentNum = s + num
            Log.d(TAG, "keypadClicked: at start of the phone number ")
            subStringLen = 0
        } else {
            currentNum = num + s
            subStringLen = bottomSheetDialog.editTextTextDigits.selectionEnd
        }
        dialerViewModel?.getPhoneNumber()?.value = currentNum
    }

    private fun getPhoneNumFromViewModel(): String? {
        val no: MutableLiveData<String>? = dialerViewModel.getPhoneNumber()
        val num = no?.value
        if(num.equals(null)){
            return ""
        }
        return num;
    }

    override var isDefaultFgmnt: Boolean
        get() = isDflt
        set(value) {isDflt = value}

}