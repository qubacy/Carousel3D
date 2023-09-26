package com.example.carousel3d.data.repository.datasource

import com.example.carousel3d.data.repository.User

interface UserDataSource {
    fun getUsers(): List<User>
    fun deleteUser(user: User)
}