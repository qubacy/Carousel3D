package com.example.carousel3dlib.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.carousel3dlib.view.Carousel3DOpenableView

abstract class Carousel3DViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    abstract fun getOpenableView(root: ViewGroup, topMarginPx: Int): Carousel3DOpenableView
    abstract fun recycleOpenableView(openableView: Carousel3DOpenableView, topMarginPx: Int)
}