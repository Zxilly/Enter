package top.learningman.enter

import android.content.Context
import android.util.AttributeSet
import android.util.Log
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
    private var mLastRawX = 0f
    private var mLastRawY = 0f

    private var mLastX = 0
    private var mLastY = 0

    private var mIsTouching = false

    private val mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop

    private var mCurrentMode = MODE_NONE

    private var mWidth: Int? = null
    private var mHeight: Int? = null

    init {
        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mIsTouching = true
                    mLastDownTime = System.currentTimeMillis()
                    mLastRawX = event.rawX
                    mLastRawY = event.rawY
                    mLastX = mLayoutParams.x
                    mLastY = mLayoutParams.y
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isTouchSlop(event)) {
                        return@setOnTouchListener true
                    }
                    // Log.d("ButtonView", "movedX: $movedX, movedY: $movedY")
                    mLayoutParams = mLayoutParams.apply {
                        Log.d(
                            "ButtonView", "rawX: ${event.rawX}, rawY: ${event.rawY}" +
                                    " x: $x, y: $y"
                        )
                        x = (event.rawX - mLastRawX + mLastX).toInt()
                        y = (event.rawY - mLastRawY + mLastY).toInt()
                    }
                    mWindowManager.updateViewLayout(this@ButtonView, mLayoutParams)
                    mCurrentMode = MODE_MOVE
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    mIsTouching = false
                    if (isClick(event)) {
                        performClick()
                    }
                    mLayoutParams = mLayoutParams.apply {
                        x = (event.rawX - mLastRawX + mLastX).toInt()
                        y = (event.rawY - mLastRawY + mLastY).toInt()
                    }
                    mCurrentMode = MODE_NONE
                }
            }
            return@setOnTouchListener true
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mHeight = height
        mWidth = width
    }

    private fun isTouchSlop(event: MotionEvent): Boolean {
        val x = event.rawX
        val y = event.rawY
        return abs(x - mLastRawX) < mTouchSlop && abs(y - mLastRawY) < mTouchSlop
    }

    fun setWindowLayoutParams(layoutParams: WindowManager.LayoutParams) {
        mLayoutParams = layoutParams
    }

    private fun isClick(event: MotionEvent): Boolean {
        val offsetX = abs(event.rawX - mLastRawX)
        val offsetY = abs(event.rawY - mLastRawY)
        val time = System.currentTimeMillis() - mLastDownTime
        return offsetX < mTouchSlop * 2 && offsetY < mTouchSlop * 2 && time < CLICK_LIMIT
    }

    companion object {
        private const val CLICK_LIMIT: Long = 200
        private const val MODE_NONE = 0x000
        private const val MODE_MOVE = 0x001
    }
}