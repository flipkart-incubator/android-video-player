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
package com.flipkart.flick.listeners

import com.flipkart.flick.ui.fragments.FlickFragmentFactory

interface PlayerControlViewListener : AutoHideable {

    /**
     * Shows the control on the player
     */
    fun showPlayerControl()

    /**
     * when one of the options is clicked
     *
     * @param option type
     */
    fun onPlaybackOptionClick(@FlickFragmentFactory.Options.PlaybackOptions option: String)

    /**
     * Reset the auto hide control
     */
    fun resetAutoHidePlayerControl()
}
