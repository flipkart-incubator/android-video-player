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

import androidx.annotation.StringDef
import androidx.fragment.app.Fragment
import com.flipkart.flick.ui.fragments.FlickFragmentFactory.Options.Companion.NEXT_VIDEO_THUMBNAIL
import com.flipkart.flick.ui.fragments.FlickFragmentFactory.Options.Companion.TEXT_AUDIO_ICON
import com.flipkart.flick.ui.fragments.FlickFragmentFactory.Options.Companion.VIDEO_QUALITY_ICON

/**
 * Fragment factory to create fragments for different controls
 */
object FlickFragmentFactory {
    fun createFragment(@Options.PlaybackOptions option: String): Fragment {
        when (option) {
            TEXT_AUDIO_ICON -> {
                return AudioSubTitleSelectionFragment()
            }
            VIDEO_QUALITY_ICON -> {
                return QualitySelectionFragment()
            }
            NEXT_VIDEO_THUMBNAIL -> {
                return AutoPlayNextFragment()
            }
        }
        throw IllegalArgumentException("$option not supported")
    }

    class Options {
        @Retention(AnnotationRetention.RUNTIME)
        @StringDef(TEXT_AUDIO_ICON, VIDEO_QUALITY_ICON)
        annotation class PlaybackOptions

        companion object {
            const val TEXT_AUDIO_ICON = "TEXT_AUDIO_ICON" //NON-NLS
            const val VIDEO_QUALITY_ICON = "VIDEO_QUALITY_ICON" //NON-NLS
            const val NEXT_VIDEO_THUMBNAIL = "NEXT_VIDEO_THUMBNAIL" //NON-NLS
        }
    }
}
