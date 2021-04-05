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
package com.flipkart.flick.core.db.viewmodel

import android.os.CancellationSignal
import android.os.CountDownTimer
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.flipkart.flick.core.components.AdsModel
import com.flipkart.flick.core.components.PlaybackContextModel
import com.flipkart.flick.core.db.repository.AdsRepository
import com.flipkart.flick.core.db.repository.PlaybackContextRepository
import com.flipkart.flick.core.db.viewmodel.PlaybackContextWithAdsViewModel.Companion.TIMEOUT
import com.flipkart.flick.core.interfaces.AdsNetworkLayer
import com.flipkart.flick.core.interfaces.PlaybackNetworkLayer
import com.flipkart.flick.core.model.ContentRequest

/**
 * A view model which behind the scenes delegates work to 2 APIs can combines them.
 *
 * Make call to get Ads VMAP, and parallely make call to get playback context.
 * If both succeed within the timeout, return a model containing both.
 * Otherwise just give one with playback context.
 * There;s also a timeout for ads response See [TIMEOUT], within which the response has to come
 */
class PlaybackContextWithAdsViewModel(
    private var combinedLiveData: MediatorLiveData<PlaybackContextWithAdsViewModel> = MediatorLiveData(),
    private val cancellationSignal: CancellationSignal = CancellationSignal()
) : ViewModel() {
    private val playbackRepo: PlaybackContextRepository = PlaybackContextRepository()
    private val adsRepo: AdsRepository = AdsRepository()
    private var countDownTimer: CountDownTimer? = null
    private var timedOut = false
    var playbackContext: PlaybackContextModel? = null
    var adsModel: AdsModel? = null

    init {
        combinedLiveData.addSource(playbackRepo.liveData) {
            playbackContext = it
            checkAndRespond()
        }
        combinedLiveData.addSource(adsRepo.liveData) {
            if (!timedOut) {
                // if timeout is done, then don't send the response over since the countdown would have sent it anyway
                adsModel = it
                checkAndRespond()
            }
        }
    }

    fun getPlaybackContextWithAds(
        request: ContentRequest,
        playbackNetworkLayer: PlaybackNetworkLayer,
        adsNetworkLayer: AdsNetworkLayer
    ): MutableLiveData<PlaybackContextWithAdsViewModel> {
        playbackContext = null
        adsModel = null
        countDownTimer?.cancel()
        timedOut = false
        countDownTimer = object : CountDownTimer(TIMEOUT, TIMEOUT) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                timedOut = true
                checkAndRespond()
            }
        }

        countDownTimer?.start()
        playbackRepo.get(
            request,
            playbackNetworkLayer,
            cancellationSignal
        )
        adsRepo.get(request.contentId, adsNetworkLayer, cancellationSignal)
        return combinedLiveData
    }

    private fun checkAndRespond() {
        if ((adsModel != null && playbackContext != null) || (playbackContext != null && timedOut)) {
            // if both ads and playback context is there, send it over
            // or if timeout is reached and playback context is there, send it over
            combinedLiveData.value = this
            countDownTimer?.cancel()
        }
    }

    public override fun onCleared() {
        super.onCleared()
        // cancel the call if pending
        cancellationSignal.cancel()
        countDownTimer?.cancel()
    }

    companion object {
        private const val TIMEOUT: Long = 5000
    }
}
