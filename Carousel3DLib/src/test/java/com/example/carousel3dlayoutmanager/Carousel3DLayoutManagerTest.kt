package com.example.carousel3dlayoutmanager

import android.view.View
import com.example.carousel3dlib.layoutmanager.Carousel3DLayoutManager
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import java.lang.reflect.Field
import java.lang.reflect.Method

class Carousel3DLayoutManagerTest {
    companion object {
        const val TAG = "LayoutManagerTest"
    }

    class GetItemPositionFromVisualIndexTestData(
        val spiedLayoutManager: Carousel3DLayoutManager,
        val curItemIndexOffsetReflection: Field,
        val getItemPositionFromVisualIndexMethodReflection: Method)
    {
        private var mSpiedItemCount = 0

        fun setSpiedItemCount(itemCount: Int) {
            mSpiedItemCount = itemCount

            doReturn(itemCount).`when`(spiedLayoutManager)
                .getItemCount()
        }

        fun setCurItemIndexOffset(curItemIndexOffset: Int) {
            curItemIndexOffsetReflection.setInt(spiedLayoutManager, curItemIndexOffset)
        }

        fun getItemPositionFromVisualIndex(itemVisualIndex: Int): Int {
            return getItemPositionFromVisualIndexMethodReflection
                .invoke(spiedLayoutManager, itemVisualIndex) as Int
        }
    }

    class GetItemIndexOffsetAfterHorizontalSwipeTestData(
        val spiedLayoutManager: Carousel3DLayoutManager,
        val curItemIndexOffsetReflection: Field,
        val getItemPositionFromVisualIndexMethodReflection: Method)
    {
        fun setSpiedItemCount(itemCount: Int) {
            doReturn(itemCount).`when`(spiedLayoutManager)
                .getItemCount()
        }

        fun setCurItemIndexOffset(curItemIndexOffset: Int) {
            curItemIndexOffsetReflection.setInt(spiedLayoutManager, curItemIndexOffset)
        }

        fun getItemIndexOffsetAfterHorizontalSwipe(): Int {
            return getItemPositionFromVisualIndexMethodReflection
                .invoke(spiedLayoutManager) as Int
        }
    }

    class CalculateTopCenteringOffsetByItemViewTestData(
        val spiedLayoutManager: Carousel3DLayoutManager,
        val spiedItemView: View,
        val calculateTopCenteringOffsetsByItemViewMethodReflection: Method,
        val topCenteringOffsetPxFieldReflection: Field)
    {
        fun setSpiedHeight(height: Int) {
            doReturn(height).`when`(spiedLayoutManager).getHeight()
        }

        fun setSpiedItemMeasuredHeight(itemMeasuredHeight: Int) {
            `when`(spiedItemView.measuredHeight).thenReturn(itemMeasuredHeight)
        }

        fun calculateTopCenteringOffsetsByItemView() {
            calculateTopCenteringOffsetsByItemViewMethodReflection.invoke(
                spiedLayoutManager, spiedItemView)
        }

        fun getTopCenteringOffsetPx(): Int {
            return topCenteringOffsetPxFieldReflection.getInt(spiedLayoutManager)
        }
    }

    private lateinit var layoutManager: Carousel3DLayoutManager
    private lateinit var itemView: View

    private lateinit var getItemPositionFromVisualIndexTestData:
            GetItemPositionFromVisualIndexTestData
    private lateinit var getItemIndexOffsetAfterHorizontalSwipeTestData:
            GetItemIndexOffsetAfterHorizontalSwipeTestData
    private lateinit var calculateTopCenteringOffsetByItemViewTestData:
            CalculateTopCenteringOffsetByItemViewTestData

