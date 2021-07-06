package com.projectx.eyemusic;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.Feature;
import com.projectx.eyemusic.Features.Feature1;
import com.projectx.eyemusic.Features.FeatureExtractor;
import com.projectx.eyemusic.Features.RawFeature;
import com.projectx.eyemusic.Graphics.DotGraphic;
import com.projectx.eyemusic.Graphics.GraphicOverlay;
import com.projectx.eyemusic.Model.CalibratedModel;
import com.projectx.eyemusic.Model.CalibrationError;
import com.projectx.eyemusic.Model.GazeModelManager;
import com.projectx.eyemusic.Model.GazePoint;

import java.util.ArrayList;
import java.util.List;

public class CalibrationRunnable implements Runnable {
    private static final String TAG = "CalibrationRunnable";
    private GraphicOverlay graphicOverlayCalibration;
    private CalibrationActivity activity;

    private static boolean newFeatureCaptured;
    private static Feature1 newFeature; //contains the the frames and all the landmarks and other things needed
    private Feature1 capturedFeature;

    private  List<GazePoint> points;
    private volatile List<Feature1> features; //volatile -> so that two thread do not use the cashed value
    private final static int SCREEN_WIDTH = Utilities.getScreenWidth();
    private final static int SCREEN_HEIGHT = Utilities.getScreenHeight();

    private static TextView calibrationInstructionsTextview;
    private static String calibrationResults;
    private static String resultMessage;

