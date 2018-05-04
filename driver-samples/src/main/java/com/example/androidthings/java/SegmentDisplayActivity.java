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
import android.util.Log;

import com.google.android.things.contrib.driver.ht16k33.AlphanumericDisplay;

import java.io.IOException;

public class SegmentDisplayActivity extends Activity {
    private static final String TAG = "SegmentDisplayActivity";

    private AlphanumericDisplay mDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Starting SegmentDisplayActivity");

        try {
            initDisplay(BoardDefaults.getI2cPortName());
            setDisplayText("ABCD");
        } catch (IOException e) {
            Log.e(TAG, "Error configuring display", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDisplay != null) {
            Log.i(TAG, "Closing display");
            try {
                closeDisplay();
            } catch (IOException e) {
                Log.e(TAG, "Error closing display", e);
            } finally {
                mDisplay = null;
            }
        }
    }

    // [START driver-ht16k33-java]
    private void initDisplay(String i2cPort) throws IOException {
        mDisplay = new AlphanumericDisplay(i2cPort);
        mDisplay.setBrightness(1.0f);
        mDisplay.setEnabled(true);
        mDisplay.clear(); // if the display had text leftover from previous use, this clears it.
    }

    private void setDisplayText(String text) throws IOException {
        mDisplay.display(text);
    }

    private void closeDisplay() throws IOException {
        mDisplay.close();
    }
    // [END driver-ht16k33-java]
}
