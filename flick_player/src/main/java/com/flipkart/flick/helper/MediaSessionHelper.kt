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
package com.flipkart.flick.helper

import android.app.Activity
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context.AUDIO_SERVICE
import android.content.Intent
import android.media.AudioManager
import android.os.RemoteException
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.kaltura.playkit.Player


/**
 * Anything to with media session, showing buttons on remote view etc are here
 * It also manages interactions with headphone controls for play / pause
 *
 * This class was specifically introduced to show player controls within PIP window.
 * But MediaSession in general is useful for other cases like headphone support / Android auto support and background playback etc.
 *
 * This class also manages Audio focus via [retrieveAudioFocus], make sure you call this method before playback.
 * Audio focus is useful in cases where another app starts playing audio while this video is running.
 * For e.g, start playing youtube videos when a video is playing in this app (PIP mode maybe), in such a case the video here will gracefully pause.
 *
 */
class MediaSessionHelper(val activity: Activity?, val player: Player?) :
    AudioManager.OnAudioFocusChangeListener {

    private var mediaControllerCompat: MediaControllerCompat? = null
    private var mediaSessionCompat: MediaSessionCompat? = null

    fun initMediaSession(mediaSessionCallback: MediaSessionCompat.Callback) {
        retrieveAudioFocus()
        try {
            activity?.let {
                val mediaButtonReceiver =
                    ComponentName(activity.applicationContext, RemoteControlReceiver::class.java)
                mediaSessionCompat = MediaSessionCompat(
                    activity.applicationContext,
                    "Flick",
                    mediaButtonReceiver,
                    null
                )

                mediaSessionCompat?.setCallback(mediaSessionCallback)
                mediaSessionCompat?.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
                mediaSessionCompat?.setPlaybackToLocal(AudioManager.STREAM_MUSIC)
                val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
                mediaButtonIntent.setClass(activity, RemoteControlReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(activity, 0, mediaButtonIntent, 0)
                mediaSessionCompat?.setMediaButtonReceiver(pendingIntent)

                mediaSessionCompat?.let {
                    mediaControllerCompat = MediaControllerCompat(activity, it.sessionToken)
                }

                mediaControllerCompat?.registerCallback(mMediaControllerCompatCallback)
                MediaControllerCompat.setMediaController(it, mediaControllerCompat)
                mediaSessionCompat?.isActive = true
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    fun setMediaPlaybackState(state: Int) {
        val playbackStateBuilder = PlaybackStateCompat.Builder()
        if (state == PlaybackStateCompat.STATE_PLAYING) {
            playbackStateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PAUSE)
        } else {
            playbackStateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_PLAY)
        }
        playbackStateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f)
        mediaSessionCompat?.setPlaybackState(playbackStateBuilder.build())
    }

    fun retrieveAudioFocus(): Boolean {
        val audioManager = activity?.getSystemService(AUDIO_SERVICE) as AudioManager?
        val result = audioManager?.requestAudioFocus(
            this,
            AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN
        )
        return result == AudioManager.AUDIOFOCUS_GAIN
    }

    private val mMediaControllerCompatCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            if (state == null) {
                return
            }

            when (state.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                }
            }
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                if (player?.isPlaying != false) {
                    player?.pause()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                player?.pause()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                player?.setVolume(0.3f)
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (player != null) {
                    if (!player.isPlaying) {
                        player.play()

                    }
                    player.setVolume(1f)
                }
            }
        }
    }
}
