package com.projectx.eyemusic.Features;

import com.projectx.eyemusic.CalibrationRunnable;
import com.projectx.eyemusic.MainActivity;
import com.projectx.eyemusic.PredictionThread;

public class FeatureExtractor {

    private static boolean calibrationMode = false;

    public static void getData(RawFeature rawFeature){
        Feature1 newFeature = new Feature1(rawFeature.getOriginal(), rawFeature.getFace());

        // TODO: complete the feature
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
