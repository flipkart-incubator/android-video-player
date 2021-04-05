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
import androidx.lifecycle.ViewModelProviders
import com.flipkart.flick.R
import com.flipkart.flick.adapter.FlickApplicationAdapterProvider
import com.flipkart.flick.core.db.viewmodel.PlayerControlViewModel
import com.flipkart.flick.helper.utils.ParentFinder
import com.flipkart.flick.listeners.OnItemSelectedListener
import com.flipkart.flick.listeners.PlaybackControlListener
import com.flipkart.flick.ui.fragments.TrackSelectionFragment.Companion.TITLE_ARG
import com.flipkart.flick.ui.fragments.TrackSelectionFragment.Companion.TRACK_ARG
import com.flipkart.flick.ui.listeners.OnTrackChangeListener
import com.flipkart.flick.ui.model.TrackModel
import kotlinx.android.synthetic.*

/**
 * Bottom sheet to select video quality
 */
class QualitySelectionFragment : androidx.fragment.app.DialogFragment(),
    OnItemSelectedListener<TrackModel> {
    // callback listeners
    private var playbackControlListener: PlaybackControlListener? = null
    private var flickApplicationAdapterProvider: FlickApplicationAdapterProvider? = null
    private var trackChangeListener: OnTrackChangeListener? = null

    override fun onItemSelected(value: TrackModel) {
        // fire track change playbackControlListener
        playbackControlListener?.playbackControlClosed()
        trackChangeListener?.onTrackChange(value.id)
        dialog?.dismiss()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.tab_layout_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val heading = context?.resources?.getText(R.string.video_quality_title)?.toString()
        parentFragment?.let {
            val viewModel = ViewModelProviders.of(it).get(PlayerControlViewModel::class.java)
            viewModel.qualityTracks?.let {
                val fragment = TrackSelectionFragment().apply {
                    val bundle = Bundle()
                    bundle.putParcelableArrayList(TRACK_ARG, it)
                    bundle.putString(TITLE_ARG, heading)
                    arguments = bundle
                }
                childFragmentManager.beginTransaction().add(R.id.view_pager_container, fragment)
                    .commitAllowingStateLoss()
            }
        }
    }

    override fun getTheme(): Int {
        return R.style.DialogStyle
    }

    override fun onDestroyView() {
        super.onDestroyView()
        playbackControlListener?.playbackControlClosed()
        clearFindViewByIdCache()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.flickApplicationAdapterProvider =
            ParentFinder(this, FlickApplicationAdapterProvider::class.java).find()
        this.playbackControlListener =
            ParentFinder(this, PlaybackControlListener::class.java).find()
        this.trackChangeListener = ParentFinder(this, OnTrackChangeListener::class.java).find()
    }

    override fun onDetach() {
        super.onDetach()
        this.flickApplicationAdapterProvider = null
        this.playbackControlListener = null
        this.trackChangeListener = null
    }
}
