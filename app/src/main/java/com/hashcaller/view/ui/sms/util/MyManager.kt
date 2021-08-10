package com.hashcaller.view.ui.sms.util

import com.hashcaller.local.db.blocklist.SpamListDAO

class MyManager private constructor(private val spamListDAO: SpamListDAO?) {

    fun doSomething() {

    }

    companion object : SingletonHolder<MyManager, SpamListDAO>(::MyManager){

    }
}