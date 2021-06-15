package com.projectx.eyemusic.Model;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

//TODO: check for trained
public class CalibratedModel {
    private static final String TAG = "CalibratedModel";
    private LinearRegressionModel x_model = null;
    private LinearRegressionModel y_model =null;
    private CalibrationError trainingError = null;
    private boolean trained = false;

    CalibratedModel(){
        x_model = new LinearRegressionModel();
        y_model = new LinearRegressionModel();
        trainingError = new CalibrationError();
    }

    CalibratedModel(List<GazePoint> predictions, List <GazePoint> coordinates){
        List<Float> x_array = new ArrayList<Float>();
        List<Float> y_array = new ArrayList<Float>();
        List<Float> x_prediction_array = new ArrayList<Float>();
        List<Float> y_prediction_array = new ArrayList<Float>();

        for (GazePoint prediction: predictions){
            x_prediction_array.add(prediction.getX());
            y_prediction_array.add(prediction.getY());
        }

        for(GazePoint coordinate: coordinates){
            x_array.add(coordinate.getX());
            y_array.add(coordinate.getY());
        }

        x_model = new LinearRegressionModel(x_prediction_array, y_prediction_array, x_array, LinearRegressionModel.TRAIN_NORM_EQUATION);
        y_model = new LinearRegressionModel(x_prediction_array, y_prediction_array, y_array, LinearRegressionModel.TRAIN_NORM_EQUATION);

        if (!x_model.isTrained() || !y_model.isTrained()){
            trainingError = null;
            trained = false;
        }
        else{
            trained = true;
            // predicting the predictions once again to find the error
            List<GazePoint> calibPredictions = new ArrayList<GazePoint>();
            for (GazePoint prediction: predictions){
                calibPredictions.add(predict(prediction));
            }

            trainingError = new CalibrationError(predictions, calibPredictions);
        }
    }

    public GazePoint predict(GazePoint input){
        if (!trained){
            return null;
        }
        Float x_predicted = x_model.predict(input.getX(), input.getY());
        Float y_predicted = y_model.predict(input.getY(), input.getY());
        return new GazePoint(x_predicted, y_predicted);
    }

    public CalibrationError getTrainingError() {
        if(!trained)
            return null;
        return trainingError;
    }

    public boolean isTrained() {
        return trained;
    }
}
