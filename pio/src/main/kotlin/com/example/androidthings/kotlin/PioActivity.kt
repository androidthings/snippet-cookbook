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
import android.os.Handler
import android.util.Log
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManager
import com.google.android.things.pio.Pwm

class PioActivity : Activity() {

    companion object {
        private const val TAG = "PioActivity"

        // Button constants
        private const val INTERVAL_BETWEEN_BLINKS_MS = 1000L

        // PWM constants
        private const val PULSE_PERIOD_MS = 20.0 // Frequency 50Hz (= 1000/20)
        private const val MIN_ACTIVE_PULSE_DURATION_MS = 1.0
        private const val MAX_ACTIVE_PULSE_DURATION_MS = 2.0
        private const val PULSE_CHANGE_STEP = 0.2
        private const val PULSE_CHANGE_DELAY_MS = 1000L
    }

    private lateinit var ledGpio: Gpio
    private var ledState = false

    private lateinit var buttonGpio: Gpio

    private lateinit var pwm: Pwm
    private var activePulseDuration = MIN_ACTIVE_PULSE_DURATION_MS
    private var isPulseIncreasing = true

    private val handler = Handler()

    private val blinkRunnable = object : Runnable {
        var canceled = false

        override fun run() {
            if (canceled) return

            // Toggle the GPIO state
            ledState = !ledState
            setLedValue(ledState)
            Log.d(TAG, "State set to $ledState")

            // Reschedule the same runnable
            handler.postDelayed(this,
                INTERVAL_BETWEEN_BLINKS_MS
            )
        }
    }

    private val changePwmRunnable = object: Runnable {
        var canceled = false

        override fun run() {
            if (canceled) return

            // Change the active pulse duration. Step direction depends on [isPulseIncreasing].
            // We constrain the pulse duration and also flip the step direction so that it
            // oscillates between the min and max.
            if (isPulseIncreasing) {
                activePulseDuration += PULSE_CHANGE_STEP
                if (activePulseDuration > MAX_ACTIVE_PULSE_DURATION_MS) {
                    activePulseDuration = MAX_ACTIVE_PULSE_DURATION_MS
                    isPulseIncreasing = false
                }
            } else {
                activePulseDuration -= PULSE_CHANGE_STEP
                if (activePulseDuration < MIN_ACTIVE_PULSE_DURATION_MS) {
                    activePulseDuration = MIN_ACTIVE_PULSE_DURATION_MS
                    isPulseIncreasing = true
                }
            }

            Log.d(TAG, "Changing PWM active pulse duration to $activePulseDuration ms")
            setPwmDutyCycle(activePulseDuration)

            // Reschedule the same runnable
            handler.postDelayed(this, PULSE_CHANGE_DELAY_MS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "Starting PioActivity")

        initLed()
        initButton()
        initPwm()

        // Schedule runnable to make the LED blink on and off
        handler.post(blinkRunnable)
        // Schedule runnable to oscillate the PWM duty cycle
        handler.post(changePwmRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove pending runnables.
        blinkRunnable.canceled = true
        handler.removeCallbacks(blinkRunnable)
        changePwmRunnable.canceled = true
        handler.removeCallbacks(changePwmRunnable)

        // Close peripherals.
        closeLed()
        closeButton()
        closePwm()
    }

    // [START peripheral-gpio-output-kotlin]
    private fun initLed() {
        val pinName = BoardDefaults.gpioLedPinName
        ledGpio = PeripheralManager.getInstance().openGpio(pinName).apply {
            // Configure as an output with the signal LOW at first.
            setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
        }
    }

    private fun setLedValue(value: Boolean) {
        // true = HIGH, false = LOW
        // When connected to an LED, it should turn on or off based on the signal value.
        ledGpio.value = value
    }

    private fun closeLed() {
        ledGpio.close()
    }
    // [END peripheral-gpio-output-kotlin]

    // [START peripheral-gpio-input-kotlin]
    private fun initButton() {
        val pinName = BoardDefaults.gpioButtonPinName
        buttonGpio = PeripheralManager.getInstance().openGpio(pinName).apply {
            // Configure as an input.
            setDirection(Gpio.DIRECTION_IN)
            // Set which signal changes should trigger any event callbacks. EDGE_FALLING triggers
            // when the incoming signal changes from HIGH to LOW.
            setEdgeTriggerType(Gpio.EDGE_FALLING)
            // Listen to changes in GPIO signal state.
            registerGpioCallback {
                Log.i(TAG, "GPIO changed, button pressed")
                // Return true to continue listening to events
                true
            }
        }
    }

    private fun closeButton() {
        buttonGpio.close()
    }
    // [END peripheral-gpio-input-kotlin]

    // [START peripheral-pwm-kotlin]
    private fun initPwm() {
        val pinName = BoardDefaults.pwmPinName
        pwm = PeripheralManager.getInstance().openPwm(pinName).apply {
            // Always set frequency and inital duty cycle before enabling PWM
            setPwmFrequencyHz(1000 / PULSE_PERIOD_MS)
            setPwmDutyCycle(activePulseDuration)
            setEnabled(true)
        }
    }

    private fun setPwmDutyCycle(activePulseDuration: Double) {
        // Duty cycle is the percentage of active (on) pulse over the total pulse duration
        pwm.setPwmDutyCycle(100 * activePulseDuration / PULSE_PERIOD_MS)
    }

    private fun closePwm() {
        pwm.close()
    }
    // [END peripheral-pwm-kotlin]
}
