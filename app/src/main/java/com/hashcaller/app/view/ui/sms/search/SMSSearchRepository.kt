package com.hashcaller.app.view.ui.sms.search

import com.hashcaller.app.local.db.sms.search.ISmsQueriesDAO
import com.hashcaller.app.local.db.sms.search.SmsSearchQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SMSSearchRepository(private val smsQueriesDAO: ISmsQueriesDAO) {
    suspend fun insertSearchQueryToDB(queryText: String) {
            smsQueriesDAO.insert(SmsSearchQueries(queryText))
    }

    suspend fun getAllSearchHistory(): List<SmsSearchQueries> = withContext(Dispatchers.IO) {
        return@withContext smsQueriesDAO.getAll()
    }

}