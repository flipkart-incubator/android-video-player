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
package com.flipkart.flick.ui.listeners

interface PlayerParentInteractionListener {
    /**
     * Will be called when player has nothing more to be done.
     * For e.g when the video has ended and post roll ad has finished and has auto play next API call returns with no movie to play (or fails or is still in progress), then this gets called
     */
    fun onPlayerPresentationEnded()
}
