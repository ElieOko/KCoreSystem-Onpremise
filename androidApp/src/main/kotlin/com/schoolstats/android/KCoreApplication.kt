package com.schoolstats.android

import android.app.Application
import com.schoolstats.data.local.AndroidContextHolder
import com.schoolstats.data.sync.SyncManager
import com.schoolstats.di.androidModule
import com.schoolstats.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform.getKoin

class KCoreApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidContextHolder.application = this
        startKoin {
            androidContext(this@KCoreApplication)
            modules(appModules + androidModule)
        }
        getKoin().get<SyncManager>().start()
    }
}
