/*
 * Copyright 2020 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.lite.examples.detection;

import static com.google.common.truth.Truth.assertThat;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.Log;
import android.util.Size;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tensorflow.lite.examples.detection.env.ImageUtils;

import cn.com.startprinter.Detector;
import cn.com.startprinter.RecognitionCDC;
import cn.com.startprinter.TFLiteCDCPageIdentifyModel;

/** Golden test for Object Detection Reference app. */
@RunWith(AndroidJUnit4.class)
public class DetectorTest {

  private final static String TAG = DetectorTest.class.getSimpleName();

  private static final int MODEL_INPUT_SIZE = 300;
  private static final boolean IS_MODEL_QUANTIZED = false;
  private static final String CONFIG_FILE = "modelconfig.properties";

  private Detector detector;

  @Before
  public void setUp() throws IOException {
    detector =
        TFLiteCDCPageIdentifyModel.create(
            InstrumentationRegistry.getInstrumentation().getContext(),
            CONFIG_FILE,
            IS_MODEL_QUANTIZED);
    int cropSize = MODEL_INPUT_SIZE;
    int sensorOrientation = 0;
  }

  @Test
  public void detectionResultsShouldNotChange() throws Exception {
//    Canvas canvas = new Canvas(croppedBitmap);
//    canvas.drawBitmap(loadImage("table.jpg"), frameToCropTransform, null);
    final List<RecognitionCDC> results = detector.recognizeImage(loadImage("5.jpg"));

    for (RecognitionCDC result : results) {

      Log.d(TAG, result.toString());
    }
  }

  private static Bitmap loadImage(String fileName) throws Exception {
    AssetManager assetManager =
        InstrumentationRegistry.getInstrumentation().getContext().getAssets();
    InputStream inputStream = assetManager.open(fileName);
    return BitmapFactory.decodeStream(inputStream);
  }

  // The format of result:
  // category bbox.left bbox.top bbox.right bbox.bottom confidence
  // ...
  // Example:
  // Apple 99 25 30 75 80 0.99
  // Banana 25 90 75 200 0.98
  // ...
  private static List<RecognitionCDC> loadRecognitions(String fileName) throws Exception {
    AssetManager assetManager =
        InstrumentationRegistry.getInstrumentation().getContext().getAssets();
    InputStream inputStream = assetManager.open(fileName);
    Scanner scanner = new Scanner(inputStream);
    List<RecognitionCDC> result = new ArrayList<>();
    while (scanner.hasNext()) {
      String category = scanner.next();
      category = category.replace('_', ' ');
      if (!scanner.hasNextFloat()) {
        break;
      }
      float left = scanner.nextFloat();
      float top = scanner.nextFloat();
      float right = scanner.nextFloat();
      float bottom = scanner.nextFloat();
      RectF boundingBox = new RectF(left, top, right, bottom);
      float confidence = scanner.nextFloat();
      RecognitionCDC recognition = new RecognitionCDC(null, category, confidence, boundingBox);
      result.add(recognition);
    }
    return result;
  }
}
