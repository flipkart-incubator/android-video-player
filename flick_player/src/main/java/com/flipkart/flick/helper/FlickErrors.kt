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

/**
 * Error codes
 */
enum class FlickError(val errorCode: Int, val errorTitle: String, val errorBody: String) {
    NO_CONNECTION(5001, "No Connection", "Please check your internet connection and try again"),
    PLAYBACK_CONTEXT_REQUEST_FAILED(
        5002,
        "Playback request failed",
        "There was a network error in making a playback request"
    ),
    NO_MEDIA_ENTRY_TO_PLAY(5003, "Media entry error", "There was no media entry available to play"),
    EMPTY_CONTENT_ID(5004, "Empty content", "The content you are trying to play is empty"),
    BUFFER_NETWORK_ERROR(
        5005,
        "Network error",
        "There was a network problem in downloading the stream."
    ),
    BUFFER_RENDER_ERROR(
        5006,
        "Rendering error",
        "There was a rendering problem in playing the stream."
    )
}
