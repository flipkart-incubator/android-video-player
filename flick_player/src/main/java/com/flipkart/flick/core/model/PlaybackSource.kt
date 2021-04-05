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

class PlaybackSource {
    var url: String? = null
    var id: Int? = null
    var type: String? = null
    var duration: Long? = null
    var externalId: String? = null
    var altExternalId: String? = null
    var defaultLanguage = false
    var language: String? = null
    var outputProtectionLevel: String? = null
    var drm: List<DrmPlaybackData>? = null
    var format: String? = null
    var protocols: String? = null
    var tokenized = false
}
