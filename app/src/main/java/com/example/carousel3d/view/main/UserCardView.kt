package com.example.carousel3d.view.main

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.cardview.widget.CardView

class UserCardView(
    context: Context, attrs: AttributeSet
) : CardView(context, attrs) {
    companion object {
        const val TAG = "UserCardView"
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        Log.d(TAG, "onSizeChanged(): view = ${this.toString()}")
    }

    override fun onSetAlpha(alpha: Int): Boolean {
        Log.d(TAG, "onSetAlpha(): view = ${this.toString()}")

        return super.onSetAlpha(alpha)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        Log.d(TAG, "onLayout(): view = ${this.toString()}")
    }


}