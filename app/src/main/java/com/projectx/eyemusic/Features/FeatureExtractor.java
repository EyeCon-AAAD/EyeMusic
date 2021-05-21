package com.projectx.eyemusic.Features;

import com.projectx.eyemusic.CalibrationRunnable;
import com.projectx.eyemusic.MainActivity;
import com.projectx.eyemusic.PredictionThread;


/*this class  is the Feature Extraction mainly responsible for sending the features to either calibration thread or prediction thread
* the data comes from the VisionProcessorBase.sendData function (meaning that the sendData function calls the getData function inside it)
* the decision to send the data to either of the threads (calibration or prediction) is made by the calibrationMode attribute which is set by the MainActivity
* when the calibration button is pressed.
* */
public class FeatureExtractor {

    private static boolean calibrationMode = false;

    public static void getData(RawFeature rawFeature){
         Feature1 newFeature = new Feature1(rawFeature.getOriginal(), rawFeature.getFace());

         if (calibrationMode){
             MainActivity.getGraphicOverlayGazeLocation().clear();
             CalibrationRunnable.setNewFeature(newFeature);
         }else{
             PredictionThread.getHandler().post(new PredictionThread.GazeRunnable(newFeature));
         }
    }

    public static void setCalibrationMode(boolean calibrationMode) {
        FeatureExtractor.calibrationMode = calibrationMode;
    }
}
