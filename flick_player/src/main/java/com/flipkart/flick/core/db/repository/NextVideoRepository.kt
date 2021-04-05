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
package com.flipkart.flick.core.db.repository

import android.os.CancellationSignal
import androidx.lifecycle.MutableLiveData
import com.flipkart.flick.core.components.NextVideoModel
import com.flipkart.flick.core.interfaces.NetworkResultListener
import com.flipkart.flick.core.interfaces.PlaybackNetworkLayer
import com.flipkart.flick.core.model.ContentData
import com.flipkart.flick.core.model.ContentRequest

/**
 * Repository for next auto play video
 */
class NextVideoRepository : Repository {

    fun get(
        liveData: MutableLiveData<NextVideoModel>,
        contentId: String,
        networkLayer: PlaybackNetworkLayer,
        cancellationSignal: CancellationSignal
    ): MutableLiveData<NextVideoModel> {
        liveData.value = null
        fetchFromNetwork(
            liveData,
            contentId,
            networkLayer,
            cancellationSignal
        )
        return liveData
    }

    /**
     * Fetch the playback context from the network
     */
    private fun fetchFromNetwork(
        liveData: MutableLiveData<NextVideoModel>,
        contentId: String,
        networkLayer: PlaybackNetworkLayer,
        cancellationSignal: CancellationSignal
    ) {
        val request = ContentRequest(contentId)
        networkLayer.getNextAsset(request, object : NetworkResultListener<ContentData> {
            override fun onSuccess(statusCode: Int, response: ContentData) {
                // post value to live data, all the observers will be notified
                val model = NextVideoModel()
                model.nextContent = response
                liveData.postValue(model)
            }

            override fun onFailure(failureCode: Int, reason: String) {
                // create a fake object for powering error case
                val model = NextVideoModel()
                model.error = reason
                liveData.postValue(model)
            }

        }, cancellationSignal)
    }
}
