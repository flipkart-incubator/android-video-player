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

import android.os.CancellationSignal
import android.os.Handler
import android.util.Log
import com.flipkart.flick.core.interfaces.AdsNetworkLayer
import com.flipkart.flick.core.interfaces.BookmarkNetworkLayer
import com.flipkart.flick.core.interfaces.NetworkResultListener
import com.flipkart.flick.core.interfaces.PlaybackNetworkLayer
import com.flipkart.flick.core.model.*
import com.flipkart.flick.helper.plugins.BookmarkPlugin
import java.util.*
import kotlin.collections.ArrayList

/**
 * A network layer which returns hardcoded responses
 */
class MockedNetworkLayer : AdsNetworkLayer,
    BookmarkNetworkLayer, PlaybackNetworkLayer {
    private val handler = Handler()

    override fun getNextAsset(
        request: ContentRequest,
        resultListener: NetworkResultListener<ContentData>,
        cancellationSignal: CancellationSignal
    ) {
        when (request.contentId) {
            "auto_play_next_error" -> {
                handler.postDelayed({ resultListener.onFailure(500, "Request failed") }, 2000)
            }
            "next_content_id" -> {
                val nextResponse = ContentData("next_content_id_2")
                nextResponse.title = "Next Content 1"
                nextResponse.image = "https://i.ibb.co/M9pB6h0/Screenshot-1617013404.png"
                handler.postDelayed({ resultListener.onSuccess(200, nextResponse) }, 2000)
            }
            else -> {
                val nextResponse = ContentData("next_content_id")
                nextResponse.title = "Next Content 2"
                nextResponse.image = "https://i.ibb.co/M9pB6h0/Screenshot-1617013404.png"
                handler.postDelayed({ resultListener.onSuccess(200, nextResponse) }, 2000)
            }
        }
    }

    override fun getAdsVMAP(
        contentId: String,
        resultListener: NetworkResultListener<AdsData>,
        cancellationSignal: CancellationSignal
    ) {
        val adsVmapResponse = AdsData()
        adsVmapResponse.vmap = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<vmap:VMAP xmlns:vmap=\"http://www.iab.net/videosuite/vmap\" version=\"1.0\">\n" +
                " <vmap:AdBreak timeOffset=\"start\" breakType=\"linear\" breakId=\"preroll\">\n" +
                "  <vmap:AdSource id=\"preroll-ad-1\" allowMultipleAds=\"false\" followRedirects=\"true\">\n" +
                "         <vmap:VASTAdData>\n" +
                "<VAST xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"vast.xsd\" version=\"3.0\">\n" +
                " <Ad id=\"709684336\">\n" +
                "  <InLine>\n" +
                "   <AdSystem>GDFP</AdSystem>\n" +
                "   <AdTitle>External NCA1C1L1 Preroll</AdTitle>\n" +
                "   <Description><![CDATA[External NCA1C1L1 Preroll ad]]></Description>\n" +
                "   <Error><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=BQd21UJ0kXZSyKs6_ogPOzZCoB9iuj-sGAAAAEAEgqN27JjgAWICYpMbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIjLzEyNDMxOTA5Ni9leHRlcm5hbC9hZF9ydWxlX3NhbXBsZXP4AoTSHoADAZADmgiYA6wCqAMB4AQB0gUGEPDYs9ICkAYBoAYj2AcB4AcL0ggHCIBhEAEYDQ&sigh=bP5CGssbMRw&label=videoplayfailed[ERRORCODE]]]></Error>\n" +
                "   <Impression><![CDATA[https://securepubads.g.doubleclick.net/pcs/view?xai=AKAOjsv1Rp256RCLSzGIsAFLPjFuYj6OxvO7tckFTbWKj1Wr2-drh3xhNa50KDFJjzm1cU_dFt4ym6zM66IhDOoeD79B28h1MZRCpABFzHJS60SkEFDRzyKU6q6P9LefMVSu0Fj7oziZkGeAVl3z5_8ozOoIRZp6VdGMIUjXS2NYxEuNvKgqV3eZglKYBI_yIwbYfksEZwnKtpIIIUHqzih8WnFkxxWRVA5DYhoYlLRt66ISn-gXZIu9Xf37RjvRfjtSfbntFYImxHzFsKmtTYvxa9lMoA&sig=Cg0ArKJSzChekHhmInKIEAE&adurl=]]></Impression>\n" +
                "   <Creatives>\n" +
                "    <Creative id=\"57861016576\" sequence=\"1\">\n" +
                "     <Linear>\n" +
                "      <Duration>00:00:10</Duration>\n" +
                "      <TrackingEvents>\n" +
                "       <Tracking event=\"start\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=BQd21UJ0kXZSyKs6_ogPOzZCoB9iuj-sGAAAAEAEgqN27JjgAWICYpMbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIjLzEyNDMxOTA5Ni9leHRlcm5hbC9hZF9ydWxlX3NhbXBsZXP4AoTSHoADAZADmgiYA6wCqAMB4AQB0gUGEPDYs9ICkAYBoAYj2AcB4AcL0ggHCIBhEAEYDQ&sigh=bP5CGssbMRw&label=part2viewed&ad_mt=[AD_MT]]]></Tracking>\n" +
                "       <Tracking event=\"firstQuartile\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=BQd21UJ0kXZSyKs6_ogPOzZCoB9iuj-sGAAAAEAEgqN27JjgAWICYpMbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIjLzEyNDMxOTA5Ni9leHRlcm5hbC9hZF9ydWxlX3NhbXBsZXP4AoTSHoADAZADmgiYA6wCqAMB4AQB0gUGEPDYs9ICkAYBoAYj2AcB4AcL0ggHCIBhEAEYDQ&sigh=bP5CGssbMRw&label=videoplaytime25&ad_mt=[AD_MT]]]></Tracking>\n" +
                "       <Tracking event=\"midpoint\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=BQd21UJ0kXZSyKs6_ogPOzZCoB9iuj-sGAAAAEAEgqN27JjgAWICYpMbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIjLzEyNDMxOTA5Ni9leHRlcm5hbC9hZF9ydWxlX3NhbXBsZXP4AoTSHoADAZADmgiYA6wCqAMB4AQB0gUGEPDYs9ICkAYBoAYj2AcB4AcL0ggHCIBhEAEYDQ&sigh=bP5CGssbMRw&label=videoplaytime50&ad_mt=[AD_MT]]]></Tracking>\n" +
                "       <Tracking event=\"thirdQuartile\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=BQd21UJ0kXZSyKs6_ogPOzZCoB9iuj-sGAAAAEAEgqN27JjgAWICYpMbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIjLzEyNDMxOTA5Ni9leHRlcm5hbC9hZF9ydWxlX3NhbXBsZXP4AoTSHoADAZADmgiYA6wCqAMB4AQB0gUGEPDYs9ICkAYBoAYj2AcB4AcL0ggHCIBhEAEYDQ&sigh=bP5CGssbMRw&label=videoplaytime75&ad_mt=[AD_MT]]]></Tracking>\n" +
                "       <Tracking event=\"complete\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=BQd21UJ0kXZSyKs6_ogPOzZCoB9iuj-sGAAAAEAEgqN27JjgAWICYpMbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIjLzEyNDMxOTA5Ni9leHRlcm5hbC9hZF9ydWxlX3NhbXBsZXP4AoTSHoADAZADmgiYA6wCqAMB4AQB0gUGEPDYs9ICkAYBoAYj2AcB4AcL0ggHCIBhEAEYDQ&sigh=bP5CGssbMRw&label=videoplaytime100&ad_mt=[AD_MT]]]></Tracking>\n" +
                "       <Tracking event=\"mute\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=BQd21UJ0kXZSyKs6_ogPOzZCoB9iuj-sGAAAAEAEgqN27JjgAWICYpMbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIjLzEyNDMxOTA5Ni9leHRlcm5hbC9hZF9ydWxlX3NhbXBsZXP4AoTSHoADAZADmgiYA6wCqAMB4AQB0gUGEPDYs9ICkAYBoAYj2AcB4AcL0ggHCIBhEAEYDQ&sigh=bP5CGssbMRw&label=admute&ad_mt=[AD_MT]]]></Tracking>\n" +
                "       <Tracking event=\"unmute\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=BQd21UJ0kXZSyKs6_ogPOzZCoB9iuj-sGAAAAEAEgqN27JjgAWICYpMbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIjLzEyNDMxOTA5Ni9leHRlcm5hbC9hZF9ydWxlX3NhbXBsZXP4AoTSHoADAZADmgiYA6wCqAMB4AQB0gUGEPDYs9ICkAYBoAYj2AcB4AcL0ggHCIBhEAEYDQ&sigh=bP5CGssbMRw&label=adunmute&ad_mt=[AD_MT]]]></Tracking>\n" +
                "       <Tracking event=\"rewind\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=BQd21UJ0kXZSyKs6_ogPOzZCoB9iuj-sGAAAAEAEgqN27JjgAWICYpMbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIjLzEyNDMxOTA5Ni9leHRlcm5hbC9hZF9ydWxlX3NhbXBsZXP4AoTSHoADAZADmgiYA6wCqAMB4AQB0gUGEPDYs9ICkAYBoAYj2AcB4AcL0ggHCIBhEAEYDQ&sigh=bP5CGssbMRw&label=adrewind&ad_mt=[AD_MT]]]></Tracking>\n" +
                "       <Tracking event=\"pause\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=BQd21UJ0kXZSyKs6_ogPOzZCoB9iuj-sGAAAAEAEgqN27JjgAWICYpMbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIjLzEyNDMxOTA5Ni9leHRlcm5hbC9hZF9ydWxlX3NhbXBsZXP4AoTSHoADAZADmgiYA6wCqAMB4AQB0gUGEPDYs9ICkAYBoAYj2AcB4AcL0ggHCIBhEAEYDQ&sigh=bP5CGssbMRw&label=adpause&ad_mt=[AD_MT]]]></Tracking>\n" +
                "       <Tracking event=\"resume\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=BQd21UJ0kXZSyKs6_ogPOzZCoB9iuj-sGAAAAEAEgqN27JjgAWICYpMbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIjLzEyNDMxOTA5Ni9leHRlcm5hbC9hZF9ydWxlX3NhbXBsZXP4AoTSHoADAZADmgiYA6wCqAMB4AQB0gUGEPDYs9ICkAYBoAYj2AcB4AcL0ggHCIBhEAEYDQ&sigh=bP5CGssbMRw&label=adresume&ad_mt=[AD_MT]]]></Tracking>\n" +
                "       <Tracking event=\"fullscreen\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=BQd21UJ0kXZSyKs6_ogPOzZCoB9iuj-sGAAAAEAEgqN27JjgAWICYpMbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIjLzEyNDMxOTA5Ni9leHRlcm5hbC9hZF9ydWxlX3NhbXBsZXP4AoTSHoADAZADmgiYA6wCqAMB4AQB0gUGEPDYs9ICkAYBoAYj2AcB4AcL0ggHCIBhEAEYDQ&sigh=bP5CGssbMRw&label=adfullscreen&ad_mt=[AD_MT]]]></Tracking>\n" +
                "       <Tracking event=\"creativeView\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=BQd21UJ0kXZSyKs6_ogPOzZCoB9iuj-sGAAAAEAEgqN27JjgAWICYpMbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIjLzEyNDMxOTA5Ni9leHRlcm5hbC9hZF9ydWxlX3NhbXBsZXP4AoTSHoADAZADmgiYA6wCqAMB4AQB0gUGEPDYs9ICkAYBoAYj2AcB4AcL0ggHCIBhEAEYDQ&sigh=bP5CGssbMRw&label=vast_creativeview&ad_mt=[AD_MT]]]></Tracking>\n" +
                "       <Tracking event=\"exitFullscreen\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=BQd21UJ0kXZSyKs6_ogPOzZCoB9iuj-sGAAAAEAEgqN27JjgAWICYpMbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIjLzEyNDMxOTA5Ni9leHRlcm5hbC9hZF9ydWxlX3NhbXBsZXP4AoTSHoADAZADmgiYA6wCqAMB4AQB0gUGEPDYs9ICkAYBoAYj2AcB4AcL0ggHCIBhEAEYDQ&sigh=bP5CGssbMRw&label=vast_exit_fullscreen&ad_mt=[AD_MT]]]></Tracking>\n" +
                "       <Tracking event=\"acceptInvitationLinear\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=BQd21UJ0kXZSyKs6_ogPOzZCoB9iuj-sGAAAAEAEgqN27JjgAWICYpMbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIjLzEyNDMxOTA5Ni9leHRlcm5hbC9hZF9ydWxlX3NhbXBsZXP4AoTSHoADAZADmgiYA6wCqAMB4AQB0gUGEPDYs9ICkAYBoAYj2AcB4AcL0ggHCIBhEAEYDQ&sigh=bP5CGssbMRw&label=acceptinvitation&ad_mt=[AD_MT]]]></Tracking>\n" +
                "       <Tracking event=\"closeLinear\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=BQd21UJ0kXZSyKs6_ogPOzZCoB9iuj-sGAAAAEAEgqN27JjgAWICYpMbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIjLzEyNDMxOTA5Ni9leHRlcm5hbC9hZF9ydWxlX3NhbXBsZXP4AoTSHoADAZADmgiYA6wCqAMB4AQB0gUGEPDYs9ICkAYBoAYj2AcB4AcL0ggHCIBhEAEYDQ&sigh=bP5CGssbMRw&label=adclose&ad_mt=[AD_MT]]]></Tracking>\n" +
                "      </TrackingEvents>\n" +
                "      <VideoClicks>\n" +
                "       <ClickThrough id=\"GDFP\"><![CDATA[https://pubads.g.doubleclick.net/pcs/click?xai=AKAOjsui5JgAZr6VApt6TnLPxTy6_zw6O6GgWdekmFfKn3D0Guk81-x7zETB-7MBKC8QrhSnUFufWP8zvR5brGmSyg0QOxVILJT-yAt5C_YDs_WqFDsPG5GVzNDqY5r9UXLD8-ibb33IwSqQb5ahYQ6WIHdK2VapF6JG0-plfwgMxpE3RlGlAK5w9CWu9UIIaOeKjhkSVuv0FKTqZcEp4KyPASmXmmbZR1NMfGG6v1cr5Q28_WQejSeTVbwDbLE-dks1h4vqmHcEfXrGhvUw4uzoLg&sig=Cg0ArKJSzApum_DkL3Xq&adurl=https://developers.google.com/interactive-media-ads/docs/vastinspector_dual]]></ClickThrough>\n" +
                "      </VideoClicks>\n" +
                "      <MediaFiles>\n" +
                "       <MediaFile id=\"GDFP\" delivery=\"progressive\" width=\"1280\" height=\"720\" type=\"video/mp4\" bitrate=\"400\" scalable=\"true\" maintainAspectRatio=\"true\"><![CDATA[https://www.thekirankumar.com/blog/vast/preroll.mp4]]></MediaFile>\n" +
                "      </MediaFiles>\n" +
                "     </Linear>\n" +
                "    </Creative>\n" +
                "    <Creative id=\"57857370976\" sequence=\"1\">\n" +
                "     <CompanionAds>\n" +
                "      <Companion id=\"57857370976\" width=\"300\" height=\"250\">\n" +
                "       <StaticResource creativeType=\"image/png\"><![CDATA[https://pagead2.googlesyndication.com/pagead/imgad?id=CICAgKDTwILFiwEQrAIY-gEyCAAnmA4d6uc2]]></StaticResource>\n" +
                "       <TrackingEvents>\n" +
                "        <Tracking event=\"creativeView\"><![CDATA[https://securepubads.g.doubleclick.net/pcs/view?xai=AKAOjsvJ3mbsI1j8D-s-CSKGmPjK_w3tPf_uFlGcHg_mdUjvnRUC_dqV7shEzoSaWHaHbxDtcvpoC9pSIY6uJhvhdyjbbBBfVq0LC6AhgyPG2vjsn5YSCkkP-7D8l4_zfIvPOiBPWzVY10l7CYi2E0xrwrFGqoMxP1laMnm38-NQjfTvYt6BugdavFEbQs-qNc8OPyxGE-eDchHAs1o82jRBkcbAwIhLihhpAM1QhrOBlwFHeU0dAfbYntW-V0ljbhhveYSKLD2zgR1TBWidKYsaOPVoJw&sig=Cg0ArKJSzA9vGFQsRxjfEAE&adurl=]]></Tracking>\n" +
                "       </TrackingEvents>\n" +
                "       <CompanionClickThrough><![CDATA[https://pubads.g.doubleclick.net/pcs/click?xai=AKAOjstCZpbMgsOrxuW1h-nMqYvSOVlxr42Q1VmYgRTR_XgTkLb2fLf7hJJc0cdAuZH19ACdeixUuRILeMY7qK83P3vzmZc6mfyBpgolI7r7dNtN1VwG5ozX8mE3Uy-9eeIdUqEuPaZiX83hUMZGJfuK5xvZTf1dNT6lOWxZbCXNN_NnEPxHgcdlxsLa2UAGlCe7WwgcLyozx1mY7xwOXb-nS6DeLMBKmG670I2FSj_6co9MMJkci4jutXDz-4uALVGQdARERnvxn980y1z2NhG6Kg&sig=Cg0ArKJSzKeImgFSZEyj&adurl=https://developers.google.com/interactive-media-ads/docs/vastinspector_dual]]></CompanionClickThrough>\n" +
                "      </Companion>\n" +
                "     </CompanionAds>\n" +
                "    </Creative>\n" +
                "   </Creatives>\n" +
                "   <Extensions><Extension type=\"waterfall\" fallback_index=\"0\"/><Extension type=\"geo\"><Country>IN</Country><Bandwidth>4</Bandwidth><BandwidthKbps>15960</BandwidthKbps></Extension><Extension type=\"activeview\"><CustomTracking><Tracking event=\"viewable_impression\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=BQd21UJ0kXZSyKs6_ogPOzZCoB9iuj-sGAAAAEAEgqN27JjgAWICYpMbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIjLzEyNDMxOTA5Ni9leHRlcm5hbC9hZF9ydWxlX3NhbXBsZXP4AoTSHoADAZADmgiYA6wCqAMB4AQB0gUGEPDYs9ICkAYBoAYj2AcB4AcL0ggHCIBhEAEYDQ&sigh=bP5CGssbMRw&label=viewable_impression&acvw=[VIEWABILITY]&gv=[GOOGLE_VIEWABILITY]&ad_mt=[AD_MT]]]></Tracking><Tracking event=\"abandon\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=BQd21UJ0kXZSyKs6_ogPOzZCoB9iuj-sGAAAAEAEgqN27JjgAWICYpMbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIjLzEyNDMxOTA5Ni9leHRlcm5hbC9hZF9ydWxlX3NhbXBsZXP4AoTSHoADAZADmgiYA6wCqAMB4AQB0gUGEPDYs9ICkAYBoAYj2AcB4AcL0ggHCIBhEAEYDQ&sigh=bP5CGssbMRw&label=video_abandon&acvw=[VIEWABILITY]&gv=[GOOGLE_VIEWABILITY]]]></Tracking></CustomTracking><ActiveViewMetadata><![CDATA[cm=1&roio=1&la=1&alp=xai&alh=4001057363&]]></ActiveViewMetadata></Extension><Extension type=\"metrics\"><FeEventId>UJ0kXc-cKZCqogOh2Lf4CA</FeEventId><AdEventId>CJSawov_p-MCFc6faAodziYEdQ</AdEventId></Extension><Extension type=\"ShowAdTracking\"><CustomTracking><Tracking event=\"show_ad\"><![CDATA[https://securepubads.g.doubleclick.net/pcs/view?xai=AKAOjssXTcMVFDJXW065C1LNhaeSSzCh4_7WU3FxY1m3JRyLBdVtXxvOhDJx6N1OGIjVoOd8eUa1Ra6gmRO4kLaf-ySlk7bLXTTSwZ9gEQIjp-b9_96-0o6LGml0uiBuanr-LlbMPb-r1HM11jBlR8hQl3jh6xYCWgsHtCKiVP7aS3G7RvjJGM0UBM1sHftfaF_t4R2Ivs_kZ0UkhDCkJRiUCimj8goQdS6aDM3_jfb7w3R-vEHBCHg3NcdcZwY47Zpyq80TYkqZyYvHtuOosPJPaV_QwBVH&sig=Cg0ArKJSzHmNzLmJe9JIEAE&adurl=]]></Tracking></CustomTracking></Extension><Extension type=\"video_ad_loaded\"><CustomTracking><Tracking event=\"loaded\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=BQd21UJ0kXZSyKs6_ogPOzZCoB9iuj-sGAAAAEAEgqN27JjgAWICYpMbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIjLzEyNDMxOTA5Ni9leHRlcm5hbC9hZF9ydWxlX3NhbXBsZXP4AoTSHoADAZADmgiYA6wCqAMB4AQB0gUGEPDYs9ICkAYBoAYj2AcB4AcL0ggHCIBhEAEYDQ&sigh=bP5CGssbMRw&label=video_ad_loaded]]></Tracking></CustomTracking></Extension></Extensions>\n" +
                "  </InLine>\n" +
                " </Ad>\n" +
                "</VAST>\n" +
                "  </vmap:VASTAdData>\n" +
                "  </vmap:AdSource>\n" +
                "<vmap:Extensions>\n" +
                "</vmap:Extensions>" +
                " </vmap:AdBreak>\n" +
                " <vmap:AdBreak timeOffset=\"00:05:00.000\" breakType=\"linear\" breakId=\"midroll-1\">\n" +
                "  <vmap:AdSource id=\"midroll-1-ad-1\" allowMultipleAds=\"false\" followRedirects=\"true\">\n" +
                " <vmap:VASTAdData>\n" +

                "<VAST xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"vast.xsd\" version=\"3.0\">\n" +
                " <Ad id=\"697200496\">\n" +
                "  <InLine>\n" +
                "   <AdSystem>GDFP</AdSystem>\n" +
                "   <AdTitle>External NCA1C1L1 LinearInlineSkippable</AdTitle>\n" +
                "   <Description><![CDATA[External NCA1C1L1 LinearInlineSkippable ad]]></Description>\n" +
                "   <Error><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=Brxvo2VA4XbOWMJTHogPXkry4ApDVj-sGAAAAEAEgqN27JjgAWLCUgsbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIlLzEyNDMxOTA5Ni9leHRlcm5hbC9zaW5nbGVfYWRfc2FtcGxlc_gCg9IegAMBkAOaCJgDrAKoAwHgBAHSBQYQ8N65zAKQBgGgBiPYBwHgBwrSCAcIgGEQARgN&sigh=-nGf-mbB8pc&label=videoplayfailed[ERRORCODE]]]></Error>\n" +
                "   <Impression><![CDATA[https://securepubads.g.doubleclick.net/pcs/view?xai=AKAOjsshV8AloIjisHhED8kWSha2c4RhcHg03KhXlGwvj77YIK6runo6flpjKf9WoYBjDPwOcQBebtG-nAgsBjJHqs-qA3EshbnRNZG8ox-W1N0k56QcBHEwxuSKu7kqPJKiu0uTOWdyVPXzFNQAPQ25WM7hAm-Yh7ibLdij5RJUtEL5RAoK3LtiweyjqLooYhMRdJUXKnuj5NdURARl3aFq5P8a6XCW5BBBVz8pA7IMskeTgyiD6qBL7rlUbbU0ezL3JmEcmzDClVKT_Dq0u4kGlIj9YxpDLpHh6wAN&sig=Cg0ArKJSzNJF4X1Op31AEAE&adurl=]]></Impression>\n" +
                "   <Creatives>\n" +
                "    <Creative id=\"57860459056\" sequence=\"1\">\n" +
                "     <Linear skipoffset=\"00:00:05\">\n" +
                "      <Duration>00:00:10</Duration>\n" +
                "      <TrackingEvents>\n" +
                "       <Tracking event=\"start\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=Brxvo2VA4XbOWMJTHogPXkry4ApDVj-sGAAAAEAEgqN27JjgAWLCUgsbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIlLzEyNDMxOTA5Ni9leHRlcm5hbC9zaW5nbGVfYWRfc2FtcGxlc_gCg9IegAMBkAOaCJgDrAKoAwHgBAHSBQYQ8N65zAKQBgGgBiPYBwHgBwrSCAcIgGEQARgN&sigh=-nGf-mbB8pc&label=part2viewed&ad_mt=[AD_MT]]]></Tracking>\n" +
                "       <Tracking event=\"firstQuartile\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=Brxvo2VA4XbOWMJTHogPXkry4ApDVj-sGAAAAEAEgqN27JjgAWLCUgsbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIlLzEyNDMxOTA5Ni9leHRlcm5hbC9zaW5nbGVfYWRfc2FtcGxlc_gCg9IegAMBkAOaCJgDrAKoAwHgBAHSBQYQ8N65zAKQBgGgBiPYBwHgBwrSCAcIgGEQARgN&sigh=-nGf-mbB8pc&label=videoplaytime25&ad_mt=[AD_MT]]]></Tracking>\n" +
                "       <Tracking event=\"midpoint\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=Brxvo2VA4XbOWMJTHogPXkry4ApDVj-sGAAAAEAEgqN27JjgAWLCUgsbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIlLzEyNDMxOTA5Ni9leHRlcm5hbC9zaW5nbGVfYWRfc2FtcGxlc_gCg9IegAMBkAOaCJgDrAKoAwHgBAHSBQYQ8N65zAKQBgGgBiPYBwHgBwrSCAcIgGEQARgN&sigh=-nGf-mbB8pc&label=videoplaytime50&ad_mt=[AD_MT]]]></Tracking>\n" +
                "       <Tracking event=\"thirdQuartile\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=Brxvo2VA4XbOWMJTHogPXkry4ApDVj-sGAAAAEAEgqN27JjgAWLCUgsbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIlLzEyNDMxOTA5Ni9leHRlcm5hbC9zaW5nbGVfYWRfc2FtcGxlc_gCg9IegAMBkAOaCJgDrAKoAwHgBAHSBQYQ8N65zAKQBgGgBiPYBwHgBwrSCAcIgGEQARgN&sigh=-nGf-mbB8pc&label=videoplaytime75&ad_mt=[AD_MT]]]></Tracking>\n" +
                "       <Tracking event=\"complete\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=Brxvo2VA4XbOWMJTHogPXkry4ApDVj-sGAAAAEAEgqN27JjgAWLCUgsbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIlLzEyNDMxOTA5Ni9leHRlcm5hbC9zaW5nbGVfYWRfc2FtcGxlc_gCg9IegAMBkAOaCJgDrAKoAwHgBAHSBQYQ8N65zAKQBgGgBiPYBwHgBwrSCAcIgGEQARgN&sigh=-nGf-mbB8pc&label=videoplaytime100&ad_mt=[AD_MT]]]></Tracking>\n" +
                "       <Tracking event=\"mute\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=Brxvo2VA4XbOWMJTHogPXkry4ApDVj-sGAAAAEAEgqN27JjgAWLCUgsbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIlLzEyNDMxOTA5Ni9leHRlcm5hbC9zaW5nbGVfYWRfc2FtcGxlc_gCg9IegAMBkAOaCJgDrAKoAwHgBAHSBQYQ8N65zAKQBgGgBiPYBwHgBwrSCAcIgGEQARgN&sigh=-nGf-mbB8pc&label=admute&ad_mt=[AD_MT]]]></Tracking>\n" +
                "       <Tracking event=\"unmute\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=Brxvo2VA4XbOWMJTHogPXkry4ApDVj-sGAAAAEAEgqN27JjgAWLCUgsbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIlLzEyNDMxOTA5Ni9leHRlcm5hbC9zaW5nbGVfYWRfc2FtcGxlc_gCg9IegAMBkAOaCJgDrAKoAwHgBAHSBQYQ8N65zAKQBgGgBiPYBwHgBwrSCAcIgGEQARgN&sigh=-nGf-mbB8pc&label=adunmute&ad_mt=[AD_MT]]]></Tracking>\n" +
                "       <Tracking event=\"rewind\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=Brxvo2VA4XbOWMJTHogPXkry4ApDVj-sGAAAAEAEgqN27JjgAWLCUgsbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIlLzEyNDMxOTA5Ni9leHRlcm5hbC9zaW5nbGVfYWRfc2FtcGxlc_gCg9IegAMBkAOaCJgDrAKoAwHgBAHSBQYQ8N65zAKQBgGgBiPYBwHgBwrSCAcIgGEQARgN&sigh=-nGf-mbB8pc&label=adrewind&ad_mt=[AD_MT]]]></Tracking>\n" +
                "       <Tracking event=\"pause\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=Brxvo2VA4XbOWMJTHogPXkry4ApDVj-sGAAAAEAEgqN27JjgAWLCUgsbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIlLzEyNDMxOTA5Ni9leHRlcm5hbC9zaW5nbGVfYWRfc2FtcGxlc_gCg9IegAMBkAOaCJgDrAKoAwHgBAHSBQYQ8N65zAKQBgGgBiPYBwHgBwrSCAcIgGEQARgN&sigh=-nGf-mbB8pc&label=adpause&ad_mt=[AD_MT]]]></Tracking>\n" +
                "       <Tracking event=\"resume\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=Brxvo2VA4XbOWMJTHogPXkry4ApDVj-sGAAAAEAEgqN27JjgAWLCUgsbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIlLzEyNDMxOTA5Ni9leHRlcm5hbC9zaW5nbGVfYWRfc2FtcGxlc_gCg9IegAMBkAOaCJgDrAKoAwHgBAHSBQYQ8N65zAKQBgGgBiPYBwHgBwrSCAcIgGEQARgN&sigh=-nGf-mbB8pc&label=adresume&ad_mt=[AD_MT]]]></Tracking>\n" +
                "       <Tracking event=\"fullscreen\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=Brxvo2VA4XbOWMJTHogPXkry4ApDVj-sGAAAAEAEgqN27JjgAWLCUgsbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIlLzEyNDMxOTA5Ni9leHRlcm5hbC9zaW5nbGVfYWRfc2FtcGxlc_gCg9IegAMBkAOaCJgDrAKoAwHgBAHSBQYQ8N65zAKQBgGgBiPYBwHgBwrSCAcIgGEQARgN&sigh=-nGf-mbB8pc&label=adfullscreen&ad_mt=[AD_MT]]]></Tracking>\n" +
                "       <Tracking event=\"creativeView\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=Brxvo2VA4XbOWMJTHogPXkry4ApDVj-sGAAAAEAEgqN27JjgAWLCUgsbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIlLzEyNDMxOTA5Ni9leHRlcm5hbC9zaW5nbGVfYWRfc2FtcGxlc_gCg9IegAMBkAOaCJgDrAKoAwHgBAHSBQYQ8N65zAKQBgGgBiPYBwHgBwrSCAcIgGEQARgN&sigh=-nGf-mbB8pc&label=vast_creativeview&ad_mt=[AD_MT]]]></Tracking>\n" +
                "       <Tracking event=\"exitFullscreen\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=Brxvo2VA4XbOWMJTHogPXkry4ApDVj-sGAAAAEAEgqN27JjgAWLCUgsbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIlLzEyNDMxOTA5Ni9leHRlcm5hbC9zaW5nbGVfYWRfc2FtcGxlc_gCg9IegAMBkAOaCJgDrAKoAwHgBAHSBQYQ8N65zAKQBgGgBiPYBwHgBwrSCAcIgGEQARgN&sigh=-nGf-mbB8pc&label=vast_exit_fullscreen&ad_mt=[AD_MT]]]></Tracking>\n" +
                "       <Tracking event=\"acceptInvitationLinear\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=Brxvo2VA4XbOWMJTHogPXkry4ApDVj-sGAAAAEAEgqN27JjgAWLCUgsbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIlLzEyNDMxOTA5Ni9leHRlcm5hbC9zaW5nbGVfYWRfc2FtcGxlc_gCg9IegAMBkAOaCJgDrAKoAwHgBAHSBQYQ8N65zAKQBgGgBiPYBwHgBwrSCAcIgGEQARgN&sigh=-nGf-mbB8pc&label=acceptinvitation&ad_mt=[AD_MT]]]></Tracking>\n" +
                "       <Tracking event=\"closeLinear\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=Brxvo2VA4XbOWMJTHogPXkry4ApDVj-sGAAAAEAEgqN27JjgAWLCUgsbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIlLzEyNDMxOTA5Ni9leHRlcm5hbC9zaW5nbGVfYWRfc2FtcGxlc_gCg9IegAMBkAOaCJgDrAKoAwHgBAHSBQYQ8N65zAKQBgGgBiPYBwHgBwrSCAcIgGEQARgN&sigh=-nGf-mbB8pc&label=adclose&ad_mt=[AD_MT]]]></Tracking>\n" +
                "       <Tracking event=\"skip\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=Brxvo2VA4XbOWMJTHogPXkry4ApDVj-sGAAAAEAEgqN27JjgAWLCUgsbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIlLzEyNDMxOTA5Ni9leHRlcm5hbC9zaW5nbGVfYWRfc2FtcGxlc_gCg9IegAMBkAOaCJgDrAKoAwHgBAHSBQYQ8N65zAKQBgGgBiPYBwHgBwrSCAcIgGEQARgN&sigh=-nGf-mbB8pc&label=videoskipped&ad_mt=[AD_MT]]]></Tracking>\n" +
                "       <Tracking event=\"progress\" offset=\"00:00:05\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=Brxvo2VA4XbOWMJTHogPXkry4ApDVj-sGAAAAEAEgqN27JjgAWLCUgsbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIlLzEyNDMxOTA5Ni9leHRlcm5hbC9zaW5nbGVfYWRfc2FtcGxlc_gCg9IegAMBkAOaCJgDrAKoAwHgBAHSBQYQ8N65zAKQBgGgBiPYBwHgBwrSCAcIgGEQARgN&sigh=-nGf-mbB8pc&label=video_skip_shown&ad_mt=[AD_MT]]]></Tracking>\n" +
                "       <Tracking event=\"progress\" offset=\"00:00:30\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=Brxvo2VA4XbOWMJTHogPXkry4ApDVj-sGAAAAEAEgqN27JjgAWLCUgsbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIlLzEyNDMxOTA5Ni9leHRlcm5hbC9zaW5nbGVfYWRfc2FtcGxlc_gCg9IegAMBkAOaCJgDrAKoAwHgBAHSBQYQ8N65zAKQBgGgBiPYBwHgBwrSCAcIgGEQARgN&sigh=-nGf-mbB8pc&label=video_engaged_view&ad_mt=[AD_MT]]]></Tracking>\n" +
                "      </TrackingEvents>\n" +
                "      <AdParameters><![CDATA[custom_param=some_value]]></AdParameters>\n" +
                "      <VideoClicks>\n" +
                "       <ClickThrough id=\"GDFP\"><![CDATA[https://pubads.g.doubleclick.net/pcs/click?xai=AKAOjssHCP06yvRKHdhd3b7g37y3uy7OuPdlnAGni9FiYoZC905krAzpY3bCgtl4ySOqASwC95UfFHxc6sEFBkfrOq-75JY86XA2_89k6-iwqBLqG6rt5V1UehSNyBJXouCKVenbPAqnotKHYUA7BMAE63aSrR7dkEMmKHD75B0T_WT7oIXpv14VFf70h7q5hUkRBr_UovP-JM_66D7gqC01cBHqb7bTTExvI6ZzBswMxedfMgqKLSREnu_-zgy1bQy-ngPiWpxNFUljcustMmlYS2DiXv14_Lgr&sig=Cg0ArKJSzJm-8crNR2sr&adurl=https://developers.google.com/interactive-media-ads/docs/vastinspector_dual]]></ClickThrough>\n" +
                "      </VideoClicks>\n" +
                "      <MediaFiles>\n" +
                "       <MediaFile id=\"GDFP\" delivery=\"progressive\" width=\"1280\" height=\"720\" type=\"video/mp4\" bitrate=\"533\" scalable=\"true\" maintainAspectRatio=\"true\"><![CDATA[https://redirector.gvt1.com/videoplayback/id/b96674ee53e47835/itag/15/source/gfp_video_ads/requiressl/yes/acao/yes/mime/video%2Fmp4/ctier/L/ip/0.0.0.0/ipbits/0/expire/1563993401/sparams/ip,ipbits,expire,id,itag,source,requiressl,acao,mime,ctier/signature/22622D819639D704F6052D3AB43FF5B0E5D38233.8B57A0B776B499D9A32C7BF98590CAC40FBEFD14/key/ck2/file/file.mp4]]></MediaFile>\n" +
                "       <MediaFile id=\"GDFP\" delivery=\"progressive\" width=\"176\" height=\"144\" type=\"video/3gpp\" bitrate=\"36\" scalable=\"true\" maintainAspectRatio=\"true\"><![CDATA[https://redirector.gvt1.com/videoplayback/id/b96674ee53e47835/itag/17/source/gfp_video_ads/requiressl/yes/acao/yes/mime/video%2F3gpp/ctier/L/ip/0.0.0.0/ipbits/0/expire/1563993401/sparams/ip,ipbits,expire,id,itag,source,requiressl,acao,mime,ctier/signature/7A04F28CEDF2C885FB15FC26246BE542C7F9C2A0.B394E210E17080A0A15B1A431820141C353FCC5C/key/ck2/file/file.3gp]]></MediaFile>\n" +
                "       <MediaFile id=\"GDFP\" delivery=\"progressive\" width=\"640\" height=\"360\" type=\"video/mp4\" bitrate=\"122\" scalable=\"true\" maintainAspectRatio=\"true\"><![CDATA[https://redirector.gvt1.com/videoplayback/id/b96674ee53e47835/itag/18/source/gfp_video_ads/requiressl/yes/acao/yes/mime/video%2Fmp4/ctier/L/ip/0.0.0.0/ipbits/0/expire/1563993401/sparams/ip,ipbits,expire,id,itag,source,requiressl,acao,mime,ctier/signature/605173EB08233687DA22279671A60FE1A0CEE645.1EAF02357DAAEF47D8116DD6B912C3876A5295BE/key/ck2/file/file.mp4]]></MediaFile>\n" +
                "       <MediaFile id=\"GDFP\" delivery=\"progressive\" width=\"320\" height=\"180\" type=\"video/3gpp\" bitrate=\"74\" scalable=\"true\" maintainAspectRatio=\"true\"><![CDATA[https://redirector.gvt1.com/videoplayback/id/b96674ee53e47835/itag/36/source/gfp_video_ads/requiressl/yes/acao/yes/mime/video%2F3gpp/ctier/L/ip/0.0.0.0/ipbits/0/expire/1563993401/sparams/ip,ipbits,expire,id,itag,source,requiressl,acao,mime,ctier/signature/6D9A7967327CF895405583742548F25BB3528D46.A78A2253FDE5334B5320EC4F250A356537AF147A/key/ck2/file/file.3gp]]></MediaFile>\n" +
                "       <MediaFile id=\"GDFP\" delivery=\"progressive\" width=\"640\" height=\"360\" type=\"video/webm\" bitrate=\"125\" scalable=\"true\" maintainAspectRatio=\"true\"><![CDATA[https://redirector.gvt1.com/videoplayback/id/b96674ee53e47835/itag/43/source/gfp_video_ads/requiressl/yes/acao/yes/mime/video%2Fwebm/ctier/L/ip/0.0.0.0/ipbits/0/expire/1563993401/sparams/ip,ipbits,expire,id,itag,source,requiressl,acao,mime,ctier/signature/62FC61A17B073438D67BA272FBE1BF9A9D149260.598174135B097C4B0A92B2A83213F65C9F6F4CFA/key/ck2/file/file.webm]]></MediaFile>\n" +
                "       <MediaFile id=\"GDFP\" delivery=\"progressive\" width=\"1280\" height=\"720\" type=\"video/mp4\" bitrate=\"252\" scalable=\"true\" maintainAspectRatio=\"true\"><![CDATA[https://redirector.gvt1.com/videoplayback/id/b96674ee53e47835/itag/22/source/gfp_video_ads/requiressl/yes/acao/yes/mime/video%2Fmp4/ctier/L/ip/0.0.0.0/ipbits/0/expire/1563993401/sparams/ip,ipbits,expire,id,itag,source,requiressl,acao,mime,ctier/signature/41988E4AD92D49EE48C861B5BED7F26324A78A15.BA754C86E375EF00D0BDDA0788DAA79A6BCAFCC1/key/ck2/file/file.mp4]]></MediaFile>\n" +
                "       <MediaFile id=\"GDFP\" delivery=\"progressive\" width=\"1280\" height=\"720\" type=\"video/webm\" bitrate=\"245\" scalable=\"true\" maintainAspectRatio=\"true\"><![CDATA[https://redirector.gvt1.com/videoplayback/id/b96674ee53e47835/itag/45/source/gfp_video_ads/requiressl/yes/acao/yes/mime/video%2Fwebm/ctier/L/ip/0.0.0.0/ipbits/0/expire/1563993401/sparams/ip,ipbits,expire,id,itag,source,requiressl,acao,mime,ctier/signature/48D9ECFFF36C63B18707733DAE1E9A884C949FC4.50D45200A5D9DB76EA93C26390825795DA93FA5C/key/ck2/file/file.webm]]></MediaFile>\n" +
                "       <MediaFile id=\"GDFP\" delivery=\"progressive\" width=\"854\" height=\"480\" type=\"video/webm\" bitrate=\"139\" scalable=\"true\" maintainAspectRatio=\"true\"><![CDATA[https://redirector.gvt1.com/videoplayback/id/b96674ee53e47835/itag/44/source/gfp_video_ads/requiressl/yes/acao/yes/mime/video%2Fwebm/ctier/L/ip/0.0.0.0/ipbits/0/expire/1563993401/sparams/ip,ipbits,expire,id,itag,source,requiressl,acao,mime,ctier/signature/6473D4FA59E8D0F162F09E01279A6C03E720AE2E.74C5C4E1BD67047ED7378E69E9716F0AC6C7F653/key/ck2/file/file.webm]]></MediaFile>\n" +
                "      </MediaFiles>\n" +
                "     </Linear>\n" +
                "    </Creative>\n" +
                "    <Creative id=\"57857370976\" sequence=\"1\">\n" +
                "     <CompanionAds>\n" +
                "      <Companion id=\"57857370976\" width=\"300\" height=\"250\">\n" +
                "       <StaticResource creativeType=\"image/png\"><![CDATA[https://pagead2.googlesyndication.com/pagead/imgad?id=CICAgKDTwILFiwEQrAIY-gEyCAAnmA4d6uc2]]></StaticResource>\n" +
                "       <TrackingEvents>\n" +
                "        <Tracking event=\"creativeView\"><![CDATA[https://securepubads.g.doubleclick.net/pcs/view?xai=AKAOjsu_SLkKpQfacfHmiGrQAvbtvHwl4ORg9zHqd3lpt9ZkQm4rE2CU5vEu4PqriBepi6lT_XzojfZCuUfRa87KLTWXhN1H2rrN0guBnrPujT8c3oFawppK1uA8a5TTZggY3XXlaEiKIGlgqmLowhkY1DQypHCjmZQSo9oocKQLKFaiOG-2HSOVUQLYip7FlZhbDa5x7oDqyvK8FPlR-MKPHWQSFDsinBmWgzsjix3IGxw2kb2B1pJvyR4S6zD0QsJwf0OwGfi923Yls1-Cv0Itm8BB3_Vb4CWG2qx2&sig=Cg0ArKJSzP7XSLrtgePYEAE&adurl=]]></Tracking>\n" +
                "       </TrackingEvents>\n" +
                "       <CompanionClickThrough><![CDATA[https://pubads.g.doubleclick.net/pcs/click?xai=AKAOjsuvYlAw8ht2vkAepKVyJtgp_re0_NCoLk8nSyGfLQspz8zlNBvk1rUj_JN2uNhm6nC4w9srwNyscR2Ias2CeI_SOUJ_XTg-EMBg7ehCmpy1Hl6itZIlMSIA8QcLhpr1QxKXYjZtmQ2905qgf2K_itOvStTfQZDxDemhs5Z9t4WrezRL7n1AgWsM8odDLT7vIlaYmEOPz6tBfKdA-eTGP3Ld0tMPzQ9qfJEqV8GBI1UMN8dMDnFbqMNI9s5W_uh_VT0DT7eAQ0c1VXK3fUJPuB-QWtm8MQaj&sig=Cg0ArKJSzFUkusY6wCcM&adurl=https://developers.google.com/interactive-media-ads/docs/vastinspector_dual]]></CompanionClickThrough>\n" +
                "      </Companion>\n" +
                "     </CompanionAds>\n" +
                "    </Creative>\n" +
                "   </Creatives>\n" +
                "   <Extensions><Extension type=\"waterfall\" fallback_index=\"0\"/><Extension type=\"geo\"><Country>IN</Country><Bandwidth>4</Bandwidth><BandwidthKbps>13180</BandwidthKbps></Extension><Extension type=\"activeview\"><CustomTracking><Tracking event=\"viewable_impression\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=Brxvo2VA4XbOWMJTHogPXkry4ApDVj-sGAAAAEAEgqN27JjgAWLCUgsbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIlLzEyNDMxOTA5Ni9leHRlcm5hbC9zaW5nbGVfYWRfc2FtcGxlc_gCg9IegAMBkAOaCJgDrAKoAwHgBAHSBQYQ8N65zAKQBgGgBiPYBwHgBwrSCAcIgGEQARgN&sigh=-nGf-mbB8pc&label=viewable_impression&acvw=[VIEWABILITY]&gv=[GOOGLE_VIEWABILITY]&ad_mt=[AD_MT]]]></Tracking><Tracking event=\"abandon\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=Brxvo2VA4XbOWMJTHogPXkry4ApDVj-sGAAAAEAEgqN27JjgAWLCUgsbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIlLzEyNDMxOTA5Ni9leHRlcm5hbC9zaW5nbGVfYWRfc2FtcGxlc_gCg9IegAMBkAOaCJgDrAKoAwHgBAHSBQYQ8N65zAKQBgGgBiPYBwHgBwrSCAcIgGEQARgN&sigh=-nGf-mbB8pc&label=video_abandon&acvw=[VIEWABILITY]&gv=[GOOGLE_VIEWABILITY]]]></Tracking></CustomTracking><ActiveViewMetadata><![CDATA[ud=1&la=1&alp=xai&alh=1572451082&]]></ActiveViewMetadata></Extension><Extension type=\"DFP\"><SkippableAdType>Generic</SkippableAdType></Extension><Extension type=\"metrics\"><FeEventId>2VA4XfLdL4OtoAOA2bjwDA</FeEventId><AdEventId>CPPmvP3IzeMCFZSjaAodVwkPJw</AdEventId></Extension><Extension type=\"ShowAdTracking\"><CustomTracking><Tracking event=\"show_ad\"><![CDATA[https://securepubads.g.doubleclick.net/pcs/view?xai=AKAOjsuuIXwXBPBYwGQ-uK-wEPmV8iuuXcdcsJmjteRax0T3WGZ0BQVfRI2xEGE2ulTZuxuutm6xWAxr6bzv6uYkJXt4xMXYmpxR5i5Sm32j5jLl4T1z1Au3O3H0vQ44-EGHU4WqRCEBnlmy7SSXPNmnJOZJAOXgb5z6a0qZ16jsNSjlkxseoS6jf0Ygl84Uzqj28WynUzREb9xu8kcOt_986f3I_cTDuj4pKMpWkNS_PJZGvGZglR5l0b__zs8rl0co6Lac6CNTTCxG14UUbIhlHNjBsU0ED6Sl0BR-z1o&sig=Cg0ArKJSzNj7E95bZGimEAE&adurl=]]></Tracking></CustomTracking></Extension><Extension type=\"video_ad_loaded\"><CustomTracking><Tracking event=\"loaded\"><![CDATA[https://pubads.g.doubleclick.net/pagead/conversion/?ai=Brxvo2VA4XbOWMJTHogPXkry4ApDVj-sGAAAAEAEgqN27JjgAWLCUgsbXAWDlyuWDtA6yARVkZXZlbG9wZXJzLmdvb2dsZS5jb226AQo3Mjh4OTBfeG1syAEF2gFIaHR0cHM6Ly9kZXZlbG9wZXJzLmdvb2dsZS5jb20vaW50ZXJhY3RpdmUtbWVkaWEtYWRzL2RvY3Mvc2Rrcy9odG1sNS90YWdzwAIC4AIA6gIlLzEyNDMxOTA5Ni9leHRlcm5hbC9zaW5nbGVfYWRfc2FtcGxlc_gCg9IegAMBkAOaCJgDrAKoAwHgBAHSBQYQ8N65zAKQBgGgBiPYBwHgBwrSCAcIgGEQARgN&sigh=-nGf-mbB8pc&label=video_ad_loaded]]></Tracking></CustomTracking></Extension></Extensions>\n" +
                "  </InLine>\n" +
                " </Ad>\n" +
                "</VAST>\n" +
                "\n" +
                "  </vmap:VASTAdData>\n" +
                "   </vmap:AdSource>\n" +
                " </vmap:AdBreak>\n" +
                " <vmap:AdBreak timeOffset=\"end\" breakType=\"linear\" breakId=\"postroll\">\n" +
                "  <vmap:AdSource id=\"postroll-ad-1\" allowMultipleAds=\"false\" followRedirects=\"true\">\n" +
                "   <vmap:AdTagURI templateType=\"vast3\"><![CDATA[https://pubads.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=https://developers.google.com/interactive-media-ads/docs/sdks/html5/tags&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=postroll&pod=3&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&video_doc_id=short_onecue&cmsid=496&kfa=0&tfcd=0]]></vmap:AdTagURI>\n" +
                "  </vmap:AdSource>\n" +
                " </vmap:AdBreak>\n" +
                "</vmap:VMAP>\n" +
                "\n"
        handler.postDelayed({
            resultListener.onSuccess(200, adsVmapResponse)
        }, NETWORK_DELAY)
    }

    override fun getPlaybackContext(
        request: ContentRequest,
        resultListener: NetworkResultListener<PlaybackContext>,
        cancellationSignal: CancellationSignal
    ) {
        val response = PlaybackContext()
        val playbackSource = PlaybackSource()
        when (request.contentId) {
            "next_content_id" -> {
                playbackSource.url =
                    "https://bitdash-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8"
            }
            "slow" -> {
                playbackSource.url =
                    "http://slowwly.robertomurray.co.uk/delay/20000/url/http://www.google.co.uk"
            }
            else -> {
                playbackSource.url =
                    "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8"
            }
        }
        playbackSource.format = "applehttp"
        response.sources = ArrayList(Collections.singleton(playbackSource))
        response.startPosition = 0

        if (request.contentId != "error") {
            handler.postDelayed({
                resultListener.onSuccess(200, response)
            }, NETWORK_DELAY)
        } else {
            handler.postDelayed({
                resultListener.onFailure(0, "Network error")
            }, NETWORK_DELAY)
        }
    }

    override fun sendBookmarkEvent(
        event: BookmarkPlugin.BookmarkActionType,
        position: Long,
        contentId: String,
        resultListener: NetworkResultListener<String>
    ) {
        Log.d(
            "bookmark",
            "sendBookmarkEvent() called with: event = [$event], position = [$position], contentId = [$contentId], resultListener = [$resultListener]"
        )
        resultListener.onSuccess(200, "")
    }

    companion object {
        private const val NETWORK_DELAY: Long = 1000
    }
}
