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
package com.flipkart.flick.ui.views

import android.content.Context
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.flipkart.flick.R
import com.flipkart.flick.helper.utils.Util
import com.flipkart.flick.helper.utils.Util.PROGRESS_BAR_MAX
import com.flipkart.flick.helper.utils.Util.stringForTime
import com.flipkart.flick.listeners.PlayerListener
import com.flipkart.flick.ui.fragments.FlickFragmentFactory
import com.flipkart.flick.ui.fragments.PlayerControlsFragment.Companion.FORWARD_REWIND_TIME
import com.flipkart.flick.ui.helper.OnPlayerGestureListener
import com.flipkart.flick.ui.helper.PlayerGestureHelper
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ui.TimeBar
import com.kaltura.playkit.PlayerState
import kotlinx.android.synthetic.main.player_control_view.view.*
import kotlinx.android.synthetic.main.player_seekbar_layout.view.*
import kotlinx.android.synthetic.main.player_settings_layout.view.*
import kotlin.math.roundToInt

/**
 * Player Control View which powers playVideo / pauseVideo and other features
 */
class PlayerControlView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), View.OnClickListener,
    TimeBar.OnScrubListener, OnPlayerGestureListener {
    private var durationWasSet: Boolean = false
    private var previouslyPlaying: Boolean = false
    private var mainContainerHidden: Boolean = true
    private var isBeingDragged = false
    private var playerState: PlayerState? = null
    private lateinit var playerCallbackListener: PlayerListener

    private val pauseToPlayDrawable: Drawable by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            resources.getDrawable(R.drawable.pause_to_play)
        } else {
            VectorDrawableCompat.create(
                context.resources,
                R.drawable.pause_to_play,
                null
            ) as Drawable
        }
    }

    private val playToPauseDrawable: Drawable by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            resources.getDrawable(R.drawable.play_to_pause)
        } else {
            VectorDrawableCompat.create(
                context.resources,
                R.drawable.play_to_pause,
                null
            ) as Drawable
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.player_control_view, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        play_pause_button?.setOnClickListener(this)
        exo_progress?.addListener(this)
        subtitle_text_view?.setOnClickListener(this)
        video_quality_text_view?.setOnClickListener(this)

        val audioSubtitleVector =
            VectorDrawableCompat.create(context.resources, R.drawable.audio_subtitle_drawable, null)
        subtitle_text_view?.setCompoundDrawablesWithIntrinsicBounds(
            audioSubtitleVector,
            null,
            null,
            null
        )

        val videoQualityVector =
            VectorDrawableCompat.create(context.resources, R.drawable.settings_drawable, null)
        video_quality_text_view?.setCompoundDrawablesWithIntrinsicBounds(
            videoQualityVector,
            null,
            null,
            null
        )

        back_arrow.setOnClickListener {
            playerCallbackListener.onBack()
        }

        /** Adding gesture handling to the seek buttons since users might double click on the buttons too and expect it to work **/
        PlayerGestureHelper(rewind, this)
        PlayerGestureHelper(forward, this)
    }

    fun setTitle(titleString: String?) {
        title?.text = titleString
    }

    fun toggleQualityViewVisibility(visible: Boolean) {
        if (visible) {
            video_quality_text_view?.visibility = View.VISIBLE
        } else {
            video_quality_text_view?.visibility = View.GONE
        }
    }

    fun toggleAudioSubTitleViewVisibility(visible: Boolean) {
        if (visible) {
            subtitle_text_view?.visibility = View.VISIBLE
        } else {
            subtitle_text_view?.visibility = View.GONE
        }
    }

    fun togglePlaybackControlVisibility(visible: Boolean) {
        if (visible) {
            video_settings_layout?.visibility = View.VISIBLE
        } else {
            video_settings_layout?.visibility = View.GONE
        }
    }

    fun toggleRewindForwardVisibility(visible: Boolean) {
        if (visible) {
            rewind_group?.visibility = View.VISIBLE
            forward_group?.visibility = View.VISIBLE
        } else {
            rewind_group?.visibility = View.GONE
            forward_group?.visibility = View.GONE
        }
    }

    fun toggleScrubberSeek(enable: Boolean) {
        exo_progress.isEnabled = enable
    }

    fun hidePlayPause() {
        play_pause_button?.visibility = View.INVISIBLE
    }

    fun setPlayerCallbackListener(playerCallbackListener: PlayerListener) {
        this.playerCallbackListener = playerCallbackListener
    }

    fun updatePlayIcon(isPlaying: Boolean) {
        play_pause_button?.visibility = View.VISIBLE
        if (isPlaying) {
            play_pause_button?.setImageDrawable(playToPauseDrawable)
            if (!previouslyPlaying) {
                (playToPauseDrawable as? Animatable)?.start()
            }
        } else {
            play_pause_button?.setImageDrawable(pauseToPlayDrawable)
            if (previouslyPlaying) {
                (pauseToPlayDrawable as? Animatable)?.start()
            }
        }
        previouslyPlaying = isPlaying
    }

    /** Currently only handles forward and rewind buttons and NOT for the entire player control view **/
    override fun onMultiTap(helper: PlayerGestureHelper, widthFactor: Float, count: Int): Boolean {
        super.onMultiTap(helper, widthFactor, count)
        return handleTap(helper, count)
    }

    /** Currently only handles forward and rewind buttons and NOT for the entire player control view **/
    override fun onSingleTap(helper: PlayerGestureHelper): Boolean {
        super.onSingleTap(helper)
        return handleTap(helper, 1)
    }

    private fun handleTap(helper: PlayerGestureHelper, count: Int): Boolean {
        if (helper.gestureCapturingView?.id == R.id.forward) {
            val seekForwardBy = playerCallbackListener.seekForwardBy()
            if (playerCallbackListener.seekTo(playerCallbackListener.currentPosition + seekForwardBy)) {
                if (seekForwardBy >= 1000) {
                    showAndAnimateForwardSeek(count * FORWARD_REWIND_TIME / 1000)
                }
                playerCallbackListener.resetAutoHidePlayerControl()
            }
            return true
        } else if (helper.gestureCapturingView?.id == R.id.rewind) {
            val seekRewindBy = playerCallbackListener.seekRewindBy()
            if (playerCallbackListener.seekTo(playerCallbackListener.currentPosition - seekRewindBy)) {
                if (seekRewindBy >= 1000) {
                    showAndAnimateRewindSeek(count * FORWARD_REWIND_TIME / 1000)
                }
                playerCallbackListener.resetAutoHidePlayerControl()
            }
            return true
        }
        return false
    }

    override fun onClick(v: View) {
        if (!mainContainerHidden) {
            when (v.id) {
                R.id.play_pause_button -> {
                    playerCallbackListener.togglePlayPauseState()
                    playerCallbackListener.resetAutoHidePlayerControl()
                }
                R.id.video_quality_text_view -> {
                    playerCallbackListener.onPlaybackOptionClick(FlickFragmentFactory.Options.VIDEO_QUALITY_ICON)
                }
                R.id.subtitle_text_view -> {
                    playerCallbackListener.onPlaybackOptionClick(FlickFragmentFactory.Options.TEXT_AUDIO_ICON)
                }
            }
        } else {
            toggleVisibility()
        }
    }

    private fun positionValue(progress: Long): Long {
        val duration = playerCallbackListener.duration
        var positionValue = 0L
        if (duration > 0)
            positionValue = (duration * progress / PROGRESS_BAR_MAX).toFloat().roundToInt().toLong()
        return positionValue
    }

    fun setPlayerState(playerState: PlayerState) {
        this.playerState = playerState
        updateProgress()
    }

    fun updateProgress() {
        val duration = playerCallbackListener.duration
        val position = playerCallbackListener.currentPosition
        val bufferedPosition = playerCallbackListener.bufferedPosition

        if (duration != C.TIME_UNSET) {
            exo_duration?.text = stringForTime(duration)
        }

        if (!isBeingDragged && position != C.POSITION_UNSET.toLong() && duration != C.TIME_UNSET) {
            if (!durationWasSet) {
                /** Duration is not set until the first [updateProgress] call **/
                /** Fade in the seekbar once the video loads **/
                durationWasSet = true
                seekbar_parent?.visibility = View.VISIBLE
                seekbar_parent?.alpha = 0f
                seekbar_parent?.animate()?.cancel()
                seekbar_parent?.animate()?.duration = 500
                seekbar_parent?.animate()?.alpha(1f)

                if (!mainContainerHidden) {
                    /** Fade in rewind buttons **/
                    rewind?.visibility = View.VISIBLE
                    rewind?.alpha = 0f
                    rewind?.animate()?.alpha(1f)

                    /** Fade in forward buttons **/
                    forward?.visibility = View.VISIBLE
                    forward?.alpha = 0f
                    forward?.animate()?.alpha(1f)

                    /** Fade in play pause button **/
                    play_pause_button?.alpha = 0f
                    play_pause_button?.animate()?.alpha(1f)
                }
            }
            exo_position?.text = stringForTime(position)
            exo_progress?.setPosition(Util.progressBarValue(position, duration).toLong())
            exo_progress?.setDuration(Util.progressBarValue(duration, duration).toLong())
        }

        exo_progress?.setBufferedPosition(
            Util.progressBarValue(
                bufferedPosition,
                duration
            ).toLong()
        )
    }

    override fun onScrubStart(timeBar: TimeBar, position: Long) {
        isBeingDragged = true
        middle_controls?.visibility = View.INVISIBLE
        video_settings_layout?.visibility = View.INVISIBLE
        playerCallbackListener.onScrubStart(position)
    }

    override fun onScrubMove(timeBar: TimeBar, position: Long) {
        exo_position?.text =
            stringForTime((position * playerCallbackListener.duration) / PROGRESS_BAR_MAX)
    }

    override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
        timeBar.setPosition(position)
        isBeingDragged = false
        playerCallbackListener.seekTo(positionValue(position))
        playerCallbackListener.onScrubStop(position)
        middle_controls?.visibility = View.VISIBLE
        video_settings_layout?.visibility = View.VISIBLE
    }

    fun setMarkers(adCuePoints: List<Long>, showAdsCuePoints: Boolean) {
        if (adCuePoints.isNotEmpty() && showAdsCuePoints) {
            val adPoints = ArrayList<Long>()
            val playedAdGroups = ArrayList<Boolean>()
            adCuePoints.forEach { cuepoint ->
                when (cuepoint) {
                    0L -> adPoints.add(cuepoint)
                    -1L -> adPoints.add(playerCallbackListener.duration)
                    else -> {
                        adPoints.add(
                            Util.progressBarValue(
                                cuepoint,
                                playerCallbackListener.duration
                            ).toLong()
                        )
                    }
                }
                playedAdGroups.add(false)
            }
            exo_progress?.setAdGroupTimesMs(
                adPoints.toLongArray(),
                playedAdGroups.toBooleanArray(),
                adPoints.size
            )
        }
    }

    fun hideAll() {
        top_bar?.animate()?.alpha(0f)?.translationY((-100f))
            ?.withEndAction { top_bar?.visibility = View.GONE }
        bottom_controls?.animate()?.alpha(0f)?.translationY(100f)
            ?.withEndAction { bottom_controls?.visibility = View.GONE }
        forward?.animate()
            ?.alpha(0f)?.withEndAction { forward?.visibility = View.GONE }
        forward_shadow?.animate()?.alpha(0f)
        forward_text?.animate()?.alpha(0f)
        rewind?.animate()?.alpha(0f)?.withEndAction { rewind?.visibility = View.GONE }
        rewind_shadow?.animate()?.alpha(0f)
        rewind_text?.animate()?.alpha(0f)
        play_pause_button?.animate()?.alpha(0f)
            ?.withEndAction { play_pause_button?.visibility = View.INVISIBLE }
        mainContainerHidden = true
    }

    fun showAll() {
        visibility = View.VISIBLE
        top_bar?.visibility = View.VISIBLE
        top_bar?.animate()?.alpha(1f)?.translationY(0f)
        bottom_controls?.visibility = View.VISIBLE
        bottom_controls?.animate()?.alpha(1f)?.translationY(0f)
        if (durationWasSet) {
            rewind?.visibility = View.VISIBLE
            rewind?.animate()?.alpha(1f)
            forward?.visibility = View.VISIBLE
            forward?.animate()?.alpha(1f)
        }
        play_pause_button?.visibility = View.VISIBLE
        play_pause_button?.animate()?.alpha(1f)
        mainContainerHidden = false
    }

    fun showAndAnimateForwardSeek(seconds: Int) {
        showAndAnimateSeek(seconds, "+", forward_text, forward, forward_shadow)
    }

    fun showAndAnimateRewindSeek(seconds: Int) {
        showAndAnimateSeek(seconds, "-", rewind_text, rewind, rewind_shadow)
    }

    private fun showAndAnimateSeek(
        seconds: Int,
        sign: String,
        textView: TextView?,
        imageView: ImageView?,
        shadowView: View?
    ) {
        visibility = View.VISIBLE
        textView?.text = "$sign $seconds seconds"
        textView?.animate()?.cancel()
        textView?.alpha = .8f
        textView?.visibility = View.VISIBLE
        textView?.animate()?.alpha(1f)
        imageView?.visibility = View.VISIBLE
        imageView?.animate()?.cancel()
        imageView?.animate()?.alpha(1f)
        (imageView?.drawable as? Animatable)?.start()
        if (shadowView?.visibility == View.INVISIBLE) {
            shadowView.visibility = View.VISIBLE
            shadowView.alpha = 0f
            shadowView.animate()?.cancel()
            shadowView.animate()?.alpha(1f)
            shadowView.animate().startDelay = 0
        } else {
            shadowView?.animate()?.cancel()
        }

        imageView?.animate()?.alpha(1f)?.withEndAction {
            if (mainContainerHidden) {
                imageView.animate()?.alpha(0f)?.withEndAction { imageView.visibility = View.GONE }
            }
            shadowView?.animate()?.alpha(0f)
                ?.withEndAction { shadowView.visibility = View.INVISIBLE }
            textView?.animate()?.alpha(0f)?.withEndAction {
                textView.visibility = View.INVISIBLE
            }
        }
    }

    fun toggleVisibility() {
        if (mainContainerHidden) {
            showAll()
        } else {
            hideAll()
        }
    }
}
