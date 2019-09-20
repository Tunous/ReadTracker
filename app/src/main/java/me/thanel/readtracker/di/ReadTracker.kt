package me.thanel.readtracker.di

import android.app.Application

object ReadTracker {
    lateinit var dependencyInjector: RootComponent
        internal set

    fun initDependencies(application: Application) {
        dependencyInjector = DaggerRootComponent.builder()
            .rootModule(RootModule(application))
            .build()
    }
}
