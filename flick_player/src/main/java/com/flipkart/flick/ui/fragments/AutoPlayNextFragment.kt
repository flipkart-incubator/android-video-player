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

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.lifecycle.ViewModelProviders
import com.flipkart.flick.R
import com.flipkart.flick.adapter.FlickAnalyticsAdapterProvider
import com.flipkart.flick.adapter.FlickApplicationAdapterProvider
import com.flipkart.flick.core.db.viewmodel.PlayerControlViewModel
import com.flipkart.flick.helper.utils.ParentFinder
import com.flipkart.flick.ui.listeners.OnContentChangeListener
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_auto_play_next.*

class AutoPlayNextFragment : androidx.fragment.app.Fragment(), View.OnClickListener {
    private var flickApplicationAdapterProvider: FlickApplicationAdapterProvider? = null
    private var flickAnalyticsAdapterProvider: FlickAnalyticsAdapterProvider? = null
    private var viewModel: PlayerControlViewModel? = null
    private var contentChangeListener: OnContentChangeListener? = null
    private var progressAnimator: ObjectAnimator? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_auto_play_next, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        thumbnail?.setOnClickListener(this)
        thumbnail?.setImageBitmap(null)
        this.viewModel = parentFragment?.let {
            ViewModelProviders.of(it).get(PlayerControlViewModel::class.java)
        }
        viewModel?.nextAsset?.value?.nextContent.let {
            it?.image?.let { it1 ->
                flickApplicationAdapterProvider?.flickApplicationAdapter?.loadImage(
                    this, thumbnail,
                    it1, context?.resources?.getColor(R.color.grey_color) ?: 0, true
                )
            }
            it?.title?.let { text ->
                title?.setText(text)
            }
            startAnimation()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        clearFindViewByIdCache()
        progressAnimator?.cancel()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.thumbnail -> {
                gotoNextVideo()
            }
        }
    }

    private fun gotoNextVideo() {
        viewModel?.nextAsset?.value?.nextContent?.let {
            if (!TextUtils.isEmpty(it.contentId)) {
                /** content id should not be null **/
                flickAnalyticsAdapterProvider?.flickAnalyticsAdapter?.onAutoPlayNext(it)
                contentChangeListener?.onContentChange(it)
            }
        }
    }

    private fun startAnimation() {
        progressAnimator = ObjectAnimator.ofInt(progress_bar, "progress", 0, 1000)
        progressAnimator?.duration = 10000
        progressAnimator?.interpolator = LinearInterpolator()
        progressAnimator?.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                gotoNextVideo()
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }
        })
        progressAnimator?.start()
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            progressAnimator?.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            progressAnimator?.resume()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.contentChangeListener = ParentFinder(this, OnContentChangeListener::class.java).find()
        this.flickApplicationAdapterProvider =
            ParentFinder(this, FlickApplicationAdapterProvider::class.java).find()
        this.flickAnalyticsAdapterProvider =
            ParentFinder(this, FlickAnalyticsAdapterProvider::class.java).find()
    }

    override fun onDetach() {
        super.onDetach()
        this.contentChangeListener = null
        this.flickApplicationAdapterProvider = null
        this.flickAnalyticsAdapterProvider = null
    }
}
