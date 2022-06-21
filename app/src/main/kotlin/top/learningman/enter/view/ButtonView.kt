package top.learningman.enter.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.WindowManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import top.learningman.enter.Config
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

    private var mCurrentMode = Mode.NONE

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
                    mCurrentMode = Mode.MOVE
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    mIsTouching = false
                    val offsetX = abs(event.rawX - mLastRawX)
                    val offsetY = abs(event.rawY - mLastRawY)
                    if (isClick(offsetX, offsetY)) {
                        performClick()
                    } else if (isLongClick(offsetX, offsetY)) {
                        performLongClick()
                    }
                    mLayoutParams = mLayoutParams.apply {
                        x = (event.rawX - mLastRawX + mLastX).toInt()
                        y = (event.rawY - mLastRawY + mLastY).toInt()
                    }
                    mCurrentMode = Mode.NONE
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

    private fun isClick(offsetX: Float, offsetY: Float): Boolean {
        val time = System.currentTimeMillis() - mLastDownTime
        return isSmallMove(offsetX, offsetY) && time < Config.CLICK_LIMIT
    }

    private fun isLongClick(offsetX: Float, offsetY: Float): Boolean {
        return isSmallMove(offsetX, offsetY)
    }

    private fun isSmallMove(x: Float, y: Float): Boolean {
        return x < mTouchSlop * 2 && y < mTouchSlop * 2
    }

    companion object {
        enum class Mode {
            NONE,
            MOVE
        }
    }
}