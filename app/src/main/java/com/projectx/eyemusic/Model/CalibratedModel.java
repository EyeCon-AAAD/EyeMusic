package com.projectx.eyemusic.Model;

import java.util.ArrayList;
import java.util.List;

//TODO: check for trained
public class CalibratedModel {
    private LinearRegressionModel x_model = null;
    private LinearRegressionModel y_model =null;
    private CalibrationError trainingError = null;

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

        // predicting the predictions once again to find the error
        List<GazePoint> calibPredictions = new ArrayList<GazePoint>();
        for (GazePoint prediction: predictions){
            // For aisan is this how you wanna do it?
            GazePoint tmp = predict(prediction);
            if (tmp == null) {
                System.out.println("predict result returns null");
                continue;
            }
            calibPredictions.add(tmp);
        }

        trainingError = new CalibrationError(predictions, calibPredictions);
    }

    public GazePoint predict(GazePoint input){
        float tmpX;
        float tmpY;
        try{
            tmpX = x_model.predict(input.getX(), input.getY());
        }
        catch (NullPointerException e) {
            System.out.println("tmpX prediction returns null");
            return null;
        }

        try {
            tmpY = y_model.predict(input.getY(), input.getY());
        }
        catch (NullPointerException e) {
            System.out.println("tmpY prediction returns null");
            return null;
        }
        return new GazePoint(tmpX, tmpY);
    }

    public CalibrationError getTrainingError() {
        return trainingError;
    }

    ////////////////////////////////////////////////////////////////////
    // Setters and getters used for testing
    public LinearRegressionModel getX_model() {
        return x_model;
    }

//    public void setX_model(LinearRegressionModel x_model) {
//        this.x_model = x_model;
//    }

    public LinearRegressionModel getY_model() {
        return y_model;
    }

//    public void setY_model(LinearRegressionModel y_model) {
//        this.y_model = y_model;
//    }

//    public void setTrainingError(CalibrationError trainingError) {
//        this.trainingError = trainingError;
//    }
}
