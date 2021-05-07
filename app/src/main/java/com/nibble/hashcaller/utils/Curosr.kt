package com.nibble.hashcaller.utils

import android.database.Cursor

fun Cursor.getStringValue(key: String) = getString(getColumnIndex(key))
