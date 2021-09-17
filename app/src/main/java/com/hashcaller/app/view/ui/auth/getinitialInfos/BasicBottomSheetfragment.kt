package com.hashcaller.app.view.ui.auth.getinitialInfos
import com.hashcaller.app.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hashcaller.app.databinding.BottomSheetProfileEditBinding


class BasicBottomSheetfragment : BottomSheetDialogFragment(), View.OnClickListener {
    private lateinit var binding:BottomSheetProfileEditBinding

    companion object{
        fun newInstance(): BasicBottomSheetfragment? {
            return BasicBottomSheetfragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.edtTextBSheet.setOnClickListener(this)

    }

    @Nullable
    override fun onCreateView(
        inflater: LayoutInflater,
        @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View? {
        binding = BottomSheetProfileEditBinding.inflate(layoutInflater, container, false)
        // get the views and attach the listener
        return binding.root
    }

    override fun onClick(v: View?) {


    }
}