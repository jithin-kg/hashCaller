package com.nibble.hashcaller.view.ui.sms.util

import android.R
import android.content.Context
import android.os.Build
import android.util.SparseBooleanArray
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.appcompat.view.ActionMode
import androidx.core.view.MenuItemCompat
import com.nibble.hashcaller.view.ui.MainActivity
import com.nibble.hashcaller.view.ui.sms.list.SMSListAdapter


class PrimaryActionModeCallback : ActionMode.Callback {

    var onActionItemClickListener: OnActionItemClickListener? = null

    private var mode: ActionMode? = null
    @MenuRes
    private var menuResId: Int = 0
    private var title: String? = null
    private var subtitle: String? = null

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        this.mode = mode
        mode.menuInflater.inflate(menuResId, menu)
        mode.title = title
        mode.subtitle = subtitle
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        this.mode = null
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        onActionItemClickListener?.onActionItemClick(item)
        mode.finish()
        return true
    }

    fun startActionMode(view: View,
                        @MenuRes menuResId: Int,
                        title: String? = null,
                        subtitle: String? = null) {
        this.menuResId = menuResId
        this.title = title
        this.subtitle = subtitle
//        view.startActionMode(this)
    }

    fun finishActionMode() {
        mode?.finish()
    }
}
interface OnActionItemClickListener {
    fun onActionItemClick(item: MenuItem)
}