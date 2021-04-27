package com.nibble.hashcaller.view.ui.call.dialer

import android.util.Log
import com.nibble.hashcaller.view.ui.call.dialer.DialerViewModel.Companion.cancelJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class NumberToStringMapper {

    companion object{
//        var count = 0
        var list:MutableList<String> = mutableListOf()

        fun printStrings(
            phNo: String, i: Int,
            hm: HashMap<Char, String>,
            str: StringBuilder
        ){

            if (list.size >= 500 || cancelJob)
                return
            if (i == phNo.length) {
//                DialerViewModel.strCombinationForNum = str.toString()
//                count++
                    list.add(str.toString())
                Log.d(TAG, "$str ")
                return
            }
            val s = hm[phNo[i]]
            if (s != null) {

                for (element in s) {
                    str.append(element)
                    printStrings(phNo, i + 1, hm, str)
                    str.deleteCharAt(str.length - 1)
                }

            }
            return
        }

        /**
         * function to genrate combination of character for dialpad
         * and changes
         */
        suspend fun printStringForNumber(phNo: String?): MutableList<String> = withContext(
            Dispatchers.Default) {
            // Create a HashMap
            list.clear()

//            count = 0
            val hm = HashMap<Char, String>()

            // For every digit, store characters that can
            // be used to dial it.
            hm['2'] = "ABC"
            hm['3'] = "DEF"
            hm['4'] = "GHI"
            hm['5'] = "JKL"
            hm['6'] = "MNO"
            hm['7'] = "PQRS"
            hm['8'] = "TUV"
            hm['9'] = "WXYZ"
            hm['1'] = "1"
            hm['0'] = "0"

            // Create a string to store a particular output
            // string
            val str = java.lang.StringBuilder()

            // Call recursive function
            phNo?.let {

                printStrings(it, 0, hm, str)

            }

            return@withContext list
        }
        const val TAG = "__NumberToStringMapper"
    }

}