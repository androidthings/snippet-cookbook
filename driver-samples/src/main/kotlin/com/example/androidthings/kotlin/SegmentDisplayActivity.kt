/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.androidthings.kotlin

import android.app.Activity
import android.os.Bundle
import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay

class SegmentDisplayActivity : Activity() {

    private lateinit var display: AlphanumericDisplay

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDisplay(BoardDefaults.i2cPortName)
    }

    override fun onDestroy() {
        super.onDestroy()
        closeDisplay()
    }

    // [START driver-ht16k33-kotlin]
    private fun initDisplay(i2cPort: String) {
        display = AlphanumericDisplay(i2cPort).apply {
            setBrightness(1f)
            setEnabled(true)
            clear() // if the display had text leftover from previous use, this clears it.
        }
    }

    private fun setDisplayText(text: String) {
        display.display(text)
    }

    private fun closeDisplay() {
        display.close()
    }
    // [END driver-ht16k33-kotlin]
}
