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
package com.flipkart.flick.ui.helper

import com.flipkart.flick.core.model.ContentData
import com.flipkart.flick.helper.plugins.BookmarkPlugin
import com.google.gson.JsonObject
import com.kaltura.playkit.PKPluginConfigs
import com.kaltura.playkit.plugins.ima.IMAConfig

class PluginConfigFactory {

    fun createBookmarkConfig(contentData: ContentData?): PKPluginConfigs {
        val configs = PKPluginConfigs()

        // creating json objects
        val jsonObject = JsonObject()
        jsonObject.addProperty(
            BookmarkPlugin.MEDIA_END_THRESHOLD_ARG,
            contentData?.mediaEndThreshold
        )
        jsonObject.addProperty(
            BookmarkPlugin.MEDIA_START_THRESHOLD_ARG,
            contentData?.mediaStartThreshold
        )
        jsonObject.addProperty(
            BookmarkPlugin.BOOKMARK_FREQUENCY_ARG,
            contentData?.bookmarkFrequency
        )
        jsonObject.addProperty(BookmarkPlugin.CONTENT_ID_ARG, contentData?.contentId ?: "")
        configs.setPluginConfig(BookmarkPlugin.factory.name, jsonObject)

        return configs
    }

    fun createImaConfig(adVMAP: String?): IMAConfig {
        val imaConfig = IMAConfig()
        if (adVMAP != null) {
            imaConfig.adTagResponse = adVMAP
            val videoMimeTypes = ArrayList<String>()
            videoMimeTypes.add("video/mp4")
            videoMimeTypes.add("application/x-mpegURL")
            videoMimeTypes.add("application/dash+xml")
            imaConfig.videoMimeTypes = videoMimeTypes
            imaConfig.adLoadTimeOut = 5
            imaConfig.setAdAttribution(false).adCountDown = false
        }
        return imaConfig
    }
}
