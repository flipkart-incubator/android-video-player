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

/*
 * Copyright (C) 2014 The Android Open Source Project
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

import android.app.Activity
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.WindowManager

/**
 * Helper for controlling the visibility of the System UI across the various API levels. To use
 * this API, instantiate an instance of this class with the required level. The level specifies the
 * extent to which the System UI's visibility is changed when you call [.hide]
 * or [.toggle].
 */

class SystemUiHelper
/**
 * Construct a new SystemUiHelper.
 *
 * @param activity The Activity who's system UI should be changed
 * @param level    The level of hiding. Should be either [.LEVEL_LOW_PROFILE],
 * [.LEVEL_HIDE_STATUS_BAR], [.LEVEL_LEAN_BACK] or
 * [.LEVEL_IMMERSIVE]
 * @param flags    Additional options. See [.FLAG_LAYOUT_IN_SCREEN_OLDER_DEVICES] and
 * [.FLAG_IMMERSIVE_STICKY]
 * @param listener A listener which is called when the system visibility is changed
 */
@JvmOverloads constructor(
    activity: Activity, level: Int, flags: Int,
    listener: OnVisibilityChangeListener? = null
) {

    private val mImpl: SystemUiHelperImpl
    private val mHandler: Handler = Handler(Looper.getMainLooper())
    private val mHideRunnable: Runnable

    /**
     * @return true if the system UI is currently showing. What this means depends on the mode this
     * [SystemUiHelper] was instantiated with.
     */
    val isShowing: Boolean
        get() = mImpl.isShowing

    init {

        mHideRunnable = HideRunnable()

        // Create impl
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mImpl = SystemUiHelperImplKK(activity, level, flags, listener)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mImpl = SystemUiHelperImplJB(activity, level, flags, listener)
        } else {
            mImpl = SystemUiHelperImplBase(activity, level, flags, listener)
        }
    }

    /**
     * Show the system UI. What this means depends on the mode this [SystemUiHelper] was
     * instantiated with.
     *
     *
     * Any currently queued delayed hide requests will be removed.
     */
    fun show() {
        // Ensure that any currently queued hide calls are removed
        removeQueuedRunnables()

        mImpl.show()
    }

    /**
     * Hide the system UI. What this means depends on the mode this [SystemUiHelper] was
     * instantiated with.
     *
     *
     * Any currently queued delayed hide requests will be removed.
     */
    fun hide() {
        // Ensure that any currently queued hide calls are removed
        removeQueuedRunnables()

        mImpl.hide()
    }

    /**
     * Request that the system UI is hidden after a delay.
     *
     *
     * Any currently queued delayed hide requests will be removed.
     *
     * @param delayMillis The delay (in milliseconds) until the Runnable
     * will be executed.
     */
    fun delayHide(delayMillis: Long) {
        // Ensure that any currently queued hide calls are removed
        removeQueuedRunnables()

        mHandler.postDelayed(mHideRunnable, delayMillis)
    }

    /**
     * Toggle whether the system UI is displayed.
     */
    fun toggle() {
        if (mImpl.isShowing) {
            mImpl.hide()
        } else {
            mImpl.show()
        }
    }

    private fun removeQueuedRunnables() {
        // Ensure that any currently queued hide calls are removed
        mHandler.removeCallbacks(mHideRunnable)
    }

    /**
     * A callback interface used to listen for system UI visibility changes.
     */
    interface OnVisibilityChangeListener {
        /**
         * Called when the system UI visibility has changed.
         *
         * @param visible True if the system UI is visible.
         */
        fun onVisibilityChange(visible: Boolean)
    }

    internal abstract class SystemUiHelperImpl(
        val mActivity: Activity, val mLevel: Int, val mFlags: Int,
        val mOnVisibilityChangeListener: OnVisibilityChangeListener?
    ) {

        var mIsShowing = true

        var isShowing: Boolean
            get() = mIsShowing
            set(isShowing) {
                mIsShowing = isShowing
                mOnVisibilityChangeListener?.onVisibilityChange(mIsShowing)
            }

        internal abstract fun show()

        internal abstract fun hide()
    }

    /**
     * Base implementation. Used on API level 10 and below.
     */
    internal class SystemUiHelperImplBase(
        activity: Activity, level: Int, flags: Int,
        onVisibilityChangeListener: OnVisibilityChangeListener?
    ) : SystemUiHelperImpl(activity, level, flags, onVisibilityChangeListener) {

        init {

            if (mFlags and FLAG_LAYOUT_IN_SCREEN_OLDER_DEVICES != 0) {
                mActivity.window.addFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                )
            }
        }

        override fun show() {
            if (mLevel > LEVEL_LOW_PROFILE) {
                mActivity.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                isShowing = true
            }
        }

        override fun hide() {
            if (mLevel > LEVEL_LOW_PROFILE) {
                mActivity.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                isShowing = false
            }
        }
    }

    private inner class HideRunnable : Runnable {
        override fun run() {
            hide()
        }
    }

    companion object {

        /**
         * In this level, the helper will toggle low profile mode.
         */
        const val LEVEL_LOW_PROFILE = 0

        /**
         * In this level, the helper will toggle the visibility of the status bar.
         * If there is a navigation bar, it will toggle low profile mode.
         */
        const val LEVEL_HIDE_STATUS_BAR = 1

        /**
         * In this level, the helper will toggle the visibility of the navigation bar
         * (if present and if possible) and status bar. In cases where the navigation
         * bar is present but cannot be hidden, it will toggle low profile mode.
         */
        const val LEVEL_LEAN_BACK = 2

        /**
         * In this level, the helper will toggle the visibility of the navigation bar
         * (if present and if possible) and status bar, in an immersive mode. This means that the app
         * will continue to receive all touch events. The user can reveal the system bars with an
         * inward swipe along the region where the system bars normally appear.
         *
         *
         * The [.FLAG_IMMERSIVE_STICKY] flag can be used to control how the system bars are
         * displayed.
         */
        const val LEVEL_IMMERSIVE = 3

        /**
         * When this flag is set, the
         * [android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN]
         * flag will be set on older devices, making the status bar "float" on top
         * of the activity layout. This is most useful when there are no controls at
         * the top of the activity layout.
         *
         *
         * This flag isn't used on newer devices because the [action
         * bar](http://developer.android.com/design/patterns/actionbar.html), the most important structural element of an Android app, should
         * be visible and not obscured by the system UI.
         */
        const val FLAG_LAYOUT_IN_SCREEN_OLDER_DEVICES = 0x1

        /**
         * Used with [.LEVEL_IMMERSIVE]. When this flag is set, an inward swipe in the system
         * bars areas will cause the system bars to temporarily appear in a semi-transparent state,
         * but no flags are cleared, and your system UI visibility change listeners are not triggered.
         * The bars automatically hide again after a short delay, or if the user interacts with the
         * middle of the screen.
         */
        const val FLAG_IMMERSIVE_STICKY = 0x2

        private val LOG_TAG = SystemUiHelper::class.java.simpleName
    }
}
