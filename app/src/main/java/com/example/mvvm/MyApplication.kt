package com.example.mvvm

import android.app.Application
import com.example.mvvm.startup.DatabaseInitializer
import dagger.hilt.android.HiltAndroidApp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application() {

    @Inject
    lateinit var dbInit: DatabaseInitializer

    private val startupScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // populate DB in background safely
        startupScope.launch {
            try {
                dbInit.populateIfNeeded(R.raw.questions)
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        startupScope.cancel()
    }
}
