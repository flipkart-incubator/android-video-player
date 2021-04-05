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
package com.flipkart.flick.helper.plugins

import android.content.Context
import android.os.CountDownTimer
import com.flipkart.flick.adapter.FlickApplicationAdapterProvider
import com.flipkart.flick.core.interfaces.BookmarkNetworkLayer
import com.flipkart.flick.core.interfaces.NetworkResultListener
import com.flipkart.flick.helper.utils.ParentFinder
import com.google.gson.JsonObject
import com.kaltura.playkit.*
import com.kaltura.playkit.plugins.ads.AdEvent
import com.kaltura.playkit.utils.Consts
import java.util.*

class BookmarkPlugin : PKPlugin() {
    private var bookmarkFrequency: Long = DEFAULT_BOOKMARK_FREQUENCY
    private var mediaStartThreshold: Double = DEFAULT_MEDIA_START_THRESHOLD
    private var mediaEndThreshold: Double = DEFAULT_MEDIA_ENDED_THRESHOLD
    private var playEventWasFired: Boolean = false
    private var intervalOn = false
    private var isFirstPlay = true
    private var isMediaFinished = false
    private var messageBus: MessageBus? = null
    private var isAdPlaying: Boolean = false
    private var bookmarkTimer: Timer? = null
    private var startThresholdTimer: CountDownTimer? = null
    private var contentId: String? = null

    internal var player: Player? = null
    internal var lastKnownPlayerPosition: Long = 0
    internal var lastKnownPlayerDuration: Long = 0

    enum class BookmarkActionType {
        HIT,
        PLAY,
        STOP,
        PAUSE,
        FIRST_PLAY,
        FINISH,
        ERROR
    }

    override fun onLoad(player: Player, config: Any, messageBus: MessageBus, context: Context) {
        setConfig(config)
        this.player = player
        this.messageBus = messageBus
        this.bookmarkTimer = Timer()
        addListeners()
    }

    private fun setConfig(config: Any) {
        if (config is PKPluginConfigs) {
            val pluginConfig = config.getPluginConfig(factory.name)
            if (pluginConfig is JsonObject) {
                val bookmarkFreqConfig = pluginConfig.get(BOOKMARK_FREQUENCY_ARG)
                val mediaStartConfig = pluginConfig.get(MEDIA_START_THRESHOLD_ARG)
                val mediaEndConfig = pluginConfig.get(MEDIA_END_THRESHOLD_ARG)
                val contentIdConfig = pluginConfig.get(CONTENT_ID_ARG)

                if (!bookmarkFreqConfig.isJsonNull) {
                    bookmarkFrequency =
                        if (bookmarkFreqConfig.asLong > DEFAULT_BOOKMARK_FREQUENCY) bookmarkFreqConfig.asLong else DEFAULT_BOOKMARK_FREQUENCY
                }
                if (!mediaStartConfig.isJsonNull) {
                    mediaStartThreshold =
                        if (mediaStartConfig.asDouble > 0) mediaStartConfig.asDouble else DEFAULT_MEDIA_START_THRESHOLD
                }
                if (!mediaEndConfig.isJsonNull) {
                    mediaEndThreshold =
                        if (mediaEndConfig.asDouble > 0) mediaEndConfig.asDouble else DEFAULT_MEDIA_ENDED_THRESHOLD
                }
                if (!contentIdConfig.isJsonNull) {
                    contentId = contentIdConfig.asString
                }
            }
        }
    }

    private fun addListeners() {
        messageBus?.addListener(this, PlayerEvent.playheadUpdated) { event ->
            if (!isAdPlaying) {
                event?.let {
                    if (it.position > 0) {
                        lastKnownPlayerPosition = it.position / Consts.MILLISECONDS_MULTIPLIER
                    }
                    if (it.duration > 0) {
                        lastKnownPlayerDuration = it.duration / Consts.MILLISECONDS_MULTIPLIER
                    }
                }
            }
        }

        messageBus?.addListener(this, PlayerEvent.durationChanged) { event ->
            event?.let {
                lastKnownPlayerDuration = it.duration / Consts.MILLISECONDS_MULTIPLIER

                // send start threshold event only once
                if (startThresholdTimer == null) {
                    startThresholdTimer =
                        object :
                            CountDownTimer((mediaStartThreshold * it.duration).toLong(), 1000) {
                            override fun onFinish() {
                                sendAnalyticsEvent(BookmarkActionType.HIT)
                                cancelCountdownTimer()
                            }

                            override fun onTick(millisUntilFinished: Long) {
                                // no-op
                            }
                        }
                    startThresholdTimer?.start()
                }
            }
        }

        messageBus?.addListener(this, PlayerEvent.stopped) {
            if (isMediaFinished) {
                return@addListener
            }
            isAdPlaying = false
            sendAnalyticsEvent(BookmarkActionType.STOP)
            resetBookmarkTimer()
        }

        messageBus?.addListener(this, PlayerEvent.ended) {
            resetBookmarkTimer()
            sendAnalyticsEvent(BookmarkActionType.FINISH)
            playEventWasFired = false
            isMediaFinished = true
            isFirstPlay = true
        }

        messageBus?.addListener(this, PlayerEvent.error) { event ->
            resetBookmarkTimer()
            val error = event.error
            if (error != null && !error.isFatal) {
                return@addListener
            }
            sendAnalyticsEvent(BookmarkActionType.ERROR)
        }

        messageBus?.addListener(this, PlayerEvent.pause) {
            if (isMediaFinished) {
                return@addListener
            }
            if (playEventWasFired) {
                sendAnalyticsEvent(BookmarkActionType.PAUSE)
                playEventWasFired = false
            }
            resetBookmarkTimer()
        }

        messageBus?.addListener(this, PlayerEvent.play) {
            if (isMediaFinished) {
                return@addListener
            }
            if (isFirstPlay) {
                playEventWasFired = true
                sendAnalyticsEvent(BookmarkActionType.FIRST_PLAY)
            }
            if (!intervalOn) {
                startMediaHitInterval()
            }
        }

        messageBus?.addListener(this, PlayerEvent.playing) {
            isMediaFinished = false
            if (!isFirstPlay && !playEventWasFired) {
                sendAnalyticsEvent(BookmarkActionType.PLAY)
                playEventWasFired = true
            } else {
                isFirstPlay = false
            }
            isAdPlaying = false
        }

        messageBus?.addListener(this, PlayerEvent.seeked) {
            isMediaFinished = false
            sendAnalyticsEvent(BookmarkActionType.HIT)
        }

        messageBus?.addListener(this, PlayerEvent.replay) {
            isMediaFinished = false
        }

        messageBus?.addListener(this, AdEvent.contentPauseRequested) {
            isAdPlaying = true
        }

        messageBus?.addListener(this, AdEvent.contentResumeRequested) {
            isAdPlaying = false
        }
    }

