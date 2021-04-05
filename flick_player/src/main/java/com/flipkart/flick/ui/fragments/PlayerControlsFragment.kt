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

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.flipkart.flick.R
import com.flipkart.flick.adapter.FlickAnalyticsAdapterProvider
import com.flipkart.flick.adapter.FlickApplicationAdapterProvider
import com.flipkart.flick.core.components.NextVideoModel
import com.flipkart.flick.core.db.viewmodel.PlayerControlViewModel
import com.flipkart.flick.core.model.ContentData
import com.flipkart.flick.helper.AutoHideHandler
import com.flipkart.flick.helper.PlayerTrackResolver
import com.flipkart.flick.helper.plugins.BookmarkPlugin
import com.flipkart.flick.helper.utils.ParentFinder
import com.flipkart.flick.listeners.PlaybackControlListener
import com.flipkart.flick.listeners.PlayerControlViewListener
import com.flipkart.flick.listeners.PlayerListener
import com.flipkart.flick.ui.helper.OnPlayerGestureListener
import com.flipkart.flick.ui.helper.PlayerGestureHelper
import com.flipkart.flick.ui.helper.isAlive
import com.flipkart.flick.ui.helper.isInPictureInPictureMode
import com.flipkart.flick.ui.listeners.OnContentChangeListener
import com.flipkart.flick.ui.listeners.PlayerParentInteractionListener
import com.flipkart.flick.ui.provider.PlayerDataProvider
import com.kaltura.playkit.Player
import com.kaltura.playkit.PlayerEvent
import com.kaltura.playkit.PlayerState
import com.kaltura.playkit.ads.AdController
import com.kaltura.playkit.player.PKAspectRatioResizeMode
import com.kaltura.playkit.plugins.ads.AdEvent
import com.kaltura.playkit.utils.Consts
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.player_control_fragment_layout.*

/**
 * Houses the code and layout for for play/pause/seek/audio/subtitles/seekbar
 * This class is the starting point for anything to do with overlay on top of player.
 * For ads overlay use [AdsControlsFragment]
 *
 * TODO : The whole code related to autoplay next handling needs a refactor because of the zillion states it keeps
 *
 * Uses [PlayerControlViewModel] as the view model powering the UI
 */
