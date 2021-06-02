package com.nibble.hashcaller.view.ui.call.dialer

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ContactSearchResultItemBinding
import com.nibble.hashcaller.stubs.Contact
import com.nibble.hashcaller.view.ui.contacts.utils.loadImage
import com.nibble.hashcaller.view.ui.extensions.setRandomBackgroundCircle
import com.nibble.hashcaller.view.ui.sms.individual.util.TYPE_CLICK
import com.nibble.hashcaller.view.ui.sms.individual.util.TYPE_MAKE_CALL
import com.nibble.hashcaller.work.formatPhoneNumber
import java.util.*

class DialerAdapter(private val context: Context, private val onContactItemClickListener: (binding: ContactSearchResultItemBinding, contactItem: Contact, clickType:Int)->Unit) :
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
        contacts = newContactList
        this.submitList(newContactList)
    }

    fun setQuery(queryPhone: String) {
        searchQueryPhone = queryPhone
    }

    class ViewHolder(private val binding: ContactSearchResultItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private val name = binding.textVContactName
        private val circle = binding.textViewcontactCrclr;
//        private val image = view.findViewById<ImageView>(R.id.contact_image)

        fun bind(
            contact: Contact, context: Context,
            onContactItemClickListener: (binding: ContactSearchResultItemBinding, contactItem: Contact, clickType:Int) -> Unit
        ) {
            name.text = contact.firstName

//            binding.tvNumber.text = contact.phoneNumber
            setSpanForNameSearch(contact, context)
//           if(contact.photoThumnail !=null){
//               view.imgViewCntct.setImageURI(Uri.parse(contact.photoThumnail))
//           }
            //        Log.i(TAG, String.valueOf(no));
            if(!contact.photoThumnailServer.isNullOrEmpty()){
                binding.textViewcontactCrclr.visibility = View.INVISIBLE
                binding.imgViewCntct.visibility = View.VISIBLE
                binding.contactCard.visibility = View.VISIBLE
                loadImage(context, binding.imgViewCntct, contact.photoThumnailServer)


            }else{
                binding.imgViewCntct.setImageURI(Uri.parse(""))
                binding.imgViewCntct.visibility = View.INVISIBLE
                binding.textViewcontactCrclr.visibility = View.VISIBLE
                binding.contactCard.visibility = View.INVISIBLE
                setNameFirstChar(contact)
                generateCircleView(contact, context);
            }

            val pNo = contact.phoneNumber
            Log.d(TAG, "phone num $pNo ")
//            Glide.with(context).load(R.drawable.ic_account_circle_24px).into(image)

            binding.imgBtnCall.setOnClickListener {
                onContactItemClickListener(binding, contact, TYPE_MAKE_CALL)
            }

            binding.root.setOnClickListener{

                onContactItemClickListener(binding, contact, TYPE_CLICK)
            }
        }

        private fun setSpanForNameSearch(contact: Contact, context: Context) {
            var firstChar = ""
            if(contact.spanEndPosName!=0 && contact.firstName!=null ){
                binding .textVContactName.text = getSpannedString(contact.firstName!!, contact.spanStartPosName, contact.spanEndPosName, context)
                firstChar = contact.firstName!![0].toString().toUpperCase()
            }else{
                binding.textVContactName.text = contact.firstName
            }
            if(contact.phoneNumber!=null && contact.spanEndPosNum !=0){

                binding .tvNumber.text = getSpannedString(
                    contact.phoneNumber!!,
                    contact.spanStartPosNum,
                    contact.spanEndPosNum,
                    context
                )
                firstChar = formatPhoneNumber(contact.phoneNumber!!).replace("+","")[0].toString().toUpperCase()
            }else{
                binding.tvNumber.text = contact.phoneNumber
            }
//            else{
////                name.text = contact.phoneNumber
////                firstChar = formatPhoneNumber(contact.phoneNumber!!).replace("+","")[0].toString().toUpperCase()
//
//            }
//            if(firstChar.isNullOrEmpty()){
//                setNameFirstChar("+")
//            }else{
//                setNameFirstChar(firstChar)
//            }
        }

        private fun getSpannedString(str: String, startPos: Int, endPos: Int, context: Context): SpannableStringBuilder {
            val yellow =
                ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorPrimary))
            val span =  SpannableStringBuilder(str)
            if(startPos in 0 until endPos){
                span.setSpan(
                    yellow,
                    startPos,
                    endPos,
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE
                )
                span.setSpan(
                    StyleSpan(Typeface.BOLD), startPos,
                    endPos,
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE )
            }

            return span
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