    @Before
    fun setup() {
        layoutManager = Carousel3DLayoutManager()
        itemView = mock(View::class.java)

        val curItemIndexOffsetField =
            layoutManager::class.java.getDeclaredField("curItemIndexOffset")
                .apply {
                    isAccessible = true
                }
        val getItemPositionFromVisualIndexMethod =
            layoutManager::class.java.getDeclaredMethod(
                "getItemPositionFromVisualIndex", Int::class.java)
                .apply {
                    isAccessible = true
                }
        val getItemIndexOffsetAfterHorizontalSwipeMethod =
            layoutManager::class.java.getDeclaredMethod(
                "getItemIndexOffsetAfterHorizontalSwipe")
                .apply {
                    isAccessible = true
                }
        val topCenteringOffsetPxField =
            layoutManager::class.java.getDeclaredField("topCenteringOffsetPx")
                .apply {
                    isAccessible = true
                }
        val calculateTopCenteringOffsetsByItemViewMethod =
            layoutManager::class.java.getDeclaredMethod(
                "calculateTopCenteringOffsetsByItemView", View::class.java)
                .apply {
                    isAccessible = true
                }

        getItemPositionFromVisualIndexTestData =
            GetItemPositionFromVisualIndexTestData(
                spy(layoutManager),
                curItemIndexOffsetField,
                getItemPositionFromVisualIndexMethod)
        getItemIndexOffsetAfterHorizontalSwipeTestData =
            GetItemIndexOffsetAfterHorizontalSwipeTestData(
                spy(layoutManager),
                curItemIndexOffsetField,
                getItemIndexOffsetAfterHorizontalSwipeMethod)
        calculateTopCenteringOffsetByItemViewTestData =
            CalculateTopCenteringOffsetByItemViewTestData(
                spy(layoutManager),
                itemView,
                calculateTopCenteringOffsetsByItemViewMethod,
                topCenteringOffsetPxField)
    }

    // Carousel3DLayoutManager.getItemPositionFromVisualIndex:

    data class GetItemPositionFromVisualIndexTestCase(
        val itemIndexOffset: Int,
        val itemVisualIndex: Int,
        val expectedItemPosition: Int
    )

    @Test
    fun getItemPositionFromVisualIndexForZeroItemsTest() {
        val itemCount = 0

        getItemPositionFromVisualIndexTestData.setSpiedItemCount(itemCount)

        val itemIndexOffset = 0
        val itemVisualIndex = 0
        val expectedItemPosition = 0

        getItemPositionFromVisualIndexTestData.setCurItemIndexOffset(itemIndexOffset)

        val curItemPositionFromVisualIndex =
            getItemPositionFromVisualIndexTestData.getItemPositionFromVisualIndex(itemVisualIndex)

        Assert.assertEquals(expectedItemPosition, curItemPositionFromVisualIndex)
    }

    @Test
    fun getItemPositionFromVisualIndexForOneItemTest() {
        val itemCount = 1

        getItemPositionFromVisualIndexTestData.setSpiedItemCount(itemCount)

        val testCases = listOf<GetItemPositionFromVisualIndexTestCase>(
//            GetItemPositionFromVisualIndexTestCase(-1,0,0),
//            GetItemPositionFromVisualIndexTestCase(-1,1,0),
//            GetItemPositionFromVisualIndexTestCase(-1,2,0),

            GetItemPositionFromVisualIndexTestCase(0,0,0),
            GetItemPositionFromVisualIndexTestCase(0,1,0),
            GetItemPositionFromVisualIndexTestCase(0,2,0),

//            GetItemPositionFromVisualIndexTestCase(1,0,0),
//            GetItemPositionFromVisualIndexTestCase(1,1,0),
//            GetItemPositionFromVisualIndexTestCase(1,2,0)
        )

        for (curTestCase in testCases) {
            System.out.println("$TAG curTestCase: indexOffset = ${curTestCase.itemIndexOffset}; visualIndex = ${curTestCase.itemVisualIndex}; expectedItemPosition = ${curTestCase.expectedItemPosition}")

            getItemPositionFromVisualIndexTestData.setCurItemIndexOffset(curTestCase.itemIndexOffset)

            val curItemPositionFromVisualIndex =
                getItemPositionFromVisualIndexTestData.getItemPositionFromVisualIndex(
                    curTestCase.itemVisualIndex)

            Assert.assertEquals(curTestCase.expectedItemPosition, curItemPositionFromVisualIndex)
        }
    }

