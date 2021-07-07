package com.projectx.eyemusic.Model;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//TODO: check for trained
public class CalibratedModel implements Serializable {
    private static final String TAG = "CalibratedModel";
    private LinearRegressionModel x_model = null;
    private LinearRegressionModel y_model =null;
    private CalibrationError trainingError = null;
    private boolean trained = false;

    private Integer initialTrainSampleSize;
    private Integer normalizedTrainSampleSize;

    CalibratedModel(){
        x_model = new LinearRegressionModel();
        y_model = new LinearRegressionModel();
        trainingError = new CalibrationError();
    }

    CalibratedModel(List<GazePoint> predictions, List <GazePoint> coordinates){
        Objects.requireNonNull(predictions, "the prediction argument to Calibrated Model Constructor must not be null");
        Objects.requireNonNull(coordinates, "the coordinates argument to Calibrated Model Constructor must not be null");
        if (predictions.size() != coordinates.size()){
            throw new IllegalArgumentException("the arguments to the Calibrated Model Constructor should be of same size");
        }

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

        //adding normalizing
        List<Integer> x_prediction_outliers = getOutliersIndex(x_prediction_array);
        List<Integer> y_prediction_outliers = getOutliersIndex(y_prediction_array);
        List<Integer> outliers_indexes = new ArrayList<>(x_prediction_outliers);
        outliers_indexes.addAll(y_prediction_outliers);

        //deleting the outliers
        List<Float> normalized_x_array = deleteOutliers(x_array, outliers_indexes);
        List<Float> normalized_y_array = deleteOutliers(y_array, outliers_indexes);
        List<Float> normalized_x_prediction_array = deleteOutliers(x_prediction_array, outliers_indexes);
        List<Float> normalized_y_prediction_array = deleteOutliers(y_prediction_array, outliers_indexes);

        // setting the size of the initial and normalized train samples
        normalizedTrainSampleSize = normalized_x_prediction_array.size();
        initialTrainSampleSize = x_prediction_array.size();
        Log.d(TAG, "CalibratedModel: normalizedsample: " + normalizedTrainSampleSize);

        //training the model
        x_model = new LinearRegressionModel(normalized_x_prediction_array, normalized_y_prediction_array, normalized_x_array, LinearRegressionModel.TRAIN_NORM_EQUATION);
        y_model = new LinearRegressionModel(normalized_x_prediction_array, normalized_y_prediction_array, normalized_y_array, LinearRegressionModel.TRAIN_NORM_EQUATION);

        //setting the training error metrics
        if (!x_model.isTrained() || !y_model.isTrained()){
            trainingError = null;
            trained = false;
        }
        else{
            trained = true;
            // predicting the predictions once again to find the error
            List<GazePoint> normalized_calibration_predictions = new ArrayList<>();
            List<GazePoint> normalized_predictions = deleteOutliers(predictions, outliers_indexes);
            List<GazePoint> normalized_coordinates = deleteOutliers(coordinates, outliers_indexes);

            for (GazePoint prediction: normalized_predictions){
                normalized_calibration_predictions.add(predict(prediction));
            }

            trainingError = new CalibrationError(normalized_coordinates, normalized_calibration_predictions);
        }

    }

    //for deleting the outliers from the list passed
    private <T> List<T> deleteOutliers(List<T> initialList, List<Integer> outlierIndexes){
        List<T> normalizedList = new ArrayList<>();
        for(int i=0; i<initialList.size(); i++){
            if(!outlierIndexes.contains(i)){
                normalizedList.add(initialList.get(i));
            }
        }
        return normalizedList;
    }


    // finding the outliers in a list
    private List<Integer> getOutliersIndex(List<Float> input) {
        if(input==null) return null;

        //at least two numbers
        if(input.size()<2){
            return null;
        }
        List<Integer> outliers_index = new ArrayList<>();
        List<Float> sorted_input= new ArrayList<>(input);
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

    public Integer getInitialTrainSampleSize(){
        return initialTrainSampleSize;
    }

    public Integer getNormalizedTrainSampleSize() {
        return normalizedTrainSampleSize;
    }

    public boolean isTrained() {
        return trained;
    }
}
