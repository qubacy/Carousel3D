package com.example.carousel3dlib.layoutmanager

import android.animation.ValueAnimator
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.recyclerview.widget.RecyclerView
import com.example.carousel3dlib.adapter.Carousel3DAdapter
import com.example.carousel3dlib.general.Carousel3DContext
import com.example.carousel3dlib.view.Carousel3DOpenableView

open class Carousel3DLayoutManager()
    : RecyclerView.LayoutManager(),
    Carousel3DHorizontalSwipeHandler
{
    enum class AnimState {
        INIT,
        DEFAULT,
        HORIZONTAL_SWIPE,
        AFTER_HORIZONTAL_SWIPE
    }

    companion object {
        const val TAG = "CAROUSEL_3D"

        const val DEFAULT_SCALE = 1f
        const val SCALE_STEP = 0.1f

        const val OVERLAPPING_PERCENT = 0.5f

        const val DEFAULT_VISIBLE_ITEMS_COUNT = 3
        const val EDGE_INVISIBLE_ITEMS_COUNT = 2

        const val ROTATION_ANIMATION_DURATION = 300L
        const val SLIDING_ANIMATION_DURATION = 300L
        const val INIT_FOREGROUND_ITEM_SHAKE_ANIMATION_DURATION = 1000L

        const val INIT_FOREGROUND_ITEM_SHAKE_ANIMATION_TRANSLATION_X = 100f

        const val HORIZONTAL_SCROLL_OFFSET_LEFT_SWIPE = -300f
        const val HORIZONTAL_SCROLL_OFFSET_RIGHT_SWIPE = 300f
    }

    private var topCenteringOffsetPx = 0
    private var foregroundItemHeightPx = 0

    private var foregroundItemBottomPx = 0
    private var foregroundItemTopPx = 0

    private var visualItemsCount = 0

    private var curScrollingState = RecyclerView.SCROLL_STATE_IDLE
    private var curScrollingOrientation = Carousel3DContext.ScrollOrientation.NONE
    private var curScrollingVerticalOffset = 0
    private var curScrollingHorizontalOffset = 0
    private var lastBouncingBackAnimator: ViewPropertyAnimator? = null

    private var itemRotationAfterSwipeCounter = 0

    private var curItemIndexOffset = -1 // default offset
    private var isAnimating = false

    private var carouselAdapter: Carousel3DAdapter<*>? = null
    private var isHorizontalSwipeRequested: Boolean = false
    private var curAnimState: AnimState = AnimState.INIT

    private var isForegroundItemExtended = false
    private var openableView: Carousel3DOpenableView? = null

    private var layoutManagerEventListenerList: MutableList<LayoutManagerEventListener> =
        mutableListOf()

    override fun onAdapterChanged(
        oldAdapter: RecyclerView.Adapter<*>?,
        newAdapter: RecyclerView.Adapter<*>?
    ) {
        super.onAdapterChanged(oldAdapter, newAdapter)

        if (newAdapter !is Carousel3DAdapter<*>) return

        carouselAdapter = newAdapter
    }

    fun addLayoutManagerEventListener(layoutManagerEventListener: LayoutManagerEventListener) {
        layoutManagerEventListenerList.add(layoutManagerEventListener)
    }

    private fun notifyLayoutManagerEventListeners(event: LayoutManagerEventListener.Event) {
        for (layoutManagerEventListener in layoutManagerEventListenerList) {
            when (event) {
                LayoutManagerEventListener.Event.CHILDREN_LAYOUTED -> {
                    layoutManagerEventListener.onChildrenLayouted()
                }
            }
        }
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.WRAP_CONTENT,
            RecyclerView.LayoutParams.WRAP_CONTENT)
    }

    override fun onLayoutChildren(
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?)
    {
        Log.d(TAG, "entering onLayoutChildren()..")

        if (recycler == null || isAnimating) return

        visualItemsCount = Math.min(
            DEFAULT_VISIBLE_ITEMS_COUNT + EDGE_INVISIBLE_ITEMS_COUNT,
            itemCount + EDGE_INVISIBLE_ITEMS_COUNT
        )

        if (curAnimState == AnimState.AFTER_HORIZONTAL_SWIPE) {
            removeAndRecycleAllViews(recycler)
            recycler.clear()
        } else
            detachAndScrapAttachedViews(recycler)

        if (itemCount == 0) return

        Log.d(TAG, "onLayoutChildren() cur RecyclerView height: $height")

        var consumedVerticallyPx = 0
        var curScale = (DEFAULT_SCALE + SCALE_STEP) - ((visualItemsCount - 1) * SCALE_STEP)

        Log.d(TAG, "onLayoutChildren() paddingTop = $paddingTop; paddingLeft = $paddingLeft; paddingRight = $paddingRight;")

        for (itemVisualIndex in visualItemsCount - 1 downTo 0) {
            val curItemIndex = getItemPositionFromVisualIndex(itemVisualIndex)

            Log.d(TAG, "onLayoutChildren() curItemIndex: $curItemIndex; curItemIndexOffset: $curItemIndexOffset")

            val view = recycler.getViewForPosition(curItemIndex)

            view.apply {
                scaleX = curScale
                scaleY = curScale

                translationX = 0f
                translationY = 0f
            }

            addView(view)
            measureChildWithMargins(view, 0, 0)

            if (itemVisualIndex == visualItemsCount - 1) {
                if (topCenteringOffsetPx == 0) {
                    calculateTopCenteringOffsetsByItemView(view)
                }

                consumedVerticallyPx = topCenteringOffsetPx
            }

            val topPosition = if (itemVisualIndex == visualItemsCount - 1)
                (-view.measuredHeight * OVERLAPPING_PERCENT + consumedVerticallyPx).toInt() else
                consumedVerticallyPx
            val bottomPosition = if (itemVisualIndex == visualItemsCount - 1)
                (view.measuredHeight * OVERLAPPING_PERCENT + consumedVerticallyPx).toInt() else
                (view.measuredHeight).toInt() + consumedVerticallyPx

            Log.d(TAG, "onLayoutChildren() view.scale = ${view.scaleY}; view.top = ${view.top}; view.bottom = ${view.bottom}; view.translationY = ${view.translationY}")

            curScale += SCALE_STEP

            if (itemVisualIndex == 0 || itemVisualIndex == visualItemsCount - 1) {
                view.alpha = 0f
                view.visibility = View.GONE

            } else {
                view.alpha = 1f
                view.visibility = View.VISIBLE

                consumedVerticallyPx += (view.measuredHeight
                        * view.scaleY
                        * OVERLAPPING_PERCENT).toInt()

                if (itemVisualIndex == 1) {
                    foregroundItemHeightPx = view.measuredHeight
                }
            }

            layoutDecorated(
                view,
                paddingLeft,
                topPosition,
                view.measuredWidth + paddingRight,
                bottomPosition
            )

            if (carouselAdapter != null && carouselAdapter?.areItemsExpandable() == true) {
                initOpenableItemView(view, itemVisualIndex)
            }

            Log.d(TAG, "onLayoutChildren() consumedVerticallyPx = $consumedVerticallyPx")
        }

        if (curAnimState == AnimState.INIT
            && visualItemsCount >= EDGE_INVISIBLE_ITEMS_COUNT + 1)
        {
            getChildAt(visualItemsCount - 2)?.let {
                shakeItemView(it)
            }

            curAnimState = AnimState.DEFAULT

        } else if (curAnimState == AnimState.AFTER_HORIZONTAL_SWIPE) {
            curAnimState = AnimState.DEFAULT
        }

        notifyLayoutManagerEventListeners(LayoutManagerEventListener.Event.CHILDREN_LAYOUTED)
    }

    private fun initOpenableItemView(
        itemView: View,
        itemVisualIndex: Int)
    {
        val viewHolder = carouselAdapter?.getViewHolderForItemView(itemView)
        val recyclerView = carouselAdapter?.getRecyclerView()

        if (itemVisualIndex == 1) {
            foregroundItemTopPx = itemView.top
            foregroundItemBottomPx = itemView.bottom

            if (viewHolder != null && recyclerView != null
                && recyclerView.parent != null)
            {
                val openableViewRoot = recyclerView.parent as ViewGroup

                itemView.setOnClickListener {
                    Log.d(TAG, "onLayoutChildren() the foreground item has been clicked!")

                    if (openableView == null) {
                        openableView = viewHolder.getOpenableView(
                            openableViewRoot, foregroundItemTopPx)
                    } else {
                        viewHolder.recycleOpenableView(openableView!!, foregroundItemTopPx)
                    }

                    openableView!!.apply {
                        visibility = View.VISIBLE
                        isClickable = true

                        setClickBackHandler {
                            transitionToStart {
                                openableView?.visibility = View.GONE
                                isForegroundItemExtended = false
                            }
                        }
                    }

                    Log.d(TAG, "onLayoutChildren() openableView.visibility = ${openableView?.visibility}")

                    openableView!!.transitionToEnd {
                        isForegroundItemExtended = true
                    }
                }
            }

        } else if (itemVisualIndex == 0 || itemVisualIndex == 2) {
            itemView.isClickable = false
            itemView.setOnClickListener(null)
        }
    }

    private fun calculateTopCenteringOffsetsByItemView(itemView: View) {
        val topCenteringOffsetPxBuffer = ((height - (itemView.measuredHeight * OVERLAPPING_PERCENT *
                ((DEFAULT_SCALE - SCALE_STEP * 2) +
                        (DEFAULT_SCALE - SCALE_STEP)) + itemView.measuredHeight
                ).toInt()) / 2)

        if (topCenteringOffsetPxBuffer > 0)
            topCenteringOffsetPx = topCenteringOffsetPxBuffer
        else
            topCenteringOffsetPx = 0
    }

    private fun shakeItemView(itemView: View) {
        ValueAnimator.ofFloat(
            0f,
            INIT_FOREGROUND_ITEM_SHAKE_ANIMATION_TRANSLATION_X,
            -INIT_FOREGROUND_ITEM_SHAKE_ANIMATION_TRANSLATION_X,
            0f
        ).apply {
            interpolator = AccelerateDecelerateInterpolator()
            duration = INIT_FOREGROUND_ITEM_SHAKE_ANIMATION_DURATION

            addUpdateListener {
                itemView.translationX = it.animatedValue as Float
            }

        }.start()
    }

    override fun canScrollVertically(): Boolean {
        return true
    }

    override fun canScrollHorizontally(): Boolean {
        return true
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)

        if (state == RecyclerView.SCROLL_STATE_IDLE
            && curScrollingState != RecyclerView.SCROLL_STATE_SETTLING)
        {
            if (curScrollingOrientation == Carousel3DContext.ScrollOrientation.HORIZONTAL)
                getChildAt(visualItemsCount - 1 - 1)?.let {
                    handleEndOfHorizontalScroll(it)
                }
        }

        curScrollingState = state

        Log.d(TAG, "onScrollStateChanged: $state")
    }

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        if (itemCount <= 1 || isAnimating
            || curScrollingOrientation == Carousel3DContext.ScrollOrientation.HORIZONTAL
            || isHorizontalSwipeRequested || isForegroundItemExtended)
        {
            return 0
        }

        if (curScrollingOrientation != Carousel3DContext.ScrollOrientation.VERTICAL)
            curScrollingOrientation = Carousel3DContext.ScrollOrientation.VERTICAL

        if (curScrollingState == RecyclerView.SCROLL_STATE_DRAGGING) {
            curScrollingVerticalOffset += dy

            Log.d(TAG, "scrollVerticallyBy() dy: $dy; curScrollingOffset: $curScrollingVerticalOffset")

        } else if (curScrollingState == RecyclerView.SCROLL_STATE_SETTLING) {
            var rollingDirection: Carousel3DContext.RollingDirection

            if (curScrollingVerticalOffset > 0) {
                // going "down".. \/

                if (curScrollingVerticalOffset < foregroundItemHeightPx / 2) return 0

                curItemIndexOffset = getItemIndexOffsetAfterVerticalRolling(
                    Carousel3DContext.RollingDirection.DOWN)

                rollingDirection = Carousel3DContext.RollingDirection.DOWN

            } else {
                // going "up".. /\

                if (-curScrollingVerticalOffset < foregroundItemHeightPx / 2) return 0

                curItemIndexOffset = getItemIndexOffsetAfterVerticalRolling(
                    Carousel3DContext.RollingDirection.UP)

                rollingDirection = Carousel3DContext.RollingDirection.UP
            }

            curScrollingVerticalOffset = 0

            onVerticalRolling(rollingDirection)

            animateRotation(rollingDirection, null) {
                requestLayout()

                curScrollingOrientation = Carousel3DContext.ScrollOrientation.NONE
            }

            return 0
        }

        return curScrollingVerticalOffset
    }

    private fun onVerticalRolling(rollingDirection: Carousel3DContext.RollingDirection) {
        val visualItemsCount = if (itemCount >= DEFAULT_VISIBLE_ITEMS_COUNT)
            DEFAULT_VISIBLE_ITEMS_COUNT + EDGE_INVISIBLE_ITEMS_COUNT
        else itemCount

        val edgePosition = if (rollingDirection == Carousel3DContext.RollingDirection.UP) {
            getItemPositionFromVisualIndex(visualItemsCount - 1)
        } else {
            getItemPositionFromVisualIndex(0)
        }

        carouselAdapter?.onVerticalRoll(edgePosition, rollingDirection)
    }

    private fun animateRotation(
        rollingDirection: Carousel3DContext.RollingDirection,
        itemCondition: ((itemVisualIndex: Int, item: View) -> Boolean)?,
        postAnimationRunnable: Runnable)
    {
        Log.d(TAG, "entering animateRotation() rollingDir: $rollingDirection; itemCount = $itemCount")

        isAnimating = true

        var prevChild: View? = null
        var curChild: View? = null
        var nextChild: View? = null

        var processedItemCount = 0

        for (itemVisualIndex in 0 until visualItemsCount) {
            prevChild = curChild
            curChild = (nextChild ?: getChildAt(itemVisualIndex)) ?: return
            nextChild = getChildAt(itemVisualIndex + 1)

            Log.d(TAG, "animateRotation() curChild.toString(): $curChild")

            if (itemCondition != null)
                if (!itemCondition(itemVisualIndex, curChild)) continue

            ++processedItemCount

            val curAnimator = curChild.animate()

            val translationYPx = if (nextChild != null
                && rollingDirection == Carousel3DContext.RollingDirection.UP)
            {
                nextChild.y - curChild.y

            } else if (prevChild != null
                && rollingDirection == Carousel3DContext.RollingDirection.DOWN)
            {
                -(curChild.y - prevChild.y)

            } else if (rollingDirection == Carousel3DContext.RollingDirection.UP) {
                foregroundItemHeightPx

            } else {
                -foregroundItemHeightPx
            }

            curAnimator.translationY(translationYPx.toFloat())

            val scale = if (rollingDirection == Carousel3DContext.RollingDirection.UP) {
                curChild.scaleX + SCALE_STEP
            } else {
                curChild.scaleX - SCALE_STEP
            }

            curAnimator.scaleX(scale)
            curAnimator.scaleY(scale)

            if (itemVisualIndex == visualItemsCount - 1) {
                if (rollingDirection == Carousel3DContext.RollingDirection.DOWN) {
                    curChild.visibility = View.VISIBLE
                    curAnimator.alpha(1f)
                }
            } else if (itemVisualIndex == 0
                && rollingDirection == Carousel3DContext.RollingDirection.UP)
            {
                curChild.visibility = View.VISIBLE
                curAnimator.alpha(1f)
            } else if (itemVisualIndex == 1
                && rollingDirection == Carousel3DContext.RollingDirection.DOWN)
            {
                curAnimator.alpha(0f)
            } else if (itemVisualIndex == visualItemsCount - 1 - 1
                && rollingDirection == Carousel3DContext.RollingDirection.UP)
            {
                curAnimator.alpha(0f)
            }

            curAnimator.withEndAction {
                ++itemRotationAfterSwipeCounter

                Log.d(TAG, "animateRotation() withEndAction: processedItemCount = $processedItemCount; itemRotationAfterSwipeCounter = $itemRotationAfterSwipeCounter")

                if (itemRotationAfterSwipeCounter != processedItemCount)
                    return@withEndAction

                postAnimationRunnable.run()

                isAnimating = false
                itemRotationAfterSwipeCounter = 0
            }

            curAnimator.duration = ROTATION_ANIMATION_DURATION
            curAnimator.interpolator = AccelerateDecelerateInterpolator()
            curAnimator.start()
        }

        Log.d(TAG, "exiting animateRotation()..")
    }

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        Log.d(TAG, "entering scrollHorizontallyBy()..")

        if (isAnimating || curScrollingOrientation == Carousel3DContext.ScrollOrientation.VERTICAL
            || carouselAdapter == null || isHorizontalSwipeRequested || isForegroundItemExtended)
        {
            return 0
        }

        if (curScrollingOrientation != Carousel3DContext.ScrollOrientation.HORIZONTAL)
            curScrollingOrientation = Carousel3DContext.ScrollOrientation.HORIZONTAL

        val curItem = getChildAt(visualItemsCount - 1 - 1)

        if (curItem == null) return 0

        Log.d(TAG, "scrollHorizontallyBy() dx: $dx; curScrollingOffset: $curScrollingHorizontalOffset")

        if (lastBouncingBackAnimator != null)
            lastBouncingBackAnimator?.cancel()

        if (curScrollingState == RecyclerView.SCROLL_STATE_DRAGGING) {
            curScrollingHorizontalOffset += -dx

            curItem.translationX = curScrollingHorizontalOffset.toFloat()

        } else if (curScrollingState == RecyclerView.SCROLL_STATE_SETTLING) {
            handleEndOfHorizontalScroll(curItem)

            return 0
        }

        return curScrollingHorizontalOffset
    }

    private fun handleEndOfHorizontalScroll(curItem: View) {
        val curPosition = getItemPositionFromVisualIndex(1)

        if (curScrollingHorizontalOffset <= HORIZONTAL_SCROLL_OFFSET_LEFT_SWIPE) {
            Log.d(TAG, "scrollHorizontallyBy() choosing the LEFT one")

            handleEndOfSidedHorizontalSwipe(curPosition, curItem, Carousel3DContext.SwipeDirection.LEFT)

        } else if (curScrollingHorizontalOffset >= HORIZONTAL_SCROLL_OFFSET_RIGHT_SWIPE) {
            Log.d(TAG, "scrollHorizontallyBy() choosing the RIGHT one")

            handleEndOfSidedHorizontalSwipe(curPosition, curItem, Carousel3DContext.SwipeDirection.RIGHT)

        } else {
            Log.d(TAG, "scrollHorizontallyBy() bouncing back..")

            playHorizontalSlidingAnimation(curItem) {
                curScrollingOrientation = Carousel3DContext.ScrollOrientation.NONE
            }
        }

        curScrollingHorizontalOffset = 0
    }

    private fun handleEndOfSidedHorizontalSwipe(
        curPosition: Int,
        curItem: View,
        direction: Carousel3DContext.SwipeDirection)
    {
        if (carouselAdapter?.areItemsSwipeable() == true) {
            isHorizontalSwipeRequested = true

            carouselAdapter!!.onHorizontalSwipe(curPosition, direction, this)

            playHorizontalSlidingAnimation(curItem, direction)

        } else {
            playHorizontalSlidingAnimation(curItem)
        }
    }

    private fun playHorizontalSlidingAnimation(
        item: View,
        direction: Carousel3DContext.SwipeDirection = Carousel3DContext.SwipeDirection.BACK,
        endAction: Runnable? = null)
    {
        val transitionX = when (direction) {
            Carousel3DContext.SwipeDirection.LEFT -> -(item.measuredWidth.toFloat())
            Carousel3DContext.SwipeDirection.RIGHT -> item.measuredWidth.toFloat()
            else -> 0f
        }

        item.let {
            lastBouncingBackAnimator = it.animate()
            lastBouncingBackAnimator?.apply {
                translationX(transitionX)
                duration = SLIDING_ANIMATION_DURATION
                interpolator = AccelerateDecelerateInterpolator()
            }?.withEndAction{
                lastBouncingBackAnimator = null

                endAction?.run()
            }?.start()
        }
    }

    private fun handleHorizontalSwipe(
        foregroundItem: View,
        action: Carousel3DContext.SwipeAction
    )
    {
        when (action) {
            Carousel3DContext.SwipeAction.NONE -> {
                playHorizontalSlidingAnimation(foregroundItem) {
                    curScrollingOrientation = Carousel3DContext.ScrollOrientation.NONE
                }
            }
            Carousel3DContext.SwipeAction.ERASE -> {
                curScrollingOrientation = Carousel3DContext.ScrollOrientation.NONE
                isHorizontalSwipeRequested = false

                if (itemCount == 0) {
                    requestLayout()

                    return
                }

                curAnimState = AnimState.HORIZONTAL_SWIPE

                animateRotation(Carousel3DContext.RollingDirection.UP,
                    fun (itemVisualIndex, itemView) : Boolean {
                    return (itemVisualIndex != visualItemsCount - 2
                            && itemVisualIndex != visualItemsCount - 1)
                }) {
                    curAnimState = AnimState.AFTER_HORIZONTAL_SWIPE
                    curItemIndexOffset = getItemIndexOffsetAfterHorizontalSwipe()

                    requestLayout()
                }
            }
            else -> { }
        }
    }

    private fun getItemIndexOffsetAfterHorizontalSwipe(): Int {
        if (itemCount <= 0) return 0

        val originalItemCount = itemCount + 1

        return if (originalItemCount <= 2) {
            0

        } else {
            if (curItemIndexOffset < -1) {
                curItemIndexOffset + 1

            } else if (curItemIndexOffset >= -1 && curItemIndexOffset != itemCount) {
                curItemIndexOffset

            } else {
                curItemIndexOffset - 1
            }
        }
    }

    private fun getItemIndexOffsetAfterVerticalRolling(
        direction: Carousel3DContext.RollingDirection): Int
    {
        if (itemCount <= 0) return 0

        return when (direction) {
            Carousel3DContext.RollingDirection.UP -> {
                if (curItemIndexOffset == itemCount - 1) 0 else
                    curItemIndexOffset + 1
            }
            Carousel3DContext.RollingDirection.DOWN -> {
                if (curItemIndexOffset == -(itemCount - 1)) 0 else
                    curItemIndexOffset - 1
            }
            else -> { 0 }
        }
    }

    private fun getItemPositionFromVisualIndex(itemVisualIndex: Int): Int {
        if (itemCount <= 0) return 0

        val curItemIndexBuffer = (itemVisualIndex + curItemIndexOffset)
        val curItemIndexPos =
            if (curItemIndexBuffer < 0) itemCount - (-curItemIndexBuffer)
            else curItemIndexBuffer

        return curItemIndexPos % itemCount
    }

    override fun onHorizontalSwipeAction(
        position: Int,
        swipeAction: Carousel3DContext.SwipeAction)
    {
        getChildAt(visualItemsCount - 2)?.let {
            handleHorizontalSwipe(it, swipeAction)
        }
    }
}