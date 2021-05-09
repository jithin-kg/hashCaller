package com.nibble.hashcaller.view.ui.IncommingCall

import android.app.usage.UsageEvents
import android.content.ClipData
import android.content.ClipDescription
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.nibble.hashcaller.R
import com.nibble.hashcaller.databinding.ActivityDragabbleBinding
import com.nibble.hashcaller.view.ui.sms.individual.util.toast
import kotlinx.android.synthetic.main.activity_dragabble.*

class DragabbleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDragabbleBinding
    private var dragListener:View.OnDragListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDragabbleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.dragView.setOnLongClickListener {
            val clipText = "This is cip data text"
            val item = ClipData.Item(clipText)
            val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
            val data = ClipData(clipText, mimeTypes, item)

            val dragShadowBuilder = View.DragShadowBuilder(it)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                it.startDragAndDrop(data, dragShadowBuilder, it, 0)
                it.visibility = View.INVISIBLE
            }

            true
        }

         dragListener = View.OnDragListener { view, event ->

            when(event.action){
                DragEvent.ACTION_DRAG_STARTED ->{
                    event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
                }
                DragEvent.ACTION_DRAG_ENTERED ->{
                    //called when dragview enterd boundaries
                    view.invalidate()
                    true
                }
                DragEvent.ACTION_DRAG_LOCATION ->{
                true
                }
                DragEvent.ACTION_DRAG_EXITED ->{
                 //occurs when laout leaves our boundaries
                    view.invalidate()
                    true
                }
                DragEvent.ACTION_DROP ->{
                    val item = event.clipData.getItemAt(0)
                    val dragData = item.text
                    toast(dragData.toString())
                    view.invalidate() // removed view from initial layout
                    //remove view from previous layout to new layot
                    val v = event.localState as View // here v will be our drag view
                    val owner = v.parent as ViewGroup
                    owner.removeView(v)
                    val destination = view as LinearLayout
                    destination.addView(v)
                    v.visibility = View.VISIBLE
                    true
                }
                DragEvent.ACTION_DRAG_ENDED ->{
                    view.invalidate()
                    true
                }
                else ->{
                    false
                }
            }

        }
        llTop.setOnDragListener(dragListener)
        llbottom.setOnDragListener(dragListener)
    }
}