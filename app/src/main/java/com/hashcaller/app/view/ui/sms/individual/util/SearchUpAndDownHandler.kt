package com.hashcaller.app.view.ui.sms.individual.util

import androidx.recyclerview.widget.LinearLayoutManager
import java.util.*

/**
 * Helper to handle up and down of items in search results,
 */
object SearchUpAndDownHandler {
    const val STACK_ONE = 1
    const val STACK_TWO = 2

    private var stack1:Stack<Int> = Stack() // holds all position
    private var stack2:Stack<Int> = Stack() // holds items removed from stack1

    fun clearStacks(){
        stack1.clear()
        stack2.clear()
    }


    /**
     * function called from viewmodel to add all search result to stack
     */
    fun addToStackOne(item:Int){
        stack1.push(item)
    }

    fun getScrollUpPosition(): Int? {
        var top:Int? = null
        if(stack1.isNotEmpty()){
            top = stack1.peek()

        }
        var item:Int? = null
        if(top!=null){
             item =  stack1.pop()
            stack2.push(item)
        }else{
            if(stack2.isNotEmpty()){
                item =  stack2.pop()
                if(item!=null){
                    stack1.push(item)
                }
            }

        }
        return item
    }

    fun scrollUp(layoutMngr: LinearLayoutManager) {

        val position = getScrollUpPosition() //calling pop
        if(position!=null){
            layoutMngr.scrollToPositionWithOffset(position,calculateMiddle(layoutMngr))
        }
    }

    fun getScrollDownPosition():Int?{
        var item:Int? = null
        var top:Int? = null
        if(stack2.isNotEmpty()){
            top = stack2.peek()
        }
        if(top !=null){
            item = stack2.pop()
            stack1.push(item)
        }
        return item

    }

    fun scrollDown(layoutMngr: LinearLayoutManager){
        val position = getScrollDownPosition() //calling pop
        if(position!=null){
            layoutMngr.scrollToPositionWithOffset(position,calculateMiddle(layoutMngr))
        }
    }

    /**
     * function to calculate middle of screen in recyclerview, set offset while scrolling
     */
    private fun calculateMiddle(layoutMngr: LinearLayoutManager): Int {
        val firstVisibleItemPosition = layoutMngr.findFirstVisibleItemPosition()
        val lastVisibleItemPosition = layoutMngr.findLastVisibleItemPosition()
        val middle = (firstVisibleItemPosition + lastVisibleItemPosition) / 2
        return middle
    }



}