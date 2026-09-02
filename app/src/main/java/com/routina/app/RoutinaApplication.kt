package com.routina.app

import android.app.Application
import com.routina.app.di.AppContainer

class RoutinaApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
