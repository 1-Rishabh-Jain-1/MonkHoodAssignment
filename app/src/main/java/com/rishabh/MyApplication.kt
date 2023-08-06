package com.rishabh

import android.app.Application
import android.content.Context

class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    companion object {
        private lateinit var context : Context
        public fun getAppContext(): Context {
            return context
        }
    }
}