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
package com.flipkart.flick.ui.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import com.flipkart.flick.R
import com.flipkart.flick.listeners.OnItemSelectedListener
import com.flipkart.flick.ui.model.TrackModel

/**
 * Used by track selection controls
 */
class TrackSelectionAdapter internal constructor(
    context: Context?,
    private val configs: List<TrackModel>,
    private val onItemSelectedListener: OnItemSelectedListener<TrackModel>,
    private val defaultTextColor: Int = context?.resources?.getColor(R.color.white_transparent_color)
        ?: Color.WHITE,
    private val selectedTextColor: Int = Color.WHITE
) : AbstractBottomSheetAdapter<TrackModel>(configs) {
    override fun onBindViewHolder(viewHolder: BottomSheetItemViewHolder, i: Int) {
        val config = configs[i]
        viewHolder.title.text = config.title
        if (config.selected) {
            viewHolder.selectedImage.visibility = View.VISIBLE
            viewHolder.selectedImage.isSelected = true
            viewHolder.title.setTextColor(selectedTextColor)
        } else {
            viewHolder.selectedImage.visibility = View.INVISIBLE
            viewHolder.title.setTextColor(defaultTextColor)
            viewHolder.title.setTypeface(null, Typeface.NORMAL)
        }
        viewHolder.parentView.setOnClickListener {
            onItemSelectedListener.onItemSelected(config)
        }
    }
}
