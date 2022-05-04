package top.learningman.enter

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.WindowManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.abs


class ButtonView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    FloatingActionButton(context, attrs, defStyleAttr) {
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    private val mWindowManager: WindowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private lateinit var mLayoutParams: WindowManager.LayoutParams

    private var mLastDownTime: Long = 0
    private var mLastDownX = 0f
    private var mLastDownY = 0f

    private var mIsTouching = false

    private val mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val CLICK_LIMIT: Long = 200

    private val MODE_NONE = 0x000
    private val MODE_MOVE = 0x005

    private var mCurrentMode = MODE_NONE

    private val mStatusBarHeight = getStatusBarHeight();
    private val mOffsetToParent = 32.dip2px(context) // TODO: keep position in view
    private val mOffsetToParentY = mStatusBarHeight + mOffsetToParent;

    init {
        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mIsTouching = true
                    mLastDownTime = System.currentTimeMillis()
                    mLastDownX = event.x
                    mLastDownY = event.y
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isTouchSlop(event)) {
                        return@setOnTouchListener true
                    }
                    // Log.d("ButtonView", "movedX: $movedX, movedY: $movedY")
                    mLayoutParams.apply {
                        x = (event.rawX - mOffsetToParent).toInt()
                        y = (event.rawY - mOffsetToParentY).toInt()
                    }
                    mWindowManager.updateViewLayout(this@ButtonView, mLayoutParams)
                    mCurrentMode = MODE_MOVE

                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    mIsTouching = false
                    if (isClick(event)) {
                        performClick()
                    }
                    mCurrentMode = MODE_NONE
                }
            }
            return@setOnTouchListener true
        }
    }

    private fun isTouchSlop(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        return abs(x - mLastDownX) < mTouchSlop && abs(y - mLastDownY) < mTouchSlop
    }

    fun setWindowLayoutParams(layoutParams: WindowManager.LayoutParams) {
        mLayoutParams = layoutParams
    }

    private fun isClick(event: MotionEvent): Boolean {
        val offsetX = abs(event.rawX - mLastDownX)
        val offsetY = abs(event.rawY - mLastDownY)
        val time = System.currentTimeMillis() - mLastDownTime
        return offsetX < mTouchSlop * 2 && offsetY < mTouchSlop * 2 && time < CLICK_LIMIT
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }
}