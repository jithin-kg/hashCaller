package com.hashcaller.app.view.ui.sms.search

import com.hashcaller.app.view.ui.sms.util.SMS

/**
 * class to check whether the current search query in view and
 * the result for that search query is same, because old search
 * that is pending are sometimes displayed in view
 */
data class SearchResultAndQueryTerm(
     val searchResult:MutableList<SMS>,
     val searchTerm:String
    )