    @Test
    fun getItemPositionFromVisualIndexForTwoItemsTest() {
        val itemCount = 2

        getItemPositionFromVisualIndexTestData.setSpiedItemCount(itemCount)

        val testCases = listOf<GetItemPositionFromVisualIndexTestCase>(
//            GetItemPositionFromVisualIndexTestCase(-2,0,0),
//            GetItemPositionFromVisualIndexTestCase(-2,1,1),
//            GetItemPositionFromVisualIndexTestCase(-2,2,0),
//            GetItemPositionFromVisualIndexTestCase(-2,3,1),

            GetItemPositionFromVisualIndexTestCase(-1,0,1),
            GetItemPositionFromVisualIndexTestCase(-1,1,0),
            GetItemPositionFromVisualIndexTestCase(-1,2,1),
            GetItemPositionFromVisualIndexTestCase(-1,3,0),

            GetItemPositionFromVisualIndexTestCase(0,0,0),
            GetItemPositionFromVisualIndexTestCase(0,1,1),
            GetItemPositionFromVisualIndexTestCase(0,2,0),
            GetItemPositionFromVisualIndexTestCase(0,3,1),

            GetItemPositionFromVisualIndexTestCase(1,0,1),
            GetItemPositionFromVisualIndexTestCase(1,1,0),
            GetItemPositionFromVisualIndexTestCase(1,2,1),
            GetItemPositionFromVisualIndexTestCase(1,3,0),

//            GetItemPositionFromVisualIndexTestCase(2,0,0),
//            GetItemPositionFromVisualIndexTestCase(2,1,1),
//            GetItemPositionFromVisualIndexTestCase(2,2,0),
//            GetItemPositionFromVisualIndexTestCase(2,3,1)
        )

        for (curTestCase in testCases) {
            System.out.println("$TAG curTestCase: indexOffset = ${curTestCase.itemIndexOffset}; visualIndex = ${curTestCase.itemVisualIndex}; expectedItemPosition = ${curTestCase.expectedItemPosition}")

            getItemPositionFromVisualIndexTestData.setCurItemIndexOffset(curTestCase.itemIndexOffset)

            val curItemPositionFromVisualIndex =
                getItemPositionFromVisualIndexTestData.getItemPositionFromVisualIndex(
                    curTestCase.itemVisualIndex)

            Assert.assertEquals(curTestCase.expectedItemPosition, curItemPositionFromVisualIndex)
        }
    }

    @Test
    fun getItemPositionFromVisualIndexForFiveItemsTest() {
        val itemCount = 5

        getItemPositionFromVisualIndexTestData.setSpiedItemCount(itemCount)

        val testCases = listOf<GetItemPositionFromVisualIndexTestCase>(
//            GetItemPositionFromVisualIndexTestCase(-5,0,0),
//            GetItemPositionFromVisualIndexTestCase(-5,1,1),
//            GetItemPositionFromVisualIndexTestCase(-5,2,2),
//            GetItemPositionFromVisualIndexTestCase(-5,3,3),
//            GetItemPositionFromVisualIndexTestCase(-5,4,4),

            GetItemPositionFromVisualIndexTestCase(-4,0,1),
            GetItemPositionFromVisualIndexTestCase(-4,1,2),
            GetItemPositionFromVisualIndexTestCase(-4,2,3),
            GetItemPositionFromVisualIndexTestCase(-4,3,4),
            GetItemPositionFromVisualIndexTestCase(-4,4,0),

            GetItemPositionFromVisualIndexTestCase(-3,0,2),
            GetItemPositionFromVisualIndexTestCase(-3,1,3),
            GetItemPositionFromVisualIndexTestCase(-3,2,4),
            GetItemPositionFromVisualIndexTestCase(-3,3,0),
            GetItemPositionFromVisualIndexTestCase(-3,4,1),

            GetItemPositionFromVisualIndexTestCase(-2,0,3),
            GetItemPositionFromVisualIndexTestCase(-2,1,4),
            GetItemPositionFromVisualIndexTestCase(-2,2,0),
            GetItemPositionFromVisualIndexTestCase(-2,3,1),
            GetItemPositionFromVisualIndexTestCase(-2,4,2),

            GetItemPositionFromVisualIndexTestCase(-1,0,4),
            GetItemPositionFromVisualIndexTestCase(-1,1,0),
            GetItemPositionFromVisualIndexTestCase(-1,2,1),
            GetItemPositionFromVisualIndexTestCase(-1,3,2),
            GetItemPositionFromVisualIndexTestCase(-1,4,3),

            GetItemPositionFromVisualIndexTestCase(0,0,0),
            GetItemPositionFromVisualIndexTestCase(0,1,1),
            GetItemPositionFromVisualIndexTestCase(0,2,2),
            GetItemPositionFromVisualIndexTestCase(0,3,3),
            GetItemPositionFromVisualIndexTestCase(0,4,4),

            GetItemPositionFromVisualIndexTestCase(1,0,1),
            GetItemPositionFromVisualIndexTestCase(1,1,2),
            GetItemPositionFromVisualIndexTestCase(1,2,3),
            GetItemPositionFromVisualIndexTestCase(1,3,4),
            GetItemPositionFromVisualIndexTestCase(1,4,0),

            GetItemPositionFromVisualIndexTestCase(2,0,2),
            GetItemPositionFromVisualIndexTestCase(2,1,3),
            GetItemPositionFromVisualIndexTestCase(2,2,4),
            GetItemPositionFromVisualIndexTestCase(2,3,0),
            GetItemPositionFromVisualIndexTestCase(2,4,1),

            GetItemPositionFromVisualIndexTestCase(3,0,3),
            GetItemPositionFromVisualIndexTestCase(3,1,4),
            GetItemPositionFromVisualIndexTestCase(3,2,0),
            GetItemPositionFromVisualIndexTestCase(3,3,1),
            GetItemPositionFromVisualIndexTestCase(3,4,2),

            GetItemPositionFromVisualIndexTestCase(4,0,4),
            GetItemPositionFromVisualIndexTestCase(4,1,0),
            GetItemPositionFromVisualIndexTestCase(4,2,1),
            GetItemPositionFromVisualIndexTestCase(4,3,2),
            GetItemPositionFromVisualIndexTestCase(4,4,3),

//            GetItemPositionFromVisualIndexTestCase(5,0,0),
//            GetItemPositionFromVisualIndexTestCase(5,1,1),
//            GetItemPositionFromVisualIndexTestCase(5,2,2),
//            GetItemPositionFromVisualIndexTestCase(5,3,3),
//            GetItemPositionFromVisualIndexTestCase(5,4,4),
        )

        for (curTestCase in testCases) {
            System.out.println("$TAG curTestCase: indexOffset = ${curTestCase.itemIndexOffset}; visualIndex = ${curTestCase.itemVisualIndex}; expectedItemPosition = ${curTestCase.expectedItemPosition}")

            getItemPositionFromVisualIndexTestData.setCurItemIndexOffset(curTestCase.itemIndexOffset)

            val curItemPositionFromVisualIndex =
                getItemPositionFromVisualIndexTestData.getItemPositionFromVisualIndex(
                    curTestCase.itemVisualIndex)

            Assert.assertEquals(curTestCase.expectedItemPosition, curItemPositionFromVisualIndex)
        }
    }

