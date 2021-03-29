package com.nibble.hashcaller.utils.callHandlers.base.events

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Class that invokes [MutableLiveData] only once.
 *
 * @author Zoran Sasko
 * @version 1.0.0
 */
class SingleLiveEvent<T> : MutableLiveData<T>() {

    private val pending = AtomicBoolean(false)

    @MainThread
    fun observe(owner: LifecycleOwner, observer: () -> Unit) {
        if (hasActiveObservers()) {
            Timber.i("Multiple observers registered but only one will be notified of changes.")
        }

        // Observe the internal MutableLiveData
//        super.observe(owner, { t ->
//            if (pending.compareAndSet(true, false)) {
//                observer.onChanged(t)
//            }
//        })
    }

    @MainThread
    override fun setValue(t: T?) {
        pending.set(true)
        super.setValue(t)
    }

    /**
     * Used for cases where T is Void, to make calls cleaner.
     */
    @MainThread
    fun call() {
        value = null
    }

}