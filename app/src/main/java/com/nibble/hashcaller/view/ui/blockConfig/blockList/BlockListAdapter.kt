package com.nibble.hashcaller.view.ui.blockConfig.blockList



import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nibble.hashcaller.R
import com.nibble.hashcaller.local.db.dao.BlockedListPattern
import kotlinx.android.synthetic.main.block_pattern_list.view.*
import kotlin.collections.ArrayList


class BlockListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{

    private val TAG: String = "__BlogRecyclerAdapter"

    private var items: List<BlockedListPattern> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return BlockListViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.block_pattern_list, parent, false)
        )
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

    fun submitList(blogList: List<BlockedListPattern>){
        items = blogList
        Log.d(TAG, "submitList: " + blogList.size)
        notifyDataSetChanged()
    }

    class BlockListViewHolder constructor(
        itemView: View
    ): RecyclerView.ViewHolder(itemView){

//        val blog_image = itemView.blog_image
        val block_title = itemView.textViewBlockedPattern
//        val blog_author = itemView.blog_author

        fun bind(blogPost: BlockedListPattern){

//            val requestOptions = RequestOptions()
//                .placeholder(R.drawable.ic_launcher_background)
//                .error(R.drawable.ic_launcher_background)
//
//            Glide.with(itemView.context)
//                .applyDefaultRequestOptions(requestOptions)
//                .load(blogPost.image)
//                .into(blog_image)
            block_title.setText(blogPost.numberPattern)
//            blog_author.setText(blogPost.username)

        }

    }

}
