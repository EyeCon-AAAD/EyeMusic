package com.projectx.eyemusic.Features;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.projectx.eyemusic.CalibrationRunnable;
import com.projectx.eyemusic.MainActivity;
import com.projectx.eyemusic.PredictionThread;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


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

         if (!newFeature.isFaceInImage()){
             MainActivity.updateFaceFeedback(Boolean.FALSE);
             return;
         }else{
             MainActivity.updateFaceFeedback(Boolean.TRUE);
         }

        /*if (saveCount > 0){
            saveBitmap(newFeature.getFaceImage(), "face");
            saveBitmap(newFeature.getLeftEyeImage(), "left");
            saveBitmap(newFeature.getRightEyeImage(), "right");
        }
        saveCount--;*/

         if (calibrationMode){
             MainActivity.getGraphicOverlayGazeLocation().clear();
             CalibrationRunnable.setNewFeature(newFeature);
         }else{
             Log.i(TAG, "getData: data sent to predict");
             PredictionThread.getHandler().removeCallbacksAndMessages(null); // all the pending runnable objects will be removed
             PredictionThread.getHandler().post(new PredictionThread.GazeRunnable(newFeature));
         }
    }

    public static void setCalibrationMode(boolean calibrationMode) {
        FeatureExtractor.calibrationMode = calibrationMode;
    }

    private static void saveBitmap(Bitmap bitmap, String file_name) {

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
                    Log.e("save()", e.getMessage());
                }
                finally {
                    try {
                        if (fileOutputStream != null) {
                            // fileOutputStream.flush();
                            fileOutputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }catch(Exception e){
            Log.e("save()", e.getMessage()); }

    }
}

