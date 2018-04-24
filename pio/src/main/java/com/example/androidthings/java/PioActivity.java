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

package com.example.androidthings.java;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;
import com.google.android.things.pio.Pwm;

import java.io.IOException;

public class PioActivity extends Activity {
    private static final String TAG = "PioActivity";

    // Blink constants
    private static final long BLINK_DELAY_MS = 1000L;

    // PWM constants
    // Parameters of the servo PWM
    private static final double MIN_ACTIVE_PULSE_DURATION_MS = 1;
    private static final double MAX_ACTIVE_PULSE_DURATION_MS = 2;
    private static final double PULSE_PERIOD_MS = 20;  // Frequency of 50Hz (1000/20)
    // Parameters for the servo movement over time
    private static final double PULSE_CHANGE_PER_STEP_MS = 0.2;
    private static final long PULSE_CHANGE_DELAY_MS = 1000L;

    private Gpio mLedGpio;
    private boolean mLedState = false;

    private Gpio mButtonGpio;

    private Pwm mPwm;
    private double mActivePulseDuration = MIN_ACTIVE_PULSE_DURATION_MS;
    private boolean mIsPulseIncreasing = true;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting PioActivity");

        try {
            initLed(BoardDefaults.getGpioForLed());
            // Schedule runnable to make the LED blink on and off
            mHandler.post(mBlinkRunnable);
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
        try {
            initButton(BoardDefaults.getGpioForButton());
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
        try {
            initPwm(BoardDefaults.getPwmPinName());
            // Schedule runnable to oscillate the PWM duty cycle
            mHandler.post(mChangePwmRunnable);
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove pending runnables.
        mHandler.removeCallbacks(mBlinkRunnable);
        mHandler.removeCallbacks(mChangePwmRunnable);

        // Close peripherals
        Log.i(TAG, "Closing peripherals");
        try {
            closeLed();
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        } finally {
            mLedGpio = null;
        }
        try {
            closeButton();
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        } finally {
            mButtonGpio = null;
        }
        try {
            closePwm();
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        } finally {
            mPwm = null;
        }
    }

    private Runnable mBlinkRunnable = new Runnable() {
        @Override
        public void run() {
            // Exit Runnable if the GPIO is already closed
            if (mLedGpio == null) {
                return;
            }
            try {
                // Toggle the GPIO state
                mLedState = !mLedState;
                setLedValue(mLedState);
                Log.d(TAG, "State set to " + mLedState);

                // Reschedule the same runnable
                mHandler.postDelayed(mBlinkRunnable, BLINK_DELAY_MS);
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };

    private Runnable mChangePwmRunnable = new Runnable() {
        @Override
        public void run() {
            // Exit Runnable if the port is already closed
            if (mPwm == null) {
                Log.w(TAG, "Stopping runnable since mPwm is null");
                return;
            }

            // Change the active pulse duration. Step direction depends on #mIsPulseIncreasing.
            // We constrain the pulse duration and also flip the step direction so that it
            // oscillates between the min and max.
            if (mIsPulseIncreasing) {
                mActivePulseDuration += PULSE_CHANGE_PER_STEP_MS;
            } else {
                mActivePulseDuration -= PULSE_CHANGE_PER_STEP_MS;
            }

            // Bounce mActivePulseDuration back from the limits
            if (mActivePulseDuration > MAX_ACTIVE_PULSE_DURATION_MS) {
                mActivePulseDuration = MAX_ACTIVE_PULSE_DURATION_MS;
                mIsPulseIncreasing = !mIsPulseIncreasing;
            } else if (mActivePulseDuration < MIN_ACTIVE_PULSE_DURATION_MS) {
                mActivePulseDuration = MIN_ACTIVE_PULSE_DURATION_MS;
                mIsPulseIncreasing = !mIsPulseIncreasing;
            }

            Log.d(TAG, "Changing PWM active pulse duration to " + mActivePulseDuration + " ms");

            try {
                setPwmDutyCycle(mActivePulseDuration);
                // Reschedule the same runnable in {@link #PULSE_CHANGE_DELAY_MS} milliseconds
                mHandler.postDelayed(this, PULSE_CHANGE_DELAY_MS);
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    };

    // [START peripheral-gpio-output-java]
    private void initLed(String pinName) throws IOException {
        mLedGpio = PeripheralManager.getInstance().openGpio(pinName);
        // Configure as an output with the signal LOW at first.
        mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
    }

    private void setLedValue(boolean value) throws IOException {
        // true = HIGH, false = LOW
        // When connected to an LED, it should turn on or off based on the signal value.
        mLedGpio.setValue(value);
    }

    private void closeLed() throws IOException {
        mLedGpio.close();
    }
    // [END peripheral-gpio-output-java]

    // [START peripheral-gpio-input-java]
    private void initButton(String pinName) throws IOException {
        mButtonGpio = PeripheralManager.getInstance().openGpio(pinName);
        // Configure as an input.
        mButtonGpio.setDirection(Gpio.DIRECTION_IN);
        // Set which signal changes should trigger any event callbacks. EDGE_FALLING triggers when
        // the incoming signal changes from HIGH to LOW.
        mButtonGpio.setEdgeTriggerType(Gpio.EDGE_FALLING);
        // Listen to changes in GPIO signal state.
        mButtonGpio.registerGpioCallback(new GpioCallback() {
            @Override
            public boolean onGpioEdge(Gpio gpio) {
                Log.i(TAG, "GPIO changed, button pressed");
                // Return true to continue listening to events
                return true;
            }
        });
    }

    private void closeButton() throws IOException {
        mButtonGpio.close();
    }
    // [END peripheral-gpio-input-java]

    // [START peripheral-pwm-java]
    private void initPwm(String pinName) throws IOException {
        mPwm = PeripheralManager.getInstance().openPwm(pinName);
        // Always set frequency and initial duty cycle before enabling PWM.
        mPwm.setPwmFrequencyHz(1000 / PULSE_PERIOD_MS);
        setPwmDutyCycle(mActivePulseDuration);
        mPwm.setEnabled(true);
    }

    private void setPwmDutyCycle(double activePulseDuration) throws IOException {
        // Duty cycle is the percentage of active (on) pulse over the total pulse duration
        mPwm.setPwmDutyCycle(100 * activePulseDuration / PULSE_PERIOD_MS);
    }

    private void closePwm() throws IOException {
        mPwm.close();
    }
    // [END peripheral-pwm-java]
}
