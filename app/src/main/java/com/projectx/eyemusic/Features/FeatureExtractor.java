package com.projectx.eyemusic.Features;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.mlkit.vision.face.Face;
import com.projectx.eyemusic.CalibrationRunnable;
import com.projectx.eyemusic.MainActivity;
import com.projectx.eyemusic.PredictionThread;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;


/*this class  is the Feature Extraction mainly responsible for sending the features to either calibration thread or prediction thread
* the data comes from the VisionProcessorBase.sendData function (meaning that the sendData function calls the getData function inside it)
* the decision to send the data to either of the threads (calibration or prediction) is made by the calibrationMode attribute which is set by the MainActivity
* when the calibration button is pressed.
* */
public class FeatureExtractor {
    private static final String TAG = "FeatureExtractor";
    private static boolean calibrationMode = false;
    private static int saveCount = 1;

    public static void getData(RawFeature rawFeature){
         Feature1 newFeature = new Feature1(rawFeature.getOriginal(), rawFeature.getFace());

         // TODO: complete show the error message that the face is not in the image so fix it
         if (!newFeature.isFaceInImage()){
             return;
         }

        /*
        //this block of code ws suppose to create the original image and the face objects but due to the fact that
        // Face class cannot be serializable the face objects could not be saved for later use in the unit tests
        if (saveCount > 0){
            saveBitmap(rawFeature.getOriginal(), "image_faceInImage");
            saveFaceObj2(rawFeature.getFace(), "faceObj_faceInImage");
            saveCount--;
        }*/


        // this block of code is used for saving the face, both eyes to external memory (internal memory of the phone in folder "/EyeMusicTestFeatures")
        // for checking of the these of of the image is cropped properly
        if (saveCount > 0){
            saveBitmap(newFeature.getOriginal(), "image");
            saveBitmap(newFeature.getFaceImage(), "face");
            saveBitmap(newFeature.getLeftEyeImage(), "left");
            saveBitmap(newFeature.getRightEyeImage(), "right");
            newFeature.logFaceGrid("TESTING FEATURE");
        }
        saveCount--;

         if (calibrationMode){
             MainActivity.getGraphicOverlayGazeLocation().clear();
             CalibrationRunnable.setNewFeature(newFeature);
         }else{
             Log.i(TAG, "getData: data sent to predict");
             PredictionThread.getHandler().removeCallbacksAndMessages(null); // all the pending runnable objects will be removed
             PredictionThread.getHandler().post(new PredictionThread.GazeRunnable(newFeature));
             /*
             //wanted to make teh click slower
             if(rawFeature.getSmileProb() > 0.8){
                 try {
                     Thread.sleep(2000);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             }*/
         }
    }

    public static void setCalibrationMode(boolean calibrationMode) {
        FeatureExtractor.calibrationMode = calibrationMode;
    }

    private static void saveBitmap(Bitmap bitmap, String file_name) {
        String TAG = "SaveBitmap";
        try {
            if (bitmap != null) {

                File file = new File(Environment.getExternalStorageDirectory() + "/EyeMusicTestFeatures");
                if (!file.isDirectory()) {
                    file.mkdir();
                }

                file = new File(Environment.getExternalStorageDirectory() + "/EyeMusicTestFeatures", file_name + "_" + System.currentTimeMillis() + ".jpg");
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(file);
                    if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)) {
                        //Toast saved = Toast.makeText(getApplicationContext(), "Image saved.", Toast.LENGTH_SHORT);
                        //saved.show();
                        Log.i(TAG, "saveBitmap: saved");
                    } else {
                        //Toast unsaved = Toast.makeText(getApplicationContext(), "Image not save.", Toast.LENGTH_SHORT);
                        //unsaved.show();
                        Log.i(TAG, "saveBitmap: not saved");
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
                finally {
                    try {
                        if (fileOutputStream != null) {
                            // fileOutputStream.flush();
                            fileOutputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, e.getMessage());
                    }
                }

            }
        }catch(Exception e){
            e.printStackTrace();
            Log.e("saveBitmap()", e.getMessage());
        }

    }

    private static void saveFaceObj(Face face, String file_name){
        final String TAG = "SaveFaceObj";
        try{
            if (face != null) {

                File file = new File(Environment.getExternalStorageDirectory() + "/EyeMusicTestFeatures");
                if (!file.isDirectory()) {
                    file.mkdir();
                }

                file = new File(Environment.getExternalStorageDirectory() + "/EyeMusicTestFeatures", file_name + "_" + System.currentTimeMillis() + ".dat");
                FileOutputStream fileOutputStream = null;
                ObjectOutputStream object_output_stream = null;

                try {
                    fileOutputStream = new FileOutputStream(file);
                    object_output_stream = new ObjectOutputStream(fileOutputStream);
                    object_output_stream.writeObject(face);

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
                finally {
                    try {
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }

                        if(object_output_stream != null)
                            object_output_stream.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, e.getMessage());
                    }
                }
            }

        }catch(Exception e){
            e.printStackTrace();
            Log.e(TAG, e.getMessage());

        }
    }

    private static void saveFaceObj2(Face face, String file_name){
        final String TAG = "SaveFaceObj2";
        try{
            if (face != null) {
                Gson gson = new Gson();

                File file = new File(Environment.getExternalStorageDirectory() + "/EyeMusicTestFeatures");
                if (!file.isDirectory()) {
                    file.mkdir();
                }

                file = new File(Environment.getExternalStorageDirectory() + "/EyeMusicTestFeatures", file_name + "_" + System.currentTimeMillis() + ".json");


                try {
                    gson.toJson(face, new FileWriter(file));
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getMessage());
                }
            }

        }catch(Exception e){
            e.printStackTrace();
            Log.e(TAG, e.getMessage());

        }
    }
}

