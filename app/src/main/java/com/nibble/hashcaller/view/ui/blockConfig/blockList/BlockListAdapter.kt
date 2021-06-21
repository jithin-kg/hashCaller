package com.nibble.hashcaller.view.ui.blockConfig.blockList



import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.databinding.CustomBlockedItemBinding
import com.nibble.hashcaller.local.db.blocklist.BlockedListPattern
import com.nibble.hashcaller.view.ui.sms.individual.util.NUMBER_CONTAINING
import com.nibble.hashcaller.view.ui.sms.individual.util.NUMBER_ENDS_WITH
import com.nibble.hashcaller.view.ui.sms.individual.util.NUMBER_STARTS_WITH
import com.nibble.hashcaller.view.ui.sms.individual.util.toast
import kotlin.collections.ArrayList


class BlockListAdapter : androidx.recyclerview.widget.ListAdapter<BlockedListPattern, RecyclerView.ViewHolder>(
    PatternItemDiffCallback())
{

    private val TAG: String = "__BlogRecyclerAdapter"

    private var items: ArrayList<BlockedListPattern> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = CustomBlockedItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BlockListViewHolder(binding, parent.context)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {

            is BlockListViewHolder -> {
                holder.bind(items[position])
            }

        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun submitPatternsList(patternsList: List<BlockedListPattern>){
        items = patternsList as ArrayList<BlockedListPattern>
        this.submitList(patternsList)
    }
    fun getItemAtPosition(position: Int): BlockedListPattern {
        val item = items[position]
//        val items2 = ArrayList<Int>()
        Log.d(TAG, "position $position")
        Log.d(TAG, "size ${items.size}");
        items.removeAt(position)
        return item;
    }

    private class BlockListViewHolder(
        private val binding: CustomBlockedItemBinding,
        private val context: Context
    ): RecyclerView.ViewHolder(binding.root){

//        val blog_image = itemView.blog_image
//        val block_title = itemView.textViewBlockedPattern

//        val blog_author = itemView.blog_author
        fun bind(pattern: BlockedListPattern){
//            itemView.tvPtrn.text = "hi"
            var text = ""
            var firstletter = ""
            when(pattern.type){
                NUMBER_STARTS_WITH ->{
                    text = "Number starts with"
                    firstletter = "F"
                }
                NUMBER_CONTAINING ->{
                    text = "Number containing"
                    firstletter = "C"

                }
                NUMBER_ENDS_WITH -> {
                    text = "Number ends with"
                    firstletter = "L"
                }else -> {
                    text = "Exact number"
                    firstletter = "E"
                }

            }

            Log.d(TAG, "bind: ${pattern.numberPattern}")
            binding.tvBlkType.text = text
            binding.textViewBlkPattern.text = pattern.numberPattern
            binding.tvFirstLetterBlk.text = firstletter
            binding.root.setOnLongClickListener {
                context.toast("Please swipe left to delete")
                 true
            }

//            block_title.text = pattern.numberPattern
//            blog_author.setText(blogPost.username)

        }

    }

     class PatternItemDiffCallback : DiffUtil.ItemCallback<BlockedListPattern>() {
        override fun areItemsTheSame(oldItem: BlockedListPattern, newItem: BlockedListPattern): Boolean {

            return  oldItem.id == newItem.id


        }


        override fun areContentsTheSame(oldItem: BlockedListPattern, newItem: BlockedListPattern): Boolean {
            //when we are using data class we dont need to compare all attributes
            return oldItem == newItem

        }

    }
    companion object{
        const val TAG ="__BlockListAdapter"
    }

}
