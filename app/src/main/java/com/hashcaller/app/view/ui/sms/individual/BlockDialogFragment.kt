package com.hashcaller.app.view.ui.sms.individual

import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.hashcaller.app.R
import com.hashcaller.app.utils.auth.TokenHelper
import com.hashcaller.app.view.ui.contacts.utils.ContacInjectorUtil
import com.hashcaller.app.view.ui.contacts.utils.ContactsViewModel

// TODO: Customize parameter argument names
const val ARG_ITEM_COUNT = "item_count"

/**
 *Bottom sheet dialog fragment for blocking a contact address
 *
 */
class BlockDialogFragment : BottomSheetDialogFragment(), View.OnClickListener {
    private lateinit var sheet:View
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
         sheet = inflater.inflate(R.layout.fragment_item_list_dialog_list_dialog, container, false)

        ViewModelProvider(this, ContacInjectorUtil.provideContactsViewModelFactory(
            context,
            lifecycleScope,
            TokenHelper( FirebaseAuth.getInstance().currentUser)
        )).get(
            ContactsViewModel::class.java)

        return sheet
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//    view.btnBlock.setOnClickListener(this)

    }

    private inner class ViewHolder internal constructor(
        inflater: LayoutInflater,
        parent: ViewGroup
    ) : RecyclerView.ViewHolder(
        inflater.inflate(
            R.layout.fragment_item_list_dialog_list_dialog_item,
            parent,
            false
        )
    ) {

        internal val text: TextView = itemView.findViewById(R.id.text)
    }

    private inner class ItemAdapter internal constructor(private val mItemCount: Int) :
        RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context), parent)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.text.text = position.toString()
        }

        override fun getItemCount(): Int {
            return mItemCount
        }
    }

    companion object {

        fun newInstance(itemCount: Int): BlockDialogFragment =
            BlockDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ITEM_COUNT, itemCount)
                }
            }

    }

    override fun onClick(v: View?) {
        when(v?.id){
//            R.id.btnBlock->{
//               blockThisNumber()
//            }
        }
    }

    private fun blockThisNumber() {

    }
}