package com.hashcaller.view.utils

class Singleton {
    var instance: Singleton? = null
        get() {
            if (field == null) {
                synchronized(Singleton::class.java) {
                    if (field == null) {
                        field = Singleton()
                    }
                }
            }
            return field
        }
        private set
}