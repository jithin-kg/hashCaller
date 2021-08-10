//package com.hashcaller.view.ui.BlockConfig
//
//import android.os.Bundle
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import com.hashcaller.R
//import kotlinx.android.extensions.LayoutContainer
//import kotlinx.android.synthetic.main.fragment_blank.*
//
//// TODO: Rename parameter arguments, choose names that match
//// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"
//
///**
// * A simple [Fragment] subclass.
// * Use the [BlankFragment.newInstance] factory method to
// * create an instance of this fragment.
// */
//class BlankFragment : Fragment() {
//    private lateinit var adapter: MyQuoteAdapter
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_blank, container, false)
//    }
//
//    companion object {
//
//        fun newInstance(param1: String, param2: String) =
//            BlankFragment().apply {
//                arguments = Bundle().apply {
////                    putString(ARG_PARAM1, param1)
////                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//
//        val items = listOf(
//            Quote("Premature optimization is the root of all evil", null),
//            Quote("Any sufficiently advanced technology is indistinguishable from magic.", "Arthur C. Clarke"),
//            Quote("Content 01", "Source"),
//            Quote("Content 02", "Source"),
//            Quote("Content 03", "Source"),
//            Quote("Content 04", "Source"),
//            Quote("Content 05", "Source")
//        )
//
//        adapter = MyQuoteAdapter()
//        adapter.replaceItems(items)
//        list.adapter = adapter
//    }
//
//    class MyQuoteAdapter : RecyclerView.Adapter<MyQuoteAdapter.ViewHolder>() {
//        private var items = listOf<Quote>()
//
//
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//            val view = LayoutInflater.from(parent.context)
//                .inflate(R.layout.myquote_list_item, parent, false)
//            return ViewHolder(view)
//        }
//
//        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//            val item = items[position]
//
//            holder.contentTextView.text = item.content
//            holder.sourceTextView.text = item.source
//        }
//
//        fun replaceItems(items: List<Quote>) {
//            this.items = items
//            notifyDataSetChanged()
//        }
//
//        override fun getItemCount(): Int = items.size
//
//        inner class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
//            LayoutContainer
//    }
//
//}