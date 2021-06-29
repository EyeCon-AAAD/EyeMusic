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

            trainingError = new CalibrationError(coordinates, calibPredictions);
        }

    }


    private List<Integer> getOutliersIndex(List<Float> input) {
        if(input==null) return null;

        //at least two numbers
        if(input.size()<2){
            return null;
        }
        List<Integer> outliers_index = new ArrayList<Integer>();
        List<Float> sorted_input= new ArrayList<Float>(input);
        List<Float> data1;
        List<Float> data2;

        if (sorted_input.size() % 2 == 0) {
            data1 = sorted_input.subList(0, sorted_input.size() / 2);
            data2 = sorted_input.subList(sorted_input.size() / 2, sorted_input.size());
        } else {
            data1 = sorted_input.subList(0, sorted_input.size() / 2);
            data2 = sorted_input.subList(sorted_input.size() / 2 + 1, sorted_input.size());
        }
        double q1 = getMedian(data1);
        double q3 = getMedian(data2);
        double iqr = q3 - q1;
        double lowerFence = q1 - 1.5 * iqr;
        double upperFence = q3 + 1.5 * iqr;

        for (int i = 0; i < input.size(); i++) {
            if (input.get(i) < lowerFence || input.get(i) > upperFence)
                outliers_index.add(i);
        }
        return outliers_index;
    }

    private Float getMedian(List<Float> data) {
        //testing for the wrong input
        if(data == null)
            return null;
        if(data.size()<1){
            return null;
        }

        if (data.size() % 2 == 0)
            return (data.get(data.size() / 2) + data.get(data.size() / 2 - 1)) / 2;
        else
            return data.get(data.size() / 2);
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
