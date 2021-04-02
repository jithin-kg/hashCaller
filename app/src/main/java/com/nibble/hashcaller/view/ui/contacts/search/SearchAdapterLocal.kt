package com.nibble.hashcaller.view.ui.contacts.search

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.R
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.view.ui.extensions.setRandomBackgroundCircle
import kotlinx.android.synthetic.main.search_result_layout.view.*

class SearchAdapterLocal (private val context: Context, private val onContactItemClickListener: (id:Long)->Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private  var contacts: List<Contact>? = null
    companion object{
        private val TAG  = "__SearchAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_result_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return contacts?.size?:0
    }
    fun setContactList(it: List<Contact>) {
        contacts = emptyList()
        contacts = it
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
    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
            private val name = view.textViewSearchContactName
            private val circle = view.textViewSearchCrclr;
            private val location = view.textViewSearchLocation
            private val country = view.textViewSearchCountry
//        private val image = view.findViewById<ImageView>(R.id.contact_image)

        fun bind(contact: Contact, context: Context, onContactItemClickListener :(id:Long)->Unit ) {
            name.text = contact.name
            view.textViewSearchCrclr.text = contact.firstletter
            view.textViewSearchCrclr.setRandomBackgroundCircle()

            Log.d("__ViewHolder", "bind:")
            view.setOnClickListener{
                onContactItemClickListener(1L)
            }
        }


    }

}