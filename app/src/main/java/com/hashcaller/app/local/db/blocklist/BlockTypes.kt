package com.hashcaller.app.local.db.blocklist

import androidx.annotation.Keep

@Keep
class BlockTypes {
    companion object{
        const val BLOCK_TYPE_STARTS_WITH = 0
        const val BLOCK_TYPE_STARTS_CONTAINS = 1
        const val BLOCK_TYPE_STARTS_ENDS_WITH = 2
        const val BLOCK_TYPE_EXACT_NUMBER = 3
    }
}