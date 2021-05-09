package com.nibble.hashcaller.view.ui.IncommingCall

import android.content.Context
import android.view.ViewGroup
import android.widget.PopupWindow

class SampleLayoutChild(private val ct:Context): PopupWindow() {

}

abstract class Sample(private val ct:Context) : ViewGroup(ct){
}

abstract class LyoutFinal(private val cnt:Context) : Sample(cnt){

}
