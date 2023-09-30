package com.example.carousel3dlib.layoutmanager

interface LayoutManagerEventListener {
    enum class Event {
        CHILDREN_LAYOUTED,
    }

    fun onChildrenLayouted()
}