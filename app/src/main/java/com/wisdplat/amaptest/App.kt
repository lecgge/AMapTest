package com.wisdplat.amaptest

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/**
 *
@author xuleyu
@Email xuleyumail@gmail.com
@create 2023-10-20 16:59
 *
 */
class App : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}