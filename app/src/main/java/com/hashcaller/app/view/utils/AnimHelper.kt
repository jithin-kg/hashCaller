package com.hashcaller.app.view.utils

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.view.View
import android.view.animation.DecelerateInterpolator

fun View.showAnim(delay: Long = 500) {

    //start animation only if the view is hidden.
    if (this.visibility == View.GONE) {

        this.scaleX = 0f
        this.scaleY = 0f
        this.visibility = View.VISIBLE

        val scaleX = ObjectAnimator.ofFloat(this, "scaleX", 0.8f, 1.07f, 1.0f)
        scaleX.duration = delay
        scaleX.interpolator = DecelerateInterpolator(2f)

        val scaleY = ObjectAnimator.ofFloat(this, "scaleY", 0.8f, 1.07f, 1.0f)
        scaleY.duration = delay
        scaleY.interpolator = DecelerateInterpolator(1f)

        val alpha = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f)
        alpha.duration = delay
        alpha.interpolator = DecelerateInterpolator(1f)

        val set = AnimatorSet()
        set.playTogether(scaleX, scaleY, alpha)
        set.start()
        set.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {}
            override fun onAnimationEnd(p0: Animator?) {
            }

            override fun onAnimationCancel(p0: Animator?) {}
            override fun onAnimationStart(p0: Animator?) {}
        })
    }
}

fun View.hideAnim() {

    if (this.visibility == View.VISIBLE) {

        val scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1.0f, 1.06f, 0.7f)
        scaleX.duration = 300
        scaleX.interpolator = DecelerateInterpolator(3f)

        val scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1.0f, 1.06f, 0.7f)
        scaleY.duration = 300
        scaleY.interpolator = DecelerateInterpolator(1f)

        val alpha = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f)
        alpha.duration = 300
        alpha.interpolator = DecelerateInterpolator(1f)

        val set = AnimatorSet()
        set.playTogether(scaleX, scaleY, alpha)
        set.start()
        set.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {}
            override fun onAnimationEnd(p0: Animator?) {
                this@hideAnim.visibility = View.GONE
            }

            override fun onAnimationCancel(p0: Animator?) {}
            override fun onAnimationStart(p0: Animator?) {}
        })
    }
}

fun View.bottomToTopAnim(delay: Long = 500, distance: Float = 80f): AnimatorSet {

    val translateY = ObjectAnimator.ofFloat(this, "translationY", distance, 0f)
    translateY.duration = delay
    translateY.interpolator = DecelerateInterpolator(2f)

    val scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1.01f, 1.0f)
    scaleY.duration = delay
    scaleY.interpolator = DecelerateInterpolator(1f)

    val alpha = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f)
    alpha.duration = delay
    alpha.interpolator = DecelerateInterpolator(1f)

    val set = AnimatorSet()
    set.playTogether(translateY, scaleY, alpha)
    set.start()

    return set
}

fun View.topToBottomAnim(delay: Long = 500, distance: Float = 80f): AnimatorSet {

    val translateY = ObjectAnimator.ofFloat(this, "translationY", -distance, 0f)
    translateY.duration = delay
    translateY.interpolator = DecelerateInterpolator(2f)

    val scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1.01f, 1.0f)
    scaleY.duration = delay
    scaleY.interpolator = DecelerateInterpolator(1f)

    val alpha = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f)
    alpha.duration = delay
    alpha.interpolator = DecelerateInterpolator(1f)

    val set = AnimatorSet()
    set.playTogether(translateY, scaleY, alpha)
    set.start()

    return set
}

fun View.scaleInAnim(delay: Long = 500): AnimatorSet {

    val scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1.04f, 1.0f)
    scaleX.duration = delay
    scaleX.interpolator = DecelerateInterpolator(1f)

    val scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1.04f, 1.0f)
    scaleY.duration = delay
    scaleY.interpolator = DecelerateInterpolator(1f)

    val alpha = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f)
    alpha.duration = delay
    alpha.interpolator = DecelerateInterpolator(1f)

    val set = AnimatorSet()
    set.playTogether(scaleX, scaleY, alpha)
    set.start()

    return set
}

fun View.scaleOutAnim(delay: Long = 500): AnimatorSet {

    val scalex = this.scaleX
    val scaley = this.scaleY
    val scaleX = ObjectAnimator.ofFloat(this, "scaleX", 0.7f,0.98f, scalex)
    scaleX.duration = delay
    scaleX.interpolator = DecelerateInterpolator(1f)

    val scaleY = ObjectAnimator.ofFloat(this, "scaleY", 0.7f,0.98f, scaley)
    scaleY.duration = delay
    scaleY.interpolator = DecelerateInterpolator(1f)

    val alpha = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f)
    alpha.duration = delay
    alpha.interpolator = DecelerateInterpolator(1f)

    val set = AnimatorSet()
    set.playTogether(scaleX, scaleY, alpha)
    set.start()

    return set
}

fun View.fadeInAnim(delay: Long = 500): AnimatorSet {

    val alpha = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f)
    alpha.duration = delay

    val set = AnimatorSet()
    set.playTogether(alpha)
    set.start()

    return set
}

fun View.animateColor(delay: Long = 500, startColor: Int = Color.WHITE, endColor:Int): Animator {

    val color = ObjectAnimator.ofArgb(this, "backgroundColor", startColor, endColor)
    color.duration = delay

    color.start()

    return color
}

fun View.slideInLeft(distance: Float = 100f, delay: Long = 500) {

    val translateX = ObjectAnimator.ofFloat(this, "translationX", -distance, 0f)
    translateX.duration = delay
    translateX.interpolator = DecelerateInterpolator(2f)


    val alpha = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f)
    alpha.duration = 500
    alpha.interpolator = DecelerateInterpolator(1f)

    val set = AnimatorSet()
    set.playTogether(alpha, translateX)
    set.start()


}

fun View.slideInRight(distance: Float = 100f, delay: Long = 500) {

    val translateX = ObjectAnimator.ofFloat(this, "translationX", distance, 0f)
    translateX.duration = delay
    translateX.interpolator = DecelerateInterpolator(2f)


    val alpha = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f)
    alpha.duration = 500
    alpha.interpolator = DecelerateInterpolator(1f)

    val set = AnimatorSet()
    set.playTogether(alpha, translateX)
    set.start()


}

fun View.slideInBottom(distance: Float = 100f, delay: Long = 500) {

    val translateY = ObjectAnimator.ofFloat(this, "translationY", distance, 0f)
    translateY.duration = delay
    translateY.interpolator = DecelerateInterpolator(2f)


    val alpha = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f)
    alpha.duration = 500
    alpha.interpolator = DecelerateInterpolator(1f)

    val set = AnimatorSet()
    set.playTogether(alpha, translateY)
    set.start()


}


fun View.slideBelowHide(delay: Long = 500, distance: Float = 100f): AnimatorSet {

    val translateY = ObjectAnimator.ofFloat(this, "translationY", 0f, distance)
    translateY.duration = delay
    translateY.interpolator = DecelerateInterpolator(2f)


    val alpha = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f)
    alpha.duration = delay
    alpha.interpolator = DecelerateInterpolator(1f)

    val set = AnimatorSet()
    set.playTogether(translateY, alpha)
    set.start()

    set.addListener(object : Animator.AnimatorListener {
        override fun onAnimationRepeat(p0: Animator?) {}
        override fun onAnimationEnd(p0: Animator?) {
            this@slideBelowHide.visibility = View.GONE
        }

        override fun onAnimationCancel(p0: Animator?) {}
        override fun onAnimationStart(p0: Animator?) {}
    })

    return set
}