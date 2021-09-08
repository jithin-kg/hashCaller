package com.hashcaller.app.view.ui.auth.permissionrequest.permissionitem

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.firebase.components.Dependency.optional
import com.hashcaller.app.R
import com.hashcaller.app.databinding.PermissionItemBinding
import com.hashcaller.app.view.ui.sms.individual.util.beGone

class PermissionItemView(
    context: Context,
    @DrawableRes icon: Int,
    title: String,
    description: String,
    isMandatory: Boolean,
    onEnableClicked: () -> Unit,
) : FrameLayout(context) {
    val binding: PermissionItemBinding

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = PermissionItemBinding.inflate(inflater, this@PermissionItemView, true)
        binding.title.text = title
        binding.description.text = description
        binding.icon.setImageResource(icon)
        binding.enableButton.setOnClickListener { onEnableClicked() }
        binding.permissionType.text = if (isMandatory) context.getString(R.string.mandatory) else context.getString(R.string.optional)
    }

    fun updateStatusSuccess() {
        binding.enableButton.beGone()
        setImageOnPermissionChange()
    }

    private fun setImageOnPermissionChange() = with(binding) {
        iconBackground.background =
            ContextCompat.getDrawable(
                this@PermissionItemView.context,
                R.drawable.contact_circular_background_green
            )
        icon.setImageDrawable(
            ContextCompat.getDrawable(
                this@PermissionItemView.context,
                R.drawable.avd_done
            )
        )

        val drawable: Drawable = icon.drawable

        if (drawable is AnimatedVectorDrawableCompat) {
            (drawable as AnimatedVectorDrawableCompat).start()
        } else if (drawable is AnimatedVectorDrawable) {
            (drawable as AnimatedVectorDrawable).start()
        }


    }

}

