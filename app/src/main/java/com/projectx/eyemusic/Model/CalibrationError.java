package com.projectx.eyemusic.Model;

import android.util.DisplayMetrics;
import android.util.Log;

import com.projectx.eyemusic.App;

import java.util.List;

public  class CalibrationError{
    private static final String TAG = "CalibrationError";

    private static final Float X_DPI =  App.getContext().getResources().getDisplayMetrics().xdpi;
    private static final Float Y_DPI = App.getContext().getResources().getDisplayMetrics().ydpi;
    private static final Float INCH_TO_CM_CONSTANT = 2.54f;

    //errors are in mean squared error
    float x_error;
    float y_error;
    float xy_error;

    float x_error_inch;
    float y_error_inch;
    float xy_error_inch;

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

        double x_error_sum_inch = 0;
        double y_error_sum_inch = 0;
        double xy_error_sum_inch = 0;

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

            //calculations in pixel
            double x_diff = Math.abs((double) (x1 - x2));
            double y_diff = Math.abs((double) (y1 - y2));
            x_error_sum += x_diff;
            y_error_sum += y_diff;
            xy_error_sum += Math.pow(Math.pow((double) (x_diff), 2) + Math.pow((double) (y_diff), 2), 0.5);

            //calculation in inch
            double x_diff_inch = x_diff / X_DPI;
            double y_diff_inch = y_diff / Y_DPI;
            x_error_sum_inch += x_diff_inch;
            y_error_sum_inch += y_diff_inch;
            xy_error_sum_inch += Math.pow(Math.pow((double) (x_diff_inch), 2) + Math.pow((double) (y_diff_inch), 2), 0.5);
        }

        x_error = (float) (x_error_sum/ sample_size);
        y_error = (float) (y_error_sum/ sample_size);
        xy_error = (float) (xy_error_sum/ sample_size);

        x_error_inch = (float) (x_error_sum_inch/ sample_size);
        y_error_inch = (float) (y_error_sum_inch/ sample_size);
        xy_error_inch = (float) (xy_error_sum_inch/ sample_size);
    }

    /**
     * @return the error in pixels
     */
    public float getX_error() { return x_error; }

    /**
     * @return the error in pixels
     */
    public float getY_error() { return y_error; }

    /**
     * @return the error in pixels
     */
    public float getXY_error() { return xy_error; }



    /**
     * @return the error in inch
     */
    public float getX_error_inch() { return x_error_inch; }

    /**
     * @return the error in inch
     */
    public float getY_error_inch() { return y_error_inch; }

    /**
     * @return the error in inch
     */
    public float getXy_error_inch() { return xy_error_inch; }



    /**
     * @return the error in cm
     */
    public float getX_error_cm() { return x_error_inch * INCH_TO_CM_CONSTANT; }

    /**
     * @return the error in cm
     */
    public float getY_error_cm() { return y_error_inch * INCH_TO_CM_CONSTANT; }

    /**
     * @return the error in cm
     */
    public float getXy_error_cm() { return xy_error_inch * INCH_TO_CM_CONSTANT;}



    public float getSample_size() {return sample_size; }

    public void setX_error(float x_error) { this.x_error = x_error; }
    public void setY_error(float y_error) { this.y_error = y_error; }
}
