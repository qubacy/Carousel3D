package com.example.carousel3dlib.adapter

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.carousel3dlib.general.Carousel3DContext
import com.example.carousel3dlib.layoutmanager.Carousel3DHorizontalSwipeHandler

abstract class Carousel3DAdapter<ItemType>() :
    RecyclerView.Adapter<Carousel3DViewHolder>()
{
    companion object {
        const val TAG = "CAROUSEL_ADAPTER"
    }

    private var mRecyclerView: RecyclerView? = null
    private var mItemList = listOf<ItemType>()

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        mRecyclerView = recyclerView
    }
    fun getRecyclerView(): RecyclerView? {
        return mRecyclerView
    }

    abstract fun areItemsSwipeable(): Boolean
    abstract fun onHorizontalSwipe(
        position: Int,
        direction: Carousel3DContext.SwipeDirection,
        handler: Carousel3DHorizontalSwipeHandler
    )
    abstract fun getViewHolderForItemView(view: View): Carousel3DViewHolder
    abstract fun areItemsExpandable(): Boolean

    open fun setItems(itemList: List<ItemType>) {
        Log.d(TAG, "setItems(): itemList.size = ${itemList.size}")

        // It's really important to do this kind of checking before calling notifyDataSetChanged():

        if (mRecyclerView == null) return
        if (mRecyclerView!!.scrollState != RecyclerView.SCROLL_STATE_IDLE
         || mRecyclerView!!.isInLayout)
        {
            return
        }

        mItemList = itemList

        notifyDataSetChanged()
    }

    fun getItems(): List<ItemType> {
        return mItemList
    }
}