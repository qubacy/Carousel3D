package com.example.carousel3d.data.repository.datasource.hard

import com.example.carousel3d.data.repository.User
import com.example.carousel3d.data.repository.datasource.UserDataSource

class HardUserDataSource : UserDataSource {
    companion object {
        val userList = mutableListOf<User>(
            User("User1", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."),
            User("User2", "Nothing"),
            User("User3", "At all"),
            User("User4", "Aga"),
            User("User5", "Fu"),
            User("User6", "Aga"),
            User("User7", "Aga"),
            User("User8", "Aga"),
        )
    }

    private var userCounter = userList.size

    override fun getUsers(): List<User> {
        return userList
    }

    override fun deleteUser(user: User) {
        userList.remove(user)

//        addNewUser()
    }

    private fun addNewUser() {
        ++userCounter

        userList.add(User("User $userCounter", "Some description"))
    }
}