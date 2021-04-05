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
package com.flipkart.flick.core.interfaces

import android.os.CancellationSignal
import com.flipkart.flick.core.model.PlaybackContext
import com.flipkart.flick.core.model.ContentRequest
import com.flipkart.flick.core.model.ContentData

interface PlaybackNetworkLayer {

    /**
     * Get playback context from server. By sending the contentId, we can get dash/hls URLs and DRM licenses required for playback.
     */
    fun getPlaybackContext(
        request: ContentRequest,
        resultListener: NetworkResultListener<PlaybackContext>,
        cancellationSignal: CancellationSignal
    )

    /**
     * Get next content from server for auto play feature
     */
    fun getNextAsset(
        request: ContentRequest,
        resultListener: NetworkResultListener<ContentData>,
        cancellationSignal: CancellationSignal
    )
}
