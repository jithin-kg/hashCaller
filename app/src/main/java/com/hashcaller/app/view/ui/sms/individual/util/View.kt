package com.hashcaller.app.view.ui.sms.individual.util

import android.transition.ChangeBounds
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible


fun View.slideVisibilityToBottom(visibility: Boolean, durationTime: Long = 300) {
    val transition = Slide(Gravity.TOP)
    transition.apply {
        duration = durationTime
        addTarget(this@slideVisibilityToBottom)
    }
    TransitionManager.beginDelayedTransition(this.parent as ViewGroup, transition)
    this.isVisible = visibility
}
fun View.slideVisibilityToTop(visibility: Boolean, durationTime: Long = 300) {
    val transition = Slide(Gravity.BOTTOM)
//    val tr = ChangeBounds()
    transition.apply {
        duration = durationTime
        addTarget(this@slideVisibilityToTop)
    }
    TransitionManager.beginDelayedTransition(this.parent as ViewGroup, transition)
    this.isVisible = visibility
}
fun View.beVisible() {
    visibility = View.VISIBLE
}

fun View.beInvisible(){
    visibility = View.INVISIBLE
}

fun View.beGone(){
    visibility = View.GONE
}