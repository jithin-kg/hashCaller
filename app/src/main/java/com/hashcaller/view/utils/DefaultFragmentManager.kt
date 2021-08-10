package com.hashcaller.view.utils

import com.hashcaller.R

class DefaultFragmentManager {

    companion object{
//        var id:Int = R.id.bottombaritem_contacts
        var id:Int = R.id.bottombaritem_calls

        const val SHOW_MESSAGES_FRAGMENT = 1
        const val SHOW_CONTACT_FRAGMENT = 2
        const val SHOW_CALL_FRAGMENT = 0
        const val SHOW_FULL_FRAGMENT = 5
        const val SHOW_BLOCK_FRAGMENT = 3
        const val SHOW_DIALER_FRAGMENT = 4

        /**
         * When the main Activity loads we show the call fragment as the default
         * fragment and we change it for the purpose of showing respective fragment
         * when we come from notification intent
         */
        var defaultFragmentToShow = SHOW_CALL_FRAGMENT


    }
}