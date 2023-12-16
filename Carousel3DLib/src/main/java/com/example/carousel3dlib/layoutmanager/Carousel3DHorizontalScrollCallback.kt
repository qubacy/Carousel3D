package com.example.carousel3dlib.layoutmanager

interface Carousel3DHorizontalScrollCallback {
    /*
    * Values belong to range: [-1, 1]
    */
    fun onHorizontalScroll(fraction: Float)
}