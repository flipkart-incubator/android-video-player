# flick-android-player

A video player written fully in Kotlin using View models and Live Data with repository pattern for API layer
All the video playback related features are fully implemented here, and infrastructural features like image loading, network call execution, analytics etc are to be provided by the hosting app via `Adapter`s

## Player
![Alt text](/images/player.png?raw=true "Player")

## Next Content Mode
![Alt text](/images/next_content.png?raw=true "Next Content")

## PIP Mode
![Alt text](/images/pip.png?raw=true "Picture in picture")

# Features 
- HLS/Dash streaming (by Kaltura playkit)
- DRM support for widevine (by Kaltura playkit)
- Bookmarking support to remember where you left off
- Quality selection/ Audio language selection/ Subtitle selection within player
- Pinch to zoom gestures
- Double tap / Triple tap to seek
- Play pause and seek vector animations
- Immersive mode video playback which means it uses all screen real estate and hide navigation/status bar
- Picture in picture mode (Android floating window)
- Watermark logo support (via playback context api)
- Ads support for preroll, midroll, postroll, (by Kaltura IMA Plugin)
- Auto-play-next popup at the end of video
- Headphone support for controlling play/pause remotely

# Upcoming features
There is no active development planned for this project


# Where to look
- `FlickStreamingFragment.kt` is to be instantiated by main app. This fragment renders the video and watermarks.
- `PlayerControlsFragment.kt` houses all player controls.
- `AdsControlsFragment.kt` for all Ads related controls
- `FlickApplicationAdapter.kt` is the adapter to be implemented by main app's application class for image loading, network calls etc
- `FlickAnalyticsAdapter.kt` is the adapter to be implemented by main app for tracking callbacks

# Authors
@anirudhramanan

@thekirankumar

# License

    The Apache License

    Copyright (c) 2020 Flipkart Internet Pvt. Ltd.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.

    You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

    See the License for the specific language governing permissions and
    limitations under the License.
