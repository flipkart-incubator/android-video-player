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
package com.flipkart.flick.helper;

import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import androidx.media.session.MediaButtonReceiver;

import static android.view.KeyEvent.KEYCODE_MEDIA_NEXT;
import static android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS;

public class RemoteControlReceiver extends MediaButtonReceiver {
    private static final String TAG = "RemoteControlReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                //MainActivity.mMediaControllerCompat.getTransportControls().play();
                break;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                //MainActivity.mMediaControllerCompat.getTransportControls().pause();
                break;
            case KEYCODE_MEDIA_NEXT:
                //MainActivity.mMediaControllerCompat.getTransportControls().skipToNext();
                break;
            case KEYCODE_MEDIA_PREVIOUS:
                //MainActivity.mMediaControllerCompat.getTransportControls().skipToPrevious();
                break;
        }

//            String intentAction = intent.getAction();
//                if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
//                    KeyEvent event = (KeyEvent) intent
//                            .getParcelableExtra(Intent.EXTRA_KEY_EVENT);
//                    int action = event.getAction();
//                    Log.d("XXX", "action = " + action);
//
//                    setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
//
//                    String command = null;
//                    switch (keycode) {
//                        case KeyEvent.KEYCODE_MEDIA_STOP:
//                            command = "CMDSTOP";
//                            break;
//                        case KeyEvent.KEYCODE_HEADSETHOOK:
//                        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
//                            command = "CMDTOGGLEPAUSE";
//                            break;
//                        case KeyEvent.KEYCODE_MEDIA_NEXT:
//                            command = "CMDNEXT";
//                            break;
//                        case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
//                            command = "CMDPREVIOUS";
//                            break;
//                        case KeyEvent.KEYCODE_MEDIA_PAUSE:
//                            command = "CMDPAUSE";
//                            break;
//                        case KeyEvent.KEYCODE_MEDIA_PLAY:
//                            command = "CMDPLAY";
//                            break;
//                    }
//                //player.pause();
//            }
    }
}