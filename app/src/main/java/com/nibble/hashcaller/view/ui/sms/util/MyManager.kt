package com.nibble.hashcaller.view.ui.sms.util

import android.content.Context
import com.nibble.hashcaller.local.db.blocklist.SpamListDAO

class MyManager private constructor(private val spamListDAO: SpamListDAO?) {

    fun doSomething() {

    }

    companion object : SingletonHolder<MyManager, SpamListDAO>(::MyManager){

    }
}