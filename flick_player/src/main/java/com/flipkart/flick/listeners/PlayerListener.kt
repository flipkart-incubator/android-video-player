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
package com.flipkart.flick.listeners

import com.flipkart.flick.ui.fragments.PlayerControlsFragment
import com.kaltura.playkit.ads.AdController

interface PlayerListener : PlayerControlViewListener {

    /**
     * Current position of the video being played
     *
     * @return position
     */
    val currentPosition: Long

    /**
     * Total duration of the video
     *
     * @return duration
     */
    val duration: Long

    /**
     * Get buffered position of the video
     *
     * @return position
     */
    val bufferedPosition: Long

    /**
     * get controller for ads
     *
     * @return controller
     */
    val adController: AdController?

    /**
     * Play the video
     */
    fun playVideo()

    /**
     * Pause the video
     */
    fun pauseVideo()

    /**
     * Seek the video to given position
     *
     * @param byPosition position to seek to
     */
    fun seekTo(byPosition: Long): Boolean

    /**
     * When the scrubbing is started
     */
    fun onScrubStart(positionRelativeToScrubber: Long)

    /**
     * When the scrubbing is stopped
     */
    fun onScrubStop(positionRelativeToScrubber: Long)

    /**
     * when back arrow is clicked
     */
    fun onBack()

    /**
     * Toggle between play and pause
     */
    fun togglePlayPauseState() {
    }

    /**
     * Returns the time delta for a forward seek by [PlayerControlsFragment.FORWARD_REWIND_TIME] milliseconds
     */
    fun seekForwardBy(): Long =
        if (currentPosition + PlayerControlsFragment.FORWARD_REWIND_TIME < duration) PlayerControlsFragment.FORWARD_REWIND_TIME.toLong() else duration - currentPosition

    /**
     * Returns the time delta for a backward seek by [PlayerControlsFragment.FORWARD_REWIND_TIME] milliseconds
     */
    fun seekRewindBy(): Long =
        if (currentPosition > PlayerControlsFragment.FORWARD_REWIND_TIME) PlayerControlsFragment.FORWARD_REWIND_TIME.toLong() else currentPosition

}
