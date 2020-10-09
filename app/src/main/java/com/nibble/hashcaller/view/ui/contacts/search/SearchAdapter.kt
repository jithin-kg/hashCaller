package com.nibble.hashcaller.view.ui.contacts.search

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.R
import com.nibble.hashcaller.local.db.contactInformation.ContactTable
import com.nibble.hashcaller.network.search.model.Cntct
import com.nibble.hashcaller.network.search.model.SerachRes
import com.nibble.hashcaller.repository.contacts.ContactUploadDTO
import com.nibble.hashcaller.stubs.Contact
import kotlinx.android.synthetic.main.search_result_layout.view.*

/**
 * Created by Jithin KG on 31,July,2020
 */
class SearchAdapter (private val context: Context, private val onContactItemClickListener: (id:Long)->Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
    fun setContactList(newContactList: SerachRes) {
        contacts = emptyList()
        contacts = newContactList.cntcts
        Log.d(TAG, "setContactList: $contacts")

//        Log.d(TAG, "setContactList: ${newContactList.size}")
//        Log.d(TAG, "setContactList: size of contacts ${contacts?.size}")
//        Log.d(TAG, "setContactList: size of contacts ${newContactList.size}")

        notifyDataSetChanged()
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val contact = contacts?.get(position)
//    holder.bind(contact, context, onContactItemClickListener)
        when(holder) {

            is ViewHolder -> {
                contacts?.get(position)?.let { holder.bind(it,context, onContactItemClickListener) }
            }

        }
    }
    fun setSearchResult(newContactList: List<Contact>) {
//        contacts = newContactList

        notifyDataSetChanged()
    }
    class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val name = view.textViewSearchContactName
//        private val image = view.findViewById<ImageView>(R.id.contact_image)

        fun bind(contact: Cntct, context: Context,onContactItemClickListener :(id:Long)->Unit ) {
            name.text = contact.phoneNumber
            Log.d("__ViewHolder", "bind:")
//            name.text = contact.name
//            Glide.with(context).load(R.drawable.ic_account_circle_24px).into(image)
            view.setOnClickListener{
                onContactItemClickListener(1L)
            }
        }

    }

}