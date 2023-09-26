package com.example.carousel3dlib.view

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.motion.widget.MotionLayout

abstract class CarouselOpenableView(context: Context, attributeSet: AttributeSet)
    : MotionLayout(context, attributeSet)
{
    abstract fun setClickBackHandler(clickListener: OnClickListener)
}