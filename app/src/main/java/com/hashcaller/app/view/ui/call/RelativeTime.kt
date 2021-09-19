package com.hashcaller.app.view.ui.call

class RelativeTime(var  relativeTime:String, var relativeDay:Int) {


    companion object {
         const val TODAY = 1
         const val YESTERDAY = 2
         const val OLDER = 3
    }
}