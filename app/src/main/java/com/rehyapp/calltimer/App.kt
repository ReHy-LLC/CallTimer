package com.rehyapp.calltimer

import android.app.Application
import com.rehyapp.calltimer.ui.logManagerModule
import com.rehyapp.calltimer.ui.recentsViewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(recentsViewModelModule)
            modules(logManagerModule)
        }
    }
}