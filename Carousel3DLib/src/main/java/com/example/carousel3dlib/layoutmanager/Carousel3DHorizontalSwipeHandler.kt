package com.example.carousel3dlib.layoutmanager

import com.example.carousel3dlib.general.Carousel3DContext

interface HorizontalSwipeHandler {
    fun onHorizontalSwipeAction(
        position: Int,
        swipeAction: Carousel3DContext.SwipeAction
    )
}