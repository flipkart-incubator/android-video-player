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
package com.flipkart.flick.demo

import android.util.Log
import com.flipkart.flick.adapter.FlickAnalyticsAdapter
import com.flipkart.flick.core.model.ContentData
import com.flipkart.flick.helper.FlickError
import com.kaltura.playkit.ads.PKAdErrorType
import com.kaltura.playkit.player.PKPlayerErrorType

class DemoAnalyticsAdapter : FlickAnalyticsAdapter {
    override fun onAdError(
        contentConfig: ContentData?,
        errorType: PKAdErrorType?,
        isFatal: Boolean
    ) {
        Log.d(
            TAG,
            "onAdError() called with: contentConfig = [$contentConfig], errorType = [$errorType], isFatal = [$isFatal]"
        )
    }

    override fun onAdPlayed() {
        Log.d(TAG, "onAdPlayed() called")
    }

    override fun onAdShown() {
        super.onAdShown()
        Log.d(TAG, "onAdShown() called")
    }

    override fun onPlayerPinched() {
        super.onPlayerPinched()
        Log.d(TAG, "onPlayerPinched() called")
    }

    override fun onPlayerZoomed() {
        super.onPlayerZoomed()
        Log.d(TAG, "onPlayerZoomed() called")
    }

    override fun onPlayerBufferError(
        contentConfig: ContentData?,
        error: PKPlayerErrorType?,
        flickError: FlickError
    ) {
        super.onPlayerBufferError(contentConfig, error, flickError)
        Log.d(
            TAG,
            "onPlayerBufferError() called with: contentConfig = [$contentConfig], errorReason = [$error]"
        )
    }

    override fun onPlayerStartError(
        contentConfig: ContentData?,
        error: String,
        code: Int,
        flickError: FlickError
    ) {
        super.onPlayerStartError(contentConfig, error, code, flickError)
        Log.d(
            TAG,
            "onPlayerStartError() called with: contentConfig = [$contentConfig], errorReason = [$error]"
        )
    }

    override fun onPlayerOpened(contentConfig: ContentData?) {
        super.onPlayerOpened(contentConfig)
        Log.d(TAG, "onPlayerOpened() called with: contentConfig = [$contentConfig]")
    }

    override fun onPlayerClosed(contentConfig: ContentData?) {
        super.onPlayerClosed(contentConfig)
        Log.d(TAG, "onPlayerClosed() called with: contentConfig = [$contentConfig]")
    }

    override fun onPlayerPaused(contentConfig: ContentData?) {
        super.onPlayerPaused(contentConfig)
        Log.d(TAG, "onPlayerPaused() called")
    }

    override fun onPlayerPlayed(contentConfig: ContentData?) {
        super.onPlayerPlayed(contentConfig)
        Log.d(TAG, "onPlayerPlayed() called")
    }

    override fun onPlayerSeekTo(position: Long) {
        super.onPlayerSeekTo(position)
        Log.d(TAG, "onPlayerSeekTo() called with: position = [$position]")
    }

    override fun onPlayerBottomButtonsClicked(
        contentConfig: ContentData?,
        buttonName: String
    ) {
        super.onPlayerBottomButtonsClicked(contentConfig, buttonName)
        Log.d(TAG, "onPlayerBottomButtonsClicked() called with: buttonName = [$contentConfig]")
    }

    override fun onPlayerContentChanged(contentConfig: ContentData) {
        super.onPlayerContentChanged(contentConfig)
        Log.d(TAG, "onPlayerAssetChanged() called with: contentConfig = [$contentConfig]")
    }

    override fun onPlayerTrackChange(trackId: String) {
        super.onPlayerTrackChange(trackId)
        Log.d(TAG, "onPlayerTrackChange() called with: trackId = [$trackId]")
    }

    companion object {
        private const val TAG = "LogcatAnalyticsAdapter"
    }
}
