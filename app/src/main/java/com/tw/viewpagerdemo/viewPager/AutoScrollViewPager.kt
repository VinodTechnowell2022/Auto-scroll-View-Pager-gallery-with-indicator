package com.tw.viewpagerdemo.viewPager

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.Interpolator
import androidx.core.view.MotionEventCompat
import androidx.viewpager.widget.ViewPager
import java.lang.ref.WeakReference

class AutoScrollViewPager : ViewPager {
    var interval: Long = DEFAULT_INTERVAL.toLong()
    private var direction = RIGHT
    var isCycle: Boolean = true
    var isStopScrollWhenTouch: Boolean = true
    var slideBorderMode: Int = SLIDE_BORDER_MODE_NONE
    var isBorderAnimation: Boolean = true
    private var autoScrollFactor = 1.0
    private var swipeScrollFactor = 1.0

    private var handler: Handler? = null
    private var isAutoScroll = false
    private var isStopByTouch = false
    private var touchX = 0f
    private var downX = 0f
    private var scroller: CustomDurationScroller? = null

    constructor(paramContext: Context?) : super(paramContext!!) {
        init()
    }

    constructor(paramContext: Context?, paramAttributeSet: AttributeSet?) : super(
        paramContext!!, paramAttributeSet
    ) {
        init()
    }

    private fun init() {
        handler = MyHandler(this)
        setViewPagerScroller()
    }

    fun startAutoScroll() {
        isAutoScroll = true
        sendScrollMessage((interval + scroller!!.duration / autoScrollFactor * swipeScrollFactor).toLong())
    }

    fun startAutoScroll(delayTimeInMills: Int) {
        isAutoScroll = true
        sendScrollMessage(delayTimeInMills.toLong())
    }

    fun stopAutoScroll() {
        isAutoScroll = false
        handler!!.removeMessages(SCROLL_WHAT)
    }


    fun setSwipeScrollDurationFactor(scrollFactor: Double) {
        swipeScrollFactor = scrollFactor
    }


    fun setAutoScrollDurationFactor(scrollFactor: Double) {
        autoScrollFactor = scrollFactor
    }

    private fun sendScrollMessage(delayTimeInMills: Long) {
        /* remove messages before, keeps one message is running at most */
        handler!!.removeMessages(SCROLL_WHAT)
        handler!!.sendEmptyMessageDelayed(SCROLL_WHAT, delayTimeInMills)
    }

    private fun setViewPagerScroller() {
        try {
            val scrollerField = ViewPager::class.java.getDeclaredField("mScroller")
            scrollerField.isAccessible = true
            val interpolatorField = ViewPager::class.java.getDeclaredField("sInterpolator")
            interpolatorField.isAccessible = true

            scroller = CustomDurationScroller(context, interpolatorField[null] as Interpolator)
            scrollerField[this] = scroller
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun scrollOnce() {
        try {
            val adapter = adapter
            var currentItem = currentItem
            var totalCount: Int = 0
            if (adapter == null || (adapter.count.also { totalCount = it }) <= 1) {
                return
            }

            val nextItem = if ((direction == LEFT)) --currentItem else ++currentItem
            if (nextItem < 0) {
                if (isCycle) {
                    setCurrentItem(totalCount - 1, isBorderAnimation)
                }
            } else if (nextItem == totalCount) {
                if (isCycle) {
                    setCurrentItem(0, isBorderAnimation)
                }
            } else {
                setCurrentItem(nextItem, true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val action = MotionEventCompat.getActionMasked(ev)

        if (isStopScrollWhenTouch) {
            if ((action == MotionEvent.ACTION_DOWN) && isAutoScroll) {
                isStopByTouch = true
                stopAutoScroll()
            } else if (ev.action == MotionEvent.ACTION_UP && isStopByTouch) {
                startAutoScroll()
            }
        }

        if (slideBorderMode == SLIDE_BORDER_MODE_TO_PARENT || slideBorderMode == SLIDE_BORDER_MODE_CYCLE) {
            touchX = ev.x
            if (ev.action == MotionEvent.ACTION_DOWN) {
                downX = touchX
            }
            val currentItem = currentItem
            val adapter = adapter
            val pageCount = adapter?.count ?: 0

            if ((currentItem == 0 && downX <= touchX) || (currentItem == pageCount - 1 && downX >= touchX)) {
                if (slideBorderMode == SLIDE_BORDER_MODE_TO_PARENT) {
                    parent.requestDisallowInterceptTouchEvent(false)
                } else {
                    if (pageCount > 1) {
                        setCurrentItem(pageCount - currentItem - 1, isBorderAnimation)
                    }
                    parent.requestDisallowInterceptTouchEvent(true)
                }
                return super.dispatchTouchEvent(ev)
            }
        }
        parent.requestDisallowInterceptTouchEvent(true)

        return super.dispatchTouchEvent(ev)
    }

    private class MyHandler(autoScrollViewPager: AutoScrollViewPager) : Handler() {
        private val autoScrollViewPager = WeakReference(autoScrollViewPager)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            when (msg.what) {
                SCROLL_WHAT -> {
                    val pager = autoScrollViewPager.get()
                    if (pager != null) {
                        pager.scroller!!.setScrollDurationFactor(pager.autoScrollFactor)
                        pager.scrollOnce()
                        pager.scroller!!.setScrollDurationFactor(pager.swipeScrollFactor)
                        pager.sendScrollMessage(pager.interval + pager.scroller!!.duration)
                    }
                }

                else -> {}
            }
        }
    }

    fun getDirection(): Int {
        return if ((direction == LEFT)) LEFT else RIGHT
    }

    fun setDirection(direction: Int) {
        this.direction = direction
    }

    companion object {
        const val DEFAULT_INTERVAL: Int = 4000

        const val LEFT: Int = 0
        const val RIGHT: Int = 1

        const val SLIDE_BORDER_MODE_NONE: Int = 0
        const val SLIDE_BORDER_MODE_CYCLE: Int = 1
        const val SLIDE_BORDER_MODE_TO_PARENT: Int = 2
        const val SCROLL_WHAT: Int = 0
    }
}
