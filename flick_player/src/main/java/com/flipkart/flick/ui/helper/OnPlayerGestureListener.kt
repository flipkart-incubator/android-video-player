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

/**
 * Used by [PlayerGestureHelper] to give gesture callbacks.
 *
 */
interface OnPlayerGestureListener {
    /**
     * Single tap occurred
     */
    fun onSingleTap(helper: PlayerGestureHelper): Boolean {
        return false
    }

    /**
     * Called when more than one taps occur.
     * [widthFactor] is a value between 0 and 1 which indicates what percentage of the width is the tap occuring in.
     * For e.g 1.0 indicates extreme right, 0 indicates extreme left, 0.5 indicates middle.
     * [count] indicates the number of taps the user has done in the current gesture. Useful to detect multiple taps.
     */
    fun onMultiTap(
        helper: PlayerGestureHelper,
        widthFactor: Float,
        count: Int
    ): Boolean {
        return false
    }

    /**
     * Pinch gesture occurred
     */
    fun onPinchGesture(helper: PlayerGestureHelper): Boolean {
        return false
    }

    /**
     * Zoom gesture occurred.
     */
    fun onZoomGesture(helper: PlayerGestureHelper): Boolean {
        return false
    }
}
