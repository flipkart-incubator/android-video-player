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
import com.flipkart.flick.ui.fragments.TrackSelectionFragment.Companion.TRACK_ARG
import com.flipkart.flick.ui.listeners.OnTrackChangeListener
import com.flipkart.flick.ui.model.TrackModel
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.tab_layout_fragment.*

/**
 * Bottom Sheet to select audio and subtitle tracks
 */
class AudioSubTitleSelectionFragment : androidx.fragment.app.DialogFragment(),
    OnItemSelectedListener<TrackModel> {
    // callbacks
    private var playbackControlListener: PlaybackControlListener? = null
    private var trackChangeListener: OnTrackChangeListener? = null
    private var flickApplicationAdapterProvider: FlickApplicationAdapterProvider? = null

    override fun onItemSelected(value: TrackModel) {
        playbackControlListener?.playbackControlClosed()
        // fire track change callback
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

    override fun getTheme(): Int {
        return R.style.DialogStyle
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tab_layout?.visibility = View.VISIBLE
        var subTitleTracks: ArrayList<TrackModel>? = null
        var audioTracks: ArrayList<TrackModel>? = null

        parentFragment?.let {
            val viewModel = ViewModelProviders.of(it).get(PlayerControlViewModel::class.java)
            subTitleTracks = viewModel.subTitleTracks
            audioTracks = viewModel.audioTracks
        }

        subTitleTracks?.let { tracks ->
            if (tracks.isNotEmpty()) {
                // subtitle tracks are present, add subtitle tab and fragment
                val subTitleTab = tab_layout.newTab()
                tab_layout?.addTab(subTitleTab)
                subTitleTab.setContentDescription(R.string.subtitle_text)
                subTitleTab.setText(R.string.subtitle_text)
                attachSubTitleFragment(tracks)
            }
        }

        audioTracks?.let { tracks ->
            if (tracks.isNotEmpty()) {
                // audio tracks are present, add audio tab
                val audioTab = tab_layout.newTab()
                tab_layout?.addTab(audioTab)
                audioTab.setContentDescription(R.string.audio_title)
                audioTab.setText(R.string.audio_title)

                // if subtitle tracks were empty, add audio fragment
                // not the best way, will refactor in next iter
                if (subTitleTracks == null || subTitleTracks?.isEmpty() == true) {
                    // if text tracks are empty, add audio fragment
                    // this should be a view pager, move in next iter
                    attachAudioFragment(tracks)
                }
            }
        }

        tab_layout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.text) {
                    context?.resources?.getString(R.string.subtitle_text) -> attachSubTitleFragment(
                        subTitleTracks
                    )
                    context?.resources?.getString(R.string.audio_title) -> attachAudioFragment(
                        audioTracks
                    )
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // no-op
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // no-op
            }
        })
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

    private fun attachSubTitleFragment(tracks: ArrayList<TrackModel>?) {
        tracks?.let {
            val fragment = TrackSelectionFragment().apply {
                val bundle = Bundle()
                bundle.putParcelableArrayList(TRACK_ARG, it)
                arguments = bundle
            }
            childFragmentManager.beginTransaction().add(R.id.view_pager_container, fragment)
                .commitAllowingStateLoss()
        }
    }

    private fun attachAudioFragment(tracks: ArrayList<TrackModel>?) {
        tracks?.let {
            val fragment = TrackSelectionFragment().apply {
                val bundle = Bundle()
                bundle.putParcelableArrayList(TRACK_ARG, it)
                arguments = bundle
            }
            childFragmentManager.beginTransaction().add(R.id.view_pager_container, fragment)
                .commitAllowingStateLoss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        playbackControlListener?.playbackControlClosed()
        clearFindViewByIdCache()
    }
}
