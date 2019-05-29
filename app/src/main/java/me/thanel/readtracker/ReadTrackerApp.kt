package me.thanel.readtracker

import android.app.Application
import com.chibatching.kotpref.Kotpref
import me.thanel.readtracker.di.ReadTracker

class ReadTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Kotpref.init(this)
        ReadTracker.initDependencies(this)
    }
}
