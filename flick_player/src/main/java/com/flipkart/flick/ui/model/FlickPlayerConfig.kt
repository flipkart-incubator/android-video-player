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
package com.flipkart.flick.ui.model

class FlickPlayerConfig {
    var adsEnabled: Boolean = false
    var defaultZoomAspectRatio: Boolean = true
    var trackConfig: PlayerTrackConfig? = null
    var showNextAssetThreshold: Int = 0
    var initialBitrateEstimate: Long? = null
    var minVideoBitrate: Long? = null
    var maxVideoBitrate: Long? = null

    /**
     * track config to map video/audio tracks to human readable formats
     */
    class PlayerTrackConfig {
        var qualityConfigList: List<QualityConfig>? = null
        var trackConfigList: List<TrackConfig>? = null
    }

    class QualityConfig {
        var startBitrate: Long = 0
        var endBitrate: Long = 0
        var title: String? = null
    }

    class TrackConfig {
        var language: String? = null
        var label: String? = null
    }
}
