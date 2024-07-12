package com.phenix.bluetoothchat

import android.app.Application
import android.util.Log

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("APP", "Application onCreate()")
    }
}