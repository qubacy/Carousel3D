package com.example.carousel3d.view.main.adapter

import com.example.carousel3d.data.repository.User

interface UserCarouselAdapterCallback {
    fun deleteUser(user: User)
}