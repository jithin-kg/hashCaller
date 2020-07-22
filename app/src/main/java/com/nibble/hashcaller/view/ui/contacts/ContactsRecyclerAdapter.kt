package com.nibble.hashcaller.view.ui.contacts

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.R
import com.nibble.hashcaller.data.local.db.BlockedListPattern
import com.nibble.hashcaller.data.stubs.Contact
import kotlinx.android.synthetic.main.block_pattern_list.view.*
import java.util.*

/**
 * Created by Jithin KG on 21,July,2020
 */
class ContactsRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>()  {
    private val TAG: String = "ContactsRecyclerAdapter"

    private var contacts: List<Contact> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ContactsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.block_pattern_list, parent, false)
        )
    }
    override fun getItemCount(): Int {
        return contacts.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when(holder){
                is ContactsViewHolder ->{
                    holder.bind(contacts[position])
                }
            }
    }
    fun setContacts(contactsList: List<Contact>){
        contacts = contactsList
        Log.d(TAG, "submitList: " + contactsList.size)
        notifyDataSetChanged()
    }

    class ContactsViewHolder constructor(
        itemView: View
    ): RecyclerView.ViewHolder(itemView){

        //        val blog_image = itemView.blog_image
        val blog_title = itemView.textViewBlockedPattern
//        val blog_author = itemView.blog_author

        fun bind(contact: Contact){

//            val requestOptions = RequestOptions()
//                .placeholder(R.drawable.ic_launcher_background)
//                .error(R.drawable.ic_launcher_background)
//
//            Glide.with(itemView.context)
//                .applyDefaultRequestOptions(requestOptions)
//                .load(blogPost.image)
//                .into(blog_image)
//            blog_title.setText(contact.numberPattern)
//            blog_author.setText(blogPost.username)

        }

    }


}