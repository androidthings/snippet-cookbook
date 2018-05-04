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
import android.view.KeyEvent
import com.google.android.things.contrib.driver.button.Button.LogicState
import com.google.android.things.contrib.driver.button.ButtonInputDriver

class ButtonActivity: Activity() {

    private lateinit var buttonInputDriver: ButtonInputDriver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initButtonDriver(BoardDefaults.gpioButtonPinName)
    }

    // [START driver-button-kotlin]
    private fun initButtonDriver(pinName: String) {
        // Logic state should correspond to your button setup: PRESSED_WHEN_HIGH if using a
        // pull-down resistor, PRESSED_WHEN_LOW if using a pull-up resistor.
        buttonInputDriver =
                ButtonInputDriver(pinName, LogicState.PRESSED_WHEN_HIGH, KeyEvent.KEYCODE_A)
        // Register this driver with the system. Now button presses will generate key events.
        buttonInputDriver.register()
    }

    private fun closeButtonDriver() {
        // This unregisters the driver so button presses no longer generate key events.
        buttonInputDriver.close()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // You can check for the desired keyCode here (or in other key event callbacks).
        // [START_EXCLUDE]
        return super.onKeyDown(keyCode, event)
        // [END_EXCLUDE]
    }
    // [END driver-button-kotlin]
}
