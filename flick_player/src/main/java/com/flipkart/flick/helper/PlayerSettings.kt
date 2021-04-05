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
package com.flipkart.flick.helper

import android.graphics.Color
import com.flipkart.flick.ui.model.FlickPlayerConfig
import com.kaltura.playkit.PKMediaConfig
import com.kaltura.playkit.PKMediaEntry
import com.kaltura.playkit.PKTrackConfig
import com.kaltura.playkit.player.ABRSettings
import com.kaltura.playkit.player.SubtitleStyleSettings

/**
 * Player Settings to generate media sources.
 */
object PlayerSettings {
    val subTitleStyle: SubtitleStyleSettings
        get() = SubtitleStyleSettings(null)
            .setBackgroundColor(Color.BLACK)
            .setTextColor(Color.WHITE)
            .setTextSizeFraction(SubtitleStyleSettings.SubtitleTextSizeFraction.SUBTITLE_FRACTION_100)
            .setTypeface(SubtitleStyleSettings.SubtitleStyleTypeface.DEFAULT)
            .setEdgeColor(Color.BLACK)
            .setEdgeType(SubtitleStyleSettings.SubtitleStyleEdgeType.EDGE_TYPE_NONE)

    val preferredTextTrack: PKTrackConfig
        get() = PKTrackConfig().setPreferredMode(PKTrackConfig.Mode.OFF)

    /**
     * Since the kaltura playkit has a check of the value to be greater than 0, setting as 1.
     * Setting it to 1 allows the player to pick the lowest bit rate available.
     *
     * Setting min and max video bitrates (from config) to control video quality at lower end streams
     * as well as max bandwidth consumption for higher resolution videos
     *
     */
    fun abrSettings(flickPlayerConfig: FlickPlayerConfig): ABRSettings {
        val abrSetting = ABRSettings()
        abrSetting.initialBitrateEstimate = flickPlayerConfig.initialBitrateEstimate ?: 1
        flickPlayerConfig.minVideoBitrate?.let { abrSetting.setMinVideoBitrate(it) }
        flickPlayerConfig.maxVideoBitrate?.let { abrSetting.setMaxVideoBitrate(it) }
        return abrSetting
    }

    fun createMediaConfig(mediaEntry: PKMediaEntry, startPosition: Long): PKMediaConfig {
        val pkMediaConfig = PKMediaConfig()
        pkMediaConfig.startPosition = startPosition
        pkMediaConfig.mediaEntry = mediaEntry
        return pkMediaConfig
    }
}
