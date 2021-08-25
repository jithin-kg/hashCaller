package com.hashcaller.app.view.ui.sms.util

import com.hashcaller.app.local.db.blocklist.SpamListDAO

class MyManager private constructor(private val spamListDAO: SpamListDAO?) {

    fun doSomething() {

    }

    companion object : SingletonHolder<MyManager, SpamListDAO>(::MyManager){

    }
}