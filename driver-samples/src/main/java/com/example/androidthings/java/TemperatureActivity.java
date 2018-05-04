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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.contrib.driver.bmx280.Bmx280SensorDriver;

import java.io.IOException;

public class TemperatureActivity extends Activity implements SensorEventListener {

    private static final String TAG = "TemperatureActivity";

    private Bmx280SensorDriver mSensorDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            initSensorDriver(BoardDefaults.getI2cPortName());
        } catch (IOException e) {
            Log.e(TAG, "Error initializing temperature sensor", e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startReadingTemperature();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            closeSensorDriver();
        } catch (IOException e) {
            Log.e(TAG, "Error closing temperature sensor", e);
        }
    }

    // [START driver-bmx280-java]
    private void initSensorDriver(String i2cPort) throws IOException {
        mSensorDriver = new Bmx280SensorDriver(i2cPort);
        // Register the peripheral with the system as a temperature sensor.
        mSensorDriver.registerTemperatureSensor();
    }

    private void startReadingTemperature() {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Find the Sensor that we registered.
        Sensor sensor = sensorManager.getDynamicSensorList(Sensor.TYPE_AMBIENT_TEMPERATURE).get(0);
        sensorManager.registerListener(TemperatureActivity.this, sensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void closeSensorDriver() throws IOException {
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.unregisterListener(TemperatureActivity.this);
        // This unregisters the peripheral as a temperature sensor.
        mSensorDriver.close();
    }

    // from SensorEventListener interface
    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.i(TAG, "Temperature changed: " + event.values[0]);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i(TAG, "Sensor accuracy changed: " + accuracy);
    }
    // [END driver-bmx280-java]
}
