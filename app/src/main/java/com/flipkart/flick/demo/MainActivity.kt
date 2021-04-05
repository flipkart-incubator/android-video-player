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
package com.flipkart.flick.demo

import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.flipkart.flick.FlickStreamingFragment
import com.flipkart.flick.adapter.FlickAnalyticsAdapter
import com.flipkart.flick.adapter.FlickAnalyticsAdapterProvider
import com.flipkart.flick.core.model.ContentData
import com.flipkart.flick.ui.listeners.PlayerParentInteractionListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), FlickAnalyticsAdapterProvider,
    PlayerParentInteractionListener {
    private val PLAYER_TAG: String = "Player"
    private var playerOpen: Boolean = false
    private lateinit var mockedNetworkCheckbox: CheckBox
    private lateinit var mockedAdsCheckbox: CheckBox
    private lateinit var prefs: SharedPreferences

    override val flickAnalyticsAdapter: FlickAnalyticsAdapter
        get() = DemoAnalyticsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        val openPlayerButton = findViewById<Button>(R.id.button)
        openPlayerButton.setOnClickListener { openPlayer(openPlayerButton) }

        val mockedNetworkCheckbox = findViewById<CheckBox>(R.id.mockeNetworkCheckbox)
        val oldMockedNetworkEnabled = prefs.getBoolean("mockedNetwork", false)
        oldMockedNetworkEnabled.let { mockedNetworkCheckbox.isChecked = oldMockedNetworkEnabled }
        this.mockedNetworkCheckbox = mockedNetworkCheckbox

        val mockedAdsCheckbox = findViewById<CheckBox>(R.id.adsCheckbox)
        val oldAdsEnabled = prefs.getBoolean("adsEnabled", false)
        oldAdsEnabled.let { mockedAdsCheckbox.isChecked = oldAdsEnabled }
        this.mockedAdsCheckbox = mockedAdsCheckbox
    }

    fun openPlayer(view: View) {
        val fragment = FlickStreamingFragment()
        val bundle = Bundle()

        prefs.edit().putBoolean("adsEnabled", mockedAdsCheckbox.isChecked).apply()

        val response = ContentData("test")
        response.mediaEndThreshold = 0.95
        response.mediaStartThreshold = 0.1
        response.bookmarkFrequency = 30000
        response.title = "Test title"
        response.image = "https://i.ibb.co/M9pB6h0/Screenshot-1617013404.png"

        bundle.putSerializable(FlickStreamingFragment.CONTENT_CONFIG_ARG, response)
        fragment.arguments = bundle

        container.fitsSystemWindows = false
        container.setPadding(0, 0, 0, 0) //hack : because framelayout doesnt reset its padding
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment, PLAYER_TAG)
            .setPrimaryNavigationFragment(fragment).addToBackStack(PLAYER_TAG).commit()
        playerOpen = true
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            closePlayer()
        }
    }

    private fun closePlayer() {
        val fragment = supportFragmentManager.findFragmentByTag(PLAYER_TAG)
        if (fragment != null) {
            supportFragmentManager.beginTransaction().remove(fragment).commitNowAllowingStateLoss()
        }
        playerOpen = false
        container.fitsSystemWindows = true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d("MainActivity", "onConfigurationChanged() called with: newConfig = [$newConfig]")
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (playerOpen) {
            val flickStreamingFragment =
                supportFragmentManager.findFragmentByTag(PLAYER_TAG) as? FlickStreamingFragment
            flickStreamingFragment.let {
                if (it != null) {
                    FlickStreamingFragment.enterPIPModeIfRequired(it, this)
                }
            }
        }
    }

    override fun onPlayerPresentationEnded() {
        Log.d("MainActivity", "onPlayerPresentationEnded() called")
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        } else {
            finish()
        }
    }
}
