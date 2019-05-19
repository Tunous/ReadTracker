package me.thanel.readtracker

import android.app.Application
import me.thanel.readtracker.di.ReadTracker

class ReadTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ReadTracker.initDependencies(this)
    }
}
