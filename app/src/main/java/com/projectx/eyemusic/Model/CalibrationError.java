package com.projectx.eyemusic.Model;

import android.util.Log;

import java.util.List;

//TODO: handleing of the exceptions better
public  class CalibrationError{
    private static final String TAG = "CalibrationError";
    //errors are in mean squared error
    float x_error;
    float y_error;
    float xy_error;
    int sample_size;

    public CalibrationError(){
        x_error = -1;
        y_error = -1;
        xy_error = -1;
        sample_size = -1;
    }

    public CalibrationError(List<GazePoint> originalPredictions, List<GazePoint> calibrationPredictions){

        // Should be handled with an exception instead
        if (originalPredictions == null || calibrationPredictions == null) {
            Log.e(TAG, "CalibrationError: the size of original and calibration predictions are not same" + originalPredictions.size()+" "+ calibrationPredictions.size());
            x_error = -1;
            y_error = -1;
            xy_error = -1;
            sample_size = -1;
            return;
        }

        if (originalPredictions.size() != calibrationPredictions.size()){
            Log.e(TAG, "CalibrationError: the size of original and calibration predictions are not same" + originalPredictions.size()+" "+ calibrationPredictions.size());
            x_error = -1;
            y_error = -1;
            xy_error = -1;
            sample_size = -1;
            return;
        }

        int size = originalPredictions.size();
        this.sample_size = size;
        double x_error_sum = 0;
        double y_error_sum = 0;
        double xy_error_sum = 0;
        for (int i = 0; i<size; i++){
            float x1 = originalPredictions.get(i).getX();
            float x2 = calibrationPredictions.get(i).getX();

            float y1 = originalPredictions.get(i).getY();
            float y2 = calibrationPredictions.get(i).getY();

            double x_diff = Math.pow((double) (x1 - x2), 2);
            double y_diff = Math.pow((double) (y1 - y2), 2);
            x_error_sum += x_diff;
            y_error_sum += y_diff;
            xy_error_sum = x_diff + y_diff;

        }

        x_error = (float) Math.pow(x_error_sum, 0.5);
        y_error = (float) Math.pow(y_error_sum, 0.5);
        xy_error = (float) Math.pow(xy_error_sum, 0.5);
    }

    public float getX_error() { return x_error; }
    public float getY_error() { return y_error; }
    public float getXY_error() { return xy_error; }
    public int getSample_size() {return sample_size; }

    //public void setX_error(float x_error) { this.x_error = x_error; }
    //public void setY_error(float y_error) { this.y_error = y_error; }
}