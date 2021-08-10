package com.hashcaller.utils

import android.database.Cursor

fun Cursor.getStringValue(key: String) = getString(getColumnIndex(key))