    // Carousel3DLayoutManager.getItemIndexOffsetAfterHorizontalSwipe:

    data class GetItemIndexOffsetAfterHorizontalSwipeTestCase(
        val itemCount: Int,
        val itemIndexOffset: Int,
        val expectedItemIndexOffset: Int)

    @Test
    fun getItemIndexOffsetAfterHorizontalSwipeForZeroItemsTest() {
        val itemCount = 0

        getItemIndexOffsetAfterHorizontalSwipeTestData.setSpiedItemCount(itemCount)

        val itemIndexOffset = 0
        val expectedItemIndexOffset = 0

        getItemIndexOffsetAfterHorizontalSwipeTestData.setCurItemIndexOffset(itemIndexOffset)

        val newItemIndexOffset =
            getItemIndexOffsetAfterHorizontalSwipeTestData.getItemIndexOffsetAfterHorizontalSwipe()

        Assert.assertEquals(expectedItemIndexOffset, newItemIndexOffset)
    }

    @Test
    fun getItemIndexOffsetAfterHorizontalSwipeTest() {
        val testCases = listOf<GetItemIndexOffsetAfterHorizontalSwipeTestCase>(
            GetItemIndexOffsetAfterHorizontalSwipeTestCase(0, 0, 0),

            GetItemIndexOffsetAfterHorizontalSwipeTestCase(1, 0, 0),

            GetItemIndexOffsetAfterHorizontalSwipeTestCase(2, -1, -1),
            GetItemIndexOffsetAfterHorizontalSwipeTestCase(2, 0, 0),
            GetItemIndexOffsetAfterHorizontalSwipeTestCase(2, 1, 1),

            GetItemIndexOffsetAfterHorizontalSwipeTestCase(3, -2, -1),
            GetItemIndexOffsetAfterHorizontalSwipeTestCase(3, -1, -1),
            GetItemIndexOffsetAfterHorizontalSwipeTestCase(3, 0, 0),
            GetItemIndexOffsetAfterHorizontalSwipeTestCase(3, 1, 1),
            GetItemIndexOffsetAfterHorizontalSwipeTestCase(3, 2, 2),

            GetItemIndexOffsetAfterHorizontalSwipeTestCase(5, -4, -3),
            GetItemIndexOffsetAfterHorizontalSwipeTestCase(5, -3, -2),
            GetItemIndexOffsetAfterHorizontalSwipeTestCase(5, -2, -1),
            GetItemIndexOffsetAfterHorizontalSwipeTestCase(5, -1, -1),
            GetItemIndexOffsetAfterHorizontalSwipeTestCase(5, 0, 0),
            GetItemIndexOffsetAfterHorizontalSwipeTestCase(5, 1, 1),
            GetItemIndexOffsetAfterHorizontalSwipeTestCase(5, 2, 2),
            GetItemIndexOffsetAfterHorizontalSwipeTestCase(5, 3, 3),
            GetItemIndexOffsetAfterHorizontalSwipeTestCase(5, 4, 4),

            GetItemIndexOffsetAfterHorizontalSwipeTestCase(9, -8, -7),
            GetItemIndexOffsetAfterHorizontalSwipeTestCase(9, -7, -6),
            GetItemIndexOffsetAfterHorizontalSwipeTestCase(9, -6, -5),
            GetItemIndexOffsetAfterHorizontalSwipeTestCase(9, -5, -4),
            GetItemIndexOffsetAfterHorizontalSwipeTestCase(9, -4, -3),
            GetItemIndexOffsetAfterHorizontalSwipeTestCase(9, -3, -2),
            GetItemIndexOffsetAfterHorizontalSwipeTestCase(9, -2, -1),
            GetItemIndexOffsetAfterHorizontalSwipeTestCase(9, -1, -1),
            GetItemIndexOffsetAfterHorizontalSwipeTestCase(9, 0, 0),
            GetItemIndexOffsetAfterHorizontalSwipeTestCase(9, 1, 1),
            GetItemIndexOffsetAfterHorizontalSwipeTestCase(9, 2, 2),
            GetItemIndexOffsetAfterHorizontalSwipeTestCase(9, 3, 3),
            GetItemIndexOffsetAfterHorizontalSwipeTestCase(9, 4, 4),
            GetItemIndexOffsetAfterHorizontalSwipeTestCase(9, 5, 5),
            GetItemIndexOffsetAfterHorizontalSwipeTestCase(9, 6, 6),
            GetItemIndexOffsetAfterHorizontalSwipeTestCase(9, 7, 7),
            GetItemIndexOffsetAfterHorizontalSwipeTestCase(9, 8, 8),
        )

        for (testCase in testCases) {
            System.out.println("$TAG.getItemIndexOffsetAfterHorizontalSwipeTest(): itemCount = ${testCase.itemCount}; itemIndexOffset = ${testCase.itemIndexOffset}; expectedItemIndexOffset = ${testCase.expectedItemIndexOffset};")

            getItemIndexOffsetAfterHorizontalSwipeTestData.setSpiedItemCount(testCase.itemCount)
            getItemIndexOffsetAfterHorizontalSwipeTestData.setCurItemIndexOffset(testCase.itemIndexOffset)

            val newItemIndexOffset =
                getItemIndexOffsetAfterHorizontalSwipeTestData.getItemIndexOffsetAfterHorizontalSwipe()

            Assert.assertEquals(testCase.expectedItemIndexOffset, newItemIndexOffset)
        }
    }

