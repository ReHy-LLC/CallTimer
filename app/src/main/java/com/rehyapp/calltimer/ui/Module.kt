package com.rehyapp.calltimer.ui

import com.rehyapp.calltimer.calllogging.LogManager
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val mainSharedViewModelModule = module {
    viewModel {
        MainSharedViewModel(
            get(),
            application = androidApplication()
        )
    }
}

val logManagerModule = module {
    single {
        LogManager(get())
    }
}
