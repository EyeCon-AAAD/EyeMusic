package com.projectx.eyemusic.Model;

import android.util.Log;

import java.util.List;

public  class CalibrationError{
    private static final String TAG = "CalibrationError";
    //errors are in mean squared error
    float x_error;
    float y_error;
    float xy_error;
    float sample_size;

    CalibrationError(){
        x_error = -1;
        y_error = -1;
        xy_error = -1;
        sample_size = -1;
    }

    public CalibrationError(List<GazePoint> coordinates, List<GazePoint> calibrationPredictions){
        if (coordinates.size() != calibrationPredictions.size()){
            Log.e(TAG, "CalibrationError: the size of original and calibration predictions are not same" + coordinates.size()+" "+ calibrationPredictions.size());
            x_error = -1;
            y_error = -1;
            xy_error = -1;
            sample_size = -1;
        }

        int size = coordinates.size();
        this.sample_size = size;
        double x_error_sum = 0;
        double y_error_sum = 0;
        double xy_error_sum = 0;
        for (int i = 0; i<size; i++){
            float x1 = coordinates.get(i).getX();
            float x2 = calibrationPredictions.get(i).getX();

            float y1 = coordinates.get(i).getY();
            float y2 = calibrationPredictions.get(i).getY();

//            double x_diff = Math.pow((double) (x1 - x2), 2);
//            double y_diff = Math.pow((double) (y1 - y2), 2);
//            x_error_sum += x_diff;
//            y_error_sum += y_diff;
//            xy_error_sum = x_diff + y_diff;

            double x_diff = Math.abs((double) (x1 - x2));
            double y_diff = Math.abs((double) (y1 - y2));
            x_error_sum += x_diff;
            y_error_sum += y_diff;
            xy_error_sum += Math.pow(Math.pow((double) (x1 - x2), 2) + Math.pow((double) (y1 - y2), 2), 0.5);
        }

        x_error = (float) (x_error_sum/ sample_size);
        y_error = (float) (y_error_sum/ sample_size);
        xy_error = (float) (xy_error_sum/ sample_size);
    }

    public float getX_error() { return x_error; }
    public float getY_error() { return y_error; }
    public float getXY_error() { return xy_error; }
    public float getSample_size() {return sample_size; }

    public void setX_error(float x_error) { this.x_error = x_error; }
    public void setY_error(float y_error) { this.y_error = y_error; }
}
