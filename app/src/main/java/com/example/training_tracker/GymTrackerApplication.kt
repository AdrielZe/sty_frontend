package com.example.training_tracker

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.request.CachePolicy
import com.example.training_tracker.data.AppContainer
import com.example.training_tracker.data.DefaultAppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class GymTrackerApplication : Application(), ImageLoaderFactory {

    lateinit var container: AppContainer
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this, applicationScope)
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .bitmapConfig(android.graphics.Bitmap.Config.RGB_565)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build()
}
