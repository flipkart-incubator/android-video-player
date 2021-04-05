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

import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.flipkart.flick.core.interfaces.AdsNetworkLayer
import com.flipkart.flick.core.interfaces.BookmarkNetworkLayer
import com.flipkart.flick.core.interfaces.PlaybackNetworkLayer
import com.flipkart.flick.ui.model.FlickPlayerConfig

/**
 * Application adapter implemented by the host application.
 */
interface FlickApplicationAdapter {

    // load the given image url to the image view.
    fun loadImage(
        fragment: Fragment,
        imageView: ImageView,
        url: String,
        placeholderBackgroundColor: Int,
        disableDefaultImage: Boolean
    )

    // playback context network layer
    fun getPlaybackContextNetworkLayer(): PlaybackNetworkLayer

    // bookmark network layer
    fun getBookmarkNetworkLayer(): BookmarkNetworkLayer

    // ads network layer
    fun getAdsNetworkLayer(): AdsNetworkLayer

    // get player config
    fun getFlickConfig(): FlickPlayerConfig
}
