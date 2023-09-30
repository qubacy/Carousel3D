package com.example.carousel3dlib.general

object Carousel3DContext {
    enum class SwipeDirection {
        BACK,
        LEFT,
        RIGHT
    }

    enum class RollingDirection {
        NONE,
        UP,
        DOWN
    }

    enum class SwipeAction {
        NONE,
        ERASE
    }

    enum class ScrollOrientation {
        NONE(),
        HORIZONTAL(),
        VERTICAL()
    }
}