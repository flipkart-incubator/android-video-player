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
package com.flipkart.flick

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.flipkart.flick.adapter.FlickAnalyticsAdapterProvider
import com.flipkart.flick.adapter.FlickApplicationAdapterProvider
import com.flipkart.flick.core.components.PlaybackContextModel
import com.flipkart.flick.core.db.repository.FlickStreamingViewModelFactory
import com.flipkart.flick.core.db.viewmodel.FlickStreamingViewModel
import com.flipkart.flick.core.db.viewmodel.PlaybackContextWithAdsViewModel
import com.flipkart.flick.core.model.ContentData
import com.flipkart.flick.core.model.PlaybackContext
import com.flipkart.flick.helper.FlickError
import com.flipkart.flick.helper.MediaSessionHelper
import com.flipkart.flick.helper.PlayerMode
import com.flipkart.flick.helper.PlayerSettings
import com.flipkart.flick.helper.plugins.BookmarkPlugin
import com.flipkart.flick.helper.utils.ParentFinder
import com.flipkart.flick.provider.PlaybackContextToMediaEntry
import com.flipkart.flick.ui.fragments.AdsControlsFragment
import com.flipkart.flick.ui.fragments.PlayerControlsFragment
import com.flipkart.flick.ui.helper.*
import com.flipkart.flick.ui.listeners.OnContentChangeListener
import com.flipkart.flick.ui.listeners.OnTrackChangeListener
import com.flipkart.flick.ui.model.FlickPlayerConfig
import com.flipkart.flick.ui.model.PlayerError
import com.flipkart.flick.ui.provider.PlayerDataProvider
import com.flipkart.flick.ui.provider.SystemUIHelperProvider
import com.kaltura.playkit.*
import com.kaltura.playkit.PlayKitManager.registerPlugins
import com.kaltura.playkit.ads.PKAdErrorType
import com.kaltura.playkit.player.PKAspectRatioResizeMode
import com.kaltura.playkit.player.PKPlayerErrorType
import com.kaltura.playkit.plugins.ads.AdEvent
import com.kaltura.playkit.plugins.ima.IMAPlugin
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.flick_error_layout.*
import kotlinx.android.synthetic.main.flick_streaming_fragment_layout.*

/**
 * Parent Fragment for the player. Uses Play kit wrapper (which is on top of exoplayer) to run the video.
 * To start a video, create an instance and add the following param to the bundle, e.g bundle.putSerializable(FlickStreamingFragment.ASSET_RESPONSE_ARG, [ContentData] ()
 * Switches between [AdsControlsFragment] and [PlayerControlsFragment] as deemed necessary.
 * Anything related to player video playback surface and not related to the player controls UI or ads should be put here.
 *
 * Uses [FlickStreamingViewModel] as the backing view model (for making API calls behind it) which gets cleared when a new content is selected via [onContentChange]
 */
