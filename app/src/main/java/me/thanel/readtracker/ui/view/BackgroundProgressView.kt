package me.thanel.readtracker.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.annotation.ColorInt
import androidx.core.view.GestureDetectorCompat
import me.thanel.readtracker.R
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class BackgroundProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), SwipeProgressGestureListener.Callback {

    var onProgressChangeListener: (Int) -> Unit = {}

    @ColorInt
    private fun getColor(context: Context): Int {
        val typedArray = context.obtainStyledAttributes(intArrayOf(R.attr.colorControlActivated))
        val color = typedArray.getColor(0, Color.BLACK)
        typedArray.recycle()
        return color
    }

    private val progressBackground = ColorDrawable(getColor(context))

    private val gestureDetector by lazy {
        GestureDetectorCompat(context, SwipeProgressGestureListener(this, this))
    }

    init {
        setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    override var minValue = 0
    override var maxValue = 100
    override var currentValue = 20
        set(value) {
            if (field == value) return
            field = value
            invalidate()
            onProgressChangeListener(value)
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val progressPercent = currentValue / maxValue.toFloat()
        val end = ((right - left) * progressPercent).roundToInt()
        progressBackground.setBounds(0, 0, end, bottom - top)
        progressBackground.draw(canvas)
    }
}

class SwipeProgressGestureListener(
    private val callback: Callback,
    private val view: View
) : GestureDetector.SimpleOnGestureListener() {

    private var currentScrollStartValue = 0
    private val viewWidth = view.width

    private var dragging = false

    override fun onDown(e: MotionEvent): Boolean {
        dragging = false
        currentScrollStartValue = callback.currentValue
        return super.onDown(e)
    }

    private val touchSlop = ViewConfiguration.get(view.context).scaledTouchSlop

    override fun onScroll(
        initialEvent: MotionEvent,
        currentEvent: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        // Calculate the distance in pixels from initial touch event
        val scrolledDistance = currentEvent.x - initialEvent.x

        if (!dragging) {
            val verticalScrolledDistance = currentEvent.y - initialEvent.y
            if (verticalScrolledDistance >= touchSlop) {
                return false
            }
            dragging = abs(scrolledDistance) >= touchSlop
        }

        if (!dragging) {
            return false
        }

        view.parent.requestDisallowInterceptTouchEvent(true)

        // Convert that distance to represent percentage of the view's width
        val scrollScale = scrolledDistance / viewWidth
        // Multiply it by some factor to update the value a bit slower
        val factoredScrollScale = scrollScale * SCROLL_FACTOR
        // Convert the scale to actual value
        val scrolledValue = (callback.maxValue * factoredScrollScale).roundToInt()
        // Add to it the value that was set before scroll started
        val updatedValue = scrolledValue + currentScrollStartValue
        // Finally clamp the value between min and max and notify the listener with result
        callback.currentValue = callback.clamp(updatedValue)
        return true
    }

    interface Callback {
        val minValue: Int
        val maxValue: Int
        var currentValue: Int

        fun clamp(value: Int) = value.clamp(minValue, maxValue)
    }

    companion object {
        private const val SCROLL_FACTOR = 0.5f
    }
}

fun Int.clamp(minValue: Int, maxValue: Int) = max(minValue, min(this, maxValue))
