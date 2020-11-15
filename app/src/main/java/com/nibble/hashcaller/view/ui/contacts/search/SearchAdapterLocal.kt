package com.nibble.hashcaller.view.ui.contacts.search

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.R
import com.nibble.hashcaller.local.db.contactInformation.ContactTable
import kotlinx.android.synthetic.main.search_result_layout.view.*
import java.util.*

class SearchAdapterLocal (private val context: Context, private val onContactItemClickListener: (id:Long)->Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private  var contacts: List<ContactTable>? = null
    companion object{
        private val TAG  = "__SearchAdapter"

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.search_result_layout, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
//        Log.d(TAG, "getItemCount: ${contacts?.size}")
        return contacts?.size?:0
    }
    fun setContactList(it: List<ContactTable>) {
        contacts = emptyList()
        contacts = it

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
//    fun setSearchResult(newContactList: List<Contact>) {
////        contacts = newContactList
//
//        notifyDataSetChanged()
//    }
    class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

            private val name = view.textViewSearchContactName
            private val circle = view.textViewSearchCrclr;
            private val location = view.textViewSearchLocation
            private val country = view.textViewSearchCountry
//        private val image = view.findViewById<ImageView>(R.id.contact_image)

        fun bind(contact: ContactTable, context: Context, onContactItemClickListener :(id:Long)->Unit ) {
            name.text = contact.name
            location.text = contact.location
            country.text = contact.country
            Log.d("__ViewHolder", "contact from table $contact ")

            generateCircleView(context)
            setNameFirstChar(contact)

            Log.d("__ViewHolder", "bind:")
//            name.text = contact.name
//            Glide.with(context).load(R.drawable.ic_account_circle_24px).into(image)
            view.setOnClickListener{
                onContactItemClickListener(1L)
            }
        }
    private fun setNameFirstChar(contact: ContactTable) {
        val name: String = contact.name
        val firstLetter = name[0]
        val firstLetterString = firstLetter.toString().toUpperCase()
        circle.text = firstLetterString
    }
    private fun generateCircleView(context: Context) {
        val rand = Random()
        when (rand.nextInt(5 - 1) + 1) {
            1 -> {
                circle.background = ContextCompat.getDrawable(context, R.drawable.contact_circular_background)
                circle.setTextColor(
                    ContextCompat.getColor(context, R.color.colorPrimary)
                )
            }
            2 -> {
                circle.background = ContextCompat.getDrawable(context, R.drawable.contact_circular_background2)
                circle.setTextColor(
                    ContextCompat.getColor(context, R.color.colorlightBlueviking)
                )
            }
            3 -> {
                circle.background = ContextCompat.getDrawable(context, R.drawable.contact_circular_background3)
                circle.setTextColor(
                    ContextCompat.getColor(context, R.color.colorbrightTurquoiseLightBlue
                    )
                )
            }
            else -> {
                circle.background = ContextCompat.getDrawable(context, R.drawable.contact_circular_background4)
                circle.setTextColor(
                    ContextCompat.getColor(context, R.color.colorPrimaryDark)
                )
            }
        }
    }

    }

}