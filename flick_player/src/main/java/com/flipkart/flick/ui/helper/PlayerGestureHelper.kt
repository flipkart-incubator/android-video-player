/*
 * Copyright (C) 2021 Flipkart Internet Pvt Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flipkart.flick.ui.helper

import android.util.Log
import android.view.*

/**
 * Attaches a gesture scaleGestureDetector to the {@param gestureCapturingView} for toggling between zoom (on zoom) or fit (on pinch) modes.
 * Also gives callback for double tap
 * Currently there is no way exposed by this class to remove the gestures once attached
 * Android doesn't give a singular way to detect both double tap and pinch-zoom gestures. This class does that.
 * Also this class should be the helper class for detecting all gestures on the player and giving processed callbacks to the fragments.
 */
@Suppress("ConstantConditionIf")
class PlayerGestureHelper(
    val gestureCapturingView: View?,
    private val listener: OnPlayerGestureListener
) :
    ScaleGestureDetector.OnScaleGestureListener, GestureDetector.OnDoubleTapListener,
    GestureDetector.SimpleOnGestureListener() {

    private var ignoreFutureTouchUp: Boolean = false
    private var ignoreFutureScale: Boolean = false
    private var lastUpEventY: Float = Float.MIN_VALUE
    private var lastUpEventX: Float = Float.MIN_VALUE
    private var count: Int = 1
    private val doubleTapTimeout: Long = (ViewConfiguration.getDoubleTapTimeout()).toLong()
    private var lastUpEventTime: Long = 0
    private val scaleGestureDetector: ScaleGestureDetector =
        ScaleGestureDetector(gestureCapturingView?.context, this)
    private val singleTapRunnable = Runnable {
        if (LOG_ENABLED) Log.d(
            LOG_TAG,
            "called single tap listener"
        )
        listener.onSingleTap(this)
    }
    private val viewConfig = ViewConfiguration.get(gestureCapturingView?.context)

    init {
        gestureCapturingView?.setOnTouchListener { _, event ->
            return@setOnTouchListener onTouch(event)
        }
    }

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        ignoreFutureScale = false
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
    }

    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        detector?.let {
            if (!ignoreFutureScale) {
                if (it.scaleFactor > 1) {
                    ignoreFutureScale = true
                    return listener.onZoomGesture(this)
                } else if (it.scaleFactor < 1) {
                    ignoreFutureScale = true
                    return listener.onPinchGesture(this)
                }
            }
        }
        return false
    }

    fun onTouch(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                ignoreFutureTouchUp = false
            }

            MotionEvent.ACTION_MOVE -> {
                if (scaleGestureDetector.isInProgress) {
                    /** This means that we are in the middle of a pinch/zoom gesture, so we ignore any future touch-up so as to avoid duplicate handling **/
                    ignoreFutureTouchUp = true
                }
            }

            MotionEvent.ACTION_UP -> {
                if (!ignoreFutureTouchUp) {
                    val distanceX = Math.abs(lastUpEventX - event.x)
                    val distanceY = Math.abs(lastUpEventY - event.y)
                    val distance = Math.sqrt(
                        Math.pow(
                            distanceX.toDouble(),
                            2.0
                        ) + Math.pow(distanceY.toDouble(), 2.0)
                    )

                    if (LOG_ENABLED) Log.d(LOG_TAG, "touch up : [$event]")

                    if (distance <= viewConfig.scaledDoubleTapSlop) {
                        if (LOG_ENABLED) Log.d(
                            LOG_TAG,
                            "touch came within distance (distance $distance px)"
                        )
                        // distance between successive UP Events is OK, now lets check time
                        val time = Math.abs(lastUpEventTime - event.eventTime)
                        if (time < 2 * doubleTapTimeout) {
                            if (LOG_ENABLED) Log.d(
                                LOG_TAG,
                                "touch up within time, since last up event = $time ms"
                            )
                            // time between successive UP events is also OK, this means we are in a multi tap gesture
                            gestureCapturingView?.removeCallbacks(singleTapRunnable)
                            val widthFactor =
                                event.rawX / (gestureCapturingView?.width
                                    ?: 1) // percentage of the view width
                            listener.onMultiTap(this, widthFactor, count)
                            if (LOG_ENABLED) Log.d(
                                LOG_TAG,
                                "onMultiTap called with count = [$count]"
                            )
                            count++
                        } else {
                            // this means the touch up came in too late, we treat this as a new touch gesture
                            count = 1
                            gestureCapturingView?.removeCallbacks(singleTapRunnable)
                            gestureCapturingView?.postDelayed(singleTapRunnable, doubleTapTimeout)
                            if (LOG_ENABLED) Log.d(
                                LOG_TAG,
                                "touch came too late (after $time ms) , treating as single touch"
                            )
                        }
                    } else {
                        // this means the touch up came in too far from the last one, we treat this as a new touch gesture
                        count = 1
                        gestureCapturingView?.removeCallbacks(singleTapRunnable)
                        gestureCapturingView?.postDelayed(singleTapRunnable, doubleTapTimeout)
                        if (LOG_ENABLED) Log.d(
                            LOG_TAG,
                            "touch came too far (distance $distance px) , treating as single touch"
                        )

                    }
                    lastUpEventX = event.x
                    lastUpEventY = event.y
                    lastUpEventTime = event.eventTime
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                gestureCapturingView?.removeCallbacks(singleTapRunnable)
            }

            MotionEvent.ACTION_OUTSIDE -> {
                gestureCapturingView?.removeCallbacks(singleTapRunnable)
            }
        }

        return true
    }

    companion object {
        /** Set logging to true to debug events **/
        private const val LOG_ENABLED = true
        private const val LOG_TAG = "PlayerGestureHelper"
    }
}

