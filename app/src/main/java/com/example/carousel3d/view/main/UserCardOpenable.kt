package com.example.carousel3d.view.main

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import com.example.carousel3d.databinding.UserCardOpenableBinding
import com.example.carousel3dlib.view.CarouselOpenableView

class UserCardOpenable(context: Context, attributeSet: AttributeSet)
    : CarouselOpenableView(context, attributeSet)
{
    companion object {
        const val TAG = "UserCardOpenable"
    }

    private lateinit var binding: UserCardOpenableBinding

    override fun setClickBackHandler(clickListener: OnClickListener) {
        setOnClickListener(clickListener)
        getChildAt(0).setOnTouchListener(object : OnTouchListener {
            override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                return true
            }
        })
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        binding = UserCardOpenableBinding.bind(this)
        val innerMotionLayoutView = binding.userCardOpenableContent.userCardContent

        addTransitionListener(object : TransitionListener {
            override fun onTransitionStarted(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int
            ) {
                Log.d(TAG, "onTransitionStarted()")
            }

            override fun onTransitionChange(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int,
                progress: Float
            ) {
                Log.d(TAG, "onTransitionChange() curProgress = $progress")

                if (progress !in 0.97f..1f) // why does this trick work?
                    innerMotionLayoutView.progress = progress
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                Log.d(TAG, "onTransitionCompleted()")
            }

            override fun onTransitionTrigger(
                motionLayout: MotionLayout?,
                triggerId: Int,
                positive: Boolean,
                progress: Float
            ) { }
        })
    }
}