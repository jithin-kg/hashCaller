package com.nibble.hashcaller.view.ui.contacts

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.R
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.view.ui.call.dialer.util.CallLogData
import com.nibble.hashcaller.view.ui.contacts.utils.loadImage
import com.nibble.hashcaller.view.ui.extensions.setRandomBackgroundCircle
import kotlinx.android.synthetic.main.contact_list.view.*
import java.util.*

/**
 * Created by Jithin KG on 22,July,2020
 */
class ContactAdapter(private val context: Context, private val onContactItemClickListener: (contactItem:Contact)->Unit) :
    androidx.recyclerview.widget.ListAdapter<Contact, RecyclerView.ViewHolder>(ContactItemDiffCallback()) {

    private var contacts = emptyList<Contact>()
    companion object{
        private const val TAG = "__ContactAdapter";
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_list, parent, false)

        return ViewHolder(view)
    }

//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val contact = contacts[position]
//        holder.bind(contact, context, onContactItemClickListener)
//    }
override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    val contact = contacts[position]
//    holder.bind(contact, context, onContactItemClickListener)
    when(holder) {

        is ViewHolder -> {
            holder.bind(contacts[position],context, onContactItemClickListener)
        }

    }

}

    override fun getItemCount(): Int {
//        Log.d("__ContactAdapter", "getItemCount: ${contacts.size}")
       return contacts.size
    }

    fun setContactList(newContactList: List<Contact>) {
        contacts = newContactList
        this.submitList(newContactList)
    }
     class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val name = view.textVContactName
         private val circle = view.textViewcontactCrclr;
//        private val image = view.findViewById<ImageView>(R.id.contact_image)

        fun bind(
            contact: Contact, context: Context,
            onContactItemClickListener: (contatItem: Contact) -> Unit
        ) {
            name.text = contact.name
//           if(contact.photoThumnail !=null){
//               view.imgViewCntct.setImageURI(Uri.parse(contact.photoThumnail))
//           }
            //        Log.i(TAG, String.valueOf(no));
            if(contact.photoThumnail !=null){
                view.textViewcontactCrclr.visibility = View.INVISIBLE
                view.imgViewCntct.visibility = View.VISIBLE
                view.contactCard.visibility = View.VISIBLE
                loadImage(view.context, view.imgViewCntct, contact.photoThumnail)


            }else{
                view.imgViewCntct.setImageURI(Uri.parse(""))
                view.imgViewCntct.visibility = View.INVISIBLE
                view.textViewcontactCrclr.visibility = View.VISIBLE
                view.contactCard.visibility = View.INVISIBLE
                setNameFirstChar(contact)
                generateCircleView(contact, context);
            }

            val pNo = contact.phoneNumber
            Log.d(TAG, "phone num $pNo ")
//            Glide.with(context).load(R.drawable.ic_account_circle_24px).into(image)



            view.setOnClickListener{

                onContactItemClickListener(contact)
            }
        }

         private fun setNameFirstChar(contact: Contact) {
             val name: String = contact.name!!
             val firstLetter = name[0]
             val firstLetterString = firstLetter.toString().toUpperCase()
             circle.text = firstLetterString


         }

         private fun generateCircleView(contact: Contact, context: Context) {
             val rand = Random()

             contact.drawable = circle.setRandomBackgroundCircle()

         }

     }



    class ContactItemDiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return  oldItem.id == newItem.id


        }


        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            //when we are using data class we dont need to compare all attributes
            return oldItem == newItem
            //TODO compare both messages and if the addres is same and message
        }

    }
}



