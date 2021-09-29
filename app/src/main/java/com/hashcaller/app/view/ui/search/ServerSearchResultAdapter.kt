package com.hashcaller.app.view.ui.search

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hashcaller.app.databinding.ContactSearchResultItemBinding
import com.hashcaller.app.stubs.Contact
import com.hashcaller.app.utils.Constants
import com.hashcaller.app.view.ui.contacts.setAvatar
import com.hashcaller.app.view.ui.contacts.toggleUserBadge
import com.hashcaller.app.view.ui.contacts.toggleVerifiedBadge
import com.hashcaller.app.view.ui.contacts.utils.loadImage
import com.hashcaller.app.view.ui.extensions.setRandomBackgroundCircle
import com.hashcaller.app.view.ui.sms.individual.util.TYPE_CLICK
import com.hashcaller.app.view.ui.sms.individual.util.TYPE_MAKE_CALL
import com.hashcaller.app.work.formatPhoneNumber
import java.util.*

class ServerSearchResultAdapter(private val context: Context, private val onContactItemClickListener: (binding: ContactSearchResultItemBinding, contactItem: Contact, clickType:Int)->Unit) :
    androidx.recyclerview.widget.ListAdapter<Contact, RecyclerView.ViewHolder>(ContactItemDiffCallback()) {
    private var contacts = emptyList<Contact>()
    private var searchQueryPhone = ""

    companion object{
        private const val TAG = "__ContactAdapter";
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.contact_list, parent, false)
        val binding = ContactSearchResultItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
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

    fun setList(newContactList: List<Contact>) {
        Log.d(TAG, "setList: $newContactList")
        contacts = newContactList
        this.submitList(newContactList)
    }

    fun setQuery(queryPhone: String) {
        searchQueryPhone = queryPhone
    }

    class ViewHolder(private val binding: ContactSearchResultItemBinding) : RecyclerView.ViewHolder(binding.root) {
//        private val image = view.findViewById<ImageView>(R.id.contact_image)

        fun bind(
            contact: Contact, context: Context,
            onContactItemClickListener: (binding: ContactSearchResultItemBinding, contactItem: Contact, clickType:Int) -> Unit
        ) {
            val name = Constants.setNameInView(binding.textVContactName, contact)
            binding.tvNumber.text = contact.phoneNumber

                context.setAvatar(binding.imgViewCntct,
                binding.textViewcontactCrclr,
                contact.firstName,
                contact.lastName,
                contact.nameInPhoneBook,
                contact.photoThumnailServer,
                contact.avatarGoogle,
                name
                )
            context.toggleVerifiedBadge(binding.imgVerifiedBadge, contact.isVerifiedUser)
            context.toggleUserBadge(
                binding.imgUserIconBg,
                binding.imgUserIcon,
                contact.hUid
            )

            //only show image from server, not from content provider
            val photoThumbnailFromServer = contact.photoThumnailServer?:""
            val googleAvatar = contact.avatarGoogle?:""
//            if(photoThumbnailFromServer.isNotEmpty()){
//                loadImage(context, binding.imgViewCntct, photoThumbnailFromServer)
//            }else if(googleAvatar.isNotEmpty()){
//                Glide.with(context).load(googleAvatar)
//                    .into(binding.imgViewCntct)
//            }else {
//                generateCircleView(contact);
//
//            }
//            if(contact.photoURI)
            val pNo = contact.phoneNumber

            binding.imgBtnCall.setOnClickListener {
                onContactItemClickListener(binding, contact, TYPE_MAKE_CALL)
            }

            binding.root.setOnClickListener{

                onContactItemClickListener(binding, contact, TYPE_CLICK)
            }
        }





        private fun setNameFirstChar(contact: Contact) {
            val name: String = contact.firstName
            var firstLetter = ""
                val formatedNum = formatPhoneNumber(contact.phoneNumber)
            if(name.isNullOrEmpty() ){
                if(!formatedNum.isNullOrEmpty()){
                    firstLetter = formatedNum[0].toString()
                }
            }else{
                firstLetter = name[0].toString()
            }
            val firstLetterString = firstLetter.toString().toUpperCase()
            binding.textViewcontactCrclr.text = firstLetterString


        }

        private fun generateCircleView(contact: Contact) {

            contact.drawable = binding.textViewcontactCrclr.setRandomBackgroundCircle()

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
