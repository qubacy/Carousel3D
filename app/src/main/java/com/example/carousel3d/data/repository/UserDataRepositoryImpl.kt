package com.example.carousel3d.data.repository

import android.util.Log
import com.example.carousel3d.data.repository.datasource.hard.HardUserDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class UserDataRepositoryImpl(hardUserDataSource: HardUserDataSource)
    : UserDataRepository(hardUserDataSource)
{
    companion object {
        const val TAG = "UserDataRepositoryImpl"

        const val FLOW_REPLAY = 16
    }

    private val _mutableUserFlow: MutableSharedFlow<List<User>> =
        MutableSharedFlow<List<User>>(FLOW_REPLAY).apply {
            Log.d(TAG, "Emit result: ${tryEmit(hardUserDataSource.getUsers())}")
        }
    override val userFlow: Flow<List<User>> = _mutableUserFlow.asSharedFlow()

    override suspend fun deleteUser(user: User) {
        hardUserDataSource.deleteUser(user)

        _mutableUserFlow.emit(hardUserDataSource.getUsers())
    }
}