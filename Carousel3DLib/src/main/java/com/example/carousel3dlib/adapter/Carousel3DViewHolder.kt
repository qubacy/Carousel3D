package com.example.carousel3dlib.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.carousel3dlib.view.CarouselOpenableView

abstract class Carousel3DViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    abstract fun getOpenableView(root: ViewGroup, topMarginPx: Int): CarouselOpenableView
    abstract fun recycleOpenableView(openableView: CarouselOpenableView, topMarginPx: Int)
}