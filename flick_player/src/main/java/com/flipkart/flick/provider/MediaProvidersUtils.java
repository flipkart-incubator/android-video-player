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
package com.flipkart.flick.provider;

import com.flipkart.flick.core.model.DrmPlaybackData;
import com.kaltura.playkit.PKDrmParams;
import com.kaltura.playkit.PKMediaSource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.kaltura.playkit.PKDrmParams.Scheme.PlayReadyCENC;
import static com.kaltura.playkit.PKDrmParams.Scheme.Unknown;
import static com.kaltura.playkit.PKDrmParams.Scheme.WidevineCENC;
import static com.kaltura.playkit.PKDrmParams.Scheme.WidevineClassic;

public class MediaProvidersUtils {

    public static boolean isDRMSchemeValid(PKMediaSource pkMediaSource, List<DrmPlaybackData> drmData) {
        if (drmData == null) {
            return false;
        }

        Iterator<DrmPlaybackData> drmDataItr = drmData.iterator();
        while (drmDataItr.hasNext()) {
            DrmPlaybackData drmDataItem = drmDataItr.next();
            if (drmDataItem.scheme != null && getScheme(drmDataItem.scheme) == Unknown) {
                drmDataItr.remove();
            }
        }
        return !drmData.isEmpty();
    }

    public static void updateDrmParams(PKMediaSource pkMediaSource, List<DrmPlaybackData> drmData) {
        List<PKDrmParams> drmParams = new ArrayList<>();
        for (DrmPlaybackData drm : drmData) {
            if (drm.scheme != null) {
                PKDrmParams.Scheme drmScheme = getScheme(drm.scheme);
                drmParams.add(new PKDrmParams(drm.licenseURL, drmScheme));
            }
        }
        pkMediaSource.setDrmData(drmParams);
    }

    public static PKDrmParams.Scheme getScheme(String name) {

        switch (name) {
            case "WIDEVINE_CENC":
            case "drm.WIDEVINE_CENC":
                return WidevineCENC;
            case "PLAYREADY_CENC":
            case "drm.PLAYREADY_CENC":
                return PlayReadyCENC;
            case "WIDEVINE":
            case "widevine.WIDEVINE":
                return WidevineClassic;
            default:
                return Unknown;
        }
    }
}
