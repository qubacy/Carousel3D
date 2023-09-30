package com.example.carousel3d

import android.app.Application
import com.example.carousel3d.data.repository.UserDataRepository
import com.example.carousel3d.data.repository.UserDataRepositoryImpl
import com.example.carousel3d.data.repository.datasource.hard.HardUserDataSource

open class Application : Application() {
    open class AppContainer(hardUserDataSource: HardUserDataSource) {
        open val userDataRepository: UserDataRepository = UserDataRepositoryImpl(hardUserDataSource)
    }

    open val appContainer: AppContainer by lazy {
        AppContainer(HardUserDataSource())
    }
}