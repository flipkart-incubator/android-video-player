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
import com.flipkart.flick.core.components.AdsModel
import com.flipkart.flick.core.interfaces.AdsNetworkLayer
import com.flipkart.flick.core.interfaces.NetworkResultListener
import com.flipkart.flick.core.model.AdsData

/**
 * Repository for Ads
 */
class AdsRepository : Repository {
    var liveData: MutableLiveData<AdsModel> = MutableLiveData()

    fun get(
        contentId: String,
        networkLayer: AdsNetworkLayer,
        cancellationSignal: CancellationSignal
    ): MutableLiveData<AdsModel> {
        fetchFromNetwork(contentId, networkLayer, cancellationSignal)
        return liveData
    }

    /**
     * Fetch the playback context from the network
     */
    private fun fetchFromNetwork(
        contentId: String,
        networkLayer: AdsNetworkLayer,
        cancellationSignal: CancellationSignal
    ) {
        networkLayer.getAdsVMAP(contentId, object : NetworkResultListener<AdsData> {
            override fun onSuccess(statusCode: Int, response: AdsData) {
                // post value to live data, all the observers will be notified
                val model = AdsModel()
                model.vmap = response
                liveData.postValue(model)
            }

            override fun onFailure(failureCode: Int, reason: String) {
                // create a fake object for powering error case
                val model = AdsModel()
                model.errorReason = reason
                model.errorCode = failureCode
                liveData.postValue(model)
            }

        }, cancellationSignal)
    }
}
