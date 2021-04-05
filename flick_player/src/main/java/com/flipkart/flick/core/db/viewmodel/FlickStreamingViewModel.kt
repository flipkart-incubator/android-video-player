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
package com.flipkart.flick.core.db.viewmodel

import android.app.Application
import android.os.CancellationSignal
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import com.flipkart.flick.adapter.FlickApplicationAdapterProvider
import com.flipkart.flick.core.model.ContentData
import com.flipkart.flick.core.model.ContentRequest
import com.flipkart.flick.helper.utils.ParentFinder

/**
 * View model backing [com.flipkart.flick.FlickStreamingFragment]
 */
class FlickStreamingViewModel(
    application: Application,
    private val currentContent: ContentData?
) :
    AndroidViewModel(application) {

    private val cancellationSignal = CancellationSignal()

    /**
     * Get playback context with ads for the current content.
     * Delegates the work to [PlaybackContextWithAdsViewModel]
     */
    val playbackContext by lazy {
        val applicationAdapter =
            ParentFinder(application, FlickApplicationAdapterProvider::class.java).find()
        val playbackContextNetworkLayer =
            applicationAdapter.flickApplicationAdapter.getPlaybackContextNetworkLayer()

        val adsNetworkLayer =
            applicationAdapter.flickApplicationAdapter.getAdsNetworkLayer()

        val request = ContentRequest(currentContent?.contentId ?: "")
        MediatorLiveData<PlaybackContextWithAdsViewModel>().also {
            val viewModel = PlaybackContextWithAdsViewModel(it, cancellationSignal)
            viewModel.getPlaybackContextWithAds(
                request,
                playbackContextNetworkLayer,
                adsNetworkLayer
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        cancellationSignal.cancel()
    }
}
