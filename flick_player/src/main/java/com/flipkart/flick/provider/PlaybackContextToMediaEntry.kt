/*
 * ============================================================================
 * Copyright (C) 2017 Kaltura Inc.
 *
 * Licensed under the AGPLv3 license, unless a different license for a
 * particular library is specified in the applicable library path.
 *
 * You may obtain a copy of the License at
 * https://www.gnu.org/licenses/agpl-3.0.html
 * ============================================================================
 */

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
package com.flipkart.flick.provider

import com.flipkart.flick.core.components.PlaybackContextModel
import com.flipkart.flick.core.model.PlaybackSource
import com.kaltura.playkit.PKMediaEntry
import com.kaltura.playkit.PKMediaSource
import com.kaltura.playkit.providers.base.FormatsHelper
import com.kaltura.playkit.utils.Consts
import java.util.*

object PlaybackContextToMediaEntry {

    /**
     * Creates a PKMediaEntry required by player given a list of sources
     */
    private fun getMediaFromSources(
        contentId: String,
        playbackSources: List<PlaybackSource>?
    ): PKMediaEntry {
        val mediaEntry = PKMediaEntry()
        mediaEntry.id = contentId
        mediaEntry.name = null

        val sources = ArrayList<PKMediaSource>()

        var maxDuration: Long = 0

        if (playbackSources != null) {

            // if provided, only the "formats" matching MediaFiles should be parsed and added to the PKMediaEntry media sources
            for (playbackSource in playbackSources) {

                val mediaFormat = FormatsHelper.getPKMediaFormat(
                    playbackSource.format,
                    playbackSource.drm != null && playbackSource.drm!!.isNotEmpty()
                ) ?: continue

                val pkMediaSource = PKMediaSource()
                    .setId(playbackSource.id.toString())
                    .setUrl(playbackSource.url)
                    .setMediaFormat(mediaFormat)

                val drmData = playbackSource.drm
                if (drmData != null && drmData.isNotEmpty()) {
                    if (!MediaProvidersUtils.isDRMSchemeValid(pkMediaSource, drmData)) {
                        continue
                    }
                    MediaProvidersUtils.updateDrmParams(pkMediaSource, drmData)
                }

                sources.add(pkMediaSource)
                maxDuration = (playbackSource.duration ?: 0).coerceAtLeast(maxDuration)
            }
        }
        return mediaEntry.setDuration(maxDuration * Consts.MILLISECONDS_MULTIPLIER)
            .setSources(sources)
            .setMediaType(PKMediaEntry.MediaEntryType.Vod)
    }

    /**
     * Parse and create a [PKMediaEntry] object from the API response.
     */
    fun create(contentId: String, playbackContextModel: PlaybackContextModel): PKMediaEntry {
        val mediaEntry: PKMediaEntry = getMediaFromSources(
            contentId,
            playbackContextModel.playbackContext?.sources
        )
        mediaEntry.mediaType = PKMediaEntry.MediaEntryType.Vod
        return mediaEntry
    }
}
