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
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import com.google.android.things.contrib.driver.bmx280.Bmx280SensorDriver

private const val TAG = "TemperatureActivity"

class TemperatureActivity: Activity(), SensorEventListener {

    private lateinit var sensorDriver: Bmx280SensorDriver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSensorDriver(BoardDefaults.i2cPortName)
    }

    override fun onStart() {
        super.onStart()
        startReadingTemperature()
    }

    override fun onDestroy() {
        super.onDestroy()
        closeSensorDriver()
    }

    // [START driver-bmx280-kotlin]
    private fun initSensorDriver(i2cPort: String) {
        sensorDriver = Bmx280SensorDriver(i2cPort)
        // Register the peripheral with the system as a temperature sensor.
        sensorDriver.registerTemperatureSensor()
    }

    private fun startReadingTemperature() {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // Find the Sensor that we registered.
        val sensor = sensorManager.getDynamicSensorList(Sensor.TYPE_AMBIENT_TEMPERATURE)[0]
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun closeSensorDriver() {
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.unregisterListener(this)
        // This unregisters the peripheral as a temperature sensor.
        sensorDriver.close()
    }

    // from SensorEventListener interface
    override fun onSensorChanged(event: SensorEvent) {
        Log.i(TAG, "Temperature changed: ${event.values[0]}")
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        Log.i(TAG, "Sensor accuracy changed: $accuracy")
    }
    // [END driver-bmx280-kotlin]
}
