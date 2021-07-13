package com.nibble.hashcaller.view.ui

import android.content.Context
import android.webkit.JavascriptInterface

class MyJavaScriptInterface(private val ctx: Context) {
    @JavascriptInterface
    fun showHTML(html: String?) {
        println(html)
    }

}