package com.nibble.hashcaller.view.ui.contacts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nibble.hashcaller.R
import com.nibble.hashcaller.data.stubs.Contact
import kotlinx.android.synthetic.main.contact_list.view.*

/**
 * Created by Jithin KG on 22,July,2020
 */
class ContactAdapter(private val context: Context) :
    RecyclerView.Adapter<ContactAdapter.ViewHolder>() {

    private var contacts = emptyList<Contact>()



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contacts[position]
        holder.bind(contact, context)
    }

    override fun getItemCount() = contacts.size

    fun setContactList(newContactList: List<Contact>) {
        contacts = newContactList
        notifyDataSetChanged()
    }
    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val name = view.textViewContactName
//        private val image = view.findViewById<ImageView>(R.id.contact_image)

        fun bind(contact: Contact, context: Context) {
            name.text = contact.name
//            Glide.with(context).load(R.drawable.ic_account_circle_24px).into(image)
        }
    }

}