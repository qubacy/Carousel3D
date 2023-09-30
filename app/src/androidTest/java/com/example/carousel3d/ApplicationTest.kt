package com.example.carousel3d

class ApplicationTest() : Application() {
    class AppContainer(
        override val userDataRepository: FakeUserDataRepository = FakeUserDataRepository()
    )
        : Application.AppContainer(userDataRepository.hardUserDataSource)
    { }

    override val appContainer: AppContainer by lazy { AppContainer() }
}