    // Carousel3DLayoutManager.calculateTopCenteringOffsetsByItemView:

    data class CalculateTopCenteringOffsetByItemViewTestCase(
        val height: Int,
        val itemMeasuredHeight: Int,
        val expectedTopCenteringOffsetPx: Int
    )

    @Test
    fun calculateTopCenteringOffsetByItemViewTest() {
        val testCases = listOf<CalculateTopCenteringOffsetByItemViewTestCase>(
            CalculateTopCenteringOffsetByItemViewTestCase(2000, 200, 815),
            CalculateTopCenteringOffsetByItemViewTestCase(2000, 400, 630),
            CalculateTopCenteringOffsetByItemViewTestCase(1000, 400, 130),
            CalculateTopCenteringOffsetByItemViewTestCase(500, 300, 0)
        )

        for (testCase in testCases) {
            calculateTopCenteringOffsetByItemViewTestData.apply {
                setSpiedHeight(testCase.height)
                setSpiedItemMeasuredHeight(testCase.itemMeasuredHeight)
                calculateTopCenteringOffsetsByItemView()
            }

            val topCenteringOffsetPx = calculateTopCenteringOffsetByItemViewTestData.getTopCenteringOffsetPx()

            System.out.println("$TAG.calculateTopCenteringOffsetByItemViewTest(): height = ${testCase.height}; itemMeasuredHeight = ${testCase.itemMeasuredHeight}; expectedTopCenteringOffsetPx = ${testCase.expectedTopCenteringOffsetPx};")

            Assert.assertEquals(testCase.expectedTopCenteringOffsetPx, topCenteringOffsetPx)
        }
    }
}