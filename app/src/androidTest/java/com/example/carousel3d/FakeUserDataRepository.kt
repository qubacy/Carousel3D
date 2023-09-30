package com.example.carousel3d

import com.example.carousel3d.data.repository.User
import com.example.carousel3d.data.repository.UserDataRepository
import com.example.carousel3d.data.repository.UserDataRepositoryImpl
import com.example.carousel3d.data.repository.datasource.hard.HardUserDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class FakeUserDataRepository() : UserDataRepository(HardUserDataSource()) {
    private var mBufferUserList = mutableListOf<User>()

    private val mMutableUserFlow: MutableSharedFlow<List<User>> =
        MutableSharedFlow<List<User>>(UserDataRepositoryImpl.FLOW_REPLAY)
    override val userFlow: Flow<List<User>> = mMutableUserFlow.asSharedFlow()

    override suspend fun deleteUser(user: User) {
        mBufferUserList.remove(user)
        mMutableUserFlow.emit(mBufferUserList)
    }

    suspend fun addUser(user: User) {
        mBufferUserList.add(user)
        mMutableUserFlow.emit(mBufferUserList)
    }

    suspend fun setUsers(users: List<User>) {
        mBufferUserList.clear()
        mBufferUserList.addAll(users)
    }
}