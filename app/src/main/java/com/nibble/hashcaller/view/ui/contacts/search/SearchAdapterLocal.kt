package com.nibble.hashcaller.view.ui.contacts.search

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.databinding.SearchResultLayoutBinding
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.view.ui.extensions.setRandomBackgroundCircle

class SearchAdapterLocal (private val context: Context, private val onContactItemClickListener: (binding:SearchResultLayoutBinding, contact:Contact)->Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private  var contacts: List<Contact>? = null
    companion object{
        private val TAG  = "__SearchAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_result_layout, parent, false)
        val binding = SearchResultLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
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
    inner class ViewHolder(private val binding: SearchResultLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
            private val name = binding.textViewSearchContactName
            private val circle = binding.textViewSearchCrclr;
            private val location = binding.textViewSearchLocation
            private val country = binding.textViewSearchCountry
//        private val image = view.findViewById<ImageView>(R.id.contact_image)

        fun bind(contact: Contact, context: Context, onContactItemClickListener :(binding:SearchResultLayoutBinding, contact:Contact)->Unit ) {
            name.text = contact.firstName
            binding.textViewSearchCrclr.text = contact.firstletter
            binding.textViewSearchCrclr.setRandomBackgroundCircle()

            Log.d("__ViewHolder", "bind:")
            binding.root.setOnClickListener{
                onContactItemClickListener(binding, contact)
            }
        }


    }

}