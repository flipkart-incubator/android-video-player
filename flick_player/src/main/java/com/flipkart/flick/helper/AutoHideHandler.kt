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
package com.flipkart.flick.helper

import android.os.Handler
import android.os.Message
import com.flipkart.flick.listeners.AutoHideable

class AutoHideHandler internal constructor(private val listener: AutoHideable) : Handler() {
    companion object {
        private const val AUTO_HIDE_MSG = 0
    }

    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        when (msg.what) {
            AUTO_HIDE_MSG -> this.listener.hidePlayerControl()
        }
    }
}
