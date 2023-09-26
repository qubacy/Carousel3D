package com.example.carousel3d

import android.app.Application
import com.example.carousel3d.data.repository.UserDataRepositoryImpl
import com.example.carousel3d.data.repository.datasource.hard.HardUserDataSource

class Application : Application() {
    class AppContainer(val hardUserDataSource: HardUserDataSource) {
        val userDataRepository = UserDataRepositoryImpl(hardUserDataSource)
    }

    val appContainer: AppContainer by lazy {
        AppContainer(HardUserDataSource())
    }
}