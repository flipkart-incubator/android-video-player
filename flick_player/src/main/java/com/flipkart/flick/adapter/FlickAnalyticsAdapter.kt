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
package com.flipkart.flick.adapter

import com.flipkart.flick.core.model.ContentData
import com.flipkart.flick.helper.FlickError
import com.kaltura.playkit.ads.PKAdErrorType
import com.kaltura.playkit.player.PKPlayerErrorType

/**
 * Implement this adapter to receive analytics events
 * Note that this is an interface with an empty default implementation. Hence the host app won't be forced to implement all
 */
interface FlickAnalyticsAdapter {

    /******* ADS *****/

    /**
     * Ad was paused by user
     */
    fun onAdPaused() {}

    /**
     * Ad was played by user, possible because he paused it before.
     */
    fun onAdPlayed() {}

    /**
     * Ad got
     */
    fun onAdShown() {}

    /**
     * Will be called when ad break errors happen
     */
    fun onAdError(contentConfig: ContentData?, errorType: PKAdErrorType?, isFatal: Boolean) {}


    /******* PLAYER GESTURES *****/

    /**
     * Player was pinched to fit
     */
    fun onPlayerPinched() {}

    /**
     * Player was zoomed using pinch-zoom gesture
     */
    fun onPlayerZoomed() {}


    /******* PLAYER BASICS *****/

    /**
     * Fired when playback throws an error, due to stream source error or rendering error
     */
    fun onPlayerBufferError(
        contentConfig: ContentData?,
        error: PKPlayerErrorType?,
        flickError: FlickError
    ) {
    }

    /**
     * Fired when there was an error in preparing the player. Could be playback context error or due to required params missing.
     * The [error] will contain a human readable message, [code] contains a unique code (can be server error http status codes too)
     * [flickError] can be used to find out the local error type.
     * Note that [code] and [flickError]'s errorCode might be different, same is true for [error]
     *
     */
    fun onPlayerStartError(
        contentConfig: ContentData?,
        error: String,
        code: Int,
        flickError: FlickError
    ) {
    }

    /**
     * Fired when player is opened
     */
    fun onPlayerOpened(contentConfig: ContentData?) {}

    /**
     * Fired when player is closed
     */
    fun onPlayerClosed(contentConfig: ContentData?) {}

    /**
     * Pause video was clicked
     */
    fun onPlayerPaused(contentConfig: ContentData?) {}

    /**
     * Play video was clicked
     */
    fun onPlayerPlayed(contentConfig: ContentData?) {}

    /**
     * Seek to absolute position by using scrubber or through buttons
     */
    fun onPlayerSeekTo(position: Long) {}

    /**
     * Bottom buttons in player like subtitles, audio, auto play next got clicked
     * Use [buttonName] to know what got clicked. Button names are in [com.flipkart.flick.ui.fragments.FlickFragmentFactory.Options.PlaybackOptions]
     */
    fun onPlayerBottomButtonsClicked(
        contentConfig: ContentData?,
        buttonName: String
    ) {
    }

    /**
     * Player's content got changed usually due to auto play next.
     */
    fun onPlayerContentChanged(contentConfig: ContentData) {}

    /**
     * Player track was changed by user. Can be a audio track, subtitle track or quality change track
     */
    fun onPlayerTrackChange(trackId: String) {}

    /**
     * Will be called when auto play next content request at the end of a video fails
     */
    fun onAutoPlayNextError(contentData: ContentData?, error: String) {}

    /**
     * Will be called when auto play next content is played.
     */
    fun onAutoPlayNext(contentData: ContentData?) {}
}
