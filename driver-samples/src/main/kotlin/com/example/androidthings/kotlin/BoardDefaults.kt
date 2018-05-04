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

import android.os.Build

object BoardDefaults {
    private const val DEVICE_RPI3 = "rpi3"
    private const val DEVICE_IMX6UL_PICO = "imx6ul_pico"
    private const val DEVICE_IMX7D_PICO = "imx7d_pico"

    val gpioButtonPinName = when (Build.DEVICE) {
        DEVICE_RPI3 -> "BCM21"
        DEVICE_IMX6UL_PICO -> "GPIO2_IO03"
        DEVICE_IMX7D_PICO -> "GPIO6_IO14"
        else -> throw IllegalStateException("Unknown Build.DEVICE ${Build.DEVICE}")
    }

    val i2cPortName = when (Build.DEVICE) {
        DEVICE_RPI3 -> "I2C1"
        DEVICE_IMX6UL_PICO -> "I2C2"
        DEVICE_IMX7D_PICO -> "I2C1"
        else -> throw IllegalStateException("Unknown Build.DEVICE ${Build.DEVICE}")
    }
}
