package com.example.brainbyte.ocr

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.FrameLayout

class ZoomableFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var scaleFactor = 1f
    private var minScale = 1f
    private var maxScale = 5f
    private var translateX = 0f
    private var translateY = 0f
    private var isScaling = false
    private var selectionModeEnabled = false
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private val scaleGestureDetector: ScaleGestureDetector

    init {
        scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    }

    fun setSelectionModeEnabled(enabled: Boolean) {
        selectionModeEnabled = enabled
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.pointerCount >= 2) {
            parent?.requestDisallowInterceptTouchEvent(true)
            return true
        }

        if (selectionModeEnabled) {
            return false
        }

        if (scaleFactor > 1.05f) {
            parent?.requestDisallowInterceptTouchEvent(true)
            return true
        }

        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.pointerCount >= 2) {
            parent?.requestDisallowInterceptTouchEvent(true)
            scaleGestureDetector.onTouchEvent(event)
            return true
        }

        if (selectionModeEnabled) {
            return false
        }

        if (scaleFactor > 1.05f) {
            parent?.requestDisallowInterceptTouchEvent(true)
            handleSingleFingerPan(event)
            return true
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isScaling = false
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }

        return false
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.pointerCount >= 2) {
            parent?.requestDisallowInterceptTouchEvent(true)
            scaleGestureDetector.onTouchEvent(ev)
            return true
        }

        if (scaleFactor > 1.05f) {
            parent?.requestDisallowInterceptTouchEvent(true)

            if (selectionModeEnabled) {
                return super.dispatchTouchEvent(ev)
            } else {
                handleSingleFingerPan(ev)
                return true
            }
        }

        if (ev.actionMasked == MotionEvent.ACTION_UP || ev.actionMasked == MotionEvent.ACTION_CANCEL) {
            parent?.requestDisallowInterceptTouchEvent(false)
        }

        return super.dispatchTouchEvent(ev)
    }

    private fun handleSingleFingerPan(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = (event.x - lastTouchX) * 1.8f
                val dy = (event.y - lastTouchY) * 1.8f
                translateX += dx
                translateY += dy
                constrainTranslation()
                applyTransformation()
                lastTouchX = event.x
                lastTouchY = event.y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }
    }

    private fun constrainTranslation() {
        val maxTranslateX = (width * scaleFactor - width) / 2
        val maxTranslateY = (height * scaleFactor - height) / 2
        translateX = translateX.coerceIn(-maxTranslateX, maxTranslateX)
        translateY = translateY.coerceIn(-maxTranslateY, maxTranslateY)
    }

    private fun applyTransformation() {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            child.pivotX = width / 2f
            child.pivotY = height / 2f
            child.scaleX = scaleFactor
            child.scaleY = scaleFactor
            child.translationX = translateX
            child.translationY = translateY
        }
    }

    fun resetZoom() {
        scaleFactor = 1f
        translateX = 0f
        translateY = 0f
        applyTransformation()
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            isScaling = true
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val previousScale = scaleFactor
            scaleFactor *= detector.scaleFactor
            scaleFactor = scaleFactor.coerceIn(minScale, maxScale)

            if (scaleFactor != previousScale) {
                val focusX = detector.focusX
                val focusY = detector.focusY
                val scaleChange = scaleFactor / previousScale
                translateX = focusX - scaleChange * (focusX - translateX)
                translateY = focusY - scaleChange * (focusY - translateY)
                constrainTranslation()
                applyTransformation()
            }

            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            isScaling = false
            if (scaleFactor < 1.1f) {
                resetZoom()
            }
        }
    }
}
