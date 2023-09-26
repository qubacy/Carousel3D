package com.example.carousel3d.view.main.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carousel3d.data.repository.UserDataRepository
import com.example.carousel3d.data.repository.User
import com.example.carousel3d.view.main.adapter.UserCarouselAdapterCallback
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainActivityModel(val userDataRepository: UserDataRepository)
    : ViewModel(), UserCarouselAdapterCallback
{
    val userFlow: Flow<List<User>> = userDataRepository.userFlow

    override fun deleteUser(user: User) {
        viewModelScope.launch {
            userDataRepository.deleteUser(user)
        }
    }
}

class MainActivityModelFactory(val userDataRepository: UserDataRepository)
    : ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainActivityModel::class.java))
            return MainActivityModel(userDataRepository) as T

        throw TypeCastException()
    }
}