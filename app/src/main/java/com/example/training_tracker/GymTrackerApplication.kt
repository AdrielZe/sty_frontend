package com.example.training_tracker

import android.app.Application
import com.example.training_tracker.data.AppContainer
import com.example.training_tracker.data.DefaultAppContainer

class GymTrackerApplication : Application() {

    // Instância do container que ficará viva durante todo o ciclo de vida do app
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}