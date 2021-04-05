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

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.flipkart.flick.R
import com.flipkart.flick.listeners.OnItemSelectedListener
import com.flipkart.flick.ui.adapters.TrackSelectionAdapter
import com.flipkart.flick.ui.model.TrackModel
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.bottom_sheet_view.*
import kotlinx.android.synthetic.main.recycler_view.*

/**
 * Track selection fragment (used by audio, subtitle and quality fragments)
 */
class TrackSelectionFragment : androidx.fragment.app.Fragment(),
    OnItemSelectedListener<TrackModel> {
    private var callback: OnItemSelectedListener<TrackModel>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val heading = arguments?.getString(TITLE_ARG)
        heading?.let {
            bottom_sheet_heading?.text = it
            bottom_sheet_heading?.visibility = View.VISIBLE
        } ?: run {
            bottom_sheet_heading?.visibility = View.GONE
        }

        recycler_view?.layoutManager = LinearLayoutManager(context)

        val config = arguments?.getParcelableArrayList<TrackModel>(TRACK_ARG)
        config?.let {
            val adapter = TrackSelectionAdapter(context, it, this)
            recycler_view?.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearFindViewByIdCache()
    }

    override fun onItemSelected(value: TrackModel) {
        // fire track change callback
        callback?.onItemSelected(value)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is OnItemSelectedListener<*>) {
            @Suppress("UNCHECKED_CAST")
            callback = parentFragment as OnItemSelectedListener<TrackModel>
        }
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    companion object {
        const val TRACK_ARG = "track"
        const val TITLE_ARG = "title"
    }
}