open class FlickStreamingFragment : androidx.fragment.app.Fragment(), OnTrackChangeListener,
    OnContentChangeListener,
    Observer<PlaybackContextWithAdsViewModel>, PlayerDataProvider, SystemUIHelperProvider {

    /** Is ad playing right now **/
    private var isAdVisible: Boolean = false

    /** All the data used by this fragment is behind this view model **/
    private var flickStreamingViewModel: FlickStreamingViewModel? = null

    /** Holds the last occurred error **/
    private var lastError: PlayerError? = null

    /** Indicates that someone is waiting for the first ready callback. Non critical api calls should happen after the first ready callback */
    private var waitingForFirstReady: Boolean = false

    /** Media session handling is delegated to this helper **/
    private var mediaSessionHelper: MediaSessionHelper? = null

    /** Delegates creation of new plugin configs to the this class **/
    private val pluginConfigFactory: PluginConfigFactory = PluginConfigFactory()

    /** Holds the current content config **/
    private var contentConfig: ContentData? = null

    /** Adapter to communicate with the hosting application **/
    private var flickApplicationAdapterProvider: FlickApplicationAdapterProvider? = null

    /** Adapter to communicate with the analytics adapter **/
    private var flickAnalyticsAdapterProvider: FlickAnalyticsAdapterProvider? = null

    /** Actual Kaltura Playkit player instance backing the video playback **/
    private var player: Player? = null

    /** The previous UI orientation before rotating to landscape **/
    private var previousOrientation: Int? = null

    /** Helper to show/hide system UI like status bar and navigation bar **/
    private var systemUiHelper: SystemUiHelper? = null

    /** This variable is used to refresh playback context **/
    private var playbackContextExpiryTime: Long = Long.MAX_VALUE

    /** Landscape rotation happens even during popbackstack all the way to root, using runnables hack to avoid this **/
    private val landscapeRunnable: Runnable = Runnable {
        PlayerMode.switchToLandscapeMode(activity)
    }

    /** player config controlled via app config by host application **/
    private var flickPlayerConfig: FlickPlayerConfig? = null

    override fun onTrackChange(id: String) {
        try {
            player?.changeTrack(id)
        } catch (e: IllegalArgumentException) {
            Log.d(TAG, e.message)
        } finally {
            player?.play()
        }
        flickAnalyticsAdapterProvider?.flickAnalyticsAdapter?.onPlayerTrackChange(id)
    }

    override fun getSystemHelper(): SystemUiHelper? {
        return systemUiHelper
    }

    override fun getPlayer(): Player? {
        return player
    }

    override fun getAssetResponse(): ContentData? {
        return contentConfig
    }

    /**
     * Mimics starting the fragment from scratch
     */
    private fun reloadEverything() {
        startLoadingPlaybackContext()
    }

    override fun onContentChange(content: ContentData) {
        // update the content config
        contentConfig = content
        reloadEverything()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(createBundleNoFragmentRestore(savedInstanceState))
    }

    /**
     * We don't want Player controls fragment or ads fragment to be restored on activity restore because they need player instance.
     * So we prevent restoring of fragments.
     *
     */
    private fun createBundleNoFragmentRestore(bundle: Bundle?): Bundle? {
        bundle?.remove("android:support:fragments")
        return bundle
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.flick_streaming_fragment_layout, container, false)
        contentConfig = arguments?.getSerializable(CONTENT_CONFIG_ARG) as ContentData

        val shouldAutoHideSystemUI =
            arguments?.getBoolean(SHOULD_AUTO_HIDE_SYSTEM_UI_ARG, true) ?: true
        if (shouldAutoHideSystemUI) {
            activity?.let {
                systemUiHelper =
                    SystemUiHelper(
                        it,
                        SystemUiHelper.LEVEL_IMMERSIVE,
                        SystemUiHelper.FLAG_IMMERSIVE_STICKY
                    )
            }
        }

        previousOrientation = activity?.resources?.configuration?.orientation
        return view
    }

    private fun startLoadingPlaybackContext() {
        if (!isAlive()) {
            return
        }

        /** show poster **/
        loadPoster()

        /** clear all view models **/
        viewModelStore.clear()
        waitingForFirstReady = false

        activity?.let { activity ->
            flickStreamingViewModel =
                ViewModelProviders.of(
                    this,
                    FlickStreamingViewModelFactory(activity.application, contentConfig)
                )
                    .get(FlickStreamingViewModel::class.java)
        }

        progress_bar?.visibility = View.VISIBLE
        contentConfig?.contentId?.let {
            // first step towards playback, make playback context call
            observePlaybackContextViewModel()
        } ?: run {
            val flickError = FlickError.EMPTY_CONTENT_ID
            setError(flickError.errorTitle, flickError.errorBody, false)
            flickAnalyticsAdapterProvider?.flickAnalyticsAdapter?.onPlayerStartError(
                contentConfig,
                flickError.errorBody,
                flickError.errorCode,
                flickError
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        flickAnalyticsAdapterProvider?.flickAnalyticsAdapter?.onPlayerOpened(contentConfig)
        back_arrow?.setOnClickListener {
            activity?.onBackPressed()
        }
        startLoadingPlaybackContext()
    }

    override fun onChanged(playbackContextModel: PlaybackContextWithAdsViewModel?) {
        playbackContextModel?.playbackContext?.let {
            onPlaybackContextResponse(
                it,
                playbackContextModel.adsModel?.vmap?.vmap
            )
        }
    }

    override fun onStart() {
        super.onStart()
        /** Calling player on-resume here because entering/exiting from PIP mode calls onPause/onResume which we dont want **/
        player?.onApplicationResumed()

        /** Portrait to landscape rotation is posted as a runnable so that if onStop gets called immediately we can stop the rotation from happening **/
        /** It is a hack, but there is no other clean way to detect if the fragment lifecycle is being called within a POP-ALL fragments kind of operation **/
        player_root.post(landscapeRunnable)
    }

    override fun onStop() {
        super.onStop()
        player_root.removeCallbacks(landscapeRunnable)
        PlayerMode.switchToPortraitMode(activity, previousOrientation)

        /** Calling player on-pause here because entering/exiting from PIP mode calls onPause/onResume which we dont want **/
        player?.onApplicationPaused()
    }

    override fun onPause() {
        super.onPause()
        systemUiHelper?.show()
    }

    override fun onResume() {
        super.onResume()
        if (playbackContextExpiryTime <= System.currentTimeMillis()) {
            startLoadingPlaybackContext()
        }
        systemUiHelper?.hide()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        flickStreamingViewModel?.playbackContext?.removeObservers(this)
        removeAllListeners()
        player?.destroy()
        player = null
        systemUiHelper?.show()
        clearFindViewByIdCache()
        flickAnalyticsAdapterProvider?.flickAnalyticsAdapter?.onPlayerClosed(contentConfig)
        mediaSessionHelper = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.flickAnalyticsAdapterProvider =
            ParentFinder(this, FlickAnalyticsAdapterProvider::class.java).find()
        this.flickApplicationAdapterProvider =
            ParentFinder(this, FlickApplicationAdapterProvider::class.java).find()
        this.flickPlayerConfig =
            flickApplicationAdapterProvider?.flickApplicationAdapter?.getFlickConfig()
    }

    override fun onDetach() {
        super.onDetach()
        this.flickAnalyticsAdapterProvider = null
        this.flickApplicationAdapterProvider = null
    }

    private fun loadPoster() {
        contentConfig?.image?.let {
            loading_image_view?.visibility = View.VISIBLE
            this.flickApplicationAdapterProvider?.flickApplicationAdapter?.loadImage(
                this,
                loading_image_view,
                it,
                0,
                true
            )
        }
    }

    private fun hidePoster() {
        loading_image_view?.visibility = View.GONE
    }

    /**
     * Creates player, registers plugins, attaches listeners from scratch and destroys/removes old ones if they exist
     */
    private fun createPlayer(adsVmap: String?) {
        val pluginConfigs = PKPluginConfigs()
        if (player != null) {
            player?.destroy()
        }

        if (flickPlayerConfig?.adsEnabled == true) {
            adsVmap?.let {
                /** this means that if ads API doesn't load, the player loads faster, because IMAPlugin is slow to load **/
                registerPlugins(context, IMAPlugin.factory)
                pluginConfigs.setPluginConfig(
                    IMAPlugin.factory.name,
                    pluginConfigFactory.createImaConfig(adsVmap)
                )
            }
        }

        registerPlugins(context, BookmarkPlugin.factory)
        pluginConfigs.setPluginConfig(
            BookmarkPlugin.factory.name,
            pluginConfigFactory.createBookmarkConfig(contentConfig)
        )

        removePlayerControls()
        setAdControlVisibility(false)
        player = PlayKitManager.loadPlayer(activity, pluginConfigs)
        player?.settings?.setSubtitleStyle(PlayerSettings.subTitleStyle)
        player?.settings?.setPreferredTextTrack(PlayerSettings.preferredTextTrack)

        flickPlayerConfig?.let {
            player?.settings?.setABRSettings(PlayerSettings.abrSettings(it))
        }

        /** Security feature to prevent recording **/
        player?.settings?.setSecureSurface(true)

        mediaSessionHelper = MediaSessionHelper(activity, player)
        mediaSessionHelper?.initMediaSession(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                super.onPlay()
                /** This gets called from PIP, or from headphone controls **/
                if (lastError != null) {
                    lastError?.let { onRetryErrorClick(it) }
                } else if (mediaSessionHelper?.retrieveAudioFocus() == true) {
                    player?.play()
                }
            }

            override fun onPause() {
                super.onPause()
                player?.pause()
            }
        })

        removeAllListeners()
        addPlayerListeners()
        addAdsListeners()

        player_root?.removeAllViews()
        player_root?.addView(
            player?.view,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        setPlayerControlVisibility(true)
        waitingForFirstReady = true
    }

    private fun onPlaybackContextResponse(
        playbackContextModel: PlaybackContextModel,
        adsVmap: String?
    ) {
        hideError()
        val flickError: FlickError
        if (playbackContextModel.errorReason != null) {
            /** in case of error */
            if (!isNetworkAvailable()) {
                flickError = FlickError.NO_CONNECTION
                setError(
                    flickError.errorTitle,
                    flickError.errorBody,
                    true,
                    ::reloadEverything
                )
            } else {
                flickError = FlickError.PLAYBACK_CONTEXT_REQUEST_FAILED
                setError(
                    flickError.errorTitle,
                    playbackContextModel.errorReason ?: flickError.errorBody,
                    true,
                    ::reloadEverything
                )
            }
            flickAnalyticsAdapterProvider?.flickAnalyticsAdapter?.onPlayerStartError(
                contentConfig,
                playbackContextModel.errorReason ?: flickError.errorBody,
                playbackContextModel.errorCode ?: 0,
                flickError
            )
        } else {
            /** in case of success */
            val contentId = contentConfig?.contentId
            val pkMediaEntry =
                contentId?.let { PlaybackContextToMediaEntry.create(it, playbackContextModel) }
            if (pkMediaEntry != null && pkMediaEntry.hasSources()) {
                /** Prepare the player with the media entry from server **/
                preparePlayerAndPlay(
                    pkMediaEntry,
                    playbackContextModel.playbackContext,
                    playbackContextModel.playbackContext?.startPosition?.toLong() ?: 0, adsVmap
                )
            } else {
                flickError = FlickError.NO_MEDIA_ENTRY_TO_PLAY
                setError(
                    flickError.errorTitle,
                    flickError.errorBody,
                    false
                )
                flickAnalyticsAdapterProvider?.flickAnalyticsAdapter?.onPlayerStartError(
                    contentConfig,
                    flickError.errorBody,
                    flickError.errorCode,
                    flickError
                )
            }
        }
        /** Water mark to be added and animated **/
        WatermarkHelper(
            this,
            activity,
            watermark_container,
            flickApplicationAdapterProvider,
            playbackContextModel
        )
        watermark_container?.alpha = 0f
        watermark_container?.animate()?.cancel()
        watermark_container?.animate()?.setStartDelay(1000)?.setDuration(1000)?.alpha(1f)
        /** In case where video changed within PIP window, we need to remove watermark **/
        invalidateWatermark()
    }

    private fun observePlaybackContextViewModel() {
        flickStreamingViewModel?.playbackContext?.removeObservers(this)
        /** The playback context live data is designed to talk to its repository the moment you touch it for the first time **/
        flickStreamingViewModel?.playbackContext?.observe(this, this)
    }

    private fun preparePlayerAndPlay(
        mediaEntry: PKMediaEntry,
        playbackContext: PlaybackContext?,
        startPosition: Long,
        adsVmap: String?
    ) {
        playbackContext?.ttl?.let {
            playbackContextExpiryTime = System.currentTimeMillis() + it
        }
        createPlayer(adsVmap)
        player?.prepare(PlayerSettings.createMediaConfig(mediaEntry, startPosition))
        player?.play()

        if (flickPlayerConfig?.defaultZoomAspectRatio == true) {
            /** set the aspect ratio of the player to zoom mode **/
            player?.updateSurfaceAspectRatioResizeMode(PKAspectRatioResizeMode.zoom)
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val activeNetworkInfo = connectivityManager?.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun addPlayerListeners() {
        player?.addListener(this, PlayerEvent.error) { event ->
            // we let the player continue playing if though there was an error, so that intermittent errors are shielded by the buffer
            if (event.error.isFatal) {
                player?.stop()
                // errors should show up only when playback has stalled, not otherwise
                val pkPlayerErrorType = event.error.errorType as? PKPlayerErrorType
                val error = pkPlayerErrorType?.let { "${it.name} #${it.errorCode}" }
                    ?: event.error.errorType.name
                val flickError: FlickError =
                    if (pkPlayerErrorType == PKPlayerErrorType.SOURCE_ERROR || pkPlayerErrorType == PKPlayerErrorType.LOAD_ERROR) {
                        if (!isNetworkAvailable()) {
                            FlickError.NO_CONNECTION
                        } else {
                            FlickError.BUFFER_NETWORK_ERROR
                        }
                    } else {
                        FlickError.BUFFER_RENDER_ERROR
                    }
                setError(
                    flickError.errorTitle,
                    flickError.errorBody + " \n$error",
                    true,
                    ::reloadEverything
                )
                flickAnalyticsAdapterProvider?.flickAnalyticsAdapter?.onPlayerBufferError(
                    contentConfig,
                    pkPlayerErrorType, flickError
                )
                mediaSessionHelper?.setMediaPlaybackState(PlaybackStateCompat.STATE_ERROR)
            }
        }
        player?.addListener(this, PlayerEvent.stateChanged) {
            if (it.newState == PlayerState.LOADING) {
                if (contentConfig?.image == null) {
                    /** for backward compatibility **/
                    progress_bar?.visibility = View.GONE
                    back_arrow?.visibility = View.GONE
                }
                mediaSessionHelper?.setMediaPlaybackState(PlaybackStateCompat.STATE_CONNECTING)
            } else if (it.newState == PlayerState.READY) {
                hidePoster()
                if (player?.isPlaying != false) {
                    mediaSessionHelper?.setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING)
                } else {
                    mediaSessionHelper?.setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED)
                }
                if (waitingForFirstReady) {
                    performNonCriticalWork()
                    waitingForFirstReady = false
                }
                progress_bar?.visibility = View.GONE
                back_arrow?.visibility = View.GONE
            }
        }
        player?.addListener(this, PlayerEvent.pause) {
            keepScreenOn(false)
            mediaSessionHelper?.setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED)
        }
        player?.addListener(this, PlayerEvent.play) {
            systemUiHelper?.hide()
        }
        player?.addListener(this, PlayerEvent.playing) {
            keepScreenOn(true)
            mediaSessionHelper?.setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING)
            hideError()
        }
        player?.addListener(this, PlayerEvent.stopped) {
            keepScreenOn(false)
            mediaSessionHelper?.setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED)
        }
    }

    /**
     * Do anything which should not affect playback, but has to be done ASAP.
     * This function will be called once the initial buffering is done.
     */
    private fun performNonCriticalWork() {
        // nothing as of now, can be used in future
    }

    private fun addAdsListeners() {
        player?.addListener(this, AdEvent.loaded) {
            hidePoster()
            progress_bar?.visibility = View.GONE
            back_arrow?.visibility = View.GONE
        }
        player?.addListener(this, AdEvent.started) {
            /* ads is playing, disable auto lock */
            keepScreenOn(true)
            setAdControlVisibility(true)
            setPlayerControlVisibility(false)
        }
        player?.addListener(this, AdEvent.paused) {
            /* ads is paused, enable auto lock */
            keepScreenOn(false)
        }
        player?.addListener(this, AdEvent.resumed) {
            /* ads is resumed, disable auto lock */
            keepScreenOn(true)
        }
        player?.addListener(this, AdEvent.contentResumeRequested) {
            setAdControlVisibility(false)
            setPlayerControlVisibility(true)
        }
        player?.addListener(this, AdEvent.contentPauseRequested) {
            setAdControlVisibility(true)
            setPlayerControlVisibility(false)
        }
        player?.addListener(this, AdEvent.error) {
            /* ads error, enable auto lock */
            /** Do not disable screen lock here because it causes screen to lock when invalid ad comes. **/
            if (it.error.errorType !== PKAdErrorType.QUIET_LOG_ERROR) {
                flickAnalyticsAdapterProvider?.flickAnalyticsAdapter?.onAdError(
                    contentConfig,
                    it.error.errorType as? PKAdErrorType,
                    it.error.isFatal
                )
            }
            setAdControlVisibility(false)
            setPlayerControlVisibility(true)
        }
    }

    private fun removeAllListeners() {
        player?.removeListeners(this)
    }

    private fun setAdControlVisibility(visible: Boolean) {
        if (!isAlive()) {
            return
        }
        isAdVisible = visible
        var fragment: AdsControlsFragment? =
            childFragmentManager.findFragmentByTag(TAG_AD_CONTROL) as? AdsControlsFragment
        if (fragment == null) {
            fragment = AdsControlsFragment.newInstance()
        }
        val fragmentTransaction = childFragmentManager.beginTransaction()
        if (visible) {
            fragmentTransaction.setPrimaryNavigationFragment(fragment)
                .replace(R.id.ad_controls_container, fragment, TAG_AD_CONTROL)
        } else {
            fragmentTransaction.remove(fragment)
        }
        fragmentTransaction.commitNowAllowingStateLoss()
        invalidateWatermark()
    }

    private fun setPlayerControlVisibility(visible: Boolean) {
        if (!isAlive()) {
            return
        }
        var fragment: Fragment? =
            childFragmentManager.findFragmentByTag(TAG_PLAYER_CONTROL) as? PlayerControlsFragment
        if (fragment == null) {
            fragment = PlayerControlsFragment()
        }
        val fragmentTransaction = childFragmentManager.beginTransaction()
        if (visible) {
            fragmentTransaction.setPrimaryNavigationFragment(fragment)
                .replace(R.id.player_controls_container, fragment, TAG_PLAYER_CONTROL)
                .show(fragment)
        } else {
            fragmentTransaction.hide(fragment)
        }
        fragmentTransaction.commitNowAllowingStateLoss()
        invalidateWatermark()
    }

    private fun removePlayerControls() {
        val fragment: PlayerControlsFragment? =
            childFragmentManager.findFragmentByTag(TAG_PLAYER_CONTROL) as? PlayerControlsFragment
        if (fragment != null) {
            val fragmentTransaction = childFragmentManager.beginTransaction()
            fragmentTransaction.remove(fragment)
            fragmentTransaction.commitNowAllowingStateLoss()
        }
    }

    private fun setError(
        title: String,
        error: String,
        showRetry: Boolean,
        onRetryClick: (() -> Unit)? = null
    ) {
        lastError = PlayerError(title, error, showRetry, onRetryClick)
        hidePoster()
        invalidateErrorView()
    }

    private fun invalidateErrorView() {
        if (lastError == null) {
            error_title?.text = ""
            error_body?.text = ""
            error_container?.visibility = View.GONE
        }
        lastError?.let {
            player?.stop()
            val inPictureInPictureMode = isInPictureInPictureMode()
            error_container?.visibility = View.VISIBLE
            error_title?.text = it.title
            progress_bar?.visibility = View.GONE
            if (!inPictureInPictureMode) {
                error_body?.text = it.error
                error_body?.visibility = View.VISIBLE
            } else {
                error_body?.visibility = View.GONE
            }
            if (!inPictureInPictureMode) {
                back_arrow?.visibility = View.VISIBLE
            }
            if (it.showRetry && !inPictureInPictureMode) {
                error_retry_button?.visibility = View.VISIBLE
                error_retry_button?.setOnClickListener { _ ->
                    onRetryErrorClick(it)
                }
            } else {
                error_retry_button?.visibility = View.GONE
            }
        }
    }

    private fun onRetryErrorClick(it: PlayerError) {
        hideError()
        it.onRetryClick?.let { it2 -> it2() }
    }

    private fun hideError() {
        lastError = null
        invalidateErrorView()
    }

    private fun isInPictureInPictureMode(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            activity?.isInPictureInPictureMode ?: false
        } else {
            false
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        invalidateWatermark()
        invalidateControls()
        invalidateErrorView()
    }

    private fun invalidateWatermark() {
        if (isInPictureInPictureMode() || isAdVisible) {
            watermark_container?.visibility = View.GONE
        } else {
            watermark_container?.visibility = View.VISIBLE
        }
    }

    private fun invalidateControls() {
        if (isInPictureInPictureMode()) {
            player_controls_container?.visibility = View.GONE
        } else {
            player_controls_container?.visibility = View.VISIBLE
        }
    }

    override fun getMediaSessionHelper(): MediaSessionHelper? {
        return mediaSessionHelper
    }

    companion object {
        const val TAG = "FlickStreamingFragment" //NON-NLS
        const val TAG_PLAYER_CONTROL = "PlayerControls"
        const val TAG_AD_CONTROL = "AdControls"

        // args
        const val CONTENT_CONFIG_ARG = "contentConfig"
        const val SHOULD_AUTO_HIDE_SYSTEM_UI_ARG = "shouldAutoHideSystemUI"

        /**
         * Check if PIP mode is supported
         */
        private fun isPictureInPictureSupported(context: Context?): Boolean {
            val packageManager = context?.packageManager
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                packageManager?.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE) ?: false
            } else {
                return false
            }
        }

        /**
         * This method is supposed to be blindly called by the hosting app in [Activity.onUserLeaveHint]
         * It will check if the fragment instance is right, and then enter picture-in-picture mode if video is playing
         */
        fun enterPIPModeIfRequired(fragment: Fragment, activity: Activity?) {
            activity?.let {
                val flickStreamingFragment = fragment as? FlickStreamingFragment
                if (flickStreamingFragment?.isVisible != false && flickStreamingFragment?.player?.isPlaying != false) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isPictureInPictureSupported(
                            activity
                        )
                    ) {
                        activity.enterPictureInPictureMode(PictureInPictureParams.Builder().build())
                    }
                }
            }
        }
    }
}
