package com.rehyapp.calltimer.ui

import com.rehyapp.calltimer.calllogging.LogManager
import com.rehyapp.calltimer.ui.recents.RecentsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val recentsViewModelModule = module {
    viewModel {
        RecentsViewModel(get())
    }
}

val logManagerModule = module {
    single {
        LogManager(get())
    }
}