    CalibrationRunnable(GraphicOverlay overlayGaze, CalibrationActivity activity, TextView tmpCalibrationInstruction){
        calibrationInstructionsTextview = tmpCalibrationInstruction;
        graphicOverlayCalibration= overlayGaze;
        this.activity =  activity;
        this.newFeatureCaptured = true; // meaning that the new feature has not come
        this.newFeature = null;
        this.features = new ArrayList<Feature1>();

        this.points = produceDots(3, 6);
        printPoints();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void run() {

        //Showing instructions for calibration
        calibrationInstructionsTextview.post(new Runnable() {
            @SuppressLint({"DefaultLocale", "SetTextI18n"})
            @Override
            public void run() {
                calibrationInstructionsTextview.setText("Calibration\nMultiple dots will be shown on the screen." +
                        "You should look at them\n" +
                        "The dots will appear on different places on the screen\n" +
                        "You will have enough time to look at them.\n" +
                        "Remember to:\n  *have a good lighting.\n  *look straight at the dots\n " +
                        " *Try not to move your head\n");
                calibrationInstructionsTextview.setVisibility(View.VISIBLE);
            }
        });

        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 3; i >= 0; i--) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int finalI = i;
            calibrationInstructionsTextview.post(new Runnable() {
                @SuppressLint({"DefaultLocale", "SetTextI18n"})
                @Override
                public void run() {
                    calibrationInstructionsTextview.setText(String.format("Calibration starting in %d seconds", finalI));
                    calibrationInstructionsTextview.setVisibility(View.VISIBLE);
                    if (finalI == 0) {
                        calibrationInstructionsTextview.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        // Calibration Actual start
        int i = 1;
        int size_points = points.size();
        for(GazePoint point : points){
            graphicOverlayCalibration.clear();
            DotGraphic dot = new DotGraphic(activity, graphicOverlayCalibration, point.getX(), point.getY());
            dot.setColor(Color.BLUE);
            dot.setRadius(50f);
            graphicOverlayCalibration.add(dot);
            graphicOverlayCalibration.postInvalidate();

            //giving time to the user to look at the dot
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //capturing the feature
            while (newFeatureCaptured){ //goes through here when its true meaning the feature is used so wants for feature
                Log.d(TAG, "CalibrationNewFeature: the new feature has not been come yet (in the while loop)" );
            }
            while(newFeature == null){
                Log.d(TAG, "CalibrationNewFeature: the new feature is null (in the while loop)" );
            }

            capturedFeature = newFeature;
            setNewFeatureCaptured(true);

            capturedFeature.setCoordinate(point);
            features.add(capturedFeature);
            Log.d(TAG, "CalibrationNewFeature: the new feature is added, run:" + i +"/" + size_points +" feature:"+ capturedFeature);
            i++;
        }

        //finishing the calibration
        graphicOverlayCalibration.clear();

        try{
            //TODO: show the message that the model is being calibrated
            calibrationInstructionsTextview.post(new Runnable() {
                @SuppressLint("DefaultLocale")
                @Override
                public void run() {
                    calibrationInstructionsTextview.setText("Please wait\nThe model is being calibrated!\n");
                    calibrationInstructionsTextview.setVisibility(View.VISIBLE);
                }
            });

            //updating the model
            GazeModelManager.updateCalibratedModel(features);

            // no need for the features captured so far
            features.clear();

            //checking if the calibrated model is trained or not
            boolean success = GazeModelManager.getRecentCalibrationSuccess();
            if (success){
                //TODO: show the message
                calibrationInstructionsTextview.post(new Runnable() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void run() {
                        calibrationInstructionsTextview.setText("CalibrationResult: the model is updated");
                        calibrationInstructionsTextview.setVisibility(View.VISIBLE);
                    }
                });
                Log.d(TAG, "CalibrationResult: the model is updated");
                Thread.sleep(3000);

                //Show the training error (saving for later
                CalibrationError calibError = GazeModelManager.getCalibrationTrainingError();
//                //T0 DO: show the message
//                Log.d(TAG, "CalibrationResult: calibration training error (X Y XY): "
//                        + calibError.getX_error() + " " + calibError.getY_error() + " " + calibError.getXY_error());

                calibrationResults += String.format("Calibration Error for X =>  :%.2f (dp),\t", calibError.getX_error());
                calibrationResults += String.format("%.2f (inch)\t", calibError.getX_error_inch());
                calibrationResults += String.format("%.2f (cm),\n", calibError.getX_error_cm());
                calibrationResults += String.format("Calibration Error for Y =>  :%.3f (dp),\t", calibError.getY_error());
                calibrationResults += String.format("%.2f (inch)\t", calibError.getY_error_inch());
                calibrationResults += String.format("%.2f (cm),\n", calibError.getX_error_cm());
                calibrationResults += String.format("Calibration Error for XY =>  :%.3f (dp),\t", calibError.getXY_error());
                calibrationResults += String.format("%.2f (inch)\t", calibError.getXy_error_inch());
                calibrationResults += String.format("dp=%.2f (cm),\n", calibError.getXy_error_cm());

                //TODO: show the message that they have to look at the screen
                calibrationInstructionsTextview.post(new Runnable() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void run() {
                        calibrationInstructionsTextview.setText("For testing you need to look at the colored dots!");
                        calibrationInstructionsTextview.setVisibility(View.VISIBLE);
                    }
                });

                //waiting for the person to look
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                int dotColors[] = new int[]{Color.RED, Color.YELLOW, Color.GREEN , Color.BLUE, Color.BLACK};

                for(int count = 0; count<5; count++){
                    // show the dot in the screen for showing the calibration test error
                    graphicOverlayCalibration.clear();
                    DotGraphic dot = new DotGraphic(activity, graphicOverlayCalibration, SCREEN_WIDTH/2, SCREEN_HEIGHT/2);
                    dot.setColor(dotColors[count]);
                    dot.setRadius(50f);
                    graphicOverlayCalibration.add(dot);
                    graphicOverlayCalibration.postInvalidate();

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //capturing the feature
                    while (newFeatureCaptured){ //goes through here when its true meaning the feature is used so wants for feature
                        Log.d(TAG, "CalibrationNewFeature: the new feature has not been come yet (in the while loop)" );
                    }
                    while(newFeature == null){
                        Log.d(TAG, "CalibrationNewFeature: the new feature is null (in the while loop)" );
                    }

                    //features are collected
                    capturedFeature = newFeature;
                    setNewFeatureCaptured(true);
                    capturedFeature.setCoordinate(new GazePoint(SCREEN_WIDTH/2, SCREEN_HEIGHT/2));
                    features.add(capturedFeature);
                    Log.d(TAG, "CalibrationNewFeature: the new feature is added, run:" + i +"/" + size_points +" feature:"+ capturedFeature);

                }
                List<GazePoint> calibPredictions = new ArrayList<GazePoint>();
                List <GazePoint> coordinates = new ArrayList<GazePoint>();
                for (Feature1 feature: features){
                    calibPredictions.add(GazeModelManager.predictCalibrated(feature));
                    coordinates.add(feature.getCoordinate());
                }

                //TODO: show the test error

                CalibrationError testError = new CalibrationError(coordinates, calibPredictions);
//                Log.d(TAG, "CalibrationResult: calibration test error (X Y XY): "
//                        + testError.getX_error() + " " + testError.getY_error() + " " + testError.getXY_error());


                // printing the calibration results
                graphicOverlayCalibration.clear();

                CalibratedModel tempCalibratedModel = GazeModelManager.getCalibratedModel();
                resultMessage = String.format("initial train sample size : %d\n", tempCalibratedModel.getInitialTrainSampleSize());
                resultMessage += String.format("Normalized train sample size : %d\n\n", tempCalibratedModel.getNormalizedTrainSampleSize());

                resultMessage += calibrationResults;

                resultMessage += String.format("Test Error for X =>  :%.2f (dp),\t", testError.getX_error());
                resultMessage += String.format("%.2f (inch)\t", testError.getX_error_inch());
                resultMessage += String.format("%.2f (cm),\n\n", testError.getX_error_cm());
                resultMessage += String.format("Test Error for Y =>  :%.3f (dp),\t", testError.getY_error());
                resultMessage += String.format("%.2f (inch)\t", testError.getY_error_inch());
                resultMessage += String.format("%.2f (cm),\n\n", testError.getX_error_cm());
                resultMessage += String.format("Test Error for XY =>  :%.3f (dp),\t", testError.getXY_error());
                resultMessage += String.format("%.2f (inch)\t", testError.getXy_error_inch());
                resultMessage += String.format("dp=%.2f (cm),\n\n", testError.getXy_error_cm());

                calibrationInstructionsTextview.post(new Runnable() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void run() {
                        calibrationInstructionsTextview.setText(resultMessage);
                        calibrationInstructionsTextview.setVisibility(View.VISIBLE);
                    }
                });

                Log.d(TAG, resultMessage);
                Thread.sleep(5000);

            }else {
                Log.d(TAG, "CalibrationResult: the model could not be updated");
                calibrationInstructionsTextview.post(new Runnable() {
                    @SuppressLint({"DefaultLocale", "SetTextI18n"})
                    @Override
                    public void run() {
                        calibrationInstructionsTextview.setText("CalibrationResult: the model could not be updated\n press start calibration again"); //why setTextI18n is needed ?
                        calibrationInstructionsTextview.setVisibility(View.VISIBLE);
                    }
                });
            }
        }catch (Exception e){
            Log.e(TAG, "CalibrationResult: ", e);
        }

        //finishing the calibration
        graphicOverlayCalibration.clear();
        FeatureExtractor.setCalibrationMode(false);
        activity.setCalibration(false);
        activity.calibrationFinished();
        Log.d(TAG, "Calibration: finished");
    }

    private static List<GazePoint> produceDots(int no_x, int no_y){
        List<GazePoint> points = new ArrayList<GazePoint>();
        double margin_x = SCREEN_WIDTH * 0.05;
        double margin_y = SCREEN_HEIGHT * 0.05;

        double start_x = margin_x;
        double start_y = margin_y;

        double step_x = (SCREEN_WIDTH - (margin_x*2)) /  (no_x - 1);
        double step_y = (SCREEN_HEIGHT - (margin_y*2)) / (no_y - 1);

        double x, y;
        for (int i = 0; i < no_x; i++) {
            for (int j = 0; j < no_y; j++) {
                x = start_x + step_x * i;
                y = start_y + step_y * j;
                points.add(new GazePoint((float) x,(float) y));
            }
        }
        return points;
    }

    public static boolean setNewFeature(Feature1 f){
        newFeature = f;
        setNewFeatureCaptured(false);
        Log.d("CalibrationNewFeature", "-----the new feature has arrived: ");
        return true;
    }

    private static synchronized void setNewFeatureCaptured(boolean value){
        newFeatureCaptured = value;
    }

    public void printPoints(){
        for(GazePoint point: points){
            Log.d("Calibration", "stored point: " + point.getX() + " " + point.getY());
        }
    }

}