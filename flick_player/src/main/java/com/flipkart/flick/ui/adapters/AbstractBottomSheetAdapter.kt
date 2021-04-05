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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.flipkart.flick.R

abstract class AbstractBottomSheetAdapter<T> internal constructor(val items: List<T>?) :
    RecyclerView.Adapter<AbstractBottomSheetAdapter.BottomSheetItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, i: Int): BottomSheetItemViewHolder {
        return BottomSheetItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.bottom_sheet_recycler_item,
                parent,
                false
            )
        )
    }

    abstract override fun onBindViewHolder(viewHolder: BottomSheetItemViewHolder, i: Int)

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    class BottomSheetItemViewHolder(card: View) : RecyclerView.ViewHolder(card) {
        var selectedImage: RadioButton = card.findViewById(R.id.selected_icon)
        var title: TextView = card.findViewById(R.id.title)
        var parentView: LinearLayout = card.findViewById(R.id.parentView)
    }
}
