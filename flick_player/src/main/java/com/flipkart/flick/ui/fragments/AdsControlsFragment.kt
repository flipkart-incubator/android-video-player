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
package com.flipkart.flick.ui.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.flipkart.flick.R
import com.flipkart.flick.adapter.FlickAnalyticsAdapterProvider
import com.flipkart.flick.helper.AutoHideHandler
import com.flipkart.flick.helper.utils.ParentFinder
import com.flipkart.flick.listeners.AutoHideable
import com.flipkart.flick.ui.helper.isInPictureInPictureMode
import com.flipkart.flick.ui.provider.PlayerDataProvider
import com.google.android.exoplayer2.C
import com.kaltura.playkit.Player
import com.kaltura.playkit.ads.AdController
import com.kaltura.playkit.plugins.ads.AdEvent
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.ads_control_fragment_layout.*

/**
 * Houses the play/pause/progressbar overlays for Ads
 */
class AdsControlsFragment : androidx.fragment.app.Fragment(), View.OnClickListener, AutoHideable {
    private var flickAnalyticsAdapterProvider: FlickAnalyticsAdapterProvider? = null
    private var player: Player? = null
    private var playerDataProvider: PlayerDataProvider? = null
    private var adController: AdController? = null
    private var autoHideHandler: AutoHideHandler? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.ads_control_fragment_layout, container, false)
        flickAnalyticsAdapterProvider?.flickAnalyticsAdapter?.onAdShown()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        play_button?.setOnClickListener(this)
        pause_button?.setOnClickListener(this)
        player = playerDataProvider?.getPlayer()
        adController = player?.getController(AdController::class.java)
        addListeners()
        back_arrow?.setOnClickListener {
            activity?.onBackPressed()
        }
        ad_seek_bar?.setOnTouchListener { _, _ -> true }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.playerDataProvider = ParentFinder(this, PlayerDataProvider::class.java).find()
        this.autoHideHandler = AutoHideHandler(this@AdsControlsFragment)
        this.flickAnalyticsAdapterProvider =
            ParentFinder(this, FlickAnalyticsAdapterProvider::class.java).find()
    }

    override fun onDetach() {
        super.onDetach()
        this.autoHideHandler?.removeCallbacksAndMessages(null)
        this.playerDataProvider = null
        this.autoHideHandler = null
        this.flickAnalyticsAdapterProvider = null
    }

    private fun addListeners() {
        player?.addListener(this, AdEvent.started) {
            val bumper = it.adInfo.isBumper
            if (bumper) {
                ad_seek_bar?.visibility = View.GONE
                time_left?.visibility = View.GONE
            } else {
                ad_seek_bar?.visibility = View.VISIBLE
                time_left?.visibility = View.VISIBLE
            }
            updatePlayIcon(true)
            progress_bar?.visibility = View.GONE
            playerDataProvider?.getMediaSessionHelper()
                ?.setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING)

            toggleAdsControl(!isInPictureInPictureMode())
        }
        player?.addListener(this, AdEvent.adRequested) {
            progress_bar?.visibility = View.VISIBLE
        }
        player?.addListener(this, AdEvent.paused) {
            updatePlayIcon(false)
        }
        player?.addListener(this, AdEvent.adBufferStart) {
            progress_bar?.visibility = View.VISIBLE
        }
        player?.addListener(this, AdEvent.adBufferEnd) {
            progress_bar?.visibility = View.GONE
        }
        player?.addListener(this, AdEvent.resumed) {
            updatePlayIcon(true)
        }
        player?.addListener(this, AdEvent.tapped) {
            if (play_pause_controller?.visibility == View.VISIBLE) {
                hidePlayerControl()
            } else {
                showPlayerControl()
            }
        }
        player?.addListener(this, AdEvent.adProgress) {
            val position: Long? = adController?.adCurrentPosition
            val duration: Long? = adController?.adDuration
            if (position != null && duration != null && position != C.POSITION_UNSET.toLong() && duration != C.TIME_UNSET) {
                val timeLeftText = (((duration - position) / 1000) + 1).toString()
                time_left?.text = timeLeftText
                ad_seek_bar?.progress = progressBarValue(position)
                ad_seek_bar?.max = progressBarValue(duration)
            }
        }
        player?.addListener(this, AdEvent.paused) {
            playerDataProvider?.getMediaSessionHelper()
                ?.setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED)
        }
    }

    private fun removeListeners() {
        player?.removeListeners(this)
    }

    private fun progressBarValue(position: Long): Int {
        var progressValue = 0
        var duration: Long
        adController?.let {
            if (it.adDuration >= 0 && it.adDuration != C.TIME_UNSET) {
                duration = it.adDuration
                if (duration > 0) {
                    progressValue = Math.round((position * PROGRESS_BAR_MAX / duration).toFloat())
                }
            }
        }
        return progressValue
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.play_button -> {
                playAd()
            }
            R.id.pause_button -> {
                pauseAd()
            }
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        if (!isInPictureInPictureMode) {
            /** On change of pip mode, add all the ads controls back if the player is not in pip mode. */
            toggleAdsControl(true)
        }
    }

    private fun playAd() {
        this.player?.play()
        updatePlayIcon(true)
        resetAutoHidePlayerControl()
        flickAnalyticsAdapterProvider?.flickAnalyticsAdapter?.onAdPlayed()
    }

    private fun pauseAd() {
        this.player?.pause()
        updatePlayIcon(false)
        resetAutoHidePlayerControl()
        flickAnalyticsAdapterProvider?.flickAnalyticsAdapter?.onAdPaused()
    }

    private fun updatePlayIcon(boolean: Boolean) {
        if (boolean) {
            pause_button?.visibility = View.VISIBLE
            play_button?.visibility = View.INVISIBLE
        } else {
            pause_button?.visibility = View.INVISIBLE
            play_button?.visibility = View.VISIBLE
        }
    }

    private fun showPlayerControl() {
        play_pause_controller?.visibility = View.VISIBLE
        play_pause_controller?.alpha = 0.0f
        play_pause_controller?.animate()
            ?.alpha(1.0f)
            ?.setListener(null)
        resetAutoHidePlayerControl()
    }

    override fun hidePlayerControl() {
        play_pause_controller?.animate()
            ?.alpha(0.0f)
            ?.setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    play_pause_controller?.visibility = View.GONE
                }
            })
        resetAutoHidePlayerControl()
    }

    private fun resetAutoHidePlayerControl() {
        autoHideHandler?.removeCallbacksAndMessages(null)
        autoHideHandler?.sendEmptyMessageDelayed(AUTO_HIDE_MSG, AUTO_HIDE_CONTROL_TIME.toLong())
    }

    private fun toggleAdsControl(show: Boolean) {
        time_left?.visibility = if (show) View.VISIBLE else View.GONE
        ad_seek_bar?.visibility = if (show) View.VISIBLE else View.GONE
        back_arrow?.visibility = if (show) View.VISIBLE else View.GONE
        toggleAdsOverlay(show)
    }

    /**
     * A hacky way to hide the ads overlay (learn more, skip ad layer)
     * This is a web view which gets added on top of the player view by exo-player. There is no way to hide
     * this layer, hence we loop through the children and hide the web view if present.
     *
     * NOTE: This is done to remove the overlay when the ads plays in PIP mode.
     */
    private fun toggleAdsOverlay(show: Boolean) {
        player?.view?.postDelayed({
            val playerViewChildCount = player?.view?.childCount
            playerViewChildCount?.let {
                for (i in 0 until playerViewChildCount + 1) {
                    val viewGroup = player?.view?.getChildAt(i) as? ViewGroup
                    val childCount = viewGroup?.childCount
                    childCount?.let {
                        for (j in 0 until it + 1) {
                            val webView = viewGroup.getChildAt(j) as? WebView
                            if (webView != null) {
                                webView.visibility = if (show) View.VISIBLE else View.GONE
                                break
                            }
                        }
                    }
                }
            }
        }, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeListeners()
        player = null
        clearFindViewByIdCache()
    }

    companion object {
        @JvmStatic
        fun newInstance() = AdsControlsFragment()

        private const val PROGRESS_BAR_MAX = 100

        // auto hide values
        private const val AUTO_HIDE_MSG = 0
        private const val AUTO_HIDE_CONTROL_TIME = 3000 // 3 seconds
    }
}
