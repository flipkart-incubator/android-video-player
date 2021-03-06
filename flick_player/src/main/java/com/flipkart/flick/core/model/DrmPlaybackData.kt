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
package com.flipkart.flick.core.model

import androidx.annotation.StringDef

class DrmPlaybackData {
    @JvmField
    @DrmSchemaNameDef
    var scheme: String? = null

    @JvmField
    var licenseURL: String? = null

    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @StringDef(
        DrmSchemaName.PLAYREADY_CENC,
        DrmSchemaName.WIDEVINE_CENC,
        DrmSchemaName.FAIRPLAY,
        DrmSchemaName.WIDEVINE,
        DrmSchemaName.PLAYREADY,
        DrmSchemaName.CUSTOM_DRM
    )
    annotation class DrmSchemaNameDef
    object DrmSchemaName {
        const val PLAYREADY_CENC = "PLAYREADY_CENC"
        const val WIDEVINE_CENC = "WIDEVINE_CENC"
        const val FAIRPLAY = "FAIRPLAY"
        const val WIDEVINE = "WIDEVINE"
        const val PLAYREADY = "PLAYREADY"
        const val CUSTOM_DRM = "CUSTOM_DRM"
    }
}
