package com.hashcaller.app.view.ui.blockConfig.blockList



import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hashcaller.app.databinding.CustomBlockedItemBinding
import com.hashcaller.app.databinding.ItemOverlayPermissionBinding
import com.hashcaller.app.local.db.blocklist.BlockedListPattern
import com.hashcaller.app.view.ui.blockConfig.BlockConfigFragment.Companion.LIST_DUMMY_ID
import com.hashcaller.app.view.ui.sms.individual.util.*
import kotlin.collections.ArrayList


class BlockListAdapter(
    private val onItemClickHandler: (Int) -> Unit
) : androidx.recyclerview.widget.ListAdapter<BlockedListPattern, RecyclerView.ViewHolder>(
    PatternItemDiffCallback())
{

    private val VIEW_TYPE_BLOCK_LIST = 1
    private val VIEW_TYPE_SETUP = 2
    private val TAG: String = "__BlogRecyclerAdapter"

    private var items: ArrayList<BlockedListPattern> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == VIEW_TYPE_BLOCK_LIST){
            val binding = CustomBlockedItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return BlockListViewHolder(binding, parent.context)
        }else {
            val binding = ItemOverlayPermissionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolderItemSetup(binding)
        }

    }

    inner class ViewHolderItemSetup(private val binding: ItemOverlayPermissionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(){
            binding.btnSetup.setOnClickListener {
                onItemClickHandler(TYPE_CLICK_ALLOW_OVERLAY)
                true
            }
            binding.btnDismiss.setOnClickListener {
                onItemClickHandler(TYPE_CLICK_DISMISS_OVERLAY)
                true
            }


        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder.itemViewType) {

             VIEW_TYPE_BLOCK_LIST -> {
                 (holder as BlockListViewHolder).bind(items[position])
            }
            VIEW_TYPE_SETUP -> {
                ( holder as  ViewHolderItemSetup).bind()
            }



        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
    override fun getItemViewType(position: Int): Int {
        if (items.isNotEmpty()){
            if(items[position].id == LIST_DUMMY_ID && position == 0){
                return VIEW_TYPE_SETUP
            }
        }
        return VIEW_TYPE_BLOCK_LIST

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

    fun removePermissionItemFromView() {
        if(items.isNotEmpty()){
            if(items[0].id == LIST_DUMMY_ID){
                items.removeAt(0)
                notifyItemRemoved(0)
                notifyItemRangeChanged(0, items.size)
            }
        }
    }

    fun addPermissionItemToList() {
        if(items.isEmpty()){
            items.add(0, BlockedListPattern(id=LIST_DUMMY_ID,"", "", 0))
        }else {
            if(items[0].id!= LIST_DUMMY_ID){
                items.add(0, BlockedListPattern(id=LIST_DUMMY_ID,"", "", 0))
            }
        }
        notifyItemInserted(0)
        notifyItemRangeChanged(0, items.size)
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
