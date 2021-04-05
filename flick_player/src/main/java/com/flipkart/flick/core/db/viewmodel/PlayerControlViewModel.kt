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
import androidx.lifecycle.MutableLiveData
import com.flipkart.flick.adapter.FlickApplicationAdapterProvider
import com.flipkart.flick.core.components.NextVideoModel
import com.flipkart.flick.core.db.repository.NextVideoRepository
import com.flipkart.flick.core.model.ContentData
import com.flipkart.flick.helper.utils.ParentFinder
import com.flipkart.flick.ui.model.TrackModel

/**
 * ViewModel for storing all player control data
 * The scope of this fragment should be within [com.flipkart.flick.ui.fragments.PlayerControlsFragment]
 * That way when a new content is played, a new viewmodel gets created
 */
class PlayerControlViewModel(application: Application) : AndroidViewModel(application) {

    var currentContent: ContentData? = null
    var cancellationSignal = CancellationSignal()
    var qualityTracks: ArrayList<TrackModel>? = null
    var audioTracks: ArrayList<TrackModel>? = null
    var subTitleTracks: ArrayList<TrackModel>? = null

    /**
     * Get next content for the current content
     */
    val nextAsset: MutableLiveData<NextVideoModel> by lazy {
        val applicationAdapter =
            ParentFinder(application, FlickApplicationAdapterProvider::class.java).find()
        val playbackContextNetworkLayer =
            applicationAdapter.flickApplicationAdapter.getPlaybackContextNetworkLayer()
        val nextVideoRepo = NextVideoRepository()
        MutableLiveData<NextVideoModel>().also { liveData ->
            currentContent?.let { content ->
                nextVideoRepo.get(
                    liveData,
                    content.contentId,
                    playbackContextNetworkLayer,
                    cancellationSignal
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cancellationSignal.cancel()
    }
}
