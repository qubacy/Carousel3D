package com.example.carousel3d.view.main.adapter

import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionLayout
import com.example.carousel3d.R
import com.example.carousel3d.data.repository.User
import com.example.carousel3d.databinding.UserCardBinding
import com.example.carousel3d.databinding.UserCardOpenableBinding
import com.example.carousel3dlib.adapter.Carousel3DViewHolder
import com.example.carousel3dlib.view.CarouselOpenableView

class UserCarouselViewHolder(val binding: UserCardBinding)
    : Carousel3DViewHolder(binding.root)
{
    private var lastProgressValue = 0f

    fun bind(item: User) {
        binding.userCardContentUsername.text = item.username
        binding.userCardContentDescription.text = item.description
    }

    override fun getOpenableView(root: ViewGroup, topMarginPx: Int): CarouselOpenableView {
        Log.d(UserCarouselAdapter.TAG, "entering getOpenableView().. topMarginPx: $topMarginPx")

        val layoutInflater = LayoutInflater.from(binding.root.context)
        val openableViewBinding = UserCardOpenableBinding.inflate(layoutInflater, root, true)

        recycleOpenableView(openableViewBinding.root, topMarginPx)

        openableViewBinding.root.apply {
            isClickable = true

            setTransitionListener(object : MotionLayout.TransitionListener {
                override fun onTransitionStarted(
                    motionLayout: MotionLayout?,
                    startId: Int,
                    endId: Int
                ) {
                    Log.d(UserCarouselAdapter.TAG, "onTransitionStarted()")
                }

                override fun onTransitionChange(
                    motionLayout: MotionLayout?,
                    startId: Int,
                    endId: Int,
                    progress: Float
                ) {
                    Log.d(
                        UserCarouselAdapter.TAG,
                        "onTransitionChange() progress = $progress; startId = $startId; endId = $endId"
                    )

                    if (progress in 0.70..0.80) {
                        if (progress > lastProgressValue && openableViewBinding.userCardOpenableContent.userCardContentDescription.ellipsize == TextUtils.TruncateAt.END) {
                            Log.d(UserCarouselAdapter.TAG, "onTransitionChange() going to the END")

                            openableViewBinding.userCardOpenableContent.userCardContentDescription.ellipsize =
                                null
                            openableViewBinding.userCardOpenableContent.userCardContentDescription.maxLines =
                                Int.MAX_VALUE

                        } else if (progress < lastProgressValue && openableViewBinding.userCardOpenableContent.userCardContentDescription.ellipsize == null) {
                            Log.d(UserCarouselAdapter.TAG, "onTransitionChange() going to the START")

                            openableViewBinding.userCardOpenableContent.userCardContentDescription.ellipsize =
                                TextUtils.TruncateAt.END
                            openableViewBinding.userCardOpenableContent.userCardContentDescription.maxLines =
                                1
                        }
                    }

                    lastProgressValue = progress
                }

                override fun onTransitionCompleted(
                    motionLayout: MotionLayout?,
                    currentId: Int
                ) {
                    Log.d(UserCarouselAdapter.TAG, "onTransitionCompleted()")
                }

                override fun onTransitionTrigger(
                    motionLayout: MotionLayout?,
                    triggerId: Int,
                    positive: Boolean,
                    progress: Float
                ) {
                }

            })
        }

        return openableViewBinding.root
    }

    override fun recycleOpenableView(openableView: CarouselOpenableView, topMarginPx: Int) {
        val openableViewBinding = UserCardOpenableBinding.bind(openableView)

        openableViewBinding.userCardOpenableContent.apply {
            userCardContentAvatar.setImageDrawable(binding.userCardContentAvatar.drawable)
            userCardContentUsername.text = binding.userCardContentUsername.text
            userCardContentDescription.text = binding.userCardContentDescription.text
        }

        setStartTopMarginForOpenableViewScene(openableViewBinding, topMarginPx)
    }

    private fun setStartTopMarginForOpenableViewScene(
        openableViewBinding: UserCardOpenableBinding, topMarginPx: Int
    ) {
        openableViewBinding.root.apply {
            scene.getConstraintSet(
                context,
                context.resources.getResourceName(R.id.user_card_openable_container_closed_state)
            )
                .getConstraint(R.id.user_card_openable_content).layout.topMargin =
                topMarginPx
        }
    }
}