open class PlayerControlsFragment : Fragment(), PlayerControlViewListener, PlayerListener,
    PlaybackControlListener, OnPlayerGestureListener, Observer<NextVideoModel>,
    OnContentChangeListener {
    private var playerParentInteractionListener: PlayerParentInteractionListener? = null

    // callbacks
    private var contentChangeListener: OnContentChangeListener? = null
    private var flickAnalyticsAdapterProvider: FlickAnalyticsAdapterProvider? = null
    private var viewModel: PlayerControlViewModel? = null
    private var flickApplicationAdapterProvider: FlickApplicationAdapterProvider? = null
    private var playerDataProvider: PlayerDataProvider? = null

    private var forceHideAutoPlayControl: Boolean =
        false // the auto play view was hidden by user by tapping outside
    private var cuePoints: ArrayList<Long>? = null
    private var autoHideHandler: AutoHideHandler? = null
    private var isVideoEnded = false
    private var isAutoPlayNextFragmentAdded: Boolean = false
    private var player: Player? = null
    private var firstTimeBuffering = true

    /** first time ready flag, used to set bottom play back controls */
    private var firstTimeReady = false
    private var endVideoThreshold: Int? = null
    private var nextVideoAssetFetched: Boolean = false
    private var nextVideoFetchInProgress: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.player_control_fragment_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        player = playerDataProvider?.getPlayer()
        isVideoEnded =
            player?.let { it.currentPosition != Consts.TIME_UNSET && it.duration != Consts.TIME_UNSET && it.currentPosition >= it.duration }
                ?: false

        player_control_view?.setPlayerCallbackListener(this)
        playerDataProvider?.getAssetResponse()?.let {
            player_control_view?.setTitle(it.title)
        }
        addPlayerEventListeners()

        /** multi tap, pinch zoom gesture configure **/
        PlayerGestureHelper(player_control_view, this)

        /** Toggle controls since they are hidden by default **/
        player_control_view?.toggleVisibility()
        resetAutoHidePlayerControl()
    }

    override fun onSingleTap(helper: PlayerGestureHelper): Boolean {
        if (isAutoPlayNextFragmentAdded && !isVideoEnded) {
            removeAutoPlayNextFragment()
            forceHideAutoPlayControl = true
        } else {
            player_control_view?.toggleVisibility()
            resetAutoHidePlayerControl()
        }
        return true
    }

    override fun onBack() {
        activity?.onBackPressed()
    }

    override fun onMultiTap(
        helper: PlayerGestureHelper,
        widthFactor: Float,
        count: Int
    ): Boolean {
        var didSeek = false
        when {
            widthFactor >= .60f -> {
                //right area click
                val seekForwardBy = seekForwardBy()
                didSeek =
                    seekTo(currentPosition + seekForwardBy)
                if (didSeek && seekForwardBy >= 1000) {
                    player_control_view?.showAndAnimateForwardSeek(count * FORWARD_REWIND_TIME / 1000)
                }
            }
            widthFactor < .40f -> {
                //left area click
                val seekRewindBy = seekRewindBy()
                didSeek =
                    seekTo(currentPosition - seekRewindBy)
                if (didSeek && seekRewindBy >= 1000) {
                    player_control_view?.showAndAnimateRewindSeek(count * FORWARD_REWIND_TIME / 1000)
                }
            }
        }
        if (didSeek) {
            resetAutoHidePlayerControl()
            if (isAutoPlayNextFragmentAdded && !isVideoEnded) {
                removeAutoPlayNextFragment()
                forceHideAutoPlayControl = true
            }
            return true
        }
        return false
    }

    override fun onPinchGesture(helper: PlayerGestureHelper): Boolean {
        fitPlayer()
        return true
    }

    override fun onZoomGesture(helper: PlayerGestureHelper): Boolean {
        zoomPlayer()
        return true
    }

    private fun fitPlayer() {
        player?.updateSurfaceAspectRatioResizeMode(PKAspectRatioResizeMode.fit)
        flickAnalyticsAdapterProvider?.flickAnalyticsAdapter?.onPlayerPinched()
    }

    private fun zoomPlayer() {
        player?.updateSurfaceAspectRatioResizeMode(PKAspectRatioResizeMode.zoom)
        flickAnalyticsAdapterProvider?.flickAnalyticsAdapter?.onPlayerZoomed()
    }

    private fun removeAutoPlayNextFragment() {
        if (!isAlive()) {
            return
        }
        val fragment =
            childFragmentManager.findFragmentByTag(FlickFragmentFactory.Options.NEXT_VIDEO_THUMBNAIL)
        if (fragment != null) {
            isAutoPlayNextFragmentAdded = false
            childFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.viewModel = ViewModelProviders.of(this).get(PlayerControlViewModel::class.java)
        this.autoHideHandler = AutoHideHandler(this)
        this.playerDataProvider = ParentFinder(this, PlayerDataProvider::class.java).find()
        this.flickApplicationAdapterProvider =
            ParentFinder(this, FlickApplicationAdapterProvider::class.java).find()
        this.contentChangeListener = ParentFinder(this, OnContentChangeListener::class.java).find()
        this.flickAnalyticsAdapterProvider =
            ParentFinder(this, FlickAnalyticsAdapterProvider::class.java).find()
        this.playerParentInteractionListener =
            ParentFinder(this, PlayerParentInteractionListener::class.java).find()

        this.viewModel?.currentContent = playerDataProvider?.getAssetResponse()
    }

    override fun onDetach() {
        super.onDetach()
        this.viewModel = null
        this.autoHideHandler = null
        this.playerDataProvider = null
        this.flickApplicationAdapterProvider = null
        this.contentChangeListener = null
        this.flickAnalyticsAdapterProvider = null
    }

    override fun showPlayerControl() {
        if (showAutoPlayNextOrCloseFragmentOrHideAutoPlay()) {
        } else {
            player_control_view?.visibility = View.VISIBLE
            player_control_view?.showAll()
            resetAutoHidePlayerControl()
        }
    }

    /**
     * Return true if auto-play-next can be shown
     */
    private fun shouldShowAutoPlayNextFragment(): Boolean {
        return isTimeToShowAutoPlayNext() &&
                isAdded &&
                !isAutoPlayNextFragmentAdded &&
                !nextVideoFetchInProgress &&
                allAdsCompleted()
    }

    private fun isTimeToShowAutoPlayNext(): Boolean {
        val flickConfig = flickApplicationAdapterProvider?.flickApplicationAdapter?.getFlickConfig()
        return duration != Consts.TIME_UNSET && currentPosition != Consts.TIME_UNSET && (duration - currentPosition) <= flickConfig?.showNextAssetThreshold ?: 0
    }

    private fun allAdsCompleted() =
        (adController == null || adController?.cuePoints == null || adController?.cuePoints?.adCuePoints == null || adController?.cuePoints?.adCuePoints?.size == 0 || adController?.isAllAdsCompleted == true)

    /**
     * Returns whether the current fragment should be closed or kept around
     * A fragment should be closed if video has ended and the next content is not ready and no ads are pending to be played.
     */
    private fun shouldCloseFragment() =
        isVideoEnded && viewModel?.nextAsset?.value?.nextContent == null && allAdsCompleted() && !isInPictureInPictureMode()

    /**
     *
     * Closes fragment if [shouldCloseFragment] returns true or else
     * Shows auto-play-next if [shouldShowAutoPlayNextFragment] returns true,
     */
    private fun showAutoPlayNextOrCloseFragmentOrHideAutoPlay(): Boolean {
        return if (shouldCloseFragment()) {
            player?.stop()
            playerParentInteractionListener?.onPlayerPresentationEnded()
            true
        } else if (shouldShowAutoPlayNextFragment()) {
            showAutoPlayNextFragmentNow()
            true
        } else if (!isTimeToShowAutoPlayNext() && isAutoPlayNextFragmentAdded) {
            removeAutoPlayNextFragment()
            return true
        } else {
            return false
        }
    }

    /**
     * Creates adds the [AutoPlayNextFragment] into the viewport
     */
    private fun showAutoPlayNextFragmentNow() {
        viewModel?.nextAsset?.value?.nextContent?.let {
            if (!isAutoPlayNextFragmentAdded) {
                createAndAddFragment(FlickFragmentFactory.Options.NEXT_VIDEO_THUMBNAIL)
            }
            isAutoPlayNextFragmentAdded = true
            hidePlayerControl()
        }
    }

    override fun onPlaybackOptionClick(option: String) {
        pauseVideo()
        hidePlayerControl()
        createAndAddFragment(option)
        flickAnalyticsAdapterProvider?.flickAnalyticsAdapter?.onPlayerBottomButtonsClicked(
            playerDataProvider?.getAssetResponse(),
            option
        )
    }

    /**
     * Creates and adds a sub fragment as an overall
     */
    private fun createAndAddFragment(fragmentName: String) {
        val fragment = FlickFragmentFactory.createFragment(fragmentName)
        if (fragment is DialogFragment) {
            fragment.show(childFragmentManager, fragmentName)
        } else {
            addFragment(fragment, fragmentName)
        }
    }

    private fun addFragment(fragment: Fragment, option: String) {
        childFragmentManager.beginTransaction()
            .addToBackStack(option)
            .setPrimaryNavigationFragment(fragment)
            .replace(R.id.container, fragment, option)
            .commitAllowingStateLoss()
    }

    override fun hidePlayerControl() {
        if (!isVideoEnded) {
            player_control_view?.hideAll()
        }
    }

    override fun resetAutoHidePlayerControl() {
        autoHideHandler?.removeCallbacksAndMessages(null)
        autoHideHandler?.sendEmptyMessageDelayed(AUTO_HIDE_MSG, AUTO_HIDE_CONTROL_TIME.toLong())
    }

    override val currentPosition: Long
        get() = player?.currentPosition ?: 0

    override val duration: Long
        get() = player?.duration ?: 0

    override val bufferedPosition: Long
        get() = player?.bufferedPosition ?: 0

    override val adController: AdController?
        get() = player?.getController(AdController::class.java)

    override fun playVideo() {
        if (isVideoEnded) {
            player?.replay()
        } else {
            player?.play()
        }
        flickAnalyticsAdapterProvider?.flickAnalyticsAdapter?.onPlayerPlayed(playerDataProvider?.getAssetResponse())
    }

    override fun togglePlayPauseState() {
        super.togglePlayPauseState()
        if (player?.isPlaying == true) {
            pauseVideo()
        } else {
            playVideo()
        }
    }

    override fun pauseVideo() {
        player?.pause()
        flickAnalyticsAdapterProvider?.flickAnalyticsAdapter?.onPlayerPaused(playerDataProvider?.getAssetResponse())
    }

    override fun seekTo(byPosition: Long): Boolean {
        if (byPosition >= 0 && byPosition <= (player?.duration ?: 0)) {
            player?.seekTo(byPosition)
            flickAnalyticsAdapterProvider?.flickAnalyticsAdapter?.onPlayerSeekTo(byPosition)
            return true
        }
        return false
    }

    override fun onScrubStart(positionRelativeToScrubber: Long) {
        autoHideHandler?.removeMessages(AUTO_HIDE_MSG)
        if (isAutoPlayNextFragmentAdded) {
            /** For cases where user wants to scrub back when auto play is scheduling next **/
            removeAutoPlayNextFragment()
        }
    }

    override fun onScrubStop(positionRelativeToScrubber: Long) {
        resetAutoHidePlayerControl()
    }

    fun showProgressBar(showProgress: Boolean) {
        if (showProgress) {
            progress_bar?.visibility = View.VISIBLE
        } else {
            progress_bar?.visibility = View.GONE
        }
    }

    private fun addPlayerEventListeners() {
        player?.addListener(this, PlayerEvent.stateChanged) { event ->
            when {
                event.newState == PlayerState.BUFFERING -> {
                    if (firstTimeBuffering) {
                        // we show player controls when player starts buffering for the first time (with or without ads)
                        firstTimeBuffering = false
                        showPlayerControl()
                    }
                    player_control_view?.hidePlayPause()
                    showProgressBar(true)
                }
                event.newState == PlayerState.LOADING -> {
                    player_control_view?.hidePlayPause()
                    showProgressBar(true)
                }
                event.newState == PlayerState.READY -> {
                    player_control_view?.updatePlayIcon(player?.isPlaying ?: false)
                    showProgressBar(false)
                    if (!firstTimeReady) {
                        // if this is the first time ready is called, toggle the visibility of bottom play back controls
                        player_control_view?.toggleAudioSubTitleViewVisibility(viewModel?.audioTracks?.isNotEmpty() == true || viewModel?.subTitleTracks?.isNotEmpty() == true)
                        player_control_view?.toggleQualityViewVisibility(viewModel?.qualityTracks?.isNotEmpty() == true)
                        firstTimeReady = true
                    }
                }
            }
            isVideoEnded = false
            player_control_view?.setPlayerState(event.newState)
        }

        player?.addListener(this, PlayerEvent.pause) {
            it.eventType()
            player_control_view?.updatePlayIcon(false)
        }

        player?.addListener(this, PlayerEvent.stopped) {
            it.eventType()
            player_control_view?.updatePlayIcon(false)
            showProgressBar(false)
        }

        player?.addListener(this, PlayerEvent.ended) {
            it.eventType()
            isVideoEnded = true
            player_control_view?.updatePlayIcon(false)
            showProgressBar(false)
            showAutoPlayNextOrCloseFragmentOrHideAutoPlay()
        }

        player?.addListener(this, PlayerEvent.play) {
            player_control_view?.updatePlayIcon(true)
        }

        player?.addListener(this, PlayerEvent.playheadUpdated) {
            player_control_view?.updateProgress()
            // when the end bookmark point is reached fetch the response for next video and keep it ready
            if (endVideoThreshold == null) {
                playerDataProvider?.getAssetResponse()?.let {
                    it.mediaEndThreshold?.let { end ->
                        endVideoThreshold = (duration * end).toInt()
                    } ?: run {
                        endVideoThreshold =
                            (duration * BookmarkPlugin.DEFAULT_MEDIA_ENDED_THRESHOLD).toInt()
                    }
                }
            }
            if (currentPosition > (endVideoThreshold
                    ?: 0) && !nextVideoAssetFetched && !nextVideoFetchInProgress
            ) {
                nextVideoFetchInProgress = true
                fetchNextVideoAsset()
            }
            if (!forceHideAutoPlayControl) {
                showAutoPlayNextOrCloseFragmentOrHideAutoPlay()
            }
            // check if end position, show auto play next view.
        }

        player?.addListener(this, PlayerEvent.error) {
            if (it.error.isFatal) {
                showProgressBar(false)
            }
        }

        player?.addListener(this, PlayerEvent.durationChanged) {
            cuePoints?.let {
                player_control_view?.setMarkers(it, true)
            }
        }

        player?.addListener(this, AdEvent.cuepointsChanged) { event ->
            cuePoints = event.cuePoints.adCuePoints as ArrayList<Long>
        }

        player?.addListener(this, AdEvent.contentResumeRequested) {
            showPlayerControl()
        }

        player?.addListener(this, AdEvent.allAdsCompleted) {
            // so that auto play can come after post rolls
            // after video ends and after post rolls, we don't get contentResumeRequested
            showAutoPlayNextOrCloseFragmentOrHideAutoPlay()
        }

        player?.addListener(
            this,
            PlayerEvent.tracksAvailable
        ) { event ->
            val trackConfigs =
                flickApplicationAdapterProvider?.flickApplicationAdapter?.getFlickConfig()
                    ?.trackConfig

            /** video quality tracks */
            PlayerTrackResolver.massageQualityTracks(
                trackConfigs?.qualityConfigList,
                event.tracksInfo.defaultVideoTrackIndex,
                event.tracksInfo.videoTracks
            ).apply {
                viewModel?.qualityTracks = this
            }

            /** subtitle tracks */
            PlayerTrackResolver.massageSubTitleTracks(
                trackConfigs?.trackConfigList,
                event.tracksInfo.defaultTextTrackIndex,
                event.tracksInfo.textTracks
            ).apply {
                viewModel?.subTitleTracks = this
            }

            /** audio tracks */
            PlayerTrackResolver.massageAudioTracks(
                trackConfigs?.trackConfigList,
                event.tracksInfo.defaultAudioTrackIndex,
                event.tracksInfo.audioTracks
            ).apply {
                viewModel?.audioTracks = this
            }
        }

        player?.addListener(this, PlayerEvent.videoTrackChanged) { event ->
            // update the selected track
            viewModel?.qualityTracks?.forEach {
                it.selected = it.id == event.newTrack.uniqueId
            }
        }

        player?.addListener(this, PlayerEvent.audioTrackChanged) { event ->
            // update the selected track
            viewModel?.audioTracks?.forEach {
                it.selected = it.id == event.newTrack.uniqueId
            }
        }

        player?.addListener(this, PlayerEvent.textTrackChanged) { event ->
            // update the selected track
            viewModel?.subTitleTracks?.forEach {
                it.selected = it.id == event.newTrack.uniqueId
            }
        }
    }

    private fun fetchNextVideoAsset() {
        viewModel?.nextAsset?.observe(this, this)
    }

    override fun onChanged(nextViewModel: NextVideoModel?) {
        nextViewModel?.error?.let { error ->
            flickAnalyticsAdapterProvider?.flickAnalyticsAdapter?.onAutoPlayNextError(
                playerDataProvider?.getAssetResponse(),
                error
            )
        } ?: run {
            nextViewModel?.nextContent?.let {
                // update the view model for next video content and keep it ready
                nextVideoAssetFetched = true
                nextVideoFetchInProgress = false
                showAutoPlayNextOrCloseFragmentOrHideAutoPlay()
            }
        }
    }

    override fun closePlaybackControl(fragment: Fragment) {
        childFragmentManager.beginTransaction().remove(fragment).commitNowAllowingStateLoss()
        childFragmentManager.popBackStackImmediate()
    }

    override fun playbackControlClosed() {
        playVideo()
        showPlayerControl()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player?.removeListeners(this)
        player = null
        firstTimeBuffering = false
        clearFindViewByIdCache()
    }

    override fun onContentChange(content: ContentData) {
        isVideoEnded = false
        player?.stop()
        removeAutoPlayNextFragment()
        contentChangeListener?.onContentChange(content)
        flickAnalyticsAdapterProvider?.flickAnalyticsAdapter?.onPlayerContentChanged(content)
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)

        if (isInPictureInPictureMode) {
            //zoomPlayer() /* disabled this line because it causes the player to be bigger than PIP window */
        }
    }

    companion object {
        private const val AUTO_HIDE_MSG = 0
        private const val AUTO_HIDE_CONTROL_TIME = 7000 // 3 seconds
        const val FORWARD_REWIND_TIME = 10000 // 10 seconds
    }
}
