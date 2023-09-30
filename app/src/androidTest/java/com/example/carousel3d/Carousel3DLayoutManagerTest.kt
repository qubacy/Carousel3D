package com.example.carousel3d

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.carousel3d.data.repository.User
import com.example.carousel3d.databinding.UserCardBinding
import com.example.carousel3d.databinding.UserCardOpenableBinding
import com.example.carousel3d.view.main.MainActivity
import com.example.carousel3d.view.main.UserCardOpenable
import com.example.carousel3dlib.adapter.Carousel3DAdapter
import com.example.carousel3dlib.general.Carousel3DContext
import com.example.carousel3dlib.layoutmanager.Carousel3DLayoutManager
import com.example.carousel3dlib.view.Carousel3DOpenableView
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class Carousel3DLayoutManagerTest {
    companion object {
        const val TAG = "LayoutManagerTest"

        @JvmStatic
        @BeforeClass
        fun generalSetup(): Unit {

        }
    }

    class NoScrollViewAssertion() : ViewAssertion {
        override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
            if (view == null) throw NoMatchingViewException.Builder().build()
            if (view.scrollY != 0 || view.scrollX != 0)
                throw IllegalStateException("The view has been scrolled!")
        }

    }

    class HasUserWithUsernameAtPositionViewAssertion(
        val username: String, val position: Int) : ViewAssertion
    {
        override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
            if (view == null)
                throw noViewFoundException ?: NoMatchingViewException.Builder().build()

            val viewGroup = view as ViewGroup

            if (viewGroup.childCount <= position)
                throw NoMatchingViewException.Builder().build()

            val userView = viewGroup.getChildAt(position)
            val userViewBinding = UserCardBinding.bind(userView)

            if (!username.equals(userViewBinding.userCardContentUsername.text.toString()))
                throw NoMatchingViewException.Builder().build()
        }
    }

    @JvmField
    @Rule
    var mainActivityScenarioRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    private lateinit var mContext: Context
    private lateinit var mApplication: ApplicationTest

    private lateinit var mLayoutManager: Carousel3DLayoutManager
    private lateinit var mAdapter: Carousel3DAdapter<User>

    @Before
    fun setup() {
        mContext = InstrumentationRegistry.getInstrumentation().targetContext

        mainActivityScenarioRule.scenario.moveToState(Lifecycle.State.RESUMED)
        mainActivityScenarioRule.scenario.onActivity {
            mApplication = it.application as ApplicationTest

            it.findViewById<RecyclerView>(R.id.user_recycler_view).apply {
                mLayoutManager = layoutManager as Carousel3DLayoutManager
                mAdapter = adapter as Carousel3DAdapter<User>
            }
        }
    }

    @Test
    fun outputOnNoElementsTest() {
        val scenario = mainActivityScenarioRule.scenario

        scenario.moveToState(Lifecycle.State.RESUMED)

        Espresso.onView(ViewMatchers.withId(R.id.user_recycler_view))
            .check(ViewAssertions.matches(ViewMatchers.hasChildCount(0)))
    }

    private fun setUserList(userList: List<User>) {
        mainActivityScenarioRule.scenario.onActivity {
            mAdapter.setItems(userList)
        }
    }

    @Test
    fun verticalScrollingIsNotAllowedOnOnlyOneItemTest() {
        mainActivityScenarioRule.scenario.onActivity {
            mAdapter.setItems(listOf(
                User("User 1 Test", "Something..")
            ))
        }

        Espresso.onView(ViewMatchers.withId(R.id.user_recycler_view))
            .check(ViewAssertions.matches(ViewMatchers.hasChildCount(
                1 + Carousel3DLayoutManager.EDGE_INVISIBLE_ITEMS_COUNT)))
        Espresso.onView(ViewMatchers.withId(R.id.user_recycler_view))
            .perform(ViewActions.swipeDown())
            .check(NoScrollViewAssertion())
    }

    data class VerticalScrollingOnTwoItemsTestCase(
        val username: String,
        val position: Int
    )

    @Test
    fun verticalScrollingOnTwoItemsTest() {
        val userList = listOf(
            User("User 1 Test", "Something.."),
            User("User 2 Test", "Something else..")
        )

        setUserList(userList)

        Espresso.onView(ViewMatchers.withId(R.id.user_recycler_view))
            .check(ViewAssertions.matches(ViewMatchers.hasChildCount(
                2 + Carousel3DLayoutManager.EDGE_INVISIBLE_ITEMS_COUNT)))

        val viewInteraction = Espresso.onView(ViewMatchers.withId(R.id.user_recycler_view))
            .check(HasUserWithUsernameAtPositionViewAssertion(userList.get(0).username, 2))
        val testCases = listOf(
            VerticalScrollingOnTwoItemsTestCase(userList.get(1).username, 2),
            VerticalScrollingOnTwoItemsTestCase(userList.get(0).username, 2),
        )

        for (testCase in testCases) {
            viewInteraction
                .perform(ViewActions.swipeDown())
                .check(HasUserWithUsernameAtPositionViewAssertion(testCase.username, testCase.position))
        }
    }

    data class AutoRollingAfterAcceptedHorizontalSwipeTestCase(
        val childCount: Int,
        val position: Int
    )

    @Test
    fun autoRollingAfterAcceptedHorizontalSwipeOnTwoItemsTest() {
        val userList = mutableListOf(
            User("User 1 Test", "Something.."),
            User("User 2 Test", "Something else..")
        )

        setUserList(userList)

        Espresso.onView(ViewMatchers.withId(R.id.user_recycler_view))
            .check(ViewAssertions.matches(ViewMatchers.hasChildCount(
                2 + Carousel3DLayoutManager.EDGE_INVISIBLE_ITEMS_COUNT)))

        val testCases = listOf(
            AutoRollingAfterAcceptedHorizontalSwipeTestCase(
                1 + Carousel3DLayoutManager.EDGE_INVISIBLE_ITEMS_COUNT, 1),
//            AutoRollingAfterAcceptedHorizontalSwipeTestCase(
//                0, 0)
        )

        for (testCase in testCases) {
            Espresso.onView(ViewMatchers.withId(R.id.user_recycler_view))
                .apply {
                    userList.removeAt(0)
                }
                .perform(ViewActions.swipeLeft()).apply {
//                    userList.removeAt(0)
//                    setUserList(userList)
                }
                .check(ViewAssertions.matches(ViewMatchers.hasChildCount(testCase.childCount)))
                .apply {
                    if (userList.size <= 0) return@apply

                    check(HasUserWithUsernameAtPositionViewAssertion(
                        userList.get(0).username, testCase.position))
                }
        }
    }

    @Test
    fun autoRollingAfterAcceptedHorizontalSwipeOnFiveItemsTest() {
        val userList = mutableListOf(
            User("User 1 Test", "Something.."),
            User("User 2 Test", "Something else.."),
            User("User 3 Test", "Something else.."),
            User("User 4 Test", "Something else.."),
            User("User 5 Test", "Something else.."),
        )

        setUserList(userList)

        Espresso.onView(ViewMatchers.withId(R.id.user_recycler_view))
            .check(ViewAssertions.matches(ViewMatchers.hasChildCount(
                3 + Carousel3DLayoutManager.EDGE_INVISIBLE_ITEMS_COUNT)))

        val testCases = listOf(
            AutoRollingAfterAcceptedHorizontalSwipeTestCase(
                3 + Carousel3DLayoutManager.EDGE_INVISIBLE_ITEMS_COUNT, 3),
            AutoRollingAfterAcceptedHorizontalSwipeTestCase(
                3 + Carousel3DLayoutManager.EDGE_INVISIBLE_ITEMS_COUNT, 3),
            AutoRollingAfterAcceptedHorizontalSwipeTestCase(
                2 + Carousel3DLayoutManager.EDGE_INVISIBLE_ITEMS_COUNT, 2),
            AutoRollingAfterAcceptedHorizontalSwipeTestCase(
                1 + Carousel3DLayoutManager.EDGE_INVISIBLE_ITEMS_COUNT, 1),
//            AutoRollingAfterAcceptedHorizontalSwipeTestCase(
//                0, 0),
        )

        for (testCase in testCases) {
            Log.d(TAG, "autoRollingAfterAcceptedHorizontalSwipeOnFiveItemsTest(): userList.size = ${userList.size}; testCase.childCount = ${testCase.childCount}")

            Espresso.onView(ViewMatchers.withId(R.id.user_recycler_view))
                .apply {
                    userList.removeAt(0)
                }
                .perform(ViewActions.swipeLeft())
                .check(ViewAssertions.matches(ViewMatchers.hasChildCount(testCase.childCount)))
                .apply {
                    if (userList.size <= 0) return@apply

                    check(HasUserWithUsernameAtPositionViewAssertion(
                        userList.get(0).username, testCase.position))
                }
        }
    }

    data class AutoRollingAfterAcceptedHorizontalSwipeWithVerticalScrollTestCase(
        val childCount: Int,
        val position: Int,
        val preRollingDirection: Carousel3DContext.RollingDirection,
        val userIndexToRemove: Int,
        val curUserIndex: Int
    )

    @Test
    fun autoRollingAfterAcceptedHorizontalSwipeOnNineItemsWithVerticalScrollTest() {
        val userList = mutableListOf(
            User("User 1 Test", "Something.."),
            User("User 2 Test", "Something else.."),
            User("User 3 Test", "Something else.."),
            User("User 4 Test", "Something else.."),
            User("User 5 Test", "Something else.."),
            User("User 6 Test", "Something else.."),
            User("User 7 Test", "Something else.."),
            User("User 8 Test", "Something else.."),
            User("User 9 Test", "Something else.."),
        )

        setUserList(userList)

        Espresso.onView(ViewMatchers.withId(R.id.user_recycler_view))
            .check(ViewAssertions.matches(ViewMatchers.hasChildCount(
                3 + Carousel3DLayoutManager.EDGE_INVISIBLE_ITEMS_COUNT)))

        val testCases = listOf(
            AutoRollingAfterAcceptedHorizontalSwipeWithVerticalScrollTestCase(
                3 + Carousel3DLayoutManager.EDGE_INVISIBLE_ITEMS_COUNT, 3, Carousel3DContext.RollingDirection.DOWN, 8, 0), // after cur: User 1
            AutoRollingAfterAcceptedHorizontalSwipeWithVerticalScrollTestCase(
                3 + Carousel3DLayoutManager.EDGE_INVISIBLE_ITEMS_COUNT, 3, Carousel3DContext.RollingDirection.NONE, 0, 0), // after cur: User 2
            AutoRollingAfterAcceptedHorizontalSwipeWithVerticalScrollTestCase(
                3 + Carousel3DLayoutManager.EDGE_INVISIBLE_ITEMS_COUNT, 3, Carousel3DContext.RollingDirection.UP, 1, 1), // after cur: User 4
            AutoRollingAfterAcceptedHorizontalSwipeWithVerticalScrollTestCase(
                3 + Carousel3DLayoutManager.EDGE_INVISIBLE_ITEMS_COUNT, 3, Carousel3DContext.RollingDirection.UP, 2, 2), // after cur: User 6
            AutoRollingAfterAcceptedHorizontalSwipeWithVerticalScrollTestCase(
                3 + Carousel3DLayoutManager.EDGE_INVISIBLE_ITEMS_COUNT, 3, Carousel3DContext.RollingDirection.DOWN, 1, 1), // after cur: User 6
            AutoRollingAfterAcceptedHorizontalSwipeWithVerticalScrollTestCase(
                3 + Carousel3DLayoutManager.EDGE_INVISIBLE_ITEMS_COUNT, 3, Carousel3DContext.RollingDirection.NONE, 1, 1), // after cur: User 7
            AutoRollingAfterAcceptedHorizontalSwipeWithVerticalScrollTestCase(
                2 + Carousel3DLayoutManager.EDGE_INVISIBLE_ITEMS_COUNT, 2, Carousel3DContext.RollingDirection.NONE, 1, 1), // after cur: User 8
            AutoRollingAfterAcceptedHorizontalSwipeWithVerticalScrollTestCase(
                1 + Carousel3DLayoutManager.EDGE_INVISIBLE_ITEMS_COUNT, 1, Carousel3DContext.RollingDirection.DOWN, 0,0), // after cur: User 8
//            AutoRollingAfterAcceptedHorizontalSwipeWithVerticalScrollTestCase(
//                0, 0, Carousel3DContext.RollingDirection.NONE, 0, 0), // no items
        )

        for (testCase in testCases) {
            Log.d(TAG, "autoRollingAfterAcceptedHorizontalSwipeOnNineItemsWithVerticalScrollTest(): userList.size = ${userList.size}; cur. user index = ${testCase.curUserIndex}; testCase.childCount = ${testCase.childCount}")

            Espresso.onView(ViewMatchers.withId(R.id.user_recycler_view))
                .apply {
                    when (testCase.preRollingDirection) {
                        Carousel3DContext.RollingDirection.DOWN -> {perform(ViewActions.swipeUp())}
                        Carousel3DContext.RollingDirection.UP -> {perform(ViewActions.swipeDown())}
                        else -> {}
                    }

                    val removedUser = userList.removeAt(testCase.userIndexToRemove)

                    Log.d(TAG, "autoRollingAfterAcceptedHorizontalSwipeOnNineItemsWithVerticalScrollTest(): removedUser.username = ${removedUser.username}")

                }
                .perform(ViewActions.swipeLeft())
                .check(ViewAssertions.matches(ViewMatchers.hasChildCount(testCase.childCount)))
                .apply {
                    if (userList.size <= 0) return@apply

                    check(HasUserWithUsernameAtPositionViewAssertion(
                        userList.get(testCase.curUserIndex).username, testCase.position))
                }
        }
    }

    class CloseOpenableViewByBackgroundClick() : ViewAction {
        override fun getDescription(): String {
            return "Clicking top or bottom of the provided container to close the Openable View."
        }

        override fun getConstraints(): org.hamcrest.Matcher<View> {
            return Matchers.allOf(
                ViewMatchers.isAssignableFrom(Carousel3DOpenableView::class.java),
                ViewMatchers.isDisplayed()
            )
        }

        override fun perform(uiController: UiController?, view: View?) {
            view!!.performClick()
        }
    }

    class ClickForegroundUserItemViewAction() : ViewAction {
        override fun getDescription(): String {
            return "Clicking the foreground user item of the Carousel3D"
        }

        override fun getConstraints(): org.hamcrest.Matcher<View> {
            return Matchers.allOf(
                ViewMatchers.isAssignableFrom(RecyclerView::class.java),
                ViewMatchers.hasMinimumChildCount(1))
        }

        override fun perform(uiController: UiController?, view: View?) {
            if (uiController == null)
                throw Throwable("uiController hasn't been provided!")

            val viewGroup = view as ViewGroup
            val foregroundView = viewGroup.getChildAt(viewGroup.childCount - 1 - 1)

            if (foregroundView.visibility != View.VISIBLE)
                throw Throwable("Incorrect views configuration has been detected!")

            foregroundView.performClick()
        }
    }

    @Test
    fun itemIsExpandedOnItemClickThenClosedByBackgroundClickTest() {
        val userList = listOf(
            User("User 1 Test", "Some description..")
        )

        setUserList(userList)

        Espresso.onView(ViewMatchers.withId(R.id.user_recycler_view))
            .check(ViewAssertions.matches(ViewMatchers.hasChildCount(
                1 + Carousel3DLayoutManager.EDGE_INVISIBLE_ITEMS_COUNT)))

        Espresso.onView(ViewMatchers.withId(R.id.user_recycler_view))
            .perform(ClickForegroundUserItemViewAction())
        Espresso.onView(ViewMatchers.isAssignableFrom(Carousel3DOpenableView::class.java))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            .perform(CloseOpenableViewByBackgroundClick())
            .check(ViewAssertions.matches(
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    class OpenableViewHasProvidedUsernameViewAssertion(
        val username: String
    ) : ViewAssertion {
        override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
            if (view == null)
                throw NoMatchingViewException.Builder().build()
            if (view !is UserCardOpenable)
                throw NoMatchingViewException.Builder().build()

            val userCardOpenable = view as UserCardOpenable
            val userCardOpenableBinding = UserCardOpenableBinding.bind(userCardOpenable)

            if (!userCardOpenableBinding.userCardOpenableContent
                    .userCardContentUsername.text.equals(username))
            {
                throw NoMatchingViewException.Builder().build()
            }
        }
    }

    @Test
    fun expandedItemChangesContentOnDifferentForegroundItemClickedTest() {
        val userList = listOf(
            User("User 1 Test", "Some description.."),
            User("User 2 Test", "Another desc..")
        )

        setUserList(userList)

        Espresso.onView(ViewMatchers.withId(R.id.user_recycler_view))
            .check(ViewAssertions.matches(ViewMatchers.hasChildCount(
                2 + Carousel3DLayoutManager.EDGE_INVISIBLE_ITEMS_COUNT)))

        Espresso.onView(ViewMatchers.withId(R.id.user_recycler_view))
            .perform(ClickForegroundUserItemViewAction())
        Espresso.onView(ViewMatchers.isAssignableFrom(Carousel3DOpenableView::class.java))
            .check(OpenableViewHasProvidedUsernameViewAssertion(userList.get(0).username))
            .perform(CloseOpenableViewByBackgroundClick())
        Espresso.onView(ViewMatchers.withId(R.id.user_recycler_view))
            .perform(ViewActions.swipeDown())
            .perform(ClickForegroundUserItemViewAction())
        Espresso.onView(ViewMatchers.isAssignableFrom(Carousel3DOpenableView::class.java))
            .check(OpenableViewHasProvidedUsernameViewAssertion(userList.get(1).username))
    }
}