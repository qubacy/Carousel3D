package com.example.carousel3d.data.repository

import com.example.carousel3d.data.repository.datasource.hard.HardUserDataSource
import kotlinx.coroutines.flow.Flow

abstract class UserDataRepository (
    val hardUserDataSource: HardUserDataSource)
{
    abstract val userFlow: Flow<List<User>>

    abstract suspend fun deleteUser(user: User)
}