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
package com.flipkart.flick.ui.helper

import android.app.Activity
import android.view.Gravity
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.flipkart.flick.adapter.FlickApplicationAdapterProvider
import com.flipkart.flick.core.components.PlaybackContextModel
import com.flipkart.flick.core.model.Watermark
import com.flipkart.flick.helper.utils.px

/**
 * A helper class to extract water marks from [playbackContextModel] and setting it on the [container]
 * The images present in the [playbackContextModel]'s [Watermark] will be immediately loaded.
 * To remove watermarks, call [setWatermarks] with an empty list
 */
class WatermarkHelper(
    val fragment: Fragment,
    val activity: Activity?,
    val container: FrameLayout?,
    private val applicationAdapter: FlickApplicationAdapterProvider?,
    private val playbackContextModel: PlaybackContextModel?
) {
    init {
        playbackContextModel?.playbackContext?.watermarks?.let { setWatermarks(it) }
    }

    private fun setWatermarks(watermarkModel: List<Watermark>) {
        container?.removeAllViews()
        watermarkModel.forEach {
            if (URLUtil.isValidUrl(it.url)) {
                val imageView = ImageView(activity)
                imageView.adjustViewBounds = true
                var width = 40.px
                it.width.px.let { w ->
                    if (w > 0) {
                        width = w
                    }
                }
                val layoutParams = FrameLayout.LayoutParams(
                    width,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    WatermarkUtils.enumToGravity(it.position)
                )
                layoutParams.leftMargin = 20.px
                layoutParams.rightMargin = 20.px
                layoutParams.topMargin = 20.px
                layoutParams.bottomMargin = 20.px
                imageView.layoutParams =
                    layoutParams
                container?.addView(imageView)
                applicationAdapter?.flickApplicationAdapter?.loadImage(
                    fragment,
                    imageView,
                    it.url,
                    0,
                    true
                )
            }
        }
    }
}

class WatermarkUtils {
    companion object {
        fun enumToGravity(position: String?): Int {
            return when (position) {
                Watermark.WatermarkPosition.TOP_RIGHT -> Gravity.TOP or Gravity.END
                Watermark.WatermarkPosition.TOP_LEFT -> Gravity.TOP or Gravity.START
                Watermark.WatermarkPosition.BOTTOM_LEFT -> Gravity.BOTTOM or Gravity.START
                Watermark.WatermarkPosition.BOTTOM_RIGHT -> Gravity.BOTTOM or Gravity.END
                else -> Gravity.TOP or Gravity.END
            }
        }
    }
}
