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

import com.flipkart.flick.ui.model.FlickPlayerConfig
import com.flipkart.flick.ui.model.TrackModel
import com.kaltura.playkit.player.AudioTrack
import com.kaltura.playkit.player.TextTrack
import com.kaltura.playkit.player.VideoTrack

class PlayerTrackResolver {
    companion object {

        /**
         * Map the video quality from the player to human readable strings.
         *
         * Functions:
         * 1. Bucket the video quality bit-rates to different bucket (Slow, Good and Best). If more than 1 bit-rate falls into the given bucket,
         * pick the maximum bit-rate in that bucket
         * 2. Convert the bit-rate to human readable string (powered by config)
         */
        fun massageQualityTracks(
            configList: List<FlickPlayerConfig.QualityConfig>?,
            defaultIndex: Int,
            videoTracks: List<VideoTrack>?
        ): ArrayList<TrackModel> {
            val selectedTrack = getSelectedVideoTrack(defaultIndex, videoTracks)
            val result = ArrayList<TrackModel>()
            if (configList != null) {
                for (videoQualityConfig in configList) {
                    val trackInRange = getVideoTrackInRange(
                        videoTracks,
                        videoQualityConfig.startBitrate,
                        videoQualityConfig.endBitrate
                    )
                    if (trackInRange.isNotEmpty()) {
                        val highestRateTrack = trackInRange.last()
                        result.add(
                            TrackModel(
                                highestRateTrack.uniqueId,
                                videoQualityConfig.title
                                    ?: "",
                                selectedTrack?.uniqueId == highestRateTrack.uniqueId
                            )
                        )
                    }
                }
            }
            return result
        }

        /**
         * Map the audio from the player to human readable strings.
         */
        fun massageAudioTracks(
            configList: List<FlickPlayerConfig.TrackConfig>?,
            defaultIndex: Int,
            audioTracks: List<AudioTrack>?
        ): ArrayList<TrackModel> {
            val selectedTrack = getSelectedAudioTrack(defaultIndex, audioTracks)
            val result = ArrayList<TrackModel>()
            val selectedIds = ArrayList<String>()
            if (configList != null) {
                for (subTitleConfig in configList) {
                    if (audioTracks != null) {
                        for (audioTrack in audioTracks) {
                            if (!selectedIds.contains(audioTrack.label)) {
                                if (subTitleConfig.language == audioTrack.language) {
                                    result.add(
                                        TrackModel(
                                            audioTrack.uniqueId,
                                            subTitleConfig.label
                                                ?: "",
                                            selectedTrack?.language == audioTrack.language
                                        )
                                    )
                                    selectedIds.add(audioTrack.label ?: "")
                                }
                            }
                        }
                    }
                }
            }
            return result
        }

        /**
         * Map the subtitles from the player to human readable strings.
         */
        fun massageSubTitleTracks(
            configList: List<FlickPlayerConfig.TrackConfig>?,
            defaultIndex: Int,
            textTracks: List<TextTrack>?
        ): ArrayList<TrackModel> {
            val selectedTrack = getSelectedSubTitleTrack(defaultIndex, textTracks)
            val result = ArrayList<TrackModel>()
            if (configList != null) {
                for (subTitleConfig in configList) {
                    if (textTracks != null) {
                        for (textTrack in textTracks) {
                            if (subTitleConfig.language == textTrack.language) {
                                result.add(
                                    TrackModel(
                                        textTrack.uniqueId, subTitleConfig.label
                                            ?: "", selectedTrack?.language == textTrack.language
                                    )
                                )
                            }
                        }
                    }
                }
            }
            return result
        }

        private fun getSelectedSubTitleTrack(
            defaultIndex: Int,
            textTracks: List<TextTrack>?
        ): TextTrack? {
            var result: TextTrack? = null
            textTracks?.let {
                if (it.count() > defaultIndex) {
                    result = it[defaultIndex]
                }
            }
            return result
        }

        private fun getSelectedAudioTrack(
            defaultIndex: Int,
            audioTracks: List<AudioTrack>?
        ): AudioTrack? {
            var result: AudioTrack? = null
            audioTracks?.let {
                if (it.count() > defaultIndex) {
                    result = it[defaultIndex]
                }
            }
            return result
        }

        private fun getSelectedVideoTrack(
            defaultIndex: Int,
            videoTracks: List<VideoTrack>?
        ): VideoTrack? {
            var result: VideoTrack? = null
            videoTracks?.let {
                if (it.count() > defaultIndex) {
                    result = it[defaultIndex]
                }
            }
            return result
        }

        private fun getVideoTrackInRange(
            videoTracks: List<VideoTrack>?,
            bitrateStart: Long,
            bitrateEnd: Long
        ): List<VideoTrack> {
            val result = ArrayList<VideoTrack>()
            if (videoTracks != null) {
                for (videoTrack in videoTracks) {
                    if (videoTrack.bitrate in bitrateStart..bitrateEnd) {
                        result.add(videoTrack)
                    }
                }
            }
            return result
        }
    }
}
