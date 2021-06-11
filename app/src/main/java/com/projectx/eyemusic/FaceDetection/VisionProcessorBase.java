/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.projectx.eyemusic.FaceDetection;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build.VERSION_CODES;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceLandmark;
import com.projectx.eyemusic.Features.FeatureExtractor;
import com.projectx.eyemusic.Features.RawFeature;
import com.projectx.eyemusic.PredictionThread;
import com.projectx.eyemusic.Graphics.GraphicOverlay;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Abstract base class for vision frame processors. Subclasses need to implement {@link
 * #onSuccess(Object, GraphicOverlay)} to define what they want to with the detection results and
 * {@link #detectInImage(InputImage)} to specify the detector object.
 *
 * @param <T> The type of the detected feature.
 */
public abstract class VisionProcessorBase<T> implements VisionImageProcessor {

  protected static final String MANUAL_TESTING_LOG = "LogTagForTest";
  private static final String TAG = "VisionProcessorBase";

  private final ActivityManager activityManager;
  private final Timer fpsTimer = new Timer(); //java timer
  private final ScopedExecutor executor; // defined srcs

  // Whether this processor is already shut down
  private boolean isShutdown;

  // Used to calculate latency, running in the same thread, no sync needed.
  private int numRuns = 0;
  private long totalFrameMs = 0;
  private long maxFrameMs = 0;
  private long minFrameMs = Long.MAX_VALUE;
  private long totalDetectorMs = 0;
  private long maxDetectorMs = 0;
  private long minDetectorMs = Long.MAX_VALUE;

  // Frame count that have been processed so far in an one second interval to calculate FPS.
  private int frameProcessedInOneSecondInterval = 0;
  private int framesPerSecond = 0;

  // To keep the latest images and its metadata.
  @GuardedBy("this")
  private ByteBuffer latestImage;
  @GuardedBy("this")
  private FrameMetadata latestImageMetaData;

  // To keep the images and metadata in process.
  @GuardedBy("this")
  private ByteBuffer processingImage;
  @GuardedBy("this")
  private FrameMetadata processingMetaData;

  protected VisionProcessorBase(Context context) {
    activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE); //?
    executor = new ScopedExecutor(TaskExecutors.MAIN_THREAD);
    fpsTimer.scheduleAtFixedRate(
        new TimerTask() {
          @Override
          public void run() {
            framesPerSecond = frameProcessedInOneSecondInterval;
            frameProcessedInOneSecondInterval = 0;
          }
        },
        /* delay= */ 0,
        /* period= */ 1000);
  }

  // -----------------Code for processing single still image----------------------------------------
  @Override
  public void processBitmap(Bitmap bitmap, final GraphicOverlay graphicOverlay) {
    long frameStartMs = SystemClock.elapsedRealtime();
    requestDetectInImage(
        InputImage.fromBitmap(bitmap, 0),
        graphicOverlay,
            /* originalCameraImage= */ null,
            /* shouldShowFps= */ false,
        frameStartMs,
            null);
  }

  // -----------------Code for processing live preview frame from Camera1 API-----------------------
  @Override
  public synchronized void processByteBuffer(
          ByteBuffer data, final FrameMetadata frameMetadata, final GraphicOverlay graphicOverlay) {
    latestImage = data;
    latestImageMetaData = frameMetadata;
    if (processingImage == null && processingMetaData == null) {
      processLatestImage(graphicOverlay);
    }
  }

  private synchronized void processLatestImage(final GraphicOverlay graphicOverlay) {
    processingImage = latestImage;
    processingMetaData = latestImageMetaData;
    latestImage = null;
    latestImageMetaData = null;
    if (processingImage != null && processingMetaData != null && !isShutdown) {
      processImage(processingImage, processingMetaData, graphicOverlay);
    }
  }

  private void processImage(
          ByteBuffer data, final FrameMetadata frameMetadata, final GraphicOverlay graphicOverlay) {
    long frameStartMs = SystemClock.elapsedRealtime();

    // If live viewport is on (that is the underneath surface view takes care of the camera preview
    // drawing), skip the unnecessary bitmap creation that used for the manual preview drawing.
    Bitmap bitmap =
        PreferenceUtils.isCameraLiveViewportEnabled(graphicOverlay.getContext())
            ? null
            : BitmapUtils.getBitmap(data, frameMetadata);

    requestDetectInImage(
            InputImage.fromByteBuffer(
                data,
                frameMetadata.getWidth(),
                frameMetadata.getHeight(),
                frameMetadata.getRotation(),
                InputImage.IMAGE_FORMAT_NV21),
            graphicOverlay,
            bitmap,
            /* shouldShowFps= */ true,
            frameStartMs,
            null)
        .addOnSuccessListener(executor, results -> processLatestImage(graphicOverlay));
  }

  // -----------------Code for processing live preview frame from CameraX API-----------------------
  @Override
  @RequiresApi(VERSION_CODES.KITKAT)
  @ExperimentalGetImage
  public void processImageProxy(ImageProxy image, GraphicOverlay graphicOverlay, PredictionThread predictionThread, TextView textView, boolean isCalibration) {
    long frameStartMs = SystemClock.elapsedRealtime();
    if (isShutdown) {
      image.close();
      return;
    }

    // if we want the blocking we also show the bitmap on the overlay
    Bitmap bitmap = null;
    if (!PreferenceUtils.isCameraLiveViewportEnabled(graphicOverlay.getContext())) {
      bitmap = BitmapUtils.getBitmap(image);
      //Log.i(TAG, "processImageProxy: bitmap size" + bitmap.getWidth()+", "+ bitmap.getHeight() );
    }

    requestDetectInImage(
            InputImage.fromMediaImage(image.getImage(), image.getImageInfo().getRotationDegrees()),
            graphicOverlay,
            /* originalCameraImage= */ bitmap,
            /* shouldShowFps= */ true,
            frameStartMs,
            textView)
        // When the image is from CameraX analysis use case, must call image.close() on received
        // images when finished using them. Otherwise, new images may not be received or the camera
        // may stall.
        .addOnCompleteListener(results -> image.close());
  }

  // ----------------------------------Common processing logic--------------------------------------
  private Task<T> requestDetectInImage(
          final InputImage image,
          final GraphicOverlay graphicOverlay,
          @Nullable final Bitmap originalCameraImage,
          boolean shouldShowFps,
          long frameStartMs,
          TextView textView) {

    final long detectorStartMs = SystemClock.elapsedRealtime(); //the time of start
    return detectInImage(image)
        .addOnSuccessListener( // QUESTION: how does the Task has a successListener -> when the task completes : the Task class is but the google and it has lots of listeners
            executor, //the activity
            results -> { // its the list of faces that is returned by the detectInImage
                  long endMs = SystemClock.elapsedRealtime();
                  long currentFrameLatencyMs = endMs - frameStartMs;
                  long currentDetectorLatencyMs = endMs - detectorStartMs;
                  if (numRuns >= 500) {
                    resetLatencyStats();
                    }
                  numRuns++;
                  frameProcessedInOneSecondInterval++;
                  totalFrameMs += currentFrameLatencyMs;
                  maxFrameMs = max(currentFrameLatencyMs, maxFrameMs);
                  minFrameMs = min(currentFrameLatencyMs, minFrameMs);
                  totalDetectorMs += currentDetectorLatencyMs;
                  maxDetectorMs = max(currentDetectorLatencyMs, maxDetectorMs);
                  minDetectorMs = min(currentDetectorLatencyMs, minDetectorMs);

                  // Only log inference info once per second. When frameProcessedInOneSecondInterval is
                  // equal to 1, it means this is the first frame processed during the current second.
                  if (frameProcessedInOneSecondInterval == 1) {
                    Log.d(TAG, "Num of Runs: " + numRuns);
                    Log.d(
                        TAG,
                        "Frame latency: max="
                            + maxFrameMs
                            + ", min="
                            + minFrameMs
                            + ", avg="
                            + totalFrameMs / numRuns);
                    Log.d(
                        TAG,
                        "Detector latency: max="
                            + maxDetectorMs
                            + ", min="
                            + minDetectorMs
                            + ", avg="
                            + totalDetectorMs / numRuns);
                    MemoryInfo mi = new MemoryInfo();
                    activityManager.getMemoryInfo(mi);
                    long availableMegs = mi.availMem / 0x100000L;
                    Log.d(TAG, "Memory available in system: " + availableMegs + " MB");
                  }

                  //it shows the image on the overlay
                  graphicOverlay.clear();
                  //if (originalCameraImage != null) {
                    graphicOverlay.add(new CameraImageGraphic(graphicOverlay, originalCameraImage));
                  //}


                  textView.post(new Runnable() {
                      @SuppressLint("DefaultLocale")
                      @Override
                      public void run() {
                          textView.setText(String.format(
                                  "ImageSize: %dx%d\n" + "FPS:\n%d\n" +
                                  "FrameLatency: %d ms\n" +
                                  "DetectorLatency: %d ms",
                                  graphicOverlay.getImageHeight(),
                                  graphicOverlay.getImageWidth(),
                                  framesPerSecond, currentFrameLatencyMs, currentDetectorLatencyMs));
                      }
              });

              //this shows the faces on the overlay
              VisionProcessorBase.this.onSuccess(results, graphicOverlay);

              graphicOverlay.postInvalidate();

              sendData(originalCameraImage, (List<Face>) results);
            })
        .addOnFailureListener(
            executor,
            e -> {
              graphicOverlay.clear();
              graphicOverlay.postInvalidate();
              String error = "Failed to process. Error: " + e.getLocalizedMessage();
              Toast.makeText(
                      graphicOverlay.getContext(),
                      error + "\nCause: " + e.getCause(),
                      Toast.LENGTH_SHORT)
                  .show();
              Log.d(TAG, error);
              e.printStackTrace();
              VisionProcessorBase.this.onFailure(e);
            });
  }

  @Override
  public void stop() {
    executor.shutdown();
    isShutdown = true;
    resetLatencyStats();
    fpsTimer.cancel();
  }

  private void resetLatencyStats() {
    numRuns = 0;
    totalFrameMs = 0;
    maxFrameMs = 0;
    minFrameMs = Long.MAX_VALUE;
    totalDetectorMs = 0;
    maxDetectorMs = 0;
    minDetectorMs = Long.MAX_VALUE;
  }

  protected abstract Task<T> detectInImage(InputImage image);

  protected abstract void onSuccess(@NonNull T results, @NonNull GraphicOverlay graphicOverlay);

  protected abstract void onFailure(@NonNull Exception e);


  /*
  * this function will send the raw feature to the Feature Extraction part*/
  protected void sendData(Bitmap originalCameraImage, List<Face> faces){
      try{
          if (originalCameraImage == null){
              Log.w(TAG, "sendData: original camera image is null");
              return;
          }
          if (faces == null){
              Log.w(TAG, "sendData: faces is null");
              return;
          }

          if (faces.isEmpty()){
              Log.w(TAG, "sendData: no face is detected");
              return;
          }

          Face dominantFace =  faces.get(0); // TODO: check if the dominant one is at index 0
          if(dominantFace == null){
              Log.w(TAG, "sendData: face is null");
              return;
          }

          Object smileProb = dominantFace.getSmilingProbability();
          if (smileProb == null){
              Log.w(TAG, "sendData: smileProb is null" );
              return;
          }

          Object boundingBox = dominantFace.getBoundingBox();
          if (boundingBox == null){
              Log.w(TAG, "sendData: boundingBox is null" );
              return;
          }

          Object landmarkEyeLeft = dominantFace.getLandmark(FaceLandmark.LEFT_EYE);
          if (landmarkEyeLeft == null){
              Log.w(TAG, "sendData: left eye landmark is null" );
              return;
          }

          Object landmarkEyeRight = dominantFace.getLandmark(FaceLandmark.RIGHT_EYE);
          if (landmarkEyeRight == null){
              Log.w(TAG, "sendData: right eye landmark is null" );
              return;
          }

          FeatureExtractor.getData(new RawFeature(originalCameraImage, dominantFace));

      }catch (Exception e){
          Log.e(TAG, "sendData: ", e);
      }
  }

}
