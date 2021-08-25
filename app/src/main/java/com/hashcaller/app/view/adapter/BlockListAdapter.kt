package com.hashcaller.app.view.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hashcaller.app.R
import com.hashcaller.app.local.db.blocklist.BlockedListPattern

/**
 * Created by Jithin KG on 03,July,2020
 */
class BlockListAdapter(blockedListPatterns: List<BlockedListPattern>) : RecyclerView.Adapter<BlockListAdapter.BlockListViewHolder>() {

    private var blockedListPattern = blockedListPatterns //Cached copy of blockedListPattern




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockListViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                        .inflate(R.layout.block_pattern_list, parent, false)
        return BlockListViewHolder(itemView)
    }

    override fun getItemCount()= blockedListPattern.size


    override fun onBindViewHolder(holder: BlockListViewHolder, position: Int) {
        when(holder){
            is BlockListViewHolder ->{
                var item = blockedListPattern[position].numberPattern.toString()
//                holder.bind(item)
            }
        }

//        val currentPattern = blockedListPattern[position]
//        holder.textView.text = currentPattern.numberPattern
    }
    internal fun setBlockedListPatterns(blockedListPatterns: List<BlockedListPattern>) {
        this.blockedListPattern = blockedListPattern

        notifyDataSetChanged()
    }
    inner class BlockListViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){

//        val textView: TextView = itemView.textViewBlockedPattern
//        fun bind(pattern:String){
//            textView.setText(pattern)
//        }

    }



}