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

import android.content.Context
import android.preference.PreferenceManager
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.flipkart.flick.adapter.FlickApplicationAdapter
import com.flipkart.flick.core.interfaces.AdsNetworkLayer
import com.flipkart.flick.core.interfaces.BookmarkNetworkLayer
import com.flipkart.flick.core.interfaces.PlaybackNetworkLayer
import com.flipkart.flick.ui.model.FlickPlayerConfig
import com.squareup.picasso.Picasso

class DemoApplicationAdapter(private val context: Context) : FlickApplicationAdapter {
    private val flickNetworkLayer: MockedNetworkLayer
        get() {
            return MockedNetworkLayer()
        }

    override fun getAdsNetworkLayer(): AdsNetworkLayer {
        return flickNetworkLayer
    }

    override fun getPlaybackContextNetworkLayer(): PlaybackNetworkLayer {
        return flickNetworkLayer
    }

    override fun getBookmarkNetworkLayer(): BookmarkNetworkLayer {
        return flickNetworkLayer
    }

    override fun getFlickConfig(): FlickPlayerConfig {
        val config = FlickPlayerConfig.PlayerTrackConfig()
        val qualityConfigList = ArrayList<FlickPlayerConfig.QualityConfig>()

        val qualityConfig = FlickPlayerConfig.QualityConfig()
        qualityConfig.startBitrate = 0
        qualityConfig.endBitrate = 0
        qualityConfig.title = "Auto"
        qualityConfigList.add(qualityConfig)

        val qualityConfig1 = FlickPlayerConfig.QualityConfig()
        qualityConfig1.startBitrate = 1000000
        qualityConfig1.endBitrate = 100000000
        qualityConfig1.title = "Best"
        qualityConfigList.add(qualityConfig1)

        val qualityConfig2 = FlickPlayerConfig.QualityConfig()
        qualityConfig2.startBitrate = 400000
        qualityConfig2.endBitrate = 1000000
        qualityConfig2.title = "Good"
        qualityConfigList.add(qualityConfig2)

        val qualityConfig3 = FlickPlayerConfig.QualityConfig()
        qualityConfig3.startBitrate = 100000
        qualityConfig3.endBitrate = 400000
        qualityConfig3.title = "Slow"
        qualityConfigList.add(qualityConfig3)

        config.qualityConfigList = qualityConfigList

        val trackConfigList = ArrayList<FlickPlayerConfig.TrackConfig>()

        val trackConfig = FlickPlayerConfig.TrackConfig()
        trackConfig.label = "Off"
        trackConfig.language = "none"
        trackConfigList.add(trackConfig)

        val trackConfig1 = FlickPlayerConfig.TrackConfig()
        trackConfig1.label = "English"
        trackConfig1.language = "en"
        trackConfigList.add(trackConfig1)

        config.trackConfigList = trackConfigList

        val result = FlickPlayerConfig()
        result.trackConfig = config
        result.showNextAssetThreshold = 20000

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        result.adsEnabled = prefs.getBoolean("adsEnabled", true)

        return result
    }

    override fun loadImage(
        fragment: Fragment, imageView: ImageView, url: String,
        placeholderBackgroundColor: Int,
        disableDefaultImage: Boolean
    ) {
        Picasso.get().load(url).into(imageView)
    }
}
