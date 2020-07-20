package com.nibble.hashcaller.view.ui.tabian



import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.layout_blog_list_item.view.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.nibble.hashcaller.view.ui.tabian.BlogPost
import com.nibble.hashcaller.R
import com.nibble.hashcaller.data.local.db.BlockedListPattern
import kotlinx.android.synthetic.main.block_pattern_list.view.*
import kotlin.collections.ArrayList


class BlogRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{

    private val TAG: String = "__BlogRecyclerAdapter"

    private var items: List<BlockedListPattern> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return BlogViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.block_pattern_list, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {

            is BlogViewHolder -> {
                holder.bind(items.get(position))
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

    class BlogViewHolder constructor(
        itemView: View
    ): RecyclerView.ViewHolder(itemView){

//        val blog_image = itemView.blog_image
        val blog_title = itemView.textViewBlockedPattern
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
            blog_title.setText(blogPost.numberPattern)
//            blog_author.setText(blogPost.username)

        }

    }

}
