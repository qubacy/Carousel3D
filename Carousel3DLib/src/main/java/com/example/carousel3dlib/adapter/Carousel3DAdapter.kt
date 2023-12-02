package com.example.carousel3dlib.adapter

import android.util.Log
import android.view.View
import android.view.View.OnLayoutChangeListener
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
    abstract fun onVerticalRoll(
        edgePosition: Int,
        direction: Carousel3DContext.RollingDirection
    )
    abstract fun getViewHolderForItemView(view: View): Carousel3DViewHolder
    abstract fun areItemsExpandable(): Boolean

    open fun setItems(itemList: List<ItemType>) {
        Log.d(TAG, "setItems(): itemList.size = ${itemList.size}")

        // It's really important to do this kind of checking before calling notifyDataSetChanged():

        if (mRecyclerView == null) return
        if (mRecyclerView!!.scrollState != RecyclerView.SCROLL_STATE_IDLE) {
            mRecyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)

                    if (newState != RecyclerView.SCROLL_STATE_IDLE) return

                    this@Carousel3DAdapter.setItems(itemList)

                    mRecyclerView!!.removeOnScrollListener(this)
                }
            })

            return
        }
        if (mRecyclerView!!.isInLayout) {
            mRecyclerView!!.addOnLayoutChangeListener(object : OnLayoutChangeListener {
                override fun onLayoutChange(
                    p0: View?, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int, p6: Int, p7: Int, p8: Int
                ) {
                    this@Carousel3DAdapter.setItems(itemList)

                    mRecyclerView!!.removeOnLayoutChangeListener(this)
                }
            })

            return
        }

        mItemList = itemList

        notifyDataSetChanged()
    }

    fun getItems(): List<ItemType> {
        return mItemList
    }
}