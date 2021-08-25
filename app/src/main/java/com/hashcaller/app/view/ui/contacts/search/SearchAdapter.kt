package com.hashcaller.app.view.ui.contacts.search

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hashcaller.app.R
import com.hashcaller.app.local.db.contactInformation.ContactTable
import com.hashcaller.app.network.search.model.Cntct
import com.hashcaller.app.stubs.Contact
import com.hashcaller.app.view.ui.sms.util.SMS
import kotlinx.android.synthetic.main.search_result_layout.view.*

/**
 * Created by Jithin KG on 31,July,2020
 */
class SearchAdapter (private val context: Context, private val onContactItemClickListener: (id:Long)->Unit) :
    androidx.recyclerview.widget.ListAdapter<SMS, RecyclerView.ViewHolder>(FullSearchItemDiffCallback()) {

    private  var contacts: List<Cntct>? = null
    private val TAG  = "__SearchAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_result_layout, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
//        Log.d(TAG, "getItemCount: ${contacts?.size}")
        return contacts?.size?:0
    }
    fun setContactList(newContactList: List<ContactTable>) {
        contacts = emptyList()
//        this.submitList(newContactList)
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val contact = contacts?.get(position)
//    holder.bind(contact, context, onContactItemClickListener)
        when(holder) {

            is ViewHolder -> {
//                contacts?.get(position)?.let { holder.bind(it,context, onContactItemClickListener) }
            }

        }
    }
    class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val name = view.textViewSearchContactName
        private val circle = view.textViewSearchCrclr;
        private val location = view.textViewSearchLocation
        private val country = view.textViewSearchCountry

        fun bind(contact: Contact, context: Context,onContactItemClickListener :(id:Long)->Unit ) {


            Log.d("__ViewHolder", "bind:")


            view.setOnClickListener{
                onContactItemClickListener(1L)
            }
        }


    }

    class FullSearchItemDiffCallback : DiffUtil.ItemCallback<SMS>() {
        override fun areItemsTheSame(oldItem: SMS, newItem: SMS): Boolean {

            return true

        }


        override fun areContentsTheSame(oldItem: SMS, newItem: SMS): Boolean {
            return true
        }

    }


    companion object{
        const val TAG = "__SearchAdapter"
    }

}