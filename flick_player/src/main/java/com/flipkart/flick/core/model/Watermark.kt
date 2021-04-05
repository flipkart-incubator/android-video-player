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
package com.flipkart.flick.core.model

import androidx.annotation.StringDef

class Watermark(var url: String) {
    var width = 0
    var height = 0

    @WatermarkPositionDef
    var position: String? = null

    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @StringDef(
        WatermarkPosition.TOP_RIGHT,
        WatermarkPosition.BOTTOM_RIGHT,
        WatermarkPosition.TOP_LEFT,
        WatermarkPosition.BOTTOM_LEFT
    )
    annotation class WatermarkPositionDef
    object WatermarkPosition {
        const val TOP_RIGHT = "TOP_RIGHT"
        const val BOTTOM_RIGHT = "BOTTOM_RIGHT"
        const val TOP_LEFT = "TOP_LEFT"
        const val BOTTOM_LEFT = "BOTTOM_LEFT"
    }
}