    override fun onUpdateMedia(mediaConfig: PKMediaConfig) {
        isFirstPlay = true
        playEventWasFired = false
        isMediaFinished = false
    }

    override fun onUpdateConfig(config: Any) {
        setConfig(config)
    }

    override fun onApplicationPaused() {
        player?.let {
            val playerPosOnPause = it.currentPosition
            if (playerPosOnPause > 0 && !isAdPlaying) {
                lastKnownPlayerPosition = playerPosOnPause / Consts.MILLISECONDS_MULTIPLIER
            }
        }
        cancelBookmarkTimer()
        cancelCountdownTimer()
    }

    override fun onApplicationResumed() {
        if (!isAdPlaying) {
            startMediaHitInterval()
        }
    }

    public override fun onDestroy() {
        if (messageBus != null) {
            messageBus?.removeListeners(this)
        }
        cancelBookmarkTimer()
        cancelCountdownTimer()
    }

    private fun cancelBookmarkTimer() {
        bookmarkTimer?.cancel()
        bookmarkTimer = null
        intervalOn = false
    }

    private fun cancelCountdownTimer() {
        startThresholdTimer?.cancel()
    }

    private fun resetBookmarkTimer() {
        cancelBookmarkTimer()
        bookmarkTimer = Timer()
    }

    /**
     * Media Hit analytics event
     */
    private fun startMediaHitInterval() {
        if (bookmarkTimer == null) {
            bookmarkTimer = Timer()
        }
        intervalOn = true
        bookmarkTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                sendAnalyticsEvent(BookmarkActionType.HIT)
                if (lastKnownPlayerDuration > 0 && lastKnownPlayerPosition.toFloat() / lastKnownPlayerDuration > mediaEndThreshold) {
                    sendAnalyticsEvent(BookmarkActionType.FINISH)
                    playEventWasFired = false
                    isMediaFinished = true
                    isFirstPlay = true
                }
            }
        }, bookmarkFrequency, bookmarkFrequency) // Get media hit interval from plugin config
    }

    /**
     * Send Bookmark/add event
     *
     * @param eventType - Enum stating the event type to send
     */
    private fun sendAnalyticsEvent(eventType: BookmarkActionType) {
        if (isAdPlaying && eventType != BookmarkActionType.STOP && eventType != BookmarkActionType.FINISH) {
            return
        }
        if (eventType == BookmarkActionType.FINISH) {
            lastKnownPlayerPosition = lastKnownPlayerDuration
        }
        bookmarkNetworkLayer.sendBookmarkEvent(
            eventType,
            lastKnownPlayerPosition,
            contentId ?: "",
            object :
                NetworkResultListener<String> {
                override fun onFailure(failureCode: Int, reason: String) {
                    // no-op
                }

                override fun onSuccess(statusCode: Int, response: String) {
                    // no-op
                }
            })
    }

    companion object {
        lateinit var bookmarkNetworkLayer: BookmarkNetworkLayer
        const val DEFAULT_MEDIA_ENDED_THRESHOLD = 0.98
        const val DEFAULT_MEDIA_START_THRESHOLD = 0.10
        const val DEFAULT_BOOKMARK_FREQUENCY: Long = 30000 // in milli sec

        // args
        const val BOOKMARK_FREQUENCY_ARG = "bookmarkFrequency"
        const val MEDIA_END_THRESHOLD_ARG = "mediaEndThreshold"
        const val MEDIA_START_THRESHOLD_ARG = "mediaStartThreshold"
        const val CONTENT_ID_ARG = "contentId"

        val factory: Factory = object : Factory {
            override fun getName(): String {
                return "BookmarkPlugin"
            }

            override fun newInstance(): PKPlugin {
                return BookmarkPlugin()
            }

            override fun getVersion(): String {
                return BuildConfig.VERSION_NAME
            }

            override fun warmUp(context: Context) {
                val flickApplicationAdapterProvider =
                    ParentFinder(context, FlickApplicationAdapterProvider::class.java).find()
                bookmarkNetworkLayer =
                    flickApplicationAdapterProvider.flickApplicationAdapter.getBookmarkNetworkLayer()
            }
        }
    }
}
