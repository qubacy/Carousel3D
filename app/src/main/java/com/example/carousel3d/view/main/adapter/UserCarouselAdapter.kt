package com.example.carousel3d.view.main.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.carousel3d.data.repository.User
import com.example.carousel3d.databinding.UserCardBinding
import com.example.carousel3dlib.adapter.Carousel3DAdapter
import com.example.carousel3dlib.adapter.Carousel3DViewHolder
import com.example.carousel3dlib.general.Carousel3DContext
import com.example.carousel3dlib.layoutmanager.Carousel3DHorizontalSwipeHandler

open class UserCarouselAdapter(
    val callback: UserCarouselAdapterCallback)
    : Carousel3DAdapter<User>()
{
    companion object {
        const val TAG = "CAROUSEL_ADAPTER"
    }

    override fun areItemsExpandable(): Boolean {
        return true
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserCarouselViewHolder {
        Log.d(TAG, "onCreateViewHolder() call")

        val itemBinding = UserCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)

        return UserCarouselViewHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return getItems().size
    }

    override fun onBindViewHolder(holder: Carousel3DViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder() call")

        for (user in getItems()) {
            Log.d(TAG, "onBindViewHolder(): cur user.username = ${user.username}")

        }

        (holder as UserCarouselViewHolder).bind(getItems()[position])
    }

    override fun onHorizontalSwipe(
        position: Int,
        direction: Carousel3DContext.SwipeDirection,
        handler: Carousel3DHorizontalSwipeHandler)
    {
        Log.d(TAG, "entering onHorizontalSwipe(); direction: ${if (direction == Carousel3DContext.SwipeDirection.LEFT) "left" else "right"}")

        val curItem = getItems()[position]

        if (curItem == null)
            throw IllegalStateException("Current item hasn't been found!")

        Log.d(TAG, "onHorizontalSwipe(); curItem position: $position; items.count = ${getItems().size}")

        // doing something..

        callback.deleteUser(curItem)

        // Providing an answer to the request by calling the following method on the handler:

        handler.onHorizontalSwipeAction(position, Carousel3DContext.SwipeAction.ERASE)
    }

    override fun areItemsSwipeable(): Boolean {
        return true
    }

    override fun getViewHolderForItemView(view: View): UserCarouselViewHolder {
        return getRecyclerView()?.getChildViewHolder(view) as UserCarouselViewHolder
    }
}