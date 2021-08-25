package com.hashcaller.app.view.ui

import android.app.Activity
import android.os.Build
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.annotation.RequiresApi

class MyWebViewClient internal constructor(private val activity: Activity) : WebViewClient() {

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
//        val url: String = request?.url.toString();
//        view?.loadUrl(url)
        return false
    }

    override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
        webView.loadUrl(url)

        return true
    }
    override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
        Toast.makeText(activity, "Got Error! $error", Toast.LENGTH_SHORT).show()
    }
}