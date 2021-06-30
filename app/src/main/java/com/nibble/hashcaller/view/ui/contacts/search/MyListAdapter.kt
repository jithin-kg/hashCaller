package com.nibble.hashcaller.view.ui.contacts.search

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.repository.contacts.PhoneNumWithHashedNumDTO
import kotlinx.android.synthetic.main.list_item.view.*


/**
 * Created by Jithin KG on 01,August,2020
 */
class MyListAdapter:
    RecyclerView.Adapter<MyListAdapter.ViewHolder?>() {
    private var listdata = emptyList<PhoneNumWithHashedNumDTO>()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem: View = LayoutInflater.from(parent.context).inflate(com.nibble.hashcaller.R.layout.list_item, parent, false)
        return ViewHolder(listItem)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
//        val myListData = listdata[position]
//        holder.textView.text = listdata[position].name
        holder.textView.text = listdata[position].name
        Log.d("__MYListAdapter", "onBindViewHolder: ${listdata[position]}")
//        holder.imageView.setImageResource(listdata[position].getImgId())
//        holder.relativeLayout.setOnClickListener(object : View.OnClickListener() {
//            fun onClick(view: View) {
//                Toast.makeText(
//                    view.getContext(),
//                    "click on item: " + myListData.getDescription(),
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//        })
    }

    override fun getItemCount(): Int {
        return listdata.size
    }
    fun setContactList(newContactList: List<PhoneNumWithHashedNumDTO>) {
        listdata = emptyList()
        listdata = newContactList.toList()
        Log.d("__MYListAdapter", "setContactList: ${newContactList.size}")

        notifyDataSetChanged()
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var textView: TextView
//        var relativeLayout: RelativeLayout

        init {

            textView = itemView.textViewL
//            relativeLayout = itemView.findViewById(R.id.relativeLayout)
        }
    }

    // RecyclerView recyclerView;
    init {
        this.listdata = listdata.toList()
    }
